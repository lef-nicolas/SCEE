package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_kerb_height.AddCrossingKerbHeight
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.crossing_signals.AddCrossingSignals
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CrossingQuickAnswersTest {

    @Test fun `every crossing quest type has a definition`() {
        setUpQuestPrefs()
        for (questType in crossingQuestTypes()) {
            assertNotNull(QuickAnswerRegistry.get(questType), questType.name)
        }
    }

    @Test fun `every option changes tags`() {
        for (extended in listOf(false, true)) {
            setUpQuestPrefs(extendedCrossingMarkings = extended)
            for (questType in crossingQuestTypes()) {
                val definition = QuickAnswerRegistry.get(questType)!!
                for (option in definition.options) {
                    assertTrue(apply(questType, option.key).isNotEmpty(), "${questType.name} ${option.key}")
                }
            }
        }
    }

    @Test fun `first options are the usual answers`() {
        setUpQuestPrefs()
        assertEquals(
            setOf(StringMapEntryAdd("crossing:island", "no")),
            applyFirstOption(AddCrossingIsland())
        )
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "yes")),
            applyFirstOption(AddCrossingMarkings())
        )
        assertEquals(
            setOf(StringMapEntryAdd("crossing:signals", "no")),
            applyFirstOption(AddCrossingSignals())
        )
        assertEquals(
            setOf(StringMapEntryAdd("tactile_paving", "yes")),
            applyFirstOption(AddTactilePavingCrosswalk())
        )
        assertEquals(
            setOf(StringMapEntryAdd("kerb", "lowered")),
            applyFirstOption(AddCrossingKerbHeight())
        )
        assertEquals(
            setOf(StringMapEntryAdd("button_operated", "yes")),
            applyFirstOption(AddTrafficSignalsButton())
        )
    }

    @Test fun `extended crossing markings do not offer the unspecific yes`() {
        setUpQuestPrefs(extendedCrossingMarkings = true)
        val definition = QuickAnswerRegistry.get(AddCrossingMarkings())!!
        assertFalse(definition.options.any { it.key == "YES" })
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "zebra")),
            applyFirstOption(AddCrossingMarkings())
        )
    }

    @Test fun `applying to another quest type fails`() {
        setUpQuestPrefs()
        val definition = QuickAnswerRegistry.get(AddCrossingIsland())!!
        assertFailsWith<IllegalArgumentException> {
            definition.applyAnswerTo(AddCrossingSignals(), "no", StringMapChangesBuilder(emptyMap()), testGeometry, 0)
        }
    }

    @Test fun `applying an unknown option fails`() {
        setUpQuestPrefs()
        val questType = AddCrossingIsland()
        assertFailsWith<IllegalArgumentException> { apply(questType, "maybe") }
    }
}

private val crossingTags = mapOf("highway" to "crossing")

private fun crossingQuestTypes(): List<OsmElementQuestType<*>> = listOf(
    AddCrossingIsland(),
    AddCrossingMarkings(),
    AddCrossingSignals(),
    AddTactilePavingCrosswalk(),
    AddCrossingKerbHeight(),
    AddTrafficSignalsButton(),
    AddTrafficSignalsSound(),
    AddTrafficSignalsVibration(),
)

private fun apply(questType: OsmElementQuestType<*>, key: String) =
    applyQuickAnswer(questType, key, crossingTags)

private fun applyFirstOption(questType: OsmElementQuestType<*>) =
    applyFirstQuickAnswer(questType, crossingTags)
