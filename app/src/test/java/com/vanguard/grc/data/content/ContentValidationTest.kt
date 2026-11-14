package com.vanguard.grc.data.content

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ContentValidationTest {

    @Test
    fun `curriculum assets load and validate without errors`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = ContentRepository(context.assets)
        repository.ensureLoaded()
        val index = repository.index.value
        assertTrue("Content load errors: ${index.loadErrors}", index.loadErrors.isEmpty())

        val report = ContentValidator().validate(index)
        val errors = report.issues.filter { it.severity == ContentValidator.Severity.ERROR }
        assertTrue(
            "Content validation errors: ${errors.joinToString { "[${it.contentId}] ${it.message}" }}",
            errors.isEmpty()
        )
    }
}
