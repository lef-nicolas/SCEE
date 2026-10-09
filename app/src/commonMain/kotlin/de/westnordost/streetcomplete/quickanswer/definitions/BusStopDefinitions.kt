package de.westnordost.streetcomplete.quickanswer.definitions

import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_bin.AddBinStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_lit.AddBusStopLit
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickOption
import de.westnordost.streetcomplete.resources.*

/** Quick answers for quests about public transport stops.
 *
 *  The wheelchair access quest is left out on purpose: its answer carries the descriptions entered
 *  in the form in a mutable property, so a quick answer could re-use a previously entered one. */
val busStopQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddBusStopShelter::class) { shelterOptions },
    QuickAnswerDefinition(AddBenchStatusOnBusStop::class) { yesNoOptions },
    QuickAnswerDefinition(AddBinStatusOnBusStop::class) { yesNoOptions },
    QuickAnswerDefinition(AddBusStopLit::class) { yesNoOptions },
    QuickAnswerDefinition(AddTactilePavingBusStop::class) { yesNoOptions },
)

private val shelterOptions = listOf(
    QuickOption("yes", BusStopShelterAnswer.SHELTER, Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", BusStopShelterAnswer.NO_SHELTER, Res.string.quest_generic_hasFeature_no),
    QuickOption("covered", BusStopShelterAnswer.COVERED, Res.string.quest_busStopShelter_covered),
)
