package com.vanguard.grc

import android.app.Application
import com.vanguard.grc.core.AppContainer

/**
 * Application entry point.
 *
 * The app uses a hand-rolled service locator ([AppContainer]) rather than a
 * dependency-injection framework. GRC VANGUARD is an offline-first, single
 * module application with a small number of long-lived collaborators, so an
 * explicit container keeps the wiring readable and avoids an extra runtime
 * dependency.
 */
class GrcApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
