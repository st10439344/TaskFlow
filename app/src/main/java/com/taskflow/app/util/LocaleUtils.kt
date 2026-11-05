package com.taskflow.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleUtils {
    /**
     * Returns a context whose resources use [languageTag] (en, zu or af).
     * Activities get the per-app language automatically, but services and workers do not on Android 12 and
     * below, so notifications built there use this to show text in the language chosen in Settings.
     * (Android Developers, n.d. "Add per-app language support")
     */
    fun localized(base: Context, languageTag: String): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(languageTag))
        return base.createConfigurationContext(config)
    }
}
