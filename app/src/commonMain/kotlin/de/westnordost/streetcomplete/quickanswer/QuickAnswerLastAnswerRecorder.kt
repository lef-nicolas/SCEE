package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType

/** For quest types where the user chose to remember the last answer, sets the default quick
 *  answer to the last answer given, no matter whether in the quick answer sheet or the normal
 *  quest form. */
class QuickAnswerLastAnswerRecorder(
    private val quickAnswerPrefs: QuickAnswerPrefs,
    elementEditsController: ElementEditsController,
) : ElementEditsSource.Listener {

    init {
        elementEditsController.addListener(this)
    }

    override fun onAddedEdit(edit: ElementEdit) {
        val questType = edit.type as? OsmElementQuestType<*> ?: return
        if (!quickAnswerPrefs.isEnabled(questType) || !quickAnswerPrefs.remembersLastAnswer(questType)) return
        val key = findQuickAnswerKey(edit) ?: return
        quickAnswerPrefs.setDefaultOptionKey(questType, key)
    }

    override fun onSyncedEdit(edit: ElementEdit) {}

    override fun onDeletedEdits(edits: List<ElementEdit>) {}
}

/** The key of the quick answer option that results in exactly the tag changes of [edit], if any.
 *
 *  The answer given is not part of an edit, so each option is applied to the tags of the element
 *  before the edit and the result is compared with the edit's changes. */
fun findQuickAnswerKey(edit: ElementEdit): String? {
    val questType = edit.type as? OsmElementQuestType<*> ?: return null
    val action = edit.action as? UpdateElementTagsAction ?: return null
    val definition = QuickAnswerRegistry.get(questType) ?: return null
    val element = action.originalElement
    return definition.options.firstOrNull { option ->
        val builder = StringMapChangesBuilder(element.tags)
        // never let a quest type throwing on unexpected tags break adding edits
        runCatching {
            definition.applyAnswerTo(questType, option.key, builder, edit.originalGeometry, element.timestampEdited)
        }.isSuccess && builder.create() == action.changes
    }?.key
}
