package com.jetbrains.performancePlugin.commands.chain

import com.intellij.ide.starter.ide.command.CommandChain
import com.intellij.ide.starter.sdk.SdkObject
import com.intellij.ide.starter.utils.logOutput
import java.io.File
import java.lang.reflect.Modifier
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

private const val CMD_PREFIX = '%'

const val WARMUP = "WARMUP"

const val ENABLE_SYSTEM_METRICS = "ENABLE_SYSTEM_METRICS"

const val WAIT_FOR_SMART_CMD_PREFIX = "${CMD_PREFIX}waitForSmart"

fun <T : CommandChain> T.waitForSmartMode(): T {
  addCommand(WAIT_FOR_SMART_CMD_PREFIX)
  return this
}

const val WAIT_FOR_DUMB_CMD_PREFIX = "${CMD_PREFIX}waitForDumb"
fun <T : CommandChain> T.waitForDumbMode(maxWaitingTimeInSec: Int): T {
  addCommand("$WAIT_FOR_DUMB_CMD_PREFIX $maxWaitingTimeInSec")
  return this
}

const val WAIT_FOR_GIT_LOG_INDEXING = "${CMD_PREFIX}waitForGitLogIndexing"

fun <T : CommandChain> T.waitForGitLogIndexing(): T {
  addCommand(WAIT_FOR_GIT_LOG_INDEXING)
  return this
}

const val WAIT_FOR_ASYNC_REFRESH = "${CMD_PREFIX}waitForAsyncRefresh"

fun <T : CommandChain> T.waitForAsyncRefresh(): T {
  addCommand(WAIT_FOR_ASYNC_REFRESH)
  return this
}

const val RECOVERY_ACTION_CMD_PREFIX = "${CMD_PREFIX}recovery"

fun <T : CommandChain> T.recoveryAction(action: RecoveryActionType): T {
  val possibleArguments = RecoveryActionType.values().map { it.name }

  require(possibleArguments.contains(action.toString())) {
    "Argument ${action} isn't allowed. Possible values: $possibleArguments"
  }

  addCommand(RECOVERY_ACTION_CMD_PREFIX, action.toString())
  return this
}

const val FLUSH_INDEXES_CMD_PREFIX = "${CMD_PREFIX}flushIndexes"

fun <T : CommandChain> T.flushIndexes(): T {
  addCommand(FLUSH_INDEXES_CMD_PREFIX)
  return this
}

const val SETUP_PROJECT_SDK_CMD_PREFIX = "${CMD_PREFIX}setupSDK"

fun <T : CommandChain> T.setupProjectSdk(sdk: SdkObject): T {
  appendRawLine("$SETUP_PROJECT_SDK_CMD_PREFIX \"${sdk.sdkName}\" \"${sdk.sdkType}\" \"${sdk.sdkPath}\"")
  return this
}

private fun <T : CommandChain> T.appendRawLine(line: String): T {
  require(!line.contains("\n")) { "Invalid line to include: $line" }
  addCommand(line)
  return this
}

const val OPEN_FILE_CMD_PREFIX = "${CMD_PREFIX}openFile"

fun <T : CommandChain> T.openFile(relativePath: String): T {
  addCommand(OPEN_FILE_CMD_PREFIX, relativePath)
  return this
}

const val OPEN_RANDOM_FILE_CMD_PREFIX = "${CMD_PREFIX}openRandomFile"
fun <T : CommandChain> T.openRandomFile(extension: String): T {
  addCommand(OPEN_RANDOM_FILE_CMD_PREFIX, extension)
  return this
}

const val OPEN_PROJECT_CMD_PREFIX = "${CMD_PREFIX}openProject"

fun <T : CommandChain> T.openProject(projectPath: Path, detectProjectLeak: Boolean = false): T {
  addCommand(OPEN_PROJECT_CMD_PREFIX, projectPath.toString(), true.toString(), detectProjectLeak.toString())
  return this
}

fun <T : CommandChain> T.reopenProject(): T {
  addCommand(OPEN_PROJECT_CMD_PREFIX)
  return this
}

