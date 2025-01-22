package com.example.ntools

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class NetInfo(context: Context) {
	private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    suspend fun getLocalIP(): String = withContext(Dispatchers.IO) {
		try {
			connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork //p.e. "123"
			)!!.linkAddresses[1].address.hostAddress?.toString() ?: default_network_address
		} catch (e: Exception) {
			not_available
		}
	}

    suspend fun getGateway(): String = withContext(Dispatchers.IO) {
		try {
			connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork //p.e "123"
			)!!.dhcpServerAddress.toString().substringAfter("/")
		} catch (e: Exception) {
			not_available
		}
	}

    suspend fun getSubnetMask(): String = withContext(Dispatchers.IO) {
		try {
			val cidr = connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork //p.e "123"
			)!!.linkAddresses[1].prefixLength
			createSubnetMaskFromCIDR(cidr)
		} catch (e: Exception) {
			not_available
		}
	}

    suspend fun getDNS(): String = withContext(Dispatchers.IO) {
		try {
			connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork
			)!!.dnsServers.getOrNull(0).toString().substringAfter("/")
		} catch (e: Exception) {
			not_available
		}
	}

	suspend fun getDNS2(): String = withContext(Dispatchers.IO) {
		try {
			connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork
			)!!.dnsServers.getOrNull(1).toString().substringAfter("/")
		} catch (e: Exception) {
			not_available
		}
	}

	suspend fun getInterfaceName(): String = withContext(Dispatchers.IO) {
		try {
			connectivityManager.getLinkProperties(
				connectivityManager.activeNetwork //p.e "123"
			)!!.interfaceName.toString()
		} catch (e: Exception) {
			not_available
		}
	}

	suspend fun getPublicIP(): String = withContext(Dispatchers.IO) {
		try {
			URL(api_ipify_org).readText()
		} catch (e: Exception) {
			not_available
		}
	}

	@SuppressLint("DefaultLocale")
	suspend fun getSpeed(): String = withContext(Dispatchers.IO) {
		try {
			String.format(
				"%d",
				wifiManager.connectionInfo.linkSpeed
			)
		} catch (e: Exception) {
			not_available
		}
	}
}