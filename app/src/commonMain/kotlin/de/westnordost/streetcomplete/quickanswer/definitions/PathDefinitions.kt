package de.westnordost.streetcomplete.quickanswer.definitions

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.smoothness.AddPathSmoothness
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.PathSurfaceAnswer
import de.westnordost.streetcomplete.quests.surface.SurfaceAnswer
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickOption

/** Quick answers for quests about footways, paths and steps. Whether they are lit is asked by
 *  the same quest type as for streets, see [streetQuickAnswers]. */
val pathQuickAnswers: List<QuickAnswerDefinition<*>> = listOf(
    QuickAnswerDefinition(AddPathSurface::class) { pathSurfaceOptions },
    QuickAnswerDefinition(AddPathSmoothness::class, appliesTo = { hasAllSmoothnessOptions(it.tags) }) { smoothnessOptions },
    QuickAnswerDefinition(AddHandrail::class) { yesNoOptions },
)

private val pathSurfaceOptions = listOf(
    "ASPHALT" to Surface.ASPHALT,
    "PAVING_STONES" to Surface.PAVING_STONES,
    "COMPACTED" to Surface.COMPACTED,
    "FINE_GRAVEL" to Surface.FINE_GRAVEL,
    "DIRT" to Surface.DIRT,
).map { (key, value) -> QuickOption<PathSurfaceAnswer>(key, SurfaceAnswer(value), value.title) }
