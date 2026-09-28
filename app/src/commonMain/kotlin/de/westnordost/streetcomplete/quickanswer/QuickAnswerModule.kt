package de.westnordost.streetcomplete.quickanswer

import org.koin.dsl.module

val quickAnswerModule = module {
    single { QuickAnswerPrefs(get()) }
    // created by ApplicationInitializer to record answers given before any quest is shown
    single { QuickAnswerLastAnswerRecorder(get(), get()) }
    single { QuickAnswerController(get(), get(), get(), get(), get(), get(), get()) }
}
