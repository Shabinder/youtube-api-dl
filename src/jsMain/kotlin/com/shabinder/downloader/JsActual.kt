package com.shabinder.downloader

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual val dotAllRegexOption = RegexOption.IGNORE_CASE

actual fun runTest(block: suspend () -> Unit): dynamic = GlobalScope.promise { block() }