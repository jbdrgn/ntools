package com.example.ntools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

data class PortScanResult(
	val port: Int,
	val isOpen: Boolean,
	val serviceName: String? = null
)

class PortScanner {
	private var isScanning = false

	fun scan(host: String, startPort: Int = 1, endPort: Int = 1024): Flow<PortScanResult> = flow {
		isScanning = true
		
		// Common service names mapping
		val commonPorts = mapOf(
			21 to ftp,
			22 to ssh,
			23 to telnet,
			25 to smtp,
			53 to dns,
			80 to http_,
			110 to pop3,
			143 to imap,
			443 to https_,
			445 to smb,
			3306 to mysql,
			3389 to rdp,
			5432 to postgresql,
			8080 to http_proxy
		)

		for (port in startPort..endPort) {
			if (!isScanning) break

			try {
				val socket = Socket()
				socket.connect(InetSocketAddress(host, port), default_timeout_value) // 200ms timeout
				socket.close()
				emit(PortScanResult(
					port = port,
					isOpen = true,
					serviceName = commonPorts[port]
				))
			} catch (e: SocketTimeoutException) {
				emit(PortScanResult(port = port, isOpen = false))
			} catch (e: Exception) {
				emit(PortScanResult(port = port, isOpen = false))
			}
		}
		isScanning = false
	}.flowOn(Dispatchers.IO)

	fun stop() {
		isScanning = false
	}
}