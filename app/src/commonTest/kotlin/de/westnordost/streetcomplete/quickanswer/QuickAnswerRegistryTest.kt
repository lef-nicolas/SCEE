package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.custom.CustomQuestList
import de.westnordost.streetcomplete.quests.getQuestTypeList
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/** Guards against upstream changes that silently break quick answers after a merge, e.g. a quest
 *  type removed from the quest list or merged into another one */
class QuickAnswerRegistryTest {

    private val questTypes by lazy {
        setUpQuestPrefs()
        getQuestTypeList(
            arSupportChecker = mock(),
            getCountryInfoByLocation = { fail() },
            getCountryOrSubdivisionCode = { null },
            getFeature = { null },
            osmoseDao = OsmoseDao(mock(), Prefs.preferences),
            customQuestList = CustomQuestList(),
        ).map { it.second }
    }

    @Test fun `every definition is for a quest type in the quest list`() {
        for (definition in QuickAnswerRegistry.definitions) {
            assertTrue(
                questTypes.any { definition.questType.isInstance(it) },
                "${definition.questType} is not in the quest list anymore"
            )
        }
    }

    @Test fun `every quest type has at most one definition`() {
        for (questType in questTypes) {
            val count = QuickAnswerRegistry.definitions.count { it.questType.isInstance(questType) }
            assertTrue(count <= 1, "${questType.name} has $count definitions")
        }
    }

    @Test fun `option keys are unique per definition`() {
        setUpQuestPrefs()
        for (definition in QuickAnswerRegistry.definitions) {
            val keys = definition.options.map { it.key }
            assertEquals(keys.distinct(), keys, "duplicate keys for ${definition.questType}")
        }
    }

    @Test fun `every option can be applied`() {
        for (questType in questTypes.filterIsInstance<OsmElementQuestType<*>>()) {
            val definition = QuickAnswerRegistry.get(questType) ?: continue
            for (option in definition.options) {
                // must not throw, e.g. because the answer class of the quest type changed
                applyQuickAnswer(questType, option.key, emptyMap())
            }
        }
    }
}
