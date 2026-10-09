package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quickanswer.QuickAnswerRegistry
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Additional (SCEE) filter of the quest selection screen, toggled in the overflow menu.
 *
 *  Kept in this file so that adding a filter does not touch the quest selection screen files
 *  shared with upstream StreetComplete. */
class QuestSelectionFilter(
    /** menu item to show only the quest types matching this filter */
    val label: StringResource,
    val matches: (QuestType) -> Boolean,
)

/** All additional filters, in the order they are shown in the overflow menu */
val questSelectionFilters = listOf(
    QuestSelectionFilter(Res.string.action_quick_answer_quests) { QuickAnswerRegistry.supports(it) },
)

fun List<QuestSelection>.filterBy(filters: Set<QuestSelectionFilter>): List<QuestSelection> =
    filter { quest -> filters.all { it.matches(quest.questType) } }

/** Overflow menu items to toggle each of the [questSelectionFilters] */
@Composable
fun QuestSelectionFilterMenuItems(
    activeFilters: Set<QuestSelectionFilter>,
    onToggle: (QuestSelectionFilter) -> Unit,
    onDismissMenu: () -> Unit,
) {
    for (filter in questSelectionFilters) {
        val isActive = filter in activeFilters
        DropdownMenuItem(onClick = {
            onToggle(filter)
            onDismissMenu()
        }) {
            Text(stringResource(if (isActive) Res.string.action_all_quests else filter.label))
        }
    }
}
