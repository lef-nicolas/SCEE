package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quickanswer.definitions.benchQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.bikeParkingQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.busStopQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.crossingQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.kerbQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.pathQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.shopQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.streetQuickAnswers
import de.westnordost.streetcomplete.quickanswer.definitions.toiletsQuickAnswers

/** All quest types that support quick answers, in the order they are shown in the sheet */
object QuickAnswerRegistry {
    val definitions: List<QuickAnswerDefinition<*>> =
        crossingQuickAnswers +
        kerbQuickAnswers +
        streetQuickAnswers +
        pathQuickAnswers +
        busStopQuickAnswers +
        shopQuickAnswers +
        bikeParkingQuickAnswers +
        toiletsQuickAnswers +
        benchQuickAnswers

    fun supports(questType: QuestType): Boolean = get(questType) != null

    fun get(questType: QuestType): QuickAnswerDefinition<*>? =
        definitions.firstOrNull { it.questType.isInstance(questType) }

    fun indexOf(questType: QuestType): Int =
        definitions.indexOfFirst { it.questType.isInstance(questType) }
}
