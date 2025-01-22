package com.example.ntools

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import java.util.Locale

fun capitalize(string: String): String {
    return string.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT)
        else it.toString()}
}

fun createSubnetMaskFromCIDR(cidr: Int): String {
    val fullMask = (0xffffffff.toInt() shl (32 - cidr)) and 0xffffffff.toInt()
    return listOf(
        (fullMask shr 24) and 0xff,
        (fullMask shr 16) and 0xff,
        (fullMask shr 8) and 0xff,
        fullMask and 0xff
    ).joinToString(".")
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}