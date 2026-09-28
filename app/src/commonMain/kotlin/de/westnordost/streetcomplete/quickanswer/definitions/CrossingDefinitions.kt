package de.westnordost.streetcomplete.quickanswer.definitions

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_kerb_height.AddCrossingKerbHeight
import de.westnordost.streetcomplete.quests.crossing_markings.AddCrossingMarkings
import de.westnordost.streetcomplete.quests.crossing_markings.CrossingMarkings
import de.westnordost.streetcomplete.quests.crossing_signals.AddCrossingSignals
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight
import de.westnordost.streetcomplete.quests.kerb_height.title
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.tactile_paving.TactilePavingCrosswalkAnswer
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickOption
import de.westnordost.streetcomplete.resources.*

/** Quick answers for quests about pedestrian crossings */
val crossingQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddCrossingIsland::class) { noYesOptions },
    QuickAnswerDefinition(AddCrossingMarkings::class) { crossingMarkingsOptions() },
    QuickAnswerDefinition(AddCrossingSignals::class) { noYesOptions },
    QuickAnswerDefinition(AddTactilePavingCrosswalk::class) { tactilePavingOptions },
    QuickAnswerDefinition(AddCrossingKerbHeight::class) { kerbHeightOptions },
)

private val noYesOptions = listOf(
    QuickOption("no", false, Res.string.quest_generic_hasFeature_no),
    QuickOption("yes", true, Res.string.quest_generic_hasFeature_yes),
)

private val tactilePavingOptions = listOf(
    QuickOption("yes", TactilePavingCrosswalkAnswer.YES, Res.string.quest_generic_hasFeature_yes),
    QuickOption("no", TactilePavingCrosswalkAnswer.NO, Res.string.quest_generic_hasFeature_no),
)

private val kerbHeightOptions = listOf(
    KerbHeight.LOWERED,
    KerbHeight.FLUSH,
    KerbHeight.KERB_RAMP,
    KerbHeight.RAISED,
    KerbHeight.NO_KERB,
).map { QuickOption(it.name, it, it.title) }

/** Same key as the private one in [AddCrossingMarkings] */
private const val PREF_CROSSING_MARKING_EXTENDED = "qs_AddCrossingMarkings_extended"

/** With the extended quest setting, "yes" is not a final answer: the quest asks for the kind of
 *  markings, so the options must be kinds of markings too. */
private fun crossingMarkingsOptions(): List<QuickOption<Set<CrossingMarkings>>> =
    if (Prefs.preferences.getBoolean(PREF_CROSSING_MARKING_EXTENDED, false)) {
        listOf(
            CrossingMarkings.ZEBRA,
            CrossingMarkings.LINES,
            CrossingMarkings.LADDER,
            CrossingMarkings.DASHES,
            CrossingMarkings.NO,
        ).map { QuickOption(it.name, setOf(it), it.titleRes!!) }
    } else {
        listOf(
            QuickOption("YES", setOf(CrossingMarkings.YES), Res.string.quest_generic_hasFeature_yes),
            QuickOption("NO", setOf(CrossingMarkings.NO), Res.string.quest_generic_hasFeature_no),
        )
    }
