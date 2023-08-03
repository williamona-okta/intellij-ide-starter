package com.intellij.ide.starter.models

import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.runner.IDERunContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class IDEStartResult(
  val runContext: IDERunContext,
  val executionTime: Duration = 0.minutes,
  val vmOptionsDiff: VMOptionsDiff? = null,
  val failureError: Throwable? = null
) {
  val context: IDETestContext get() = runContext.testContext

  val extraAttributes: MutableMap<String, String> = mutableMapOf()

  val mainReportAttributes get() = mapOf("execution time" to executionTime.toString())
}