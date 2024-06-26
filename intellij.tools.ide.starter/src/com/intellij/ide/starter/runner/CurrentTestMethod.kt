package com.intellij.ide.starter.runner

import java.util.concurrent.atomic.AtomicReference

data class TestMethod(val name: String, val clazz: String, val clazzSimpleName: String, val displayName: String) {
  fun fullName(): String {
    return "$clazz.$name"
  }
}

/**
 * Container that contains current test method reference
 */
object CurrentTestMethod {
  private lateinit var testMethod: AtomicReference<TestMethod>

  fun set(method: TestMethod?) {
    if (this::testMethod.isInitialized) {
      testMethod.set(method)
    }
    else {
      testMethod = AtomicReference(method)
    }
  }

  fun get(): TestMethod? {
    return if (this::testMethod.isInitialized) {
      testMethod.get()
    }
    else null
  }
}