package de.westnordost.streetcomplete.quickanswer

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.testutils.mockPrefs
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

val testGeometry = ElementPointGeometry(LatLon(0.0, 0.0))

/** Applies the quick answer with the given [key] of [questType] to an element with [tags] */
fun applyQuickAnswer(
    questType: OsmElementQuestType<*>,
    key: String,
    tags: Map<String, String>,
): Set<StringMapEntryChange> {
    val builder = StringMapChangesBuilder(tags)
    QuickAnswerRegistry.get(questType)!!.applyAnswerTo(questType, key, builder, testGeometry, 0)
    return builder.create().changes
}

fun applyFirstQuickAnswer(questType: OsmElementQuestType<*>, tags: Map<String, String>): Set<StringMapEntryChange> =
    applyQuickAnswer(questType, QuickAnswerRegistry.get(questType)!!.options.first().key, tags)

/** Quest types read their settings from [Prefs.preferences] */
fun setUpQuestPrefs(extendedCrossingMarkings: Boolean = false) {
    val settings: ObservableSettings = mock()
    every { settings.getBoolean(any(), true) } returns true
    every { settings.getBoolean(any(), false) } returns false
    every { settings.getBoolean("qs_AddCrossingMarkings_extended", false) } returns extendedCrossingMarkings
    Prefs.sharedPreferences = mockPrefs()
    Prefs.preferences = Preferences(settings)
}
