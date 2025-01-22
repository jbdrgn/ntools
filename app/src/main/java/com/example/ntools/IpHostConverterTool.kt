package com.example.ntools

import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ConversionResult(
	val query: String,
	val results: List<String>,
	val isError: Boolean = false,
	val errorMessage: String? = null
)

class IpHostConverter {
	suspend fun convert(input: String): ConversionResult = withContext(Dispatchers.IO) {
		try {
			if (input.isEmpty()) {
				return@withContext ConversionResult(
					query = input,
					results = emptyList(),
					isError = true,
					errorMessage = capitalize(please_enter_an_ip_address_or_hostname)
				)
			}

			val addresses = InetAddress.getAllByName(input)
			val results = if (isIpAddress(input)) {
				// If input is IP, do reverse DNS lookup
				addresses.map { it.hostName }
			} else {
				// If input is hostname, get all IP addresses
				addresses.map { it.hostAddress } as List<String>
			}

			ConversionResult(
				query = input,
				results = results
			)
		} catch (e: Exception) {
			ConversionResult(
				query = input,
				results = emptyList(),
				isError = true,
				errorMessage = capitalize(unable_to_resolve) +
					": ${e.message}"
			)
		}
	}

	private fun isIpAddress(input: String): Boolean {
		return input.split(".").size == 4 && 
			   input.split(".").all { it.toIntOrNull() in 0..255 }
	}
}