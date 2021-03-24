package com.shabinder.downloader

import kotlinx.coroutines.runBlocking

actual val dotAllRegexOption: RegexOption = RegexOption.DOT_MATCHES_ALL

actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }