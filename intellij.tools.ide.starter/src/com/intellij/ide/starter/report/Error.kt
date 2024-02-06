package com.intellij.ide.starter.report

import com.intellij.ide.starter.utils.generifyErrorMessage
import java.util.*

data class Error(val messageText: String, val stackTraceContent: String, val type: ErrorType) {
  private val generifiedStackTraceContent: String = generifyErrorMessage(stackTraceContent)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Error) return false
    return messageText == other.messageText && generifiedStackTraceContent == other.generifiedStackTraceContent && type == other.type
  }

  override fun hashCode(): Int {
    return Objects.hash(messageText, generifiedStackTraceContent, type)
  }
}