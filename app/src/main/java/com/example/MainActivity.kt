package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.api.ApiJsonParser
import com.example.data.api.SidebarMenu
import com.example.data.local.DataStoreManager
import com.example.presentation.collection.CollectionEntryScreen
import com.example.presentation.collection.CollectionViewModel
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardViewModel
import com.example.presentation.dues.FarmerDuesScreen
import com.example.presentation.dues.FarmerDuesViewModel
import com.example.presentation.login.LoginScreen
import com.example.presentation.login.LoginViewModel
import com.example.presentation.masters.MastersScreen
import com.example.presentation.masters.MastersViewModel
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.payment.WeeklyPaymentScreen
import com.example.presentation.payment.WeeklyPaymentViewModel
import com.example.presentation.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainAppEntry()
        }
    }
}

@Composable
fun MainAppEntry() {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Data Store setup delegate
    val dataStoreManager = remember { DataStoreManager(context) }
    val sessionState by dataStoreManager.sessionFlow.collectAsState(initial = null)
    val appTheme by dataStoreManager.themeFlow.collectAsState(initial = "System")
    val appLanguage by dataStoreManager.languageFlow.collectAsState(initial = "English")
    val isMarathi = appLanguage == "Marathi"
    val resolvedDarkTheme = when (appTheme.lowercase()) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    MyApplicationTheme(darkTheme = resolvedDarkTheme) {
        // ViewModels initialization
        val loginViewModel = remember { LoginViewModel(application) }
        val dashboardViewModel = remember { DashboardViewModel(application) }
        val collectionViewModel = remember { CollectionViewModel(application) }
        val paymentsViewModel = remember { WeeklyPaymentViewModel(application) }
        val mastersViewModel = remember { MastersViewModel(application) }
        val duesViewModel = remember { FarmerDuesViewModel(application) }

        // Screen tracker transitions: "splash", "onboarding", "login", "main"
        var activeStateSection by remember { mutableStateOf("splash") }

        // Main scaffold display tab
        var selectedTabIdx by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Morning, 2 = Evening, 3 = Payments, 4 = Masters, 5 = Dues
    
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        // Sync state routes based on splash redirection signals
        when (activeStateSection) {
        "splash" -> {
            SplashScreen(
                dataStoreManager = dataStoreManager,
                onNavigateToOnboarding = { activeStateSection = "onboarding" },
                onNavigateToLogin = { activeStateSection = "login" },
                onNavigateToDashboard = { activeStateSection = "main" }
            )
        }
        "onboarding" -> {
            OnboardingScreen(
                dataStoreManager = dataStoreManager,
                onNavigateToLogin = { activeStateSection = "login" }
            )
        }
        "login" -> {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToDashboard = {
                    activeStateSection = "main"
                    // trigger data refresh on success
                    dashboardViewModel.loadDashboardData()
                    collectionViewModel.loadData()
                    paymentsViewModel.fetchPayments()
                    mastersViewModel.loadAllMasters()
                    duesViewModel.loadData()
                }
            )
        }
        "main" -> {
            // Check session expiry to logs redirect on the go
            LaunchedEffect(sessionState) {
                if (sessionState == null || sessionState?.accessToken?.isEmpty() == true) {
                    activeStateSection = "login"
                }
            }

            // Central Drawer rendering dynamic lists
            val rawSidebarJson = sessionState?.sidebarJson ?: "[]"
            val sidebarItems = parseSidebarJson(rawSidebarJson)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0F1724), Color(0xFF0D9488))
                                    )
                                )
                                .padding(vertical = 32.dp, horizontal = 20.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🥛", fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = sessionState?.fullName ?: "Dairy Manager",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = sessionState?.roleName ?: "Standard Access",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Sidebar Navigation Lists
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(sidebarItems) { item ->
                                val active = getSidebarTargetIdx(item.menuUrl) == selectedTabIdx
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            imageVector = getSidebarIcon(item.menuUrl),
                                            contentDescription = item.menuName,
                                            tint = if (active) Color(0xFF0D9488) else Color.Gray
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = if (isMarathi) item.menuNameMr ?: item.menuName ?: "" else item.menuName ?: "",
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    selected = active,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        selectedTabIdx = getSidebarTargetIdx(item.menuUrl)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Footer Logout Clicker
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        drawerState.close()
                                        dashboardViewModel.logout()
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Logout, contentDescription = "Log", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = if (isMarathi) "बाहेर पडा (Logout)" else "Sign Out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            ) {
                // Main app Bottom navigation layout scaffold
                Scaffold(
                    modifier = Modifier.testTag("main_layout_scaffold"),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 8.dp
                        ) {
                            // Bottom button shortcut: Home
                            NavigationBarItem(
                                selected = selectedTabIdx == 0,
                                onClick = { selectedTabIdx = 0 },
                                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )

                            // Bottom button shortcut: Morning Collection
                            NavigationBarItem(
                                selected = selectedTabIdx == 1,
                                onClick = { selectedTabIdx = 1 },
                                icon = { Icon(imageVector = Icons.Default.Brightness5, contentDescription = "Morning") },
                                label = { Text("Morning", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )

                            // Bottom button shortcut: Weekly Payments
                            NavigationBarItem(
                                selected = selectedTabIdx == 3,
                                onClick = { selectedTabIdx = 3 },
                                icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Pay") },
                                label = { Text("Payments", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )

                            // Bottom button shortcut: Sliding Sidebar Trigger
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                icon = { Icon(imageVector = Icons.Default.Menu, contentDescription = "More") },
                                label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                ) { rawPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(rawPadding)
                    ) {
                        when (selectedTabIdx) {
                            0 -> {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToMorning = { selectedTabIdx = 1 },
                                    onNavigateToEvening = { selectedTabIdx = 2 },
                                    onNavigateToPayments = { selectedTabIdx = 3 },
                                    onNavigateToFarmers = { selectedTabIdx = 4 },
                                    onNavigateToDues = { selectedTabIdx = 5 }
                                )
                            }
                            1 -> {
                                CollectionEntryScreen(
                                    viewModel = collectionViewModel,
                                    isEvening = false,
                                    isMarathi = isMarathi
                                )
                            }
                            2 -> {
                                CollectionEntryScreen(
                                    viewModel = collectionViewModel,
                                    isEvening = true,
                                    isMarathi = isMarathi
                                )
                            }
                            3 -> {
                                WeeklyPaymentScreen(
                                    viewModel = paymentsViewModel,
                                    isMarathi = isMarathi
                                )
                            }
                            4 -> {
                                MastersScreen(
                                    viewModel = mastersViewModel,
                                    isMarathi = isMarathi
                                )
                            }
                            5 -> {
                                FarmerDuesScreen(
                                    viewModel = duesViewModel,
                                    isMarathi = isMarathi
                                )
                            }
                            else -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Screen config unavailable.")
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

// Map Dynamic Sidebar routes into Central indexes mapping
fun getSidebarTargetIdx(url: String?): Int {
    if (url.isNullOrEmpty()) return 0
    val normalized = url.trim().lowercase()
    return when {
        normalized.contains("dashboard") -> 0
        normalized.contains("milkcollectionentry") || normalized.contains("collection") -> 1
        normalized.contains("milkshipt") || normalized.contains("milkshift") || normalized.contains("shift") -> 2
        normalized.contains("weeklypayment") || normalized.contains("payment") -> 3
        normalized.contains("farmer") || normalized.contains("village") || normalized.contains("feed") || normalized.contains("milktype") || normalized.contains("milkrate") || normalized.contains("purchaserate") || normalized.contains("usermanagment") || normalized.contains("superadmin") || normalized.contains("dashboardwidget") || normalized.contains("email") || normalized.contains("logo") || normalized.contains("subscription") -> 4
        normalized.contains("farmerdue") || normalized.contains("advance") -> 5
        else -> 0
    }
}

// Convert sidebar URL names into elegant Material UI icon equivalents
fun getSidebarIcon(url: String?): androidx.compose.ui.graphics.vector.ImageVector {
    if (url.isNullOrEmpty()) return Icons.Default.Dashboard
    val normalized = url.trim().lowercase()
    return when {
        normalized.contains("dashboard") -> Icons.Default.Dashboard
        normalized.contains("milkcollectionentry") || normalized.contains("collection") -> Icons.Default.WaterDrop
        normalized.contains("milkshipt") || normalized.contains("milkshift") || normalized.contains("shift") -> Icons.Default.Brightness3
        normalized.contains("weeklypayment") || normalized.contains("payment") -> Icons.Default.Payment
        normalized.contains("farmer") -> Icons.Default.Group
        normalized.contains("village") -> Icons.Default.Map
        normalized.contains("farmerdue") || normalized.contains("due") -> Icons.Default.DeleteForever
        normalized.contains("advance") -> Icons.Default.AccountBalanceWallet
        normalized.contains("usermanagment") || normalized.contains("superadmin") -> Icons.Default.AdminPanelSettings
        normalized.contains("feed") -> Icons.Default.Inventory2
        normalized.contains("milktype") || normalized.contains("milkrate") || normalized.contains("purchaseratemst") -> Icons.Default.List
        else -> Icons.Default.MenuBook
    }
}

fun parseSidebarJson(json: String): List<SidebarMenu> {
    if (json.isEmpty() || json == "[]") return emptyList()
    return try {
        val array = ApiJsonParser.parseArray(json)
        val flattened = mutableListOf<SidebarMenu>()

        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val menus = ApiJsonParser.readArray(item, "menus", "Menus", "childMenus", "ChildMenus")

            if (menus.length() > 0) {
                for (j in 0 until menus.length()) {
                    val menu = menus.optJSONObject(j) ?: continue
                    flattened += SidebarMenu(
                        menuId = ApiJsonParser.readInt(menu, "menuId", "MenuId") ?: 0,
                        menuName = ApiJsonParser.readString(menu, "menuName", "MenuName") ?: "",
                        menuNameMr = ApiJsonParser.readString(menu, "menuNameMr", "MenuNameMr"),
                        menuUrl = ApiJsonParser.readString(menu, "pageRoute", "PageRoute", "menuUrl", "MenuUrl") ?: "",
                        icon = ApiJsonParser.readString(menu, "iconClass", "IconClass", "menuIcon", "MenuIcon")
                    )
                }
            } else {
                flattened += SidebarMenu(
                    menuId = ApiJsonParser.readInt(item, "menuId", "MenuId", "moduleId", "ModuleId") ?: 0,
                    menuName = ApiJsonParser.readString(item, "menuName", "MenuName", "moduleName", "ModuleName") ?: "",
                    menuNameMr = ApiJsonParser.readString(item, "menuNameMr", "MenuNameMr", "moduleNameMr", "ModuleNameMr"),
                    menuUrl = ApiJsonParser.readString(item, "pageRoute", "PageRoute", "menuUrl", "MenuUrl") ?: "",
                    icon = ApiJsonParser.readString(item, "iconClass", "IconClass", "menuIcon", "MenuIcon", "moduleIcon", "ModuleIcon")
                )
            }
        }

        flattened.filter { it.menuUrl.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}
