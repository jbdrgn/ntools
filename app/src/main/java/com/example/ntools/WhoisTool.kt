package com.example.ntools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

data class WhoisResult(
	val query: String,
	val result: String,
	val isError: Boolean = false
)

class WhoisTool {
	private var isQuerying = false

	fun lookup(query: String): Flow<WhoisResult> = flow {
		isQuerying = true
		try {
			val cleanQuery = query.trim()
			val server = determineWhoisServer(cleanQuery)
			val result = queryWhoisServer(server, cleanQuery)
			emit(WhoisResult(cleanQuery, result))
		} catch (e: Exception) {
			emit(WhoisResult(query, "Error: ${e.message}", true))
		} finally {
			isQuerying = false
		}
	}.flowOn(Dispatchers.IO)

	private fun determineWhoisServer(query: String): String {
		return when {
			query.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) -> "whois.arin.net"
			else -> {
				val domain = query.split(".").last()
				when (domain.lowercase()) {
					"com", "net", "edu" -> "whois.verisign-grs.com"
					"org" -> "whois.pir.org"
					"info" -> "whois.afilias.net"
					else -> "whois.iana.org"
				}
			}
		}
	}

	private fun queryWhoisServer(server: String, query: String): String {
		Socket(server, 43).use { socket ->
			PrintWriter(socket.getOutputStream()).use { out ->
				BufferedReader(InputStreamReader(socket.getInputStream())).use { input ->
					out.println(query)
					out.flush()
					
					return buildString {
						var line: String?
						while (input.readLine().also { line = it } != null && isQuerying) {
							appendLine(line)
						}
					}.trim()
				}
			}
		}
	}

	fun stop() {
		isQuerying = false
	}
}