package com.shabinder.downloader

import kotlinx.coroutines.CoroutineScope

actual fun runBlocking(block: suspend CoroutineScope.() -> Unit) =
    kotlinx.coroutines.runBlocking(block = block)

actual val activePlatform : TargetPlatforms = TargetPlatforms.Native