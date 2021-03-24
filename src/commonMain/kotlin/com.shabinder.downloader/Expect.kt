package com.shabinder.downloader

expect val dotAllRegexOption:RegexOption

expect fun runTest(block: suspend () -> Unit)
