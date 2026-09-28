package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** A quest shown in the quick answer sheet */
class QuickAnswerQuest(
    val quest: OsmQuest,
    val definition: QuickAnswerDefinition<*>,
    /** key of the option selected initially */
    val defaultOptionKey: String,
)

/** Finds the quests of an element that can be answered in the quick answer sheet and applies the
 *  selected answers. */
class QuickAnswerController(
    private val visibleQuestsSource: VisibleQuestsSource,
    private val mapDataSource: MapDataWithEditsSource,
    private val elementEditsController: ElementEditsController,
    private val questsHiddenController: QuestsHiddenController,
    private val surveyChecker: SurveyChecker,
    private val quickAnswerPrefs: QuickAnswerPrefs,
    private val prefs: Preferences,
) {
    // edits must be added even when the sheet is closed right after submitting
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _fullFormRequestedFor = MutableStateFlow<OsmQuestKey?>(null)
    /** Quest for which the user explicitly requested the normal quest form */
    val fullFormRequestedFor: StateFlow<OsmQuestKey?> = _fullFormRequestedFor

    fun requestFullForm(questKey: OsmQuestKey) {
        _fullFormRequestedFor.value = questKey
    }

    /** Clears the request for the normal quest form of [questKey], if any */
    fun clearFullFormRequest(questKey: OsmQuestKey) {
        _fullFormRequestedFor.compareAndSet(questKey, null)
    }

    /** Cheap check whether the quick answer sheet may be shown for [quest], to avoid delaying
     *  the normal quest form */
    fun mayShowQuickAnswers(quest: OsmQuest): Boolean =
        _fullFormRequestedFor.value != quest.key && quickAnswerPrefs.isEnabled(quest.type)

    /** Quests of the element of [quest] to show in the quick answer sheet, or null if there are
     *  fewer than two (the normal quest form is quicker then) or [quest] itself is not among them.
     *  Blocking, call off the main thread. */
    fun getQuickAnswerQuests(quest: OsmQuest): List<QuickAnswerQuest>? {
        val element = mapDataSource.get(quest.elementType, quest.elementId) ?: return null
        return visibleQuestsSource.getAll(quest.position.enclosingBoundingBox(0.5))
            .asSequence()
            .filterIsInstance<OsmQuest>()
            .filter { it.elementType == quest.elementType && it.elementId == quest.elementId }
            .filter { quickAnswerPrefs.isEnabled(it.type) }
            .sortedBy { QuickAnswerRegistry.indexOf(it.type) }
            .mapNotNull { q ->
                val definition = QuickAnswerRegistry.get(q.type) ?: return@mapNotNull null
                if (!definition.isApplicableTo(element)) return@mapNotNull null
                val defaultKey = quickAnswerPrefs.getDefaultOptionKey(q.type) ?: return@mapNotNull null
                QuickAnswerQuest(q, definition, defaultKey)
            }
            .toList()
            .takeIf { quests -> quests.size >= 2 && quests.any { it.quest.key == quest.key } }
    }

    /** Applies the selected option (by key) of each quest, in order. Each answer is applied to the
     *  element as modified by the previous ones, and skipped if its quest does not apply anymore.
     *  Creates one edit per quest, exactly like answering each quest in its own form. */
    fun submit(answers: List<Pair<QuickAnswerQuest, String>>) {
        scope.launch {
            val hideAnsweredQuests = prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false)
            for ((quickQuest, optionKey) in answers) {
                val quest = quickQuest.quest
                val element = mapDataSource.get(quest.elementType, quest.elementId) ?: return@launch
                if (quest.type.isApplicableTo(element) == false) continue

                val builder = StringMapChangesBuilder(element.tags)
                quickQuest.definition.applyAnswerTo(quest.type, optionKey, builder, quest.geometry, element.timestampEdited)
                val changes = builder.create()
                if (changes.isEmpty()) continue

                elementEditsController.add(
                    type = quest.type,
                    geometry = quest.geometry,
                    source = "survey",
                    action = UpdateElementTagsAction(element, changes),
                    isNearUserLocation = surveyChecker.checkIsSurvey(quest.geometry),
                    key = null
                )
                // same as for normal quest forms: quests that are not in db don't disappear without this
                if (hideAnsweredQuests) questsHiddenController.tempHide(quest.key)
            }
        }
    }
}
