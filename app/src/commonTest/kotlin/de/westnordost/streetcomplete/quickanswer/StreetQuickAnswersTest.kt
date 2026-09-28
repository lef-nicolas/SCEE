package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.lane_markings.AddLaneMarkings
import de.westnordost.streetcomplete.quests.lanes.AddLanes
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StreetQuickAnswersTest {

    @Test fun `every street quest type has a definition`() {
        setUpQuestPrefs()
        for (questType in streetQuestTypes()) {
            assertNotNull(QuickAnswerRegistry.get(questType), questType.name)
        }
    }

    @Test fun `every option changes tags`() {
        setUpQuestPrefs()
        for (questType in streetQuestTypes()) {
            for (option in QuickAnswerRegistry.get(questType)!!.options) {
                assertTrue(
                    applyQuickAnswer(questType, option.key, roadTags).isNotEmpty(),
                    "${questType.name} ${option.key}"
                )
            }
        }
    }

    @Test fun `first options are the usual answers`() {
        setUpQuestPrefs()
        assertEquals(
            setOf(StringMapEntryAdd("sidewalk:both", "yes")),
            applyFirstQuickAnswer(AddSidewalk(), roadTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            applyFirstQuickAnswer(AddRoadSurface(), roadTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("lane_markings", "yes")),
            applyFirstQuickAnswer(AddLaneMarkings(), roadTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("lanes", "2")),
            applyFirstQuickAnswer(AddLanes(), roadTags + ("lane_markings" to "yes"))
        )
        assertEquals(
            setOf(StringMapEntryAdd("oneway", "no")),
            applyFirstQuickAnswer(AddOneway(), roadTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("lit", "yes")),
            applyFirstQuickAnswer(AddWayLit(), roadTags)
        )
    }

    @Test fun `lanes are not quick answerable on oneways`() {
        setUpQuestPrefs()
        val definition = QuickAnswerRegistry.get(AddLanes())!!
        assertTrue(definition.isApplicableTo(way(tags = roadTags)))
        assertFalse(definition.isApplicableTo(way(tags = roadTags + ("oneway" to "yes"))))
    }
}

private val roadTags = mapOf("highway" to "residential")

private fun streetQuestTypes(): List<OsmElementQuestType<*>> = listOf(
    AddSidewalk(),
    AddRoadSurface(),
    AddLaneMarkings(),
    AddLanes(),
    AddOneway(),
    AddWayLit(),
)
