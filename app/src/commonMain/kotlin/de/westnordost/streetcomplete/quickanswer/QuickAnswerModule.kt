package de.westnordost.streetcomplete.quickanswer

import org.koin.dsl.module

val quickAnswerModule = module {
    single { QuickAnswerPrefs(get()) }
    single { QuickAnswerController(get(), get(), get(), get(), get(), get(), get()) }
}