const val STORE_INDICES_CMD_PREFIX = "${CMD_PREFIX}storeIndices"

fun <T : CommandChain> T.storeIndices(): T {
  addCommand(STORE_INDICES_CMD_PREFIX)
  return this
}

fun <T : CommandChain> T.compareIndices(): T {
  addCommand("%compareIndices")
  return this
}

const val GO_TO_CMD_PREFIX = "${CMD_PREFIX}goto"

fun <T : CommandChain> T.goto(line: Int, column: Int): T {
  addCommand(GO_TO_CMD_PREFIX, line.toString(), column.toString())
  return this
}

const val GO_TO_PSI_ELEMENT_PREFIX = "${CMD_PREFIX}goToNextPsiElement"

fun <T : CommandChain> T.gotoNextPsiElement(vararg name: String): T {
  addCommand(GO_TO_PSI_ELEMENT_PREFIX, *name)
  return this
}

fun <T : CommandChain> T.gotoNextPsiElementIfExist(vararg name: String): T {
  addCommand(GO_TO_PSI_ELEMENT_PREFIX, *name, "SUPPRESS_ERROR_IF_NOT_FOUND")
  return this
}

const val FIND_USAGES_CMD_PREFIX = "${CMD_PREFIX}findUsages"

fun <T : CommandChain> T.findUsages(): T {
  addCommand(FIND_USAGES_CMD_PREFIX)
  return this
}

const val INSPECTION_CMD_PREFIX = "${CMD_PREFIX}inspectCode"

fun <T : CommandChain> T.inspectCode(): T {
  addCommand(INSPECTION_CMD_PREFIX)
  return this
}

const val INSPECTION_EX_CMD_PREFIX = "${CMD_PREFIX}InspectCodeEx"

fun <T : CommandChain> T.inspectCodeEx(
  scopeName: String = "",
  toolShortName: String = "",
  inspectionTrueFields: List<String> = listOf(),
  inspectionFalseFields: List<String> = listOf(),
  downloadFileUrl: String = "",
  directory: String = "",
  hideResults: Boolean = false,
): T {
  var resultCommand = ""
  if (scopeName.isNotBlank()) {
    resultCommand += " -scopeName $scopeName"
  }
  if (toolShortName.isNotBlank()) {
    resultCommand += " -toolShortName $toolShortName"
  }
  if (inspectionTrueFields.isNotEmpty()) {
    resultCommand += " -inspectionTrueFields"
    inspectionTrueFields.forEach {
      resultCommand += " $it"
    }
  }
  if (inspectionFalseFields.isNotEmpty()) {
    resultCommand += " -inspectionFalseFields"
    inspectionFalseFields.forEach {
      resultCommand += " $it"
    }
  }
  if (downloadFileUrl.isNotBlank()) {
    resultCommand += " -downloadFileUrl $downloadFileUrl"
  }
  if (directory.isNotBlank()) {
    resultCommand += " -directory $directory"
  }
  resultCommand += " -hideResults $hideResults"

  addCommand(INSPECTION_EX_CMD_PREFIX + resultCommand)
  return this
}

const val CODE_ANALYSIS_CMD_PREFIX = "${CMD_PREFIX}codeAnalysis"

fun <T : CommandChain> T.checkOnRedCode(): T {
  addCommand("${CODE_ANALYSIS_CMD_PREFIX} ${CodeAnalysisType.CHECK_ON_RED_CODE}")
  return this
}

fun <T : CommandChain> T.checkWarnings(vararg args: String): T {
  val setArgs = args.toSet()
  val stringBuilder = StringBuilder("")
  setArgs.forEach { stringBuilder.append("$it,") }
  addCommand("${CODE_ANALYSIS_CMD_PREFIX} ${CodeAnalysisType.WARNINGS_ANALYSIS} $stringBuilder")
  return this
}

fun <T : CommandChain> T.project(project: File): T {
  addCommand("%%project ${project.absolutePath}")
  return this
}

const val EXIT_APP_CMD_PREFIX = "${CMD_PREFIX}exitApp"

fun <T : CommandChain> T.exitApp(forceExit: Boolean = true): T {
  addCommand(EXIT_APP_CMD_PREFIX, forceExit.toString())
  return this
}

const val EXIT_APP_WITH_TIMEOUT_CMD_PREFIX = "${CMD_PREFIX}exitAppWithTimeout"

fun <T : CommandChain> T.exitAppWithTimeout(timeoutInSeconds: Long): T {
  addCommand(EXIT_APP_WITH_TIMEOUT_CMD_PREFIX, timeoutInSeconds.toString())
  return this
}

const val START_PROFILE_CMD_PREFIX = "${CMD_PREFIX}startProfile"

fun <T : CommandChain> T.startProfile(args: String): T {
  addCommand("${START_PROFILE_CMD_PREFIX} $args")
  return this
}

const val STOP_PROFILE_CMD_PREFIX = "${CMD_PREFIX}stopProfile"

fun <T : CommandChain> T.stopProfile(args: String = "jfr"): T {
  addCommand("${STOP_PROFILE_CMD_PREFIX} $args")
  return this
}

const val MEMORY_DUMP_CMD_PREFIX = "${CMD_PREFIX}memoryDump"

fun <T : CommandChain> T.memoryDump(): T {
  addCommand(MEMORY_DUMP_CMD_PREFIX)
  return this
}

fun <T : CommandChain> T.profileIndexing(args: String): T {
  addCommand("%%profileIndexing $args")
  return this
}

const val CORRUPT_INDEXED_CMD_PREFIX = "${CMD_PREFIX}corruptIndex"

fun <T : CommandChain> T.corruptIndexes(pathToIndexesDir: Path, additionalDir: String = ""): T {
  if (additionalDir.isEmpty()) {
    addCommand(CORRUPT_INDEXED_CMD_PREFIX, pathToIndexesDir.toString())
  }
  else {
    addCommand(CORRUPT_INDEXED_CMD_PREFIX, pathToIndexesDir.toString(), additionalDir)
  }
  return this
}

fun <T : CommandChain> T.corruptIndexPerDir(indexDir: Path): T {
  val dirs = indexDir
    .listDirectoryEntries()
    .filter { it.toFile().isDirectory }
    .filter { it.toFile().name != "stubs" && it.toFile().name != "filetypes" }
    .toList()
  logOutput("Corrupting dirs count: ${dirs.size} list: $dirs")
  dirs.forEach {
    corruptIndexes(indexDir, it.toFile().name)
    flushIndexes()
    checkOnRedCode()
  }
  return this
}

fun <T : CommandChain> T.corruptIndexPerFile(indexDir: Path): T {
  val filesInDir = indexDir
    .toFile()
    .walkTopDown()
    .filter { it.isFile }
    .toList()
  logOutput("Corrupting ${filesInDir.size}")
  filesInDir.forEach {
    corruptIndexes(it.toPath())
    flushIndexes()
    checkOnRedCode()
  }
  return this
}

const val DUMP_PROJECT_FILES_CMD_PREFIX = "${CMD_PREFIX}dumpProjectFiles"

fun <T : CommandChain> T.dumpProjectFiles(): T {
  addCommand(DUMP_PROJECT_FILES_CMD_PREFIX)
  return this
}

const val COMPARE_PROJECT_FILES_CMD_PREFIX = "${CMD_PREFIX}compareProjectFiles"

fun <T : CommandChain> T.compareProjectFiles(firstDir: String, secondDir: String): T {
  addCommand(COMPARE_PROJECT_FILES_CMD_PREFIX, firstDir, secondDir)
  return this
}

const val CLEAN_CACHES_CMD_PREFIX = "${CMD_PREFIX}cleanCaches"

fun <T : CommandChain> T.cleanCaches(): T {
  addCommand(CLEAN_CACHES_CMD_PREFIX)
  return this
}

const val COMPLETION_CMD_PREFIX = "${CMD_PREFIX}doComplete"

fun <T : CommandChain> T.doComplete(completionType: CompletionType = CompletionType.BASIC): T {
  addCommand(COMPLETION_CMD_PREFIX, completionType.name)
  return this
}

fun <T : CommandChain> T.doCompleteWarmup(completionType: CompletionType = CompletionType.BASIC): T {
  addCommand(COMPLETION_CMD_PREFIX, completionType.name, WARMUP)
  return this
}

fun <T : CommandChain> T.doComplete(times: Int): T {
  for (i in 1..times) {
    doComplete()
    pressKey(Keys.ESCAPE)
    cleanCaches()
  }
  return this
}

const val DO_HIGHLIGHTING_CMD_PREFIX = "${CMD_PREFIX}doHighlight"

fun <T : CommandChain> T.doHighlightingWarmup(): T {
  addCommand(DO_HIGHLIGHTING_CMD_PREFIX, WARMUP)
  return this
}

fun <T : CommandChain> T.doHighlighting(): T {
  addCommand(DO_HIGHLIGHTING_CMD_PREFIX)
  return this
}

const val OPEN_PROJECT_VIEW_CMD_PREFIX = "${CMD_PREFIX}openProjectView"

fun <T : CommandChain> T.openProjectView(): T {
  addCommand(OPEN_PROJECT_VIEW_CMD_PREFIX)
  return this
}


const val ENTER_CMD_PREFIX = "${CMD_PREFIX}pressKey"

fun <T : CommandChain> T.pressKey(key: Keys): T {
  addCommand(ENTER_CMD_PREFIX, key.name)
  return this
}

const val DELAY_TYPE_CMD_PREFIX = "${CMD_PREFIX}delayType"

fun <T : CommandChain> T.delayType(delay: Int, text: String): T {
  addCommand(DELAY_TYPE_CMD_PREFIX, "$delay|$text")
  return this
}

const val DO_LOCAL_INSPECTION_CMD_PREFIX = "${CMD_PREFIX}doLocalInspection"

fun <T : CommandChain> T.doLocalInspection(): T {
  addCommand(DO_LOCAL_INSPECTION_CMD_PREFIX)
  return this
}

const val SHOW_ALT_ENTER_CMD_PREFIX = "${CMD_PREFIX}altEnter"

fun <T : CommandChain> T.altEnter(intention: String): T {
  addCommand(SHOW_ALT_ENTER_CMD_PREFIX, intention)
  return this
}

fun <T : CommandChain> T.callAltEnter(times: Int, intention: String = ""): T {
  for (i in 1..times) {
    altEnter(intention)
  }
  return this
}

const val CREATE_ALL_SERVICES_AND_EXTENSIONS_CMD_PREFIX = "${CMD_PREFIX}CreateAllServicesAndExtensions"

fun <T : CommandChain> T.createAllServicesAndExtensions(): T {
  addCommand(CREATE_ALL_SERVICES_AND_EXTENSIONS_CMD_PREFIX)
  return this
}

const val RUN_CONFIGURATION_CMD_PREFIX = "${CMD_PREFIX}runConfiguration"

fun <T : CommandChain> T.runConfiguration(command: String): T {
  addCommand(RUN_CONFIGURATION_CMD_PREFIX, command)
  return this
}

const val OPEN_FILE_WITH_TERMINATE_CMD_PREFIX = "${CMD_PREFIX}openFileWithTerminate"

fun <T : CommandChain> T.openFileWithTerminate(relativePath: String, terminateIdeInSeconds: Long): T {
  addCommand("${OPEN_FILE_WITH_TERMINATE_CMD_PREFIX} $relativePath $terminateIdeInSeconds")
  return this
}

const val START_POWER_SAVE_CMD_PREFIX = "${CMD_PREFIX}startPowerSave"

fun <T : CommandChain> T.startPowerSave(): T {
  addCommand(START_POWER_SAVE_CMD_PREFIX)
  return this
}

const val STOP_POWER_SAVE_CMD_PREFIX = "${CMD_PREFIX}stopPowerSave"

fun <T : CommandChain> T.stopPowerSave(): T {
  addCommand(STOP_POWER_SAVE_CMD_PREFIX)
  return this
}

