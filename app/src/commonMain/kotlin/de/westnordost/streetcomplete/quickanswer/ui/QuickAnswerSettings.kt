package de.westnordost.streetcomplete.quickanswer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quickanswer.QuickAnswerDefinition
import de.westnordost.streetcomplete.quickanswer.QuickAnswerPrefs
import de.westnordost.streetcomplete.quickanswer.QuickAnswerRegistry
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Button opening the quick answer settings of [questType]. Shows nothing if the quest type does
 *  not support quick answers. Dimmed while quick answers are disabled for this quest type. */
@Composable
fun QuickAnswerSettingsButton(
    questType: QuestType,
    modifier: Modifier = Modifier,
    quickAnswerPrefs: QuickAnswerPrefs = koinInject(),
) {
    val definition = QuickAnswerRegistry.get(questType) ?: return
    var isEnabled by remember(questType) { mutableStateOf(quickAnswerPrefs.isEnabled(questType)) }
    var showDialog by remember { mutableStateOf(false) }

    Icon(
        painter = painterResource(Res.drawable.ic_quick_answer_24),
        contentDescription = stringResource(Res.string.quick_answer_title),
        modifier = modifier
            .size(48.dp)
            .clickable { showDialog = true }
            .padding(12.dp)
            .alpha(if (isEnabled) ContentAlpha.high else ContentAlpha.disabled),
    )
    if (showDialog) {
        QuickAnswerSettingsDialog(
            questType = questType,
            definition = definition,
            onDismissRequest = {
                showDialog = false
                isEnabled = quickAnswerPrefs.isEnabled(questType)
            },
            quickAnswerPrefs = quickAnswerPrefs,
        )
    }
}

/** Enable quick answers for [questType] and choose its default answer. Changes apply immediately. */
@Composable
private fun QuickAnswerSettingsDialog(
    questType: QuestType,
    definition: QuickAnswerDefinition<*>,
    onDismissRequest: () -> Unit,
    quickAnswerPrefs: QuickAnswerPrefs,
) {
    var isEnabled by remember { mutableStateOf(quickAnswerPrefs.isEnabled(questType)) }
    var defaultKey by remember { mutableStateOf(quickAnswerPrefs.getDefaultOptionKey(questType)) }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(questType.title)) },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(isEnabled, role = Role.Checkbox) {
                        isEnabled = it
                        quickAnswerPrefs.setEnabled(questType, it)
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Checkbox(checked = isEnabled, onCheckedChange = null)
                Text(stringResource(Res.string.quick_answer_enable), style = MaterialTheme.typography.body1)
            }
            Text(
                text = stringResource(Res.string.quick_answer_default),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            for (option in definition.options) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(option.key == defaultKey, enabled = isEnabled, role = Role.RadioButton) {
                            defaultKey = option.key
                            quickAnswerPrefs.setDefaultOptionKey(questType, option.key)
                        }
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    RadioButton(selected = option.key == defaultKey, onClick = null, enabled = isEnabled)
                    Text(
                        text = stringResource(option.label),
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.alpha(if (isEnabled) ContentAlpha.high else ContentAlpha.disabled),
                    )
                }
            }
        },
        buttonRow = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(Res.string.ok)) }
        },
    )
}
