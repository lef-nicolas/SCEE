package de.westnordost.streetcomplete.quickanswer.definitions

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.lit.LitStatus
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.lane_markings.AddLaneMarkings
import de.westnordost.streetcomplete.quests.lanes.AddLanes
import de.westnordost.streetcomplete.quests.lanes.Lanes
import de.westnordost.streetcomplete.quests.lanes.LanesAnswer
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer
import de.westnordost.streetcomplete.quests.oneway.title
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.smoothness.AddRoadSmoothness
import de.westnordost.streetcomplete.quests.smoothness.Smoothness
import de.westnordost.streetcomplete.quests.smoothness.SmoothnessAnswer
import de.westnordost.streetcomplete.quests.smoothness.SmoothnessValueAnswer
import de.westnordost.streetcomplete.quests.smoothness.getImage
import de.westnordost.streetcomplete.quests.smoothness.title
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.way_lit.WayLit
import de.westnordost.streetcomplete.quests.way_lit.WayLitOrIsStepsAnswer
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickOption
import de.westnordost.streetcomplete.resources.*

/** Quick answers for quests about streets */
val streetQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddSidewalk::class) { sidewalkOptions },
    QuickAnswerDefinition(AddRoadSurface::class) { roadSurfaceOptions },
    QuickAnswerDefinition(AddRoadSmoothness::class, appliesTo = { hasAllSmoothnessOptions(it.tags) }) { smoothnessOptions },
    QuickAnswerDefinition(AddLaneMarkings::class) { yesNoOptions },
    // lanes "in each direction" make no sense on oneways
    QuickAnswerDefinition(AddLanes::class, appliesTo = { !isOneway(it.tags) }) { lanesOptions },
    // the direction of a oneway can't be answered without looking at the way direction on the map
    QuickAnswerDefinition(AddOneway::class) { onewayOptions },
    QuickAnswerDefinition(AddWayLit::class) { wayLitOptions },
)

internal val yesNoOptions = listOf(
    QuickOption("yes", true, Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", false, Res.string.quest_generic_hasFeature_no),
)

private val sidewalkOptions = listOf(
    QuickOption("both", Sides(Sidewalk.YES, Sidewalk.YES), Res.string.quick_answer_sidewalk_both),
    QuickOption("no", Sides(Sidewalk.NO, Sidewalk.NO), Res.string.quest_sidewalk_value_no),
    QuickOption("separate", Sides(Sidewalk.SEPARATE, Sidewalk.SEPARATE), Res.string.quick_answer_sidewalk_separate),
)

private val roadSurfaceOptions = listOf(
    "ASPHALT" to Surface.ASPHALT,
    "PAVING_STONES" to Surface.PAVING_STONES,
    "CONCRETE" to Surface.CONCRETE,
    "SETT" to Surface.SETT,
).map { (key, value) -> QuickOption(key, value, value.title) }

private val smoothnessByKey = listOf(
    "GOOD" to Smoothness.GOOD,
    "EXCELLENT" to Smoothness.EXCELLENT,
    "INTERMEDIATE" to Smoothness.INTERMEDIATE,
    "BAD" to Smoothness.BAD,
)

private val smoothnessValues = smoothnessByKey.map { it.second }

internal val smoothnessOptions = smoothnessByKey
    .map { (key, value) -> QuickOption<SmoothnessAnswer>(key, SmoothnessValueAnswer(value), value.title) }

/** The smoothness form only offers the values that are possible for the surface (e.g. no
 *  "excellent" gravel), so the quick answers are only offered if all of them are possible. */
internal fun hasAllSmoothnessOptions(tags: Map<String, String>): Boolean =
    smoothnessValues.all { it.getImage(tags["surface"]) != null }

private val lanesOptions = listOf<QuickOption<LanesAnswer>>(
    QuickOption("1_1", Lanes(forward = 1, backward = 1), Res.string.quick_answer_lanes_1_1),
    QuickOption("2_2", Lanes(forward = 2, backward = 2), Res.string.quick_answer_lanes_2_2),
    QuickOption("one_lane_only", LanesAnswer.OneLaneOnly, Res.string.quest_lanes_answer_oneLaneOnly),
)

private val onewayOptions = listOf(
    QuickOption("NO_ONEWAY", OnewayAnswer.NO_ONEWAY, OnewayAnswer.NO_ONEWAY.title),
)

private val wayLitOptions = listOf<QuickOption<WayLitOrIsStepsAnswer>>(
    QuickOption("yes", WayLit(LitStatus.YES), Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", WayLit(LitStatus.NO), Res.string.quest_generic_hasFeature_no),
)
