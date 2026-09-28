package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quickanswer.definitions.crossingQuickAnswers

/** All quest types that support quick answers, in the order they are shown in the sheet */
object QuickAnswerRegistry {
    private val definitions: List<QuickAnswerDefinition<*>> = crossingQuickAnswers

    fun get(questType: QuestType): QuickAnswerDefinition<*>? =
        definitions.firstOrNull { it.questType.isInstance(questType) }

    fun indexOf(questType: QuestType): Int =
        definitions.indexOfFirst { it.questType.isInstance(questType) }
}
