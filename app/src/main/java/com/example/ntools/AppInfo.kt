package com.example.ntools

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class AppInfo {
	@Composable
	fun getVersion(): String {
		return stringResource(R.string.app_version)
	}

	@Composable
	fun getUser(): String {
		return stringResource(R.string.app_dev_user)
	}
}