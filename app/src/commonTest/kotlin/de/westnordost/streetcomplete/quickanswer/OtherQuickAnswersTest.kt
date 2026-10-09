package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.accepts_cards.AddAcceptsCards
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bench_material.AddBenchMaterial
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_bin.AddBinStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_lit.AddBusStopLit
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeight
import de.westnordost.streetcomplete.quests.parking_access.AddBikeParkingAccess
import de.westnordost.streetcomplete.quests.seating.AddSeating
import de.westnordost.streetcomplete.quests.smoothness.AddPathSmoothness
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingKerb
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Quick answers for kerbs, paths, bus stops, shops, bicycle parkings, toilets and benches */
class OtherQuickAnswersTest {

    @Test fun `every quest type has a definition`() {
        setUpQuestPrefs()
        for ((questType, _) in questTypesWithTags()) {
            assertNotNull(QuickAnswerRegistry.get(questType), questType.name)
        }
    }

    @Test fun `every option changes tags`() {
        setUpQuestPrefs()
        for ((questType, tags) in questTypesWithTags()) {
            for (option in QuickAnswerRegistry.get(questType)!!.options) {
                assertTrue(
                    applyQuickAnswer(questType, option.key, tags).isNotEmpty(),
                    "${questType.name} ${option.key}"
                )
            }
        }
    }

    @Test fun `first options are the usual answers`() {
        setUpQuestPrefs()
        // the quest type always (re)sets barrier=kerb
        assertTrue(StringMapEntryAdd("kerb", "lowered") in applyFirstQuickAnswer(AddKerbHeight(), kerbTags))
        assertEquals(
            setOf(StringMapEntryAdd("shelter", "yes")),
            applyFirstQuickAnswer(AddBusStopShelter(), busStopTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("surface", "asphalt")),
            applyFirstQuickAnswer(AddPathSurface(), footwayTags)
        )
        assertEquals(
            setOf(
                StringMapEntryAdd("payment:debit_cards", "yes"),
                StringMapEntryAdd("payment:credit_cards", "yes"),
            ),
            applyFirstQuickAnswer(AddAcceptsCards(), shopTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("bicycle_parking", "stands")),
            applyFirstQuickAnswer(AddBikeParkingType(), bikeParkingTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("fee", "no")),
            applyFirstQuickAnswer(AddToiletsFee(), toiletsTags)
        )
        assertEquals(
            setOf(StringMapEntryAdd("backrest", "yes")),
            applyFirstQuickAnswer(AddBenchBackrest(), benchTags)
        )
    }

    @Test fun `wheelchair access is not quick answerable`() {
        assertNull(QuickAnswerRegistry.get(AddWheelchairAccessPublicTransport()))
    }
}

private val kerbTags = mapOf("barrier" to "kerb")
private val footwayTags = mapOf("highway" to "footway")
private val stepsTags = mapOf("highway" to "steps")
private val busStopTags = mapOf("highway" to "bus_stop", "public_transport" to "platform")
private val shopTags = mapOf("amenity" to "restaurant")
private val bikeParkingTags = mapOf("amenity" to "bicycle_parking")
private val toiletsTags = mapOf("amenity" to "toilets")
private val benchTags = mapOf("amenity" to "bench")

private fun questTypesWithTags(): List<Pair<OsmElementQuestType<*>, Map<String, String>>> = listOf(
    AddKerbHeight() to kerbTags,
    AddTactilePavingKerb() to kerbTags,
    AddPathSurface() to footwayTags,
    AddPathSmoothness() to footwayTags,
    AddHandrail() to stepsTags,
    AddBusStopShelter() to busStopTags,
    AddBenchStatusOnBusStop() to busStopTags,
    AddBinStatusOnBusStop() to busStopTags,
    AddBusStopLit() to busStopTags,
    AddTactilePavingBusStop() to busStopTags,
    AddAcceptsCash() to shopTags,
    AddAcceptsCards() to shopTags,
    AddSeating() to shopTags,
    AddToiletAvailability() to shopTags,
    AddVegetarian() to shopTags,
    AddVegan() to shopTags,
    AddBikeParkingType() to bikeParkingTags,
    AddBikeParkingCover() to bikeParkingTags,
    AddBikeParkingAccess() to bikeParkingTags,
    AddToiletsFee() to toiletsTags,
    AddBabyChangingTable() to toiletsTags,
    AddBenchBackrest() to benchTags,
    AddBenchMaterial() to benchTags,
)
