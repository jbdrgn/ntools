package com.example.ntools

import kotlinx.coroutines.launch
import com.example.ntools.ui.theme.NToolsTheme
import com.example.ntools.material.icons.filled.* //own icons Objects
import android.Manifest
import android.app.Activity
import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import android.view.ViewGroup
import androidx.lifecycle.compose.LocalLifecycleOwner

data class Tool(
    val name: String,
    val icon: ImageVector,
    val description: String
)

data class NetworkData(
    val label: String,
    val value: String
)

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(context.getString(R.string.output_text), text)
    clipboardManager.setPrimaryClip(clipData)
}

fun showToast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    ).show()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NToolsTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current

    val tools = remember {
        listOf(
            Tool(context.getString(R.string.tool_hosts_discovery), HostsDiscoveryDevices24, context.getString(R.string.desc_hosts_discovery)),
            Tool(context.getString(R.string.tool_ip_hostname_converter), IpHostsConverterTransform24, context.getString(R.string.desc_ip_hostname_converter)),
            Tool(context.getString(R.string.tool_net_calculator), NetCalculatorSummarize24, context.getString(R.string.desc_net_calculator)),
            Tool(context.getString(R.string.tool_net_info), NetInfoNetworkCheck24, context.getString(R.string.desc_net_info)),
            Tool(context.getString(R.string.tool_ping), PingNetworkPing24, context.getString(R.string.desc_ping)),
            Tool(context.getString(R.string.tool_port_scanner), PortScannerLan24, context.getString(R.string.desc_port_scanner)),
            Tool(context.getString(R.string.tool_scan_qr), QrCodeScanner24, context.getString(R.string.desc_scan_qr)),
            Tool(context.getString(R.string.tool_traceroute), TracerouteRoute24, context.getString(R.string.desc_traceroute)),
            Tool(context.getString(R.string.tool_whois_info), WhoisLanguage24, context.getString(R.string.desc_whois_info)),
            Tool(context.getString(R.string.tool_about), Icons.Default.Info, context.getString(R.string.desc_about)),
            Tool(context.getString(R.string.tool_exit), ExitToApp24, context.getString(R.string.desc_exit))
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val netInfo = remember { NetInfo(context) }
    val scope = rememberCoroutineScope()
    var selectedTool by remember { mutableStateOf<Tool?>(tools[8]) } //Whois tool
    var isCameraPermissionDenied by remember { mutableStateOf(false) }

    ModalNavigationDrawer(

        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = context.getString(R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()
                tools.forEach { tool ->
                    NavigationDrawerItem(
                        icon = { Icon(tool.icon, contentDescription = null) },
                        label = { 
                            Column {
                                Text(tool.name)
                                Text(
                                    tool.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        selected = selectedTool == tool,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                selectedTool = tool
                            }

                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedTool?.name ?: context.getString(R.string.tool_whois_info)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open()
                                    else drawerState.close()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = capitalize(context.getString(R.string.output_menu))
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            //containerColor = Color.Black
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                    when (selectedTool?.name) {
                        stringResource(R.string.tool_hosts_discovery) -> {
                            var isScanning by remember { mutableStateOf(false) }
                            var discoveredHosts by remember { mutableStateOf<List<DiscoveredHost>>(emptyList()) }
                            val hostsDiscoveryTool = remember { HostsDiscoveryTool(context) }

                            if(isNetworkAvailable(context)){
                                LaunchedEffect(isScanning) {
                                    if (isScanning) {
                                        hostsDiscoveryTool.scan().collect { host ->
                                            discoveredHosts = discoveredHosts + host
                                        }
                                        isScanning = false
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (isScanning) {
                                                hostsDiscoveryTool.stop()
                                            } else {
                                                discoveredHosts = emptyList()
                                                isScanning = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(if (isScanning) stringResource(R.string.label_stop_scan) else stringResource(R.string.label_start_scan))
                                    }

                                    if (isScanning) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp)
                                        )
                                    }

                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(discoveredHosts) { host ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val content = "${context.getString(R.string.label_ip)} ${host.ip}\n" +
                                                                (host.hostname?.let { "${context.getString(R.string.label_hostname)} $it" } ?: "")
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    },
                                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.label_ip)+" ${host.ip}",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    if (host.hostname != null) {
                                                        Text(
                                                            text = stringResource(R.string.label_hostname)+" ${host.hostname}",
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        stringResource(R.string.tool_ip_hostname_converter) -> {
                            var input by remember { mutableStateOf(quad9_dns_address) }
                            var conversionResult by remember { mutableStateOf<ConversionResult?>(null) }
                            val converter = remember { IpHostConverter() }
                            val scopeTool = rememberCoroutineScope()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = input,
                                    onValueChange = { input = it },
                                    label = { Text(context.getString(R.string.label_enter_host_or_ip)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        scopeTool.launch {
                                            conversionResult = converter.convert(input)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(context.getString(R.string.label_convert))
                                }

                                conversionResult?.let { result ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                val content = "${context.getString(R.string.label_query)} ${result.query}\n" +
                                                        "${context.getString(R.string.label_results)}\n${result.results}\n"
                                                copyToClipboard(context,content)
                                                showToast(context,context.getString(R.string.output_copied))
                                            },
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${context.getString(R.string.label_query)} ${result.query}",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (result.isError) {
                                                Text(
                                                    text = result.errorMessage ?: capitalize(context.getString(R.string.output_unknown_error)),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else {
                                                Text(
                                                    text = context.getString(R.string.label_results),
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                result.results.forEach { item ->
                                                    Text(
                                                        text = item,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        stringResource(R.string.tool_net_calculator) -> {
                            var ipAddress by remember { mutableStateOf(default_host_address) }
                            var cidr by remember { mutableStateOf(context.getString(R.string.default_value_cidr)) }
                            var calculationResult by remember { mutableStateOf<NetworkDetails?>(null) }
                            var showError by remember { mutableStateOf(false) }
                            val netCalculator = remember { NetCalculator() }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = ipAddress,
                                    onValueChange = {
                                        ipAddress = it
                                        showError = false
                                    },
                                    label = { Text(stringResource(R.string.label_ip_address)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = showError
                                )

                                OutlinedTextField(
                                    value = cidr,
                                    onValueChange = {
                                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..32)) {
                                            cidr = it
                                            showError = false
                                        }
                                    },
                                    label = { Text(stringResource(R.string.label_cidr)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Button(
                                    onClick = {
                                        if (netCalculator.isValidIpAddress(ipAddress) && cidr.toIntOrNull() != null) {
                                            calculationResult = netCalculator.calculateNetwork(ipAddress, cidr.toInt())
                                            showError = false
                                        } else {
                                            showError = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.label_calculate))
                                }

                                calculationResult?.let { result ->
                                    LazyColumn {
                                        items(listOf(
                                            context.getString(R.string.calc_network_address) to result.networkAddress,
                                            context.getString(R.string.calc_broadcast_address) to result.broadcastAddress,
                                            context.getString(R.string.calc_first_host) to result.firstUsableHost,
                                            context.getString(R.string.calc_last_host) to result.lastUsableHost,
                                            context.getString(R.string.calc_total_hosts) to result.totalHosts.toString(),
                                            context.getString(R.string.calc_usable_hosts) to result.usableHosts.toString(),
                                            context.getString(R.string.calc_subnet_mask) to result.subnetMask,
                                            context.getString(R.string.calc_wildcard_mask) to result.wildcardMask,
                                            context.getString(R.string.calc_binary_netmask) to result.binaryNetmask,
                                            context.getString(R.string.calc_cidr_notation) to result.cidrNotation
                                        )) { (label, value) ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        val content = "$label\n$value"
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = value,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        stringResource(R.string.tool_net_info) -> {
                            var networkData by remember { mutableStateOf<List<NetworkData>>(emptyList()) }
                            var isLoading by remember { mutableStateOf(true) }
                            val scopeTool = rememberCoroutineScope()

                            if(isNetworkAvailable(context)){
                                LaunchedEffect(Unit) {
                                    scopeTool.launch {
                                        networkData = listOf(
                                            NetworkData(context.getString(R.string.net_private_ip), netInfo.getLocalIP()),
                                            NetworkData(context.getString(R.string.net_gateway), netInfo.getGateway()),
                                            NetworkData(context.getString(R.string.net_subnet), netInfo.getSubnetMask()),
                                            NetworkData(context.getString(R.string.net_dns1), netInfo.getDNS()),
                                            NetworkData(context.getString(R.string.net_dns2),
                                                if(!netInfo.getDNS2().contains(null_value)) netInfo.getDNS2()
                                                else capitalize(none)),
                                            NetworkData(context.getString(R.string.net_speed), netInfo.getSpeed()),
                                            NetworkData(context.getString(R.string.net_interface_name), netInfo.getInterfaceName()),
                                            NetworkData(context.getString(R.string.net_public_ip), netInfo.getPublicIP())
                                        )
                                        isLoading = false
                                    }
                                }

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        items(networkData) { data ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        val content = "${data.label}\n${data.value}"
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = data.label,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = data.value,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }

                        }
                        stringResource(R.string.tool_ping) -> {
                            var host by remember { mutableStateOf(default_gateway_address) }
                            var isPinging by remember { mutableStateOf(false) }
                            var pingResults by remember { mutableStateOf<List<PingResult>>(emptyList()) }
                            val pingTool = remember { PingTool() }
                            val scopeTool = rememberCoroutineScope()

                            if(isNetworkAvailable(context)){
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = host,
                                        onValueChange = { host = it },
                                        label = { Text(stringResource(R.string.label_enter_host_or_ip)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (!isPinging) {
                                                isPinging = true
                                                pingResults = emptyList()
                                                scopeTool.launch {
                                                    pingTool.ping(host).collect { result ->
                                                        pingResults = pingResults + result
                                                        if (pingResults.size > 10) {
                                                            pingResults = pingResults.drop(1)
                                                        }
                                                    }
                                                    isPinging = false
                                                }
                                            } else {
                                                isPinging = false
                                                pingTool.stop()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isPinging) stringResource(R.string.label_stop) else stringResource(R.string.label_start))
                                    }

                                    LazyColumn {
                                        items(pingResults) { result ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        val content = if (result.isSuccess) {
                                                            "${context.getString(R.string.label_time)} ${result.time}${context.getString(R.string.output_ms)}"
                                                        } else {
                                                            result.message
                                                        }
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    if (result.isSuccess) {
                                                        Text("${context.getString(R.string.label_time)} ${result.time}${context.getString(R.string.output_ms)}")
                                                    } else {
                                                        Text(result.message, color = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        stringResource(R.string.tool_port_scanner) -> {
                            var host by remember { mutableStateOf(default_host_address) }
                            var startPort by remember { mutableStateOf(context.getString(R.string.default_value_start_port)) }
                            var endPort by remember { mutableStateOf(context.getString(R.string.default_value_end_port)) }
                            var isScanning by remember { mutableStateOf(false) }
                            var scanResults by remember { mutableStateOf<List<PortScanResult>>(emptyList()) }
                            val portScanner = remember { PortScanner() }
                            val scopeTool = rememberCoroutineScope()

                            if(isNetworkAvailable(context)){
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = host,
                                        onValueChange = { host = it },
                                        label = { Text(stringResource(R.string.label_enter_host_or_ip)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isScanning
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = startPort,
                                            onValueChange = {
                                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                                    startPort = it
                                                }
                                            },
                                            label = { Text(stringResource(R.string.label_start_port)) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            enabled = !isScanning
                                        )

                                        OutlinedTextField(
                                            value = endPort,
                                            onValueChange = {
                                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                                    endPort = it
                                                }
                                            },
                                            label = { Text(stringResource(R.string.label_end_port)) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            enabled = !isScanning
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (!isScanning) {
                                                isScanning = true
                                                scanResults = emptyList()
                                                scopeTool.launch {
                                                    portScanner.scan(
                                                        host,
                                                        startPort.toIntOrNull() ?: 1,
                                                        endPort.toIntOrNull() ?: 1024
                                                    ).collect { result ->
                                                        scanResults = scanResults + result
                                                    }
                                                    isScanning = false
                                                }
                                            } else {
                                                isScanning = false
                                                portScanner.stop()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isScanning) stringResource(R.string.label_stop_scan) else stringResource(R.string.label_start_scan))
                                    }

                                    if (isScanning) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    }

                                    LazyColumn {
                                        items(scanResults.filter { it.isOpen }) { result ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        val content = "${context.getString(R.string.label_port)} ${result.port}\n" +
                                                                (result.serviceName?.let { "${context.getString(R.string.label_service)} $it" } ?: "")
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "${context.getString(R.string.label_opened_port)} ${result.port}",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    result.serviceName?.let { service ->
                                                        Text(
                                                            text = service,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        stringResource(R.string.tool_scan_qr) -> {
                            var scannedResult by remember { mutableStateOf<String?>(null) }
                            var isLoading by remember { mutableStateOf(true) }
                            var isCameraPermissionGranted by remember { mutableStateOf(
                                isPermissionGranted(context, Manifest.permission.CAMERA))
                            }
                            val scopeTool = rememberCoroutineScope()
                            val qrScanner = remember { QRScannerTool(context) }
                            val lifecycleOwner = LocalLifecycleOwner.current

                            val permissionLauncher = rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { isGranted:Boolean ->
                                if(isGranted) {
                                    isCameraPermissionGranted = true
                                    isCameraPermissionDenied = false
                                }
                                else {
                                    isCameraPermissionDenied = true
                                    isCameraPermissionGranted = false
                                }
                            }

                            if (isCameraPermissionGranted) {
                                LaunchedEffect(Unit) {
                                    scopeTool.launch {
                                        isLoading = false
                                    }
                                }

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        AndroidView(
                                            factory = { ctx ->
                                                PreviewView(ctx).apply {
                                                    layoutParams = ViewGroup.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        default_camera_side
                                                    )
                                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(default_camera_side.dp),
                                            update = { previewView ->
                                                qrScanner.startScanning(
                                                    previewView,
                                                    lifecycleOwner
                                                ) { result ->
                                                    scannedResult = result
                                                }
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        scannedResult?.let { result ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        copyToClipboard(context, result)
                                                        showToast(context, context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Text(
                                                    text = result,
                                                    modifier = Modifier.padding(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    DisposableEffect(Unit) {
                                        onDispose {
                                            qrScanner.stopScanning()
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if(!isCameraPermissionDenied){
                                        Text(stringResource(R.string.output_camera_permission_required))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                                        ) {
                                            Text(stringResource(R.string.output_request_camera_permission))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.output_camera_permission_not_granted),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        stringResource(R.string.tool_traceroute) -> {
                            var host by remember { mutableStateOf(quad9_dns_address) }
                            var isTracing by remember { mutableStateOf(false) }
                            var traceResults by remember { mutableStateOf<List<TracerouteHop>>(emptyList()) }
                            val tracerouteTool = remember { TracerouteTool() }
                            val scopeTool = rememberCoroutineScope()

                            if(isNetworkAvailable(context)){
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = host,
                                        onValueChange = { host = it },
                                        label = { Text(context.getString(R.string.label_enter_host_or_ip)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (!isTracing) {
                                                isTracing = true
                                                traceResults = emptyList()
                                                scopeTool.launch {
                                                    tracerouteTool.start()
                                                    tracerouteTool.traceroute(host).collect { hop ->
                                                        traceResults = traceResults + hop
                                                    }
                                                    isTracing = false
                                                }
                                            } else {
                                                isTracing = false
                                                tracerouteTool.stop()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            if (isTracing) context.getString(R.string.label_stop)
                                            else context.getString(R.string.label_start)
                                        )
                                    }

                                    LazyColumn {
                                        items(traceResults) { hop ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable {
                                                        val content = "${capitalize(context.getString(R.string.output_hop))} ${hop.hopNumber}\n" +
                                                                "${context.getString(R.string.label_ip)} ${hop.address}\n" +
                                                                if (!hop.address.contains(context.getString(R.string.output_unreachable))) {
                                                                    "${context.getString(R.string.label_hostname)} ${hop.hostname}\n" +
                                                                            "${context.getString(R.string.label_time)} ${hop.rtt}"
                                                                } else {
                                                                    capitalize(context.getString(R.string.output_status_packet_lost))
                                                                }
                                                        copyToClipboard(context,content)
                                                        showToast(context,context.getString(R.string.output_copied))
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                ) {
                                                    Text("${capitalize(context.getString(R.string.output_hop))} ${hop.hopNumber}")
                                                    Text("${context.getString(R.string.label_ip)} ${hop.address}")
                                                    Text("${context.getString(R.string.label_hostname)} ${hop.hostname}")
                                                    if(0f < hop.rtt){
                                                        Text("${context.getString(R.string.label_time)} ${hop.rtt}")
                                                    } else {
                                                        Text(
                                                            text = "${context.getString(R.string.label_time)} ${context.getString(R.string.output_unknown)}",
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        stringResource(R.string.tool_whois_info) -> {
                            var query by remember { mutableStateOf(quad9_dns_address) }
                            var isQuerying by remember { mutableStateOf(false) }
                            var whoisResult by remember { mutableStateOf<WhoisResult?>(null) }
                            val whoisTool = remember { WhoisTool() }
                            val scopeTool = rememberCoroutineScope()

                            if(isNetworkAvailable(context)){
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        label = { Text(context.getString(R.string.label_enter_domain)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (!isQuerying && query.isNotBlank()) {
                                                isQuerying = true
                                                whoisResult = null
                                                scopeTool.launch {
                                                    whoisTool.lookup(query).collect { result ->
                                                        whoisResult = result
                                                        isQuerying = false
                                                    }
                                                }
                                            } else if (isQuerying) {
                                                isQuerying = false
                                                whoisTool.stop()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = query.isNotBlank() || isQuerying
                                    ) {
                                        Text(
                                            if (isQuerying) context.getString(R.string.label_stop)
                                            else context.getString(R.string.label_lookup)
                                        )
                                    }

                                    if (isQuerying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }

                                    whoisResult?.let { result ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .clickable {
                                                    val content = "${context.getString(R.string.label_query)} ${result.query}\n${result.result}"
                                                    copyToClipboard(context,content)
                                                    showToast(context,context.getString(R.string.output_copied))
                                                }
                                        ) {
                                            if (result.isError) {
                                                Text(
                                                    result.result,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(16.dp)
                                                )
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(16.dp)
                                                ) {
                                                    item {
                                                        Text(
                                                            "Query: ${result.query}",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            modifier = Modifier.padding(bottom = 8.dp)
                                                        )
                                                        Text(
                                                            result.result,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            modifier = Modifier.padding(bottom = 16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Text(
                                    text = stringResource(R.string.output_no_network),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp)
                                )
                            }
                        }
                        stringResource(R.string.tool_about) -> {
                            val appInfo = remember { AppInfo() }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.nt_logo),
                                    contentDescription = context.getString(R.string.app_icon),
                                    modifier = Modifier
                                        .size(128.dp)
                                        .padding(bottom = 30.dp)
                                )
                                Text(
                                    text = context.getString(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                        .clickable {
                                            val content = context.getString(R.string.app_name)
                                            copyToClipboard(context,content)
                                            showToast(context,context.getString(R.string.output_copied))
                                        }
                                )
                                Text(
                                    text = stringResource(R.string.label_version)+" ${appInfo.getVersion()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .clickable {
                                            val content = context.getString(R.string.label_version)+" "+context.getString(R.string.app_version)
                                            copyToClipboard(context,content)
                                            showToast(context,context.getString(R.string.output_copied))
                                        }
                                )
                                Text(
                                    text = stringResource(R.string.label_by_author)+" ${appInfo.getUser()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .clickable {
                                            val content = context.getString(R.string.label_by_author)+" "+context.getString(R.string.app_dev_user)
                                            copyToClipboard(context,content)
                                            showToast(context,context.getString(R.string.output_copied))
                                        }
                                )
                            }
                        }
                        stringResource(R.string.tool_exit) -> {
                            (context as? Activity)?.finish()
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.label_select_tool),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

