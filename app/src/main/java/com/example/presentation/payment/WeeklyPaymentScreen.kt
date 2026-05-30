package com.example.presentation.payment

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.api.Farmer
import com.example.data.api.MilkType
import com.example.data.api.WeeklyPayment
import com.example.util.DateUtils
import com.example.util.LanguageSettings
import com.example.util.PdfGenerator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPaymentScreen(
    viewModel: WeeklyPaymentViewModel,
    isMarathi: Boolean
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        Pair("Register", "नोंदणी यादी"),
        Pair("Single Generate", "बिल तयार करा"),
        Pair("Bulk Generate", "सर्व बिल एकत्रित")
    )

    val uiState by viewModel.uiState.collectAsState()
    val farmers by viewModel.farmers.collectAsState()
    val milkTypes by viewModel.milkTypes.collectAsState()

    val selectedFarmer by viewModel.selectedFarmer.collectAsState()
    val selectedMilkType by viewModel.selectedMilkType.collectAsState()
    val sDate by viewModel.startDate.collectAsState()
    val eDate by viewModel.endDate.collectAsState()

    val previewData by viewModel.previewData.collectAsState()
    val billReport by viewModel.billReport.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Dialog prompts
    var showMarkPaidDialog by remember { mutableStateOf<WeeklyPayment?>(null) }
    var paidAmountState by remember { mutableStateOf("") }
    var paymentModeState by remember { mutableStateOf("Cash") }

    var showBillModal by remember { mutableStateOf<WeeklyPayment?>(null) }
    var showFarmerSelectSearch by remember { mutableStateOf(false) }
    var farmerQueryStr by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.message.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = LanguageSettings.translate(LanguageSettings.weeklySummary.first, LanguageSettings.weeklySummary.second, isMarathi),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                // Tab select header
                TabRow(selectedTabIndex = activeTab) {
                    tabTitles.forEachIndexed { idx, pair ->
                        Tab(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            text = { Text(text = LanguageSettings.translate(pair.first, pair.second, isMarathi), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                // 1. REGISTER LIST TAB
                0 -> {
                    when (val state = uiState) {
                        is PaymentUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is PaymentUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⚠️", fontSize = 42.sp)
                                    Text(state.msg, textAlign = TextAlign.Center)
                                    Button(onClick = { viewModel.fetchPayments() }, modifier = Modifier.padding(top = 10.dp)) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is PaymentUiState.Success -> {
                            // Compute Summary Top values
                            val totalMilk = state.list.sumOf { it.milkAmount ?: 0.0 }
                            val totalDeductions = state.list.sumOf { it.deductionsAmount ?: 0.0 }
                            val totalNet = state.list.sumOf { it.netPayable ?: 0.0 }

                            Column(modifier = Modifier.fillMaxSize()) {
                                // Top Stats Bar
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Milk Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                            Text(String.format("₹%.1f", totalMilk), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Column {
                                            Text("Deductions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                            Text(String.format("₹%.1f", totalDeductions), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Column {
                                            Text("Net Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                            Text(String.format("₹%.1f", totalNet), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }

                                // Payments register list
                                if (state.list.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No weekly bills generated yet.", color = Color.Gray, fontSize = 13.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        items(state.list) { pay ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = pay.farmerName ?: "N/A",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${DateUtils.formatCompactDate(pay.startDate)} - ${DateUtils.formatCompactDate(pay.endDate)} | ${pay.totalLitres ?: 0.0} Ltr",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                        // Badge status
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(
                                                                    if (pay.status == "Paid") Color(0xFF10B981).copy(alpha = 0.12f)
                                                                    else Color(0xFFEF4444).copy(alpha = 0.12f)
                                                                )
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = pay.status ?: "Pending",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (pay.status == "Paid") Color(0xFF10B981) else Color(0xFFEF4444)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = String.format("Net: ₹%.1f", pay.netPayable ?: 0.0),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Row {
                                                            // View bill report
                                                            TextButton(onClick = {
                                                                showBillModal = pay
                                                                viewModel.loadBillReport(pay.paymentId ?: 0)
                                                            }) {
                                                                Text("Invoice", fontSize = 12.sp)
                                                            }
                                                            // Cash action
                                                            if (pay.status != "Paid") {
                                                                Button(
                                                                    onClick = {
                                                                        showMarkPaidDialog = pay
                                                                        paidAmountState = (pay.netPayable ?: 0.0).toString()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                                ) {
                                                                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                }
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                            }
                                                            // Delete Payment
                                                            IconButton(
                                                                onClick = { viewModel.deletePayment(pay.paymentId ?: 0) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "del", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // 2. SINGLE GENERATE TAB
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Generate Single Invoice", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Select Farmer
                                Text("Select Farmer Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showFarmerSelectSearch = true }
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedFarmer?.let { if (isMarathi) it.farmerNameMr.orEmpty().ifEmpty { it.farmerNameEn } ?: "" else it.farmerNameEn ?: "" } ?: "Tap to search farmer",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "down")
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Select Milk Category (Optional)
                                Text("Milk Category Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    // Option All
                                    FilterChip(
                                        selected = selectedMilkType == null,
                                        onClick = { viewModel.selectedMilkType.value = null },
                                        label = { Text("All", fontSize = 12.sp) },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    milkTypes.forEach { type ->
                                        FilterChip(
                                            selected = selectedMilkType?.milkTypeId == type.milkTypeId,
                                            onClick = { viewModel.selectedMilkType.value = type },
                                            label = { Text(type.milkTypeName ?: "", fontSize = 12.sp) },
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Date bounds selection
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text("From date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    val cal = Calendar.getInstance()
                                                    DatePickerDialog(context, { _, y, m, d ->
                                                        viewModel.startDate.value = DateUtils.toIso8601String(y, m, d)
                                                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(DateUtils.formatCompactDate(sDate), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("To date", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    val cal = Calendar.getInstance()
                                                    DatePickerDialog(context, { _, y, m, d ->
                                                        viewModel.endDate.value = DateUtils.toIso8601String(y, m, d)
                                                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(DateUtils.formatCompactDate(eDate), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Actions: Preview & Generate
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { viewModel.previewPayment() },
                                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        enabled = !isLoading && selectedFarmer != null
                                    ) {
                                        Text(LanguageSettings.translate(LanguageSettings.btnPreview.first, LanguageSettings.btnPreview.second, isMarathi))
                                    }

                                    Button(
                                        onClick = { viewModel.generateSinglePayment() },
                                        modifier = Modifier.weight(1f).testTag("generate_weekly_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                        enabled = !isLoading && selectedFarmer != null
                                    ) {
                                        Text(LanguageSettings.translate(LanguageSettings.btnGenerate.first, LanguageSettings.btnGenerate.second, isMarathi), color = Color.White)
                                    }
                                }
                            }
                        }

                        // Preview Visual Panel
                        previewData?.let { data ->
                            Spacer(modifier = Modifier.height(20.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D9488).copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("PROVISIONAL BILL PREVIEW", fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Milk Gross Amount: ₹${data.totalMilkAmount ?: 0.0}", fontSize = 14.sp)
                                    Text("Deductions & Advances Applied: ₹${data.totalDeductions ?: 0.0}", fontSize = 14.sp, color = Color.Red)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Provisional Net Payable: ₹${data.netPayable ?: 0.0}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }

                // 3. BULK GENERATE TAB
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0D9488).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AllInbox, contentDescription = "bulk", tint = Color(0xFF0D9488), modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bulk Payment Process Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Calculate and commit weekly invoice sheets for all active register farmers automatically inside selected dates.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        // Date constraints selectors
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Dates Range Scope", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text("From date", fontSize = 11.sp, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    val cal = Calendar.getInstance()
                                                    DatePickerDialog(context, { _, y, m, d ->
                                                        viewModel.startDate.value = DateUtils.toIso8601String(y, m, d)
                                                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                                }
                                                .padding(10.dp)
                                        ) {
                                            Text(DateUtils.formatCompactDate(sDate), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("To date", fontSize = 11.sp, color = Color.Gray)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable {
                                                    val cal = Calendar.getInstance()
                                                    DatePickerDialog(context, { _, y, m, d ->
                                                        viewModel.endDate.value = DateUtils.toIso8601String(y, m, d)
                                                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                                }
                                                .padding(10.dp)
                                        ) {
                                            Text(DateUtils.formatCompactDate(eDate), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.generateBulkPayments() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("generate_bulk_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Generate All Active Bills", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 1. Mark Paid AlertDialog
        showMarkPaidDialog?.let { pay ->
            AlertDialog(
                onDismissRequest = { showMarkPaidDialog = null },
                title = { Text("Mark Payment as Paid", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Farmer: ${pay.farmerName}", fontSize = 14.sp)
                        Text("Net Outstanding: ₹${pay.netPayable}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Paid Amount TextField
                        TextField(
                            value = paidAmountState,
                            onValueChange = { paidAmountState = it },
                            label = { Text("Paid Amount (₹)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Payment mode selection list
                        Text("Transaction Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            listOf("Cash", "UPI", "Bank").forEach { mode ->
                                val active = paymentModeState == mode
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                        .clickable { paymentModeState = mode },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) Color(0xFF0D9488).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = mode,
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) Color(0xFF0D9488) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = paidAmountState.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.markPaid(pay.paymentId ?: 0, amt, paymentModeState)
                                showMarkPaidDialog = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF))
                    ) {
                        Text("Confirm Receipt", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMarkPaidDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 2. Beautiful Detailed Bill Report Modal Dialog
        showBillModal?.let { pay ->
            AlertDialog(
                onDismissRequest = {
                    showBillModal = null
                    viewModel.loadFilters() // resets local state
                },
                title = { Text("Weekly Invoice Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            val report = billReport
                            if (report == null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No report data collected from the server.", textAlign = TextAlign.Center, color = Color.Gray)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    // Header Layout
                                    Text(text = report.dairyName ?: "DERISET DAIRY", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0D9488))
                                    Text(text = "Farmer: ${report.farmerName} (${report.farmerCode})", fontSize = 13.sp)
                                    Text(text = "Period: ${report.period}", fontSize = 12.sp, color = Color.Gray)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Content rows
                                    report.rows?.forEach { r ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(DateUtils.formatCompactDate(r.date), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("M: ${r.morningLitres ?: 0.0}L (₹${r.morningAmount ?: 0.0})", fontSize = 10.sp)
                                            Text("E: ${r.eveningLitres ?: 0.0}L (₹${r.eveningAmount ?: 0.0})", fontSize = 10.sp)
                                        }
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text("Deductions:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    report.deductions?.forEach { dec ->
                                        Text("- ${dec.type}: -₹${dec.amount} (${dec.remarks ?: ""})", fontSize = 11.sp, color = Color.Red)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0D9488).copy(alpha = 0.1f))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("TOTAL PAYABLE:", fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), fontSize = 13.sp)
                                        Text(String.format("₹%.2f", report.netPayable), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row {
                        // PDF compiling and native sharing Intent
                        billReport?.let { report ->
                            IconButton(onClick = {
                                try {
                                    val file = PdfGenerator.generateBillPdf(context, report)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Weekly PDF Bill"))
                                } catch (e: Exception) {
                                    // Handle missing file provider gracefully
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "share", tint = Color(0xFF0D9488))
                            }
                        }

                        // Send Mail trigger calls
                        IconButton(onClick = {
                            viewModel.sendEmailBill(pay.paymentId ?: 0)
                        }) {
                            Icon(imageVector = Icons.Default.MailOutline, contentDescription = "mail", tint = MaterialTheme.colorScheme.primary)
                        }

                        TextButton(onClick = { showBillModal = null }) {
                            Text("Done")
                        }
                    }
                }
            )
        }

        // Search Farmer Drawer Filter
        if (showFarmerSelectSearch) {
            AlertDialog(
                onDismissRequest = { showFarmerSelectSearch = false },
                title = { Text(text = "Search Farmer") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = farmerQueryStr,
                            onValueChange = { farmerQueryStr = it },
                            placeholder = { Text("Type Code or Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val fList = farmers.filter {
                                it.farmerNameEn?.contains(farmerQueryStr, ignoreCase = true) == true ||
                                it.farmerCode?.contains(farmerQueryStr, ignoreCase = true) == true
                            }
                            fList.forEach { f ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedFarmer.value = f
                                            showFarmerSelectSearch = false
                                        }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(f.farmerNameEn ?: "N/A", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("Code: ${f.farmerCode}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFarmerSelectSearch = false }) {
                        Text("Dismiss")
                    }
                }
            )
        }
    }
}
