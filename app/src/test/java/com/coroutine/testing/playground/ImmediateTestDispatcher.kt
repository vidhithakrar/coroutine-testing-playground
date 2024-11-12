package com.coroutine.testing.playground

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.coroutines.CoroutineContext

/**
 * A dispatcher that immediately runs tasks. It is a wrapper over [StandardTestDispatcher] which internally calls
 * [TestCoroutineScheduler.runCurrent] on every task being dispatched.
 */
@OptIn(InternalCoroutinesApi::class)
class ImmediateTestDispatcher(scheduler: TestCoroutineScheduler? = null) : CoroutineDispatcher(), Delay {
    private val delegate = StandardTestDispatcher(scheduler)

    override fun dispatch(context: CoroutineContext, block: kotlinx.coroutines.Runnable) {
        delegate.dispatch(context, block)
        delegate.scheduler.runCurrent()
    }

    /** @suppress */
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        delegate.scheduleResumeAfterDelay(timeMillis, continuation)
    }

    /** @suppress */
    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        return delegate.invokeOnTimeout(timeMillis, block, context)
    }
}