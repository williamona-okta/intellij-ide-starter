package com.intellij.ide.starter.examples

import com.intellij.ide.starter.extended.statistics.StatisticsEventsHarvester
import com.intellij.ide.starter.extended.statistics.filterByEventId
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.IDEStartResult
import com.intellij.tools.ide.metrics.collector.metrics.PerformanceMetrics.Metric
import com.intellij.tools.ide.metrics.collector.starter.metrics.extractIndexingMetrics
import com.intellij.tools.ide.metrics.collector.telemetry.SpanFilter
import com.intellij.tools.ide.metrics.collector.telemetry.getMetricsFromSpanAndChildren
import com.intellij.tools.ide.performanceTesting.commands.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

@Disabled("Requires local installation of IDE, configs and project")
class PerformanceTests {

  companion object {
    private lateinit var context: IDETestContext

    @BeforeAll
    @JvmStatic
    fun initContext() {
      context = Setup.setupTestContext()
    }
  }

  @Test
  fun openProjectIndexing() {
    val commands = CommandChain().startProfile("indexing").waitForSmartMode().stopProfile().exitApp()
    val contextForIndexing = context.copy().executeDuringIndexing()
    val results = contextForIndexing.runIDE(commands = commands, launchName = "indexing")
    val indexingMetrics = extractIndexingMetrics(results).getListOfIndexingMetrics()
    writeMetricsToCSV(results, indexingMetrics)
  }

  private fun writeMetricsToCSV(results: IDEStartResult, metrics: List<Metric>): Path {
    val resultCsv = results.runContext.reportsDir / "result.csv"
    println("#".repeat(20))
    println("Storing metrics to CSV")
    resultCsv.bufferedWriter().use { writer ->
      metrics.forEach { metric ->
        writer.write(metric.id.name + "," + metric.value)
        println("${metric.id.name}: ${metric.value}")
        writer.newLine()
      }
    }
    println("Result CSV is written to: ${resultCsv.absolutePathString()}")
    println("#".repeat(20))
    return resultCsv
  }

  @Test
  fun openFile() {
    val commandsOpenFile = CommandChain()
      .startProfile("openFile")
      .openFile("src/main/java/com/quantum/pages/GooglePage.java")
      .stopProfile().exitApp()
    val result = context.runIDE(commands = commandsOpenFile, launchName = "openFile")

    val metrics = getMetricsFromSpanAndChildren(
      (result.runContext.logsDir / "opentelemetry.json"), SpanFilter.nameEquals("firstCodeAnalysis")
    )
    writeMetricsToCSV(result, metrics)
  }


  @Test
  fun searchEverywhere() {
    val commandsSearch = CommandChain()
      .startProfile("searchEverywhere")
      .searchEverywhere("symbol", "", "GooglePage", false, true)
      .stopProfile().exitApp()
    val result = context.runIDE(commands = commandsSearch, launchName = "searchEverywhere")
    val metrics = getMetricsFromSpanAndChildren(
      (result.runContext.logsDir / "opentelemetry.json"),
      SpanFilter.nameEquals("searchEverywhere")
    )

    writeMetricsToCSV(result, metrics)
  }


  @Test
  fun reloadMavenProject() {
    val reloadMavenProject = CommandChain()
      .startProfile("reloadMavenProject")
      .importMavenProject()
      .stopProfile()
      .exitApp()
    val result = context.runIDE(commands = reloadMavenProject, launchName = "reloadMavenProject")
    val startTime = StatisticsEventsHarvester(context).getStatisticEventsByGroup("project.import")
      .filterByEventId("import_project.started").sortedBy {
        it.time
      }.first().time
    val finishTime = StatisticsEventsHarvester(context).getStatisticEventsByGroup("project.import")
      .filterByEventId("import_project.finished").sortedBy {
        it.time
      }.last().time
    writeMetricsToCSV(result, listOf(Metric.newDuration("maven.import", finishTime - startTime)))
  }


  @Test
  fun findUsage() {
    val findUsageTest = CommandChain()
      .openFile("src/main/java/com/quantum/pages/GooglePage.java")
      .goto(43, 18)
      .startProfile("findUsage")
      .findUsages("").stopProfile().exitApp()
    val result = context.runIDE(commands = findUsageTest, launchName = "findUsages")
    val metrics = getMetricsFromSpanAndChildren(
      result.runContext.logsDir / "opentelemetry.json",
      SpanFilter.nameEquals("findUsages")
    )
    writeMetricsToCSV(result, metrics)
  }

  @Test
  fun typing() {
    val typingTest = CommandChain()
      .openFile("src/main/java/com/quantum/pages/GooglePage.java")
      .goto(32, 1)
      .startProfile("typing")
      .delayType(150, "public void fooBar(String searchKey){}")
      .stopProfile().exitApp()
    val result = context.runIDE(commands = typingTest, launchName = "typing")
    val metrics = getMetricsFromSpanAndChildren(
      result.runContext.logsDir / "opentelemetry.json",
      SpanFilter.nameEquals("typing")
    )
    writeMetricsToCSV(result, metrics)
  }

  @Test
  fun completion() {
    val completion = CommandChain()
      .openFile("src/main/java/com/quantum/pages/GooglePage.java")
      .goto(34, 17)
      .startProfile("completion")
      .doComplete(1)
      .stopProfile()
      .exitApp()
    val result = context.runIDE(commands = completion, launchName = "completion")
    val metrics = getMetricsFromSpanAndChildren(
      result.runContext.logsDir / "opentelemetry.json",
      SpanFilter.nameEquals("completion")
    )
    writeMetricsToCSV(result, metrics)
  }
}