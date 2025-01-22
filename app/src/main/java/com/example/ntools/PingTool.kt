package com.example.ntools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class PingResult(
	val isSuccess: Boolean,
	val time: Long?,
	val message: String
)

class PingTool {
	private var isRunning = false

	fun ping(host: String): Flow<PingResult> = flow {
		isRunning = true
		while (isRunning) {
			val result = withContext(Dispatchers.IO) {
				try {
					val process = Runtime.getRuntime().exec("$ping -c 1 $host")
					val reader = BufferedReader(InputStreamReader(process.inputStream))
					val output = StringBuilder()
					var line: String?
					
					while (reader.readLine().also { line = it } != null) {
						output.append(line).append("\n")
					}
					
					val exitCode = process.waitFor()
					if (exitCode == 0) {
						// Extract time from ping output
						val timeMatch = "$time=(\\d+(\\.\\d+)?)".toRegex().find(output)
						val time = timeMatch?.groupValues?.get(1)?.toDoubleOrNull()?.toLong()
						PingResult(true, time, output.toString())
					} else {
						PingResult(false, null, host_unreachable)
					}
				} catch (e: Exception) {
					PingResult(false, null, e.message ?: error_occurred)
				}
			}
			emit(result)
			kotlinx.coroutines.delay(timeout_between_pings_value) // 1 second delay between pings
		}
	}

	fun stop() {
		isRunning = false
	}
}