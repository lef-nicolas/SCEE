package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quickanswer.definitions.crossingQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.streetQuickAnswers

/** All quest types that support quick answers, in the order they are shown in the sheet */
object QuickAnswerRegistry {
    private val definitions: List<QuickAnswerDefinition<*>> = crossingQuickAnswers + streetQuickAnswers

    fun supports(questType: QuestType): Boolean = get(questType) != null

    fun get(questType: QuestType): QuickAnswerDefinition<*>? =
        definitions.firstOrNull { it.questType.isInstance(questType) }

    fun indexOf(questType: QuestType): Int =
        definitions.indexOfFirst { it.questType.isInstance(questType) }
}
