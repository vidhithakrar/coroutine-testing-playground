package com.coroutine.testing.playground

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedTestDispatcherTest {

    @Test
    fun testCoroutine() = runTest(UnconfinedTestDispatcher()) {
        var result = "Start"

        launch {
            result = "End"
        }

        assertThat(result).isEqualTo("End")
    }

    @Test
    fun testCoroutineResumption() = runTest(UnconfinedTestDispatcher()) {
        var result = "Start"
        val deferredResult = CompletableDeferred<String>()

        launch {
            result = deferredResult.await()
        }

        deferredResult.complete("End")

        assertThat(result).isEqualTo("End")
    }

    @Test
    fun testCoroutineWithDeferred() = runTest(UnconfinedTestDispatcher()) {
        val deferred = async {
            "End"
        }

        assertThat(deferred.await()).isEqualTo("End")
    }

    @Test
    fun testCoroutineWithDelay() = runTest(UnconfinedTestDispatcher()) {
        var result = "Start"

        launch {
            delay(10)
            result = "End"
        }

        advanceUntilIdle()

        assertThat(result).isEqualTo("End")
    }
}