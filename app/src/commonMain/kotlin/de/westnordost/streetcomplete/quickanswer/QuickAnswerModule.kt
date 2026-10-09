package de.westnordost.streetcomplete.quickanswer

import org.koin.dsl.module

val quickAnswerModule = module {
    single { QuickAnswerPrefs(get()) }
    // created by QuickAnswerSheetIfApplicable, i.e. before any quest can be answered. Not created at
    // start because the quest types it depends on read Prefs.preferences, set after Koin starts
    single { QuickAnswerLastAnswerRecorder(get(), get()) }
    single { QuickAnswerController(get(), get(), get(), get(), get(), get(), get()) }
}
