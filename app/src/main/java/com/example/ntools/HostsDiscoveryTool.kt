package com.example.ntools

import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetAddress
import kotlin.math.pow

data class NetworkInfo(
	val localIp: String,
	val gateway: String,
	val cidr: Int
)

data class DiscoveredHost(
	val ip: String,
	val hostname: String?,
	val isReachable: Boolean
)

class HostsDiscoveryTool(private val context: Context) {
	private var isScanning = false

	private fun getNetworkInfo(): NetworkInfo? {
		val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val network = connectivityManager.activeNetwork ?: return null
		val linkProperties = connectivityManager.getLinkProperties(network)!!

		var localIp: String? = null
		var cidr = 0
		if (0 < linkProperties.linkAddresses.size) {
			if (1 < linkProperties.linkAddresses.size) {
				// Get local IP
				localIp = linkProperties.linkAddresses[1].address.hostAddress?.toString()

				// Calculate CIDR from subnet mask
				cidr = linkProperties.linkAddresses[1].prefixLength
			} else {
				// Get local IP
				localIp = linkProperties.linkAddresses[0].address.hostAddress?.toString()

				// Calculate CIDR from subnet mask
				cidr = linkProperties.linkAddresses[0].prefixLength
			}
		}

		// Get gateway
		val gateway: String = connectivityManager.getLinkProperties(network)!!.dhcpServerAddress.toString().substringAfter("/")

		return if(localIp != null && (Regex(regex_ipv4_address).matches(gateway))){
			NetworkInfo(localIp, gateway, cidr) }
		else { null }
	}

	private fun calculateIpRange(gateway: String, cidr: Int): Pair<Long, Long> {
		val parts = gateway.split(".")
		var ip = 0L
		for (i in 0..3) {
			ip = ip or (parts[i].toLong() shl (24 - 8 * i))
		}
		
		val mask = (2.0.pow(32 - cidr) - 1).toLong()
		val networkAddress = ip and (mask.inv())
		val broadcastAddress = networkAddress or mask
		
		return Pair(networkAddress + 1, broadcastAddress - 1)
	}

	private fun longToIp(ip: Long): String {
		return "${(ip shr 24) and 255}.${(ip shr 16) and 255}.${(ip shr 8) and 255}.${ip and 255}"
	}

	fun scan(): Flow<DiscoveredHost> = flow {
		isScanning = true
		val networkInfo = getNetworkInfo()

		if (networkInfo != null) {
			val (startIp, endIp) = calculateIpRange(networkInfo.gateway, networkInfo.cidr)

			for (ip in startIp..endIp) {
				if (!isScanning) break

				val ipAddress = longToIp(ip)
				try {
					val address = InetAddress.getByName(ipAddress)
					if (address.isReachable(default_timeout_value)) {
						val hostname = try {
							address.canonicalHostName
						} catch (e: Exception) {
							null
						}
						emit(DiscoveredHost(ipAddress, hostname, true))
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		} else {
			val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
			emit(DiscoveredHost(
				connectivityManager.getLinkProperties(
					connectivityManager.activeNetwork //p.e "123"
				)!!.interfaceName.toString(),
				search_not_available,
				false)
			)
		}

		isScanning = false

	}.flowOn(Dispatchers.IO)

	fun stop() {
		isScanning = false
	}
}
