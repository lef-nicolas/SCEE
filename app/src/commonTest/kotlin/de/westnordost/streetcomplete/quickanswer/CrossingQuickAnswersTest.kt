package de.westnordost.streetcomplete.quickanswer

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_kerb_height.AddCrossingKerbHeight
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.crossing_signals.AddCrossingSignals
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.testutils.mockPrefs
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CrossingQuickAnswersTest {

    @Test fun `every crossing quest type has a definition`() {
        setUpPrefs()
        for (questType in crossingQuestTypes()) {
            assertNotNull(QuickAnswerRegistry.get(questType), questType.name)
        }
    }

    @Test fun `every option changes tags`() {
        for (extended in listOf(false, true)) {
            setUpPrefs(extendedCrossingMarkings = extended)
            for (questType in crossingQuestTypes()) {
                val definition = QuickAnswerRegistry.get(questType)!!
                for (option in definition.options) {
                    assertTrue(apply(questType, option.key).isNotEmpty(), "${questType.name} ${option.key}")
                }
            }
        }
    }

    @Test fun `first options are the usual answers`() {
        setUpPrefs()
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
    }

    @Test fun `extended crossing markings do not offer the unspecific yes`() {
        setUpPrefs(extendedCrossingMarkings = true)
        val definition = QuickAnswerRegistry.get(AddCrossingMarkings())!!
        assertFalse(definition.options.any { it.key == "YES" })
        assertEquals(
            setOf(StringMapEntryAdd("crossing:markings", "zebra")),
            applyFirstOption(AddCrossingMarkings())
        )
    }

    @Test fun `applying to another quest type fails`() {
        setUpPrefs()
        val definition = QuickAnswerRegistry.get(AddCrossingIsland())!!
        assertFailsWith<IllegalArgumentException> {
            definition.applyAnswerTo(AddCrossingSignals(), "no", StringMapChangesBuilder(emptyMap()), geometry, 0)
        }
    }

    @Test fun `applying an unknown option fails`() {
        setUpPrefs()
        val questType = AddCrossingIsland()
        assertFailsWith<IllegalArgumentException> { apply(questType, "maybe") }
    }
}

private val geometry = ElementPointGeometry(LatLon(0.0, 0.0))

private fun crossingQuestTypes(): List<OsmElementQuestType<*>> = listOf(
    AddCrossingIsland(),
    AddCrossingMarkings(),
    AddCrossingSignals(),
    AddTactilePavingCrosswalk(),
    AddCrossingKerbHeight(),
)

private fun apply(questType: OsmElementQuestType<*>, key: String): Set<StringMapEntryChange> {
    val builder = StringMapChangesBuilder(mapOf("highway" to "crossing"))
    QuickAnswerRegistry.get(questType)!!.applyAnswerTo(questType, key, builder, geometry, 0)
    return builder.create().changes
}

private fun applyFirstOption(questType: OsmElementQuestType<*>): Set<StringMapEntryChange> =
    apply(questType, QuickAnswerRegistry.get(questType)!!.options.first().key)

private fun setUpPrefs(extendedCrossingMarkings: Boolean = false) {
    val settings: ObservableSettings = mock()
    every { settings.getBoolean(any(), true) } returns true
    every { settings.getBoolean(any(), false) } returns false
    every { settings.getBoolean("qs_AddCrossingMarkings_extended", false) } returns extendedCrossingMarkings
    Prefs.sharedPreferences = mockPrefs()
    Prefs.preferences = Preferences(settings)
}
