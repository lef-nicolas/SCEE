package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.tagEdit
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChanges
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryChange
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.crossing_kerb_height.AddCrossingKerbHeight
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.testutils.node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class QuickAnswerLastAnswerRecorderTest {

    @Test fun `finds option resulting in the same changes`() {
        setUpQuestPrefs()
        assertEquals("no", findQuickAnswerKey(edit(AddCrossingIsland(), StringMapEntryAdd("crossing:island", "no"))))
        assertEquals("yes", findQuickAnswerKey(edit(AddCrossingIsland(), StringMapEntryAdd("crossing:island", "yes"))))
        assertEquals("FLUSH", findQuickAnswerKey(edit(AddCrossingKerbHeight(), StringMapEntryAdd("kerb", "flush"))))
    }

    @Test fun `no option for an answer only available in the full form`() {
        setUpQuestPrefs()
        assertNull(findQuickAnswerKey(edit(AddTactilePavingCrosswalk(), StringMapEntryAdd("tactile_paving", "incorrect"))))
    }

    @Test fun `no option for edits that are not quest answers`() {
        setUpQuestPrefs()
        assertNull(findQuickAnswerKey(edit(tagEdit, StringMapEntryAdd("crossing:island", "no"))))
    }
}

private fun edit(type: ElementEditType, change: StringMapEntryChange) = ElementEdit(
    id = 1,
    type = type,
    originalGeometry = testGeometry,
    source = "survey",
    createdTimestamp = 0,
    isSynced = false,
    action = UpdateElementTagsAction(node(tags = mapOf("highway" to "crossing")), StringMapChanges(setOf(change))),
    isNearUserLocation = true,
)
