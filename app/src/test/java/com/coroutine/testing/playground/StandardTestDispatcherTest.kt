package com.coroutine.testing.playground

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StandardTestDispatcherTest {

    @Test
    fun testCoroutine() = runTest {
        var result = "Start"

        launch {
            result = "End"
        }

        runCurrent()

        assertThat(result).isEqualTo("End")
    }

    @Test
    fun testCoroutineResumption() = runTest {
        var result = "Start"
        val deferredResult = CompletableDeferred<String>()

        launch {
            result = deferredResult.await()
        }
        runCurrent()

        deferredResult.complete("End")
        runCurrent()

        assertThat(result).isEqualTo("End")
    }

    @Test
    fun testCoroutineWithDeferred() = runTest {
        val deferred = async {
            "End"
        }

        assertThat(deferred.await()).isEqualTo("End")
    }

    @Test
    fun testCoroutineWithDelay() = runTest {
        var result = "Start"

        launch {
            delay(10)
            result = "End"
        }

        advanceUntilIdle()

        assertThat(result).isEqualTo("End")
    }
}