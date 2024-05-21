package com.intellij.ide.starter.driver.engine

import com.intellij.driver.client.Driver
import com.intellij.ide.starter.models.IDEStartResult
import com.intellij.ide.starter.process.exec.ProcessExecutor.Companion.killProcessGracefully
import com.intellij.ide.starter.utils.catchAll
import com.intellij.tools.ide.util.common.logError
import com.intellij.tools.ide.util.common.logOutput
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility
import java.util.concurrent.TimeUnit
import kotlin.let
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

open class BackgroundRun(val startResult: Deferred<IDEStartResult>, val driver: Driver, val process: ProcessHandle? = null) {
  open fun <R> useDriverAndCloseIde(closeIdeTimeout: Duration = 1.minutes, block: Driver.() -> R): IDEStartResult {
    try {
      driver.withContext { block(this) }
    }
    finally {
      driver.closeIdeAndWait(closeIdeTimeout)
      runBlocking {
        startResult.await()
      }
    }
    return runBlocking { return@runBlocking startResult.await() }
  }

  open fun closeIdeAndWait(closeIdeTimeout: Duration = 1.minutes) {
    driver.closeIdeAndWait(closeIdeTimeout)
  }

  protected fun Driver.closeIdeAndWait(closeIdeTimeout: Duration, takeScreenshot: Boolean = true) {
    try {
      if (takeScreenshot) {
        takeScreenshot("beforeIdeClosed")
      }
      exitApplication()
      Awaitility.await()
        .pollInterval(3, TimeUnit.SECONDS)
        .atMost(closeIdeTimeout.inWholeSeconds, TimeUnit.SECONDS)
        .until { !isConnected }
    }
    catch (t: Throwable) {
      logError("Error on exit application via Driver", t)
      logOutput("Performing force kill")
      process?.descendants()?.forEach { catchAll { killProcessGracefully(it) } }
      catchAll { process?.let { killProcessGracefully(process) } }
    }
    finally {
      try {
        if (isConnected) close()
      }
      catch (e: Throwable) {
        logError("Error during closing Driver resources: ${e.message}")
      }
    }
  }
}