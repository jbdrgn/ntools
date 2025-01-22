package com.example.ntools


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Locale


data class TracerouteHop(
	var hopNumber: Int = -1,
	var address: String = default_ping_no_response_value,
	var hostname: String = default_ping_no_response_value,
	var rtt: Float = 0f
)

class TracerouteTool() {
	private var isRunning = true

	fun traceroute(host: String): Flow<TracerouteHop> = flow {

		if(isHostAvailable(host)) {

			try {
				val ipAddress = withContext(Dispatchers.IO) {
					InetAddress.getByName(host)
				}

				var currentTTL = 1
				val maxHops = 30

				while (isRunning && currentTTL <= maxHops) {
					val process = withContext(Dispatchers.IO) {
						Runtime.getRuntime().exec(
							arrayOf(
								ping,
								"-c",
								"1",
								"-w",
								"1",
								"-t",
								currentTTL.toString(),
								ipAddress.hostAddress
							)
						)
					}

					var output = withContext(Dispatchers.IO) {
						process.inputStream.bufferedReader().use { reader ->
							reader.lineSequence().joinToString(" ") // Join lines with a space
						}
					}

					output = output.lowercase(Locale.ROOT)
					val tracerouteHop = TracerouteHop(hopNumber = currentTTL)


					//Ping OK, Response: OK
					//ping 9.9.9.9 (9.9.9.9) 56(84) bytes of data. 64 bytes from 9.9.9.9: icmp_seq=1 ttl=59 time=274 ms  --- 9.9.9.9 ping statistics --- 1 packets transmitted, 1 received, 0% packet loss, time 0ms rtt min/avg/max/mdev = 274.651/274.651/274.651/0.000 ms

					//Ping OK, Response: KO
					//PING 9.9.9.9 (9.9.9.9) 56(84) bytes of data. From 101.99.11.21: icmp_seq=1 Time to live exceeded--- 9.9.9.9 ping statistics ---1 packets transmitted, 0 received, +1 errors, 100% packet loss, time 0ms

					//Ping KO, Response: KO
					//PING 9.9.9.9 (9.9.9.9) 56(84) bytes of data.  --- 9.9.9.9 ping statistics --- 1 packets transmitted, 0 received, 100% packet loss, time 0ms

					if (output.isNotEmpty() && output.contains("$packets $transmitted")) {
						val fromIpAddress = output.substringAfter(from).trim().substringBefore(":").trim()
						tracerouteHop.address = if(Regex(regex_ipv4_address).matches(fromIpAddress)) fromIpAddress else tracerouteHop.address
						tracerouteHop.hostname = try {
							withContext(Dispatchers.IO) {
								InetAddress.getByName(tracerouteHop.address).hostName
							}
						} catch (e: UnknownHostException) {
							tracerouteHop.hostname
						}
						tracerouteHop.rtt = output.substringAfter("$time=").substringBefore(" $ms")
							.replace(ms, "")
							.toFloatOrNull()
							?: tracerouteHop.rtt
					}

					emit(tracerouteHop)

					if (tracerouteHop.address == ipAddress.hostAddress) {
						break
					}

					currentTTL++
					process.destroy()
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		} else {
			emit(TracerouteHop(-1, unreachable, unknown, 0f))
		}
	}.flowOn(Dispatchers.IO)

	fun start() {
		isRunning = true
	}

	fun stop() {
		isRunning = false
	}

	private fun isHostAvailable(host:String):Boolean{
		var isHostAvailable = false
		try {
			val process = Runtime.getRuntime().exec("$ping -c 1 $host")
			val exitCode = process.waitFor()
			if (exitCode == 0) {
				isHostAvailable = true } else { isRunning = false }
			return isHostAvailable
		} catch (e: Exception) {
			isRunning = false
			return false
		}
	}
}