const val SEARCH_EVERYWHERE_CMD_PREFIX = "${CMD_PREFIX}searchEverywhere"

fun <T : CommandChain> T.searchEverywhere(tab: String = "all", text: String = ""): T {
  addCommand(SEARCH_EVERYWHERE_CMD_PREFIX, "$tab|$text")
  return this
}

const val SELECT_FILE_IN_PROJECT_VIEW = "${CMD_PREFIX}selectFileInProjectView"

fun <T : CommandChain> T.selectFileInProjectView(relativePath: String): T {
  addCommand(SELECT_FILE_IN_PROJECT_VIEW, relativePath)
  return this
}

const val EXPAND_PROJECT_MENU = "${CMD_PREFIX}expandProjectMenu"

fun <T : CommandChain> T.expandProjectMenu(): T {
  addCommand(EXPAND_PROJECT_MENU)
  return this
}

const val EXPAND_MAIN_MENU = "${CMD_PREFIX}expandMainMenu"

fun <T : CommandChain> T.expandMainMenu(): T {
  addCommand(EXPAND_MAIN_MENU)
  return this
}

const val EXPAND_EDITOR_MENU = "${CMD_PREFIX}expandEditorMenu"

fun <T : CommandChain> T.expandEditorMenu(): T {
  addCommand(EXPAND_EDITOR_MENU)
  return this
}

const val TAKE_SCREENSHOT = "${CMD_PREFIX}takeScreenshot"
fun <T : CommandChain> T.takeScreenshot(path: String): T {
  addCommand(TAKE_SCREENSHOT, path)
  return this
}

const val RECORD_REGISTERED_COUNTER_GROUPS = "${CMD_PREFIX}recordRegisteredCounterGroups"
fun <T : CommandChain> T.recordRegisteredCounterGroups(): T {
  addCommand(RECORD_REGISTERED_COUNTER_GROUPS)
  return this
}

const val RECORD_STATE_COLLECTORS = "${CMD_PREFIX}recordStateCollectors"
fun <T : CommandChain> T.recordStateCollectors(): T {
  addCommand(RECORD_STATE_COLLECTORS)
  return this
}

const val RELOAD_FILES = "${CMD_PREFIX}reloadFiles"
fun <T : CommandChain> T.reloadFiles(): T {
  addCommand(RELOAD_FILES)
  return this
}

const val ADD_FILE = "${CMD_PREFIX}addFile"
fun <T : CommandChain> T.addFile(path: String, fileName: String): T {
  addCommand("${ADD_FILE} $path, $fileName")
  return this
}

fun <T : CommandChain> T.call(method: KFunction<String?>, vararg args: String): T {
  val javaMethod = method.javaMethod ?: error("Failed to resolve Java Method from the declaration")
  require(Modifier.isStatic(javaMethod.modifiers)) { "Method $method must be static" }

  addCommand(CMD_PREFIX + "importCall" + " " + javaMethod.declaringClass.name)
  addCommand(CMD_PREFIX + "call" + " " + javaMethod.name + "(" + args.joinToString(", ") + ")")
  return this
}
const val DELETE_FILE = "${CMD_PREFIX}deleteFile"
fun <T : CommandChain> T.deleteFile(path: String, fileName: String): T {
  addCommand("${DELETE_FILE} $path, $fileName")
  return this
}
const val DELAY = "${CMD_PREFIX}delay"
fun <T : CommandChain> T.delay(delay: Int): T {
  addCommand("$DELAY $delay")
  return this
}

fun <T : CommandChain> T.withSystemMetrics(chain: CommandChain): T {
  if (chain == this) throw IllegalStateException("Current command chain provided")
  for (command in chain) {
    addCommand(command.storeToString(), ENABLE_SYSTEM_METRICS)
  }
  return this
}

const val PRESS_KEY_ENTER = "${CMD_PREFIX}pressKeyEnter"
fun <T : CommandChain> T.pressKeyEnter(): T {
  addCommand(PRESS_KEY_ENTER)
  return this
}