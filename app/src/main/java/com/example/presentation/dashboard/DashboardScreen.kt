package com.example.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ChartPoint
import com.example.data.api.DashboardAll
import com.example.data.api.TopFarmer
import com.example.data.api.ApiJsonParser
import com.example.util.LanguageSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMorning: () -> Unit,
    onNavigateToEvening: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToFarmers: () -> Unit,
    onNavigateToDues: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val session by viewModel.sessionFlow.collectAsState(initial = null)
    val curLanguage by viewModel.languageFlow.collectAsState(initial = "English")
    val isMarathi = curLanguage == "Marathi"

    val dairies by viewModel.dairies.collectAsState()
    val superSelectedDairy by viewModel.superSelectedDairy.collectAsState(initial = null)
    
    var showDairyPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isMarathi) "डेअरीसेट डॅशबोर्ड" else "DERISET Dashboard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        session?.let {
                            Text(
                                text = if (isMarathi) it.dairyNameMr.ifEmpty { it.dairyName } else it.dairyName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher icon
                    IconButton(onClick = { viewModel.toggleLanguage(curLanguage) }) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = "Lang")
                    }
                    
                    // Logout
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SuperAdmin Diary selector
            if (session?.isSuperAdmin == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDairyPicker = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Domain, contentDescription = "Dairy", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isMarathi) "प्रशासक: डेअरी निवडा" else "SuperAdmin: Filter Dairy",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = superSelectedDairy?.second ?: (if (isMarathi) "सर्व विभाग अहवाल" else "All Dairies Consolidated"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Dairy selection drop dialog
            if (showDairyPicker) {
                AlertDialog(
                    onDismissRequest = { showDairyPicker = false },
                    title = { Text(text = if (isMarathi) "डेअरी फिल्टर" else "Select Dairy Filter") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Consolidated overall row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.clearSuperSelectedDairy()
                                        showDairyPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = "all")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (isMarathi) "सर्व एकत्रित डेअरी" else "Consolidated View")
                            }
                            HorizontalDivider()

                            dairies.forEach { d ->
                                val dairyId = d.dairyId ?: 0
                                val dairyName = d.dairyName ?: ""
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSuperSelectedDairy(dairyId, dairyName)
                                            showDairyPicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Storefront, contentDescription = "store")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (isMarathi) d.dairyNameMr ?: dairyName else dairyName)
                                }
                                HorizontalDivider()
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDairyPicker = false }) {
                            Text(text = "Close")
                        }
                    }
                )
            }

            // Main UI loading state
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = state.message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadDashboardData() }) {
                                Text("Retry Connection")
                            }
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardScrollContent(
                        data = state.data,
                        widgetsList = parseWidgets(session?.myWidgetsJson),
                        isMarathi = isMarathi,
                        onNavigateToMorning = onNavigateToMorning,
                        onNavigateToEvening = onNavigateToEvening,
                        onNavigateToPayments = onNavigateToPayments,
                        onNavigateToFarmers = onNavigateToFarmers,
                        onNavigateToDues = onNavigateToDues
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScrollContent(
    data: DashboardAll,
    widgetsList: List<String>,
    isMarathi: Boolean,
    onNavigateToMorning: () -> Unit,
    onNavigateToEvening: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToFarmers: () -> Unit,
    onNavigateToDues: () -> Unit
) {
    // If widgets is empty, show all. Otherwise filter keys.
    val showWidget = { key: String ->
        if (widgetsList.isEmpty()) true
        else widgetsList.any { it.trim().equals(key, ignoreCase = true) || it.contains(key, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Quick Navigation Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Short morning
                QuickNavAction(
                    icon = "🌅",
                    label = LanguageSettings.translate(LanguageSettings.txMorningColl.first, LanguageSettings.txMorningColl.second, isMarathi),
                    onClick = onNavigateToMorning,
                    color = Color(0xFFFFB03A)
                )
                // Short evening
                QuickNavAction(
                    icon = "🌃",
                    label = LanguageSettings.translate(LanguageSettings.txEveningColl.first, LanguageSettings.txEveningColl.second, isMarathi),
                    onClick = onNavigateToEvening,
                    color = Color(0xFF6366F1)
                )
                // Short Payments
                QuickNavAction(
                    icon = "₹",
                    label = LanguageSettings.translate(LanguageSettings.navPayments.first, LanguageSettings.navPayments.second, isMarathi),
                    onClick = onNavigateToPayments,
                    color = Color(0xFF10B981)
                )
                // Short Farmers
                QuickNavAction(
                    icon = "👥",
                    label = LanguageSettings.translate(LanguageSettings.masFarmers.first, LanguageSettings.masFarmers.second, isMarathi),
                    onClick = onNavigateToFarmers,
                    color = Color(0xFF0D9488)
                )
            }
        }

        // 1. STAT CARDS: horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Stat Card Collection
            if (showWidget("collection")) {
                StatGradCard(
                    title = LanguageSettings.translate(LanguageSettings.todayColl.first, LanguageSettings.todayColl.second, isMarathi),
                    litres = data.todayCollection?.totalLitres ?: 0.0,
                    amount = data.todayCollection?.totalAmount ?: 0.0,
                    rightSpec = "Cow ${data.todayCollection?.cowLitres?:0.0}L | Buf ${data.todayCollection?.buffaloLitres?:0.0}L",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFF0F766E), Color(0xFF134E4A)))
                )
            }

            // Stat Card Purchase
            if (showWidget("purchase")) {
                StatGradCard(
                    title = LanguageSettings.translate(LanguageSettings.todayPurch.first, LanguageSettings.todayPurch.second, isMarathi),
                    litres = data.todayPurchase?.totalLitres ?: 0.0,
                    amount = data.todayPurchase?.totalAmount ?: 0.0,
                    rightSpec = "Cow ${data.todayPurchase?.cowLitres?:0.0}L | Buf ${data.todayPurchase?.buffaloLitres?:0.0}L",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFF312E81), Color(0xFF1E1B4B)))
                )
            }

            // Stat Card Farmers count
            if (showWidget("farmer")) {
                StatGradCard(
                    title = LanguageSettings.translate(LanguageSettings.farmerStats.first, LanguageSettings.farmerStats.second, isMarathi),
                    litres = (data.farmerStats?.totalFarmers ?: 0).toDouble(),
                    amount = (data.farmerStats?.activeFarmers ?: 0).toDouble(),
                    rightSpec = "Active vs Registered Members",
                    brush = Brush.linearGradient(colors = listOf(Color(0xFFB45309), Color(0xFF78350F))),
                    isCountMode = true
                )
            }
        }

        // 2. MILK SUMMARY VISUALS (Custom Area and Pie charts)
        if (showWidget("millk_summary") && !data.collectionChart.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isMarathi) "मागील १० दिवसांचे दूध संकलन" else "10-Day Volume Trend (Litres)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // CUSTOM CANVAS AREA CHART
                    AreaVolumeChart(points = data.collectionChart)
                }
            }
        }

        // 3. TOP FARMERS
        if (!data.topFarmers.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LanguageSettings.translate(LanguageSettings.topFarmers.first, LanguageSettings.topFarmers.second, isMarathi),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    data.topFarmers.take(5).forEachIndexed { idx, farmer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = farmer.farmerName ?: "N/A",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("Code: ${farmer.farmerCode}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${farmer.totalQuantity ?: 0.0} Ltr", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${farmer.totalAmount ?: 0.0}", fontSize = 11.sp, color = Color(0xFF10B981))
                            }
                        }
                        if (idx < 4) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }

        // 4. WEEKLY SUMMARY & PENDING DUES CARD
        if (showWidget("payment")) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = LanguageSettings.translate(LanguageSettings.weeklySummary.first, LanguageSettings.weeklySummary.second, isMarathi),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DashboardStatsColumn(
                            title = "Milk Amount",
                            valStr = "₹${data.weeklyPaymentSummary?.totalMilkAmount ?: 0.0}",
                            color = Color(0xFF0D9488)
                        )
                        DashboardStatsColumn(
                            title = "Deductions",
                            valStr = "₹${data.weeklyPaymentSummary?.totalDeductions ?: 0.0}",
                            color = Color(0xFFEF4444)
                        )
                        DashboardStatsColumn(
                            title = "Net Payable",
                            valStr = "₹${data.weeklyPaymentSummary?.netPayable ?: 0.0}",
                            color = Color(0xFF10B981)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDues() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Dues", tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageSettings.translate(LanguageSettings.pendingDues.first, LanguageSettings.pendingDues.second, isMarathi),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "₹${data.pendingDues ?: 0.0}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // 5. SUBSCRIPTION CARD
        if (showWidget("subscription") && data.subscriptionStatus != null) {
            val sub = data.subscriptionStatus
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Sub", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageSettings.translate(LanguageSettings.currSubscription.first, LanguageSettings.currSubscription.second, isMarathi),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${sub.planName ?: "Standard Tier"} Plan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${sub.daysRemaining ?: 0} " + LanguageSettings.translate(LanguageSettings.daysRemaining.first, LanguageSettings.daysRemaining.second, isMarathi),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNavAction(icon: String, label: String, onClick: () -> Unit, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 23.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 11.sp
        )
    }
}

@Composable
fun StatGradCard(
    title: String,
    litres: Double,
    amount: Double,
    rightSpec: String,
    brush: Brush,
    isCountMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(end = 12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.background(brush).padding(16.dp)) {
            Column {
                Text(text = title, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                if (isCountMode) {
                    Text(
                        text = "${litres.toInt()} Farmers",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${amount.toInt()} Active Today",
                        color = Color(0xFF2DD4BF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = String.format("%.2f Ltr", litres),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = String.format("₹%,.2f", amount),
                        color = Color(0xFF2DD4BF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = rightSpec, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun DashboardStatsColumn(title: String, valStr: String, color: Color) {
    Column {
        Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = valStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AreaVolumeChart(points: List<ChartPoint>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 20f

        val activeHeight = height - padding * 2
        val activeWidth = width - padding * 2

        val maxVal = (points.maxOfOrNull { it.totalQuantity ?: 0.0 } ?: 100.0)
        val maxTarget = if (maxVal <= 0.0) 100f else maxVal.toFloat()

        val itemWidth = activeWidth / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { idx, pt ->
            val x = padding + idx * itemWidth
            val rawQty = pt.totalQuantity ?: 0.0
            val y = height - padding - ((rawQty.toFloat() / maxTarget) * activeHeight)

            if (idx == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (idx == points.size - 1) {
                fillPath.lineTo(x, height - padding)
                fillPath.close()
            }
        }

        // Draw Area Fill holding transparent teal accent
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF14B8A6).copy(alpha = 0.35f), Color.Transparent)
            )
        )

        // Draw Line Path
        drawPath(
            path = path,
            color = Color(0xFF14B8A6),
            style = Stroke(width = 4.5f)
        )

        // Draw bottom support baseline
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2f
        )
    }
}

fun parseWidgets(myWidgetsJson: String?): List<String> {
    if (myWidgetsJson.isNullOrEmpty() || myWidgetsJson == "[]") return emptyList()
    return try {
        val array = ApiJsonParser.parseArray(myWidgetsJson)
        buildList {
            for (i in 0 until array.length()) {
                when (val value = array.opt(i)) {
                    is String -> if (value.isNotBlank()) add(value)
                    is org.json.JSONObject -> {
                        val key = ApiJsonParser.readString(value, "widgetKey", "WidgetKey", "key", "Key", "name", "Name")
                        if (!key.isNullOrBlank()) add(key)
                    }
                }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}
