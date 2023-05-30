package com.intellij.ide.starter.examples.junit5

import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.ide.command.CommandChain
import com.intellij.ide.starter.junit5.JUnit5StarterAssistant
import com.intellij.ide.starter.junit5.hyphenateWithClass
import com.intellij.ide.starter.report.publisher.ReportPublisher
import com.intellij.ide.starter.report.publisher.impl.ConsoleTestResultPublisher
import com.intellij.ide.starter.runner.TestContainerImpl
import com.intellij.ide.starter.examples.data.TestCases
import com.intellij.metricsCollector.metrics.getOpenTelemetry
import com.jetbrains.performancePlugin.commands.chain.exitApp
import com.jetbrains.performancePlugin.commands.chain.inspectCode
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.kodein.di.DI
import org.kodein.di.bindSingleton

@ExtendWith(JUnit5StarterAssistant::class)
class IdeaJUnit5ExampleTest {

  // these properties will be injected via [JUnit5StarterAssistant]
  private lateinit var testInfo: TestInfo
  private lateinit var context: TestContainerImpl

    @Test
  fun openGradleJitPack() {

    val testContext = context
      .initializeTestContext(testInfo.hyphenateWithClass(), TestCases.IC.GradleJitPackSimple)
      .prepareProjectCleanImport()
      .setSharedIndexesDownload(enable = true)

    val exitCommandChain = CommandChain().exitApp()

    testContext.runIDE(
      commands = exitCommandChain,
      launchName = "first run"
    )

    testContext.runIDE(
      commands = exitCommandChain,
      launchName = "second run"
    )
  }

  @Test
  fun openMavenProject() {

    val testContext = context
      .initializeTestContext(testInfo.hyphenateWithClass(), TestCases.IC.MavenSimpleApp)
      .prepareProjectCleanImport()
      .setSharedIndexesDownload(enable = true)

    testContext.runIDE(commands = CommandChain().exitApp())
  }

  @Test
  @Disabled("Long running test (> 10 min)")
  fun inspectMavenProject() {
    val testContext = context
      .initializeTestContext(testInfo.hyphenateWithClass(), TestCases.IC.MavenSimpleApp)
      .collectOpenTelemetry()
      .setSharedIndexesDownload(enable = true)

    testContext.runIDE(commands = CommandChain().inspectCode().exitApp())

    getOpenTelemetry(testContext, "globalInspections").metrics.forEach {
      println("Name: " + it.n)
      println("Value: " + it.v + "ms")
    }
  }
}