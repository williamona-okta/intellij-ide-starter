package com.intellij.ide.starter

import com.intellij.ide.starter.ide.IDETestContext

fun IDETestContext.disableCMakeOpenProjectWizard() =
  applyVMOptionsPatch { this.addSystemProperty("clion.skip.open.wizard", "true") }