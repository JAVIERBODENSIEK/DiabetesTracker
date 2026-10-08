package com.j4.diabetestracker

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Base configuration for Robolectric tests with Compose.
 * Use this as a base class for tests that need Compose UI testing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    application = TestConfig.TestApplication::class,
    sdk = [33],
    instrumentedPackages = ["androidx.test.core"]
)
abstract class TestConfig {
    init {
        // Redirect Robolectric logs to standard output
        ShadowLog.stream = System.out
    }

    /**
     * Test application for Robolectric tests
     */
    class TestApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            // Initialize any application-level dependencies here if needed
        }
    }

    companion object {
        /**
         * Get the application context
         */
        fun getContext(): Context {
            return ApplicationProvider.getApplicationContext()
        }
    }
}
