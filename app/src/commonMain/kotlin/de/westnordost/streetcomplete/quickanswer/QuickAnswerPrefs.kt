package de.westnordost.streetcomplete.quickanswer

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.questPrefix

/** Quick answer settings of each quest type. Keys follow the quest settings convention, so they
 *  are per preset if quest settings are per preset, and included in settings import / export. */
class QuickAnswerPrefs(private val prefs: Preferences) {

    fun isEnabled(questType: QuestType): Boolean =
        QuickAnswerRegistry.get(questType) != null && prefs.getBoolean(enabledKey(questType), false)

    fun setEnabled(questType: QuestType, enabled: Boolean) {
        if (enabled) prefs.putBoolean(enabledKey(questType), true)
        else prefs.remove(enabledKey(questType))
    }

    /** Whether the last answer given for [questType] becomes its default answer */
    fun remembersLastAnswer(questType: QuestType): Boolean =
        prefs.getBoolean(rememberLastKey(questType), false)

    fun setRemembersLastAnswer(questType: QuestType, remember: Boolean) {
        if (remember) prefs.putBoolean(rememberLastKey(questType), true)
        else prefs.remove(rememberLastKey(questType))
    }

    /** Key of the default option, falls back to the first option of the definition */
    fun getDefaultOptionKey(questType: QuestType): String? {
        val definition = QuickAnswerRegistry.get(questType) ?: return null
        val key = prefs.getString(defaultKey(questType), "")
        return (definition.option(key) ?: definition.options.firstOrNull())?.key
    }

    fun setDefaultOptionKey(questType: QuestType, key: String) {
        prefs.putString(defaultKey(questType), key)
    }

    private fun enabledKey(questType: QuestType) = "${questPrefix(prefs)}qs_${questType.name}_quick_answer"
    private fun rememberLastKey(questType: QuestType) = "${questPrefix(prefs)}qs_${questType.name}_quick_answer_remember_last"
    private fun defaultKey(questType: QuestType) = "${questPrefix(prefs)}qs_${questType.name}_quick_answer_default"
}
