package com.superlist.realdevice

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

/**
 * Real-device instrumentation entry point for critical flow execution.
 *
 * Usage (example):
 * ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.superlist.realdevice.AndroidRealDeviceTestRunner
 */
@RunWith(AndroidJUnit4::class)
class AndroidRealDeviceTestRunner
