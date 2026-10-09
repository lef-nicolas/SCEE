package de.westnordost.streetcomplete.quickanswer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quickanswer.QuickAnswerLastAnswerRecorder
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.ShownBottomSheet
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.koinInject

/** Single hook of quick answers into the main bottom sheet, so that merging upstream changes of
 *  MainBottomSheet stays easy. Shows the quick answer sheet in place of the normal quest form if
 *  quick answers are enabled for the quest types of the shown element.
 *
 *  @param checkIsSurvey whether answers for an element with the given geometry can be submitted
 *         without asking for confirmation that the user actually surveyed it
 *  @return whether the normal content of the bottom sheet must not be shown */
@Composable
fun QuickAnswerSheetIfApplicable(
    shownBottomSheet: ShownBottomSheet,
    viewModel: MainBottomSheetViewModel,
    checkIsSurvey: (ElementGeometry) -> Boolean,
    onToggleSurveyConfirmation: (dontShowAgain: Boolean) -> Unit,
    onSolved: (icon: DrawableResource, position: LatLon) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
): Boolean {
    if (shownBottomSheet !is ShownBottomSheet.OsmQuest) return false
    // every quest is shown here before it can be answered, so the last answers are all recorded
    koinInject<QuickAnswerLastAnswerRecorder>()

    val quest = shownBottomSheet.quest
    return when (val quickAnswers = rememberQuickAnswerState(quest)) {
        QuickAnswerState.NotApplicable -> false
        QuickAnswerState.Loading -> true
        is QuickAnswerState.Quests -> {
            QuickAnswersSheet(
                quests = quickAnswers.quests,
                element = shownBottomSheet.element,
                checkIsSurvey = { checkIsSurvey(quest.geometry) },
                onToggleSurveyConfirmation = onToggleSurveyConfirmation,
                onShowQuest = { viewModel.showQuest(it) },
                onSolved = { onSolved(quest.type.icon, quest.position) },
                onDismiss = onDismiss,
                modifier = modifier,
            )
            true
        }
    }
}
