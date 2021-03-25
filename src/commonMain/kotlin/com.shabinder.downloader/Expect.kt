package com.shabinder.downloader

import kotlinx.coroutines.CoroutineScope

expect fun runBlocking(block: suspend CoroutineScope.() -> Unit)

expect val activePlatform : TargetPlatforms