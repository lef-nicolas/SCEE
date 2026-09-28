package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

/** One pre-defined answer that can be selected in the quick answer sheet */
class QuickOption<T>(
    /** stable identifier, persisted in the preferences as default answer */
    val key: String,
    val answer: T,
    val label: StringResource,
)

/** Quick answers for one quest type.
 *
 *  Defined outside of the quest type itself, so that quest types (mostly from upstream
 *  StreetComplete) don't need to be modified. The answer is still applied by the quest type's own
 *  [OsmElementQuestType.applyAnswerTo], so the resulting tags are the same as when answering
 *  through the normal quest form. */
class QuickAnswerDefinition<T>(
    val questType: KClass<out OsmElementQuestType<T>>,
    /** Whether the options make sense for the given element. If not, the quest is only answerable
     *  through its normal form (e.g. "1 lane in each direction" on a oneway road). */
    private val appliesTo: (Element) -> Boolean = { true },
    /** Answers the user can cycle through. The first one is the default unless the user chose
     *  another one. Evaluated on each access, as it may depend on quest settings. */
    private val getOptions: () -> List<QuickOption<T>>,
) {
    val options: List<QuickOption<T>> get() = getOptions()

    fun isApplicableTo(element: Element): Boolean = appliesTo(element)

    fun option(key: String?): QuickOption<T>? = options.firstOrNull { it.key == key }

    /** Applies the answer with the given [key] to [tags] using [questType] */
    fun applyAnswerTo(
        questType: OsmElementQuestType<*>,
        key: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        require(this.questType.isInstance(questType)) { "${questType.name} does not match ${this.questType}" }
        val answer = requireNotNull(option(key)) { "Unknown quick answer $key for ${questType.name}" }.answer
        @Suppress("UNCHECKED_CAST")
        (questType as OsmElementQuestType<T>).applyAnswerTo(answer, tags, geometry, timestampEdited)
    }
}
