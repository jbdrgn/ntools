package com.example.ntools

import java.net.InetAddress
import kotlin.math.pow

data class NetworkDetails(
	val networkAddress: String,
	val broadcastAddress: String,
	val firstUsableHost: String,
	val lastUsableHost: String,
	val totalHosts: Long,
	val usableHosts: Long,
	val subnetMask: String,
	val wildcardMask: String,
	val binaryNetmask: String,
	val cidrNotation: String
)

class NetCalculator {
	fun calculateNetwork(ipAddress: String, cidr: Int): NetworkDetails {
		val ip = ipAddress.split(".").map { it.toInt() }
		val mask = createSubnetMaskFromCIDR(cidr)
		val maskOctets = mask.split(".").map { it.toInt() }
		
		// Calculate network address
		val networkOctets = ip.zip(maskOctets).map { (ip, mask) -> ip and mask }
		val networkAddress = networkOctets.joinToString(".")
		
		// Calculate broadcast address
		val wildcardOctets = maskOctets.map { 255 - it }
		val broadcastOctets = networkOctets.zip(wildcardOctets).map { (network, wildcard) -> network or wildcard }
		val broadcastAddress = broadcastOctets.joinToString(".")
		
		// Calculate first and last usable hosts
		val firstUsableHost = if (cidr < 31) {
			val firstOctets = networkOctets.toMutableList()
			firstOctets[3] = firstOctets[3] + 1
			firstOctets.joinToString(".")
		} else networkAddress

		val lastUsableHost = if (cidr < 31) {
			val lastOctets = broadcastOctets.toMutableList()
			lastOctets[3] = lastOctets[3] - 1
			lastOctets.joinToString(".")
		} else broadcastAddress

		// Calculate total and usable hosts
		val totalHosts = 2.0.pow(32 - cidr).toLong()
		val usableHosts = if (cidr < 31) maxOf(0, totalHosts - 2) else totalHosts

		return NetworkDetails(
			networkAddress = networkAddress,
			broadcastAddress = broadcastAddress,
			firstUsableHost = firstUsableHost,
			lastUsableHost = lastUsableHost,
			totalHosts = totalHosts,
			usableHosts = usableHosts,
			subnetMask = mask,
			wildcardMask = wildcardOctets.joinToString("."),
			binaryNetmask = maskOctets.joinToString(".") { it.toString(2).padStart(8, '0') },
			cidrNotation = "/$cidr"
		)
	}

	fun isValidIpAddress(ip: String): Boolean {
		return try {
			InetAddress.getByName(ip)
			ip.split(".").size == 4 && ip.split(".").all { it.toInt() in 0..255 }
		} catch (e: Exception) {
			false
		}
	}
}