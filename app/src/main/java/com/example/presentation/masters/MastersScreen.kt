package com.example.presentation.masters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.Farmer
import com.example.data.api.Village
import com.example.util.LanguageSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastersScreen(
    viewModel: MastersViewModel,
    isMarathi: Boolean
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Farmers, 1 = Villages
    val farmers by viewModel.farmers.collectAsState()
    val villages by viewModel.villages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Farmer Form dialog state
    var showAddFarmerDialog by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var nameMr by remember { mutableStateOf("") }
    var mob by remember { mutableStateOf("") }
    var wa by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedVillageId by remember { mutableIntStateOf(0) }
    var addressEn by remember { mutableStateOf("") }
    var addressMr by remember { mutableStateOf("") }
    var aadhar by remember { mutableStateOf("") }

    // Village Form dialog state
    var showAddVillageDialog by remember { mutableStateOf(false) }
    var vilNameEn by remember { mutableStateOf("") }
    var vilNameMr by remember { mutableStateOf("") }

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
                            text = if (isMarathi) "डेअरी मास्टर्स माहिती" else "Dairy Master Directories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                // Farmers vs Villages switcher
                TabRow(selectedTabIndex = activeSubTab) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text(LanguageSettings.translate(LanguageSettings.masFarmers.first, LanguageSettings.masFarmers.second, isMarathi), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text(LanguageSettings.translate(LanguageSettings.masVillages.first, LanguageSettings.masVillages.second, isMarathi), fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == 0) showAddFarmerDialog = true else showAddVillageDialog = true
                },
                containerColor = Color(0xFF0D9488)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Row
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Search by name, address, or code") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Query") },
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (activeSubTab) {
                    // FARMERS DIRECTORY
                    0 -> {
                        val filteredFarmers = farmers.filter {
                            it.farmerNameEn?.contains(searchQuery, ignoreCase = true) == true ||
                            it.farmerNameMr?.contains(searchQuery, ignoreCase = true) == true ||
                            it.farmerCode?.contains(searchQuery, ignoreCase = true) == true
                        }

                        if (filteredFarmers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No farmers found matching query.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filteredFarmers) { farm ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isMarathi) farm.farmerNameMr?.ifEmpty { farm.farmerNameEn } ?: "" else farm.farmerNameEn ?: "",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Code: ${farm.farmerCode} | Mobile: ${farm.mobileNumber} | Village: ${farm.villageName ?: "N/A"}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Toggle Active icon
                                                IconButton(onClick = { viewModel.toggleFarmerActive(farm.farmerId ?: 0) }) {
                                                    Icon(
                                                        imageVector = if (farm.isActive == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                        contentDescription = "Active",
                                                        tint = if (farm.isActive == true) Color(0xFF10B981) else Color.Red
                                                    )
                                                }
                                                IconButton(onClick = { viewModel.deleteFarmer(farm.farmerId ?: 0) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // VILLAGES NODES
                    1 -> {
                        val filteredVillages = villages.filter {
                            it.villageName?.contains(searchQuery, ignoreCase = true) == true ||
                            it.villageNameMarathi?.contains(searchQuery, ignoreCase = true) == true
                        }

                        if (filteredVillages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No villages found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filteredVillages) { vil ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (isMarathi) vil.villageNameMarathi ?: vil.villageName ?: "" else vil.villageName ?: "",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(text = "Node System ID: ${vil.villageId}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            IconButton(onClick = { viewModel.deleteVillage(vil.villageId ?: 0) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error)
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

        // Add Farmer Custom Modal Sheet Dialog
        if (showAddFarmerDialog) {
            AlertDialog(
                onDismissRequest = { showAddFarmerDialog = false },
                title = { Text(text = "Add New Farmer Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TextField(value = code, onValueChange = { code = it }, label = { Text("Farmer Code *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth().testTag("farmer_code_input"))
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Farmer Name (English) *") }, modifier = Modifier.fillMaxWidth().testTag("farmer_name_en_input"))
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = nameMr, onValueChange = { nameMr = it }, label = { Text("Farmer Name (Marathi) *") }, modifier = Modifier.fillMaxWidth().testTag("farmer_name_mr_input"))
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = mob, onValueChange = { mob = it }, label = { Text("Mobile Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = wa, onValueChange = { wa = it }, label = { Text("WhatsApp Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = email, onValueChange = { email = it }, label = { Text("Email address") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = aadhar, onValueChange = { aadhar = it }, label = { Text("Aadhar Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))

                        // Village Dropdown select
                        Text("Select Village Node *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        villages.forEach { vil ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedVillageId = vil.villageId ?: 0 }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = vil.villageName ?: "")
                                RadioButton(selected = selectedVillageId == vil.villageId, onClick = { selectedVillageId = vil.villageId ?: 0 })
                            }
                            HorizontalDivider()
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(value = addressEn, onValueChange = { addressEn = it }, label = { Text("Address (English)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = addressMr, onValueChange = { addressMr = it }, label = { Text("Address (Marathi)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addFarmer(
                                code, nameEn, nameMr, mob, wa, email,
                                selectedVillageId, aadhar, addressEn, addressMr
                            )
                            showAddFarmerDialog = false
                            // Clean states
                            code = ""; nameEn = ""; nameMr = ""; mob = ""; wa = ""; email = ""; selectedVillageId = 0; aadhar = ""; addressEn = ""; addressMr = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("Register Farmer", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFarmerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Village Alert Box
        if (showAddVillageDialog) {
            AlertDialog(
                onDismissRequest = { showAddVillageDialog = false },
                title = { Text(text = "Add Village Node", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextField(value = vilNameEn, onValueChange = { vilNameEn = it }, label = { Text("Village Name (English) *") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = vilNameMr, onValueChange = { vilNameMr = it }, label = { Text("Village Name (Marathi)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addVillage(vilNameEn, vilNameMr)
                            showAddVillageDialog = false
                            vilNameEn = ""; vilNameMr = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("Add Node", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVillageDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
