package de.westnordost.streetcomplete.quickanswer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.quickanswer.QuickAnswerController
import de.westnordost.streetcomplete.quickanswer.QuickAnswerQuest
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetState
import de.westnordost.streetcomplete.ui.common.dialogs.SurveyConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.QuestHeader
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Whether the quick answer sheet replaces the normal quest form */
sealed interface QuickAnswerState {
    data object NotApplicable : QuickAnswerState
    data object Loading : QuickAnswerState
    class Quests(val quests: List<QuickAnswerQuest>) : QuickAnswerState
}

@Composable
fun rememberQuickAnswerState(
    quest: OsmQuest,
    controller: QuickAnswerController = koinInject(),
): QuickAnswerState {
    val fullFormRequestedFor by controller.fullFormRequestedFor.collectAsState()
    // the request only applies while the normal form of this quest is shown
    DisposableEffect(quest) {
        onDispose { controller.clearFullFormRequest(quest.key) }
    }
    var state by remember(quest) {
        mutableStateOf(
            if (controller.mayShowQuickAnswers(quest)) QuickAnswerState.Loading
            else QuickAnswerState.NotApplicable
        )
    }
    LaunchedEffect(quest) {
        if (state != QuickAnswerState.Loading) return@LaunchedEffect
        val quests = withContext(Dispatchers.IO) { controller.getQuickAnswerQuests(quest) }
        state = if (quests != null) QuickAnswerState.Quests(quests) else QuickAnswerState.NotApplicable
    }
    // the shown quest does not change when requesting its own normal form from the sheet
    return if (fullFormRequestedFor == quest.key) QuickAnswerState.NotApplicable else state
}

/** Sheet listing all quick-answerable quests of one element, each pre-filled with its default
 *  answer, to answer all of them with a single confirmation.
 *
 *  @param element the element all [quests] refer to
 *  @param checkIsSurvey whether the answers can be submitted without asking for confirmation that
 *         the user actually surveyed the element
 *  @param onShowQuest show the normal quest form for the given quest */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuickAnswersSheet(
    quests: List<QuickAnswerQuest>,
    element: Element,
    checkIsSurvey: () -> Boolean,
    onToggleSurveyConfirmation: (dontShowAgain: Boolean) -> Unit,
    onShowQuest: (OsmQuestKey) -> Unit,
    onSolved: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    controller: QuickAnswerController = koinInject(),
    featureDictionary: FeatureDictionary = koinInject(),
) {
    // selected option key per quest, null = skip this quest
    val selection = remember(quests) {
        mutableStateMapOf<OsmQuestKey, String?>().apply {
            quests.forEach { put(it.quest.key, it.defaultOptionKey) }
        }
    }
    var confirmSurvey by remember { mutableStateOf(false) }

    val subtitle = nameAndLocationLabel(element, featureDictionary)

    fun submit() {
        val answers = quests.mapNotNull { q -> selection[q.quest.key]?.let { q to it } }
        controller.submit(answers)
        onSolved()
        onDismiss()
    }

    BackHandler { onDismiss() }

    BottomSheetFormScaffold(
        header = {
            QuestHeader(
                title = stringResource(Res.string.quick_answer_title),
                subtitle = subtitle,
                hintText = stringResource(Res.string.quick_answer_hint),
                hintImages = emptyList(),
            )
        },
        content = {
            Column(
                // bottom space so that the OK button does not overlap the last row
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (q in quests) {
                    QuickAnswerRow(
                        quickQuest = q,
                        selectedKey = selection[q.quest.key],
                        onSelect = { selection[q.quest.key] = it },
                        onShowForm = {
                            controller.requestFullForm(q.quest.key)
                            onShowQuest(q.quest.key)
                        },
                    )
                }
            }
        },
        fab = {
            FloatingOkButton(
                visible = selection.values.any { it != null },
                onClick = { if (checkIsSurvey()) submit() else confirmSurvey = true },
            )
        },
        modifier = modifier,
        // all quests must be visible without scrolling, the element itself is highlighted on the map anyway
        initialState = BottomSheetState.Expanded,
    )

    if (confirmSurvey) {
        SurveyConfirmationDialog(
            onDismissRequest = { confirmSurvey = false },
            onConfirmed = { submit() },
            onToggleDontShowAgain = onToggleSurveyConfirmation,
        )
    }
}

/** Quest icon + question, and a button cycling through the options and "skip" */
@Composable
private fun QuickAnswerRow(
    quickQuest: QuickAnswerQuest,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    onShowForm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = quickQuest.definition.options
    val selected = options.firstOrNull { it.key == selectedKey }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(quickQuest.quest.type.icon),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = stringResource(quickQuest.quest.type.title),
            style = MaterialTheme.typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        // cycle: option 1 -> … -> option n -> skip -> option 1
        val onClick = {
            val index = selected?.let { options.indexOf(it) } ?: -1
            onSelect(options.getOrNull(index + 1)?.key)
        }
        Button(onClick = onClick, modifier = Modifier.width(128.dp)) {
            Text(
                text = stringResource(selected?.label ?: Res.string.quick_answer_skip),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onShowForm) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_next_24),
                contentDescription = stringResource(Res.string.quick_answer_open_form),
            )
        }
    }
}

