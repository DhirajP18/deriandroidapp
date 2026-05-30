package com.example.presentation.dues

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.Farmer
import com.example.data.api.FarmerDue
import com.example.data.api.AdvancePayment
import com.example.util.DateUtils
import com.example.util.LanguageSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDuesScreen(
    viewModel: FarmerDuesViewModel,
    isMarathi: Boolean
) {
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Advances, 1 = Dues
    val advances by viewModel.advances.collectAsState()
    val dues by viewModel.dues.collectAsState()
    val farmers by viewModel.farmers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Dialog sheets state
    var showAddAdvanceDialog by remember { mutableStateOf(false) }
    var advAmount by remember { mutableStateOf("") }
    var advRemarks by remember { mutableStateOf("") }
    var advSelectedFarmer by remember { mutableStateOf<Farmer?>(null) }

    var showAddDueDialog by remember { mutableStateOf(false) }
    var dueAmount by remember { mutableStateOf("") }
    var dueType by remember { mutableStateOf("Feed Charge") }
    var dueRemarks by remember { mutableStateOf("") }
    var dueSelectedFarmer by remember { mutableStateOf<Farmer?>(null) }

    var showRecoverDialog by remember { mutableStateOf<FarmerDue?>(null) }
    var recoveryAmount by remember { mutableStateOf("") }
    var recoveryMode by remember { mutableStateOf("Cash") }

    var showFarmerSelectDialog by remember { mutableStateOf(false) }
    var farmerTargetRole by remember { mutableStateOf("adv") } // adv vs due
    var farmerSearchQuery by remember { mutableStateOf("") }

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
                            text = if (isMarathi) "शेतकरी कर्ज आणि अ‍ॅडव्हान्स" else "Advances & Debts Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                TabRow(selectedTabIndex = activeSubTab) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text(LanguageSettings.translate(LanguageSettings.btnDeductAdvance.first, LanguageSettings.btnDeductAdvance.second, isMarathi), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text(LanguageSettings.translate(LanguageSettings.txFarmerDues.first, LanguageSettings.txFarmerDues.second, isMarathi), fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == 0) showAddAdvanceDialog = true else showAddDueDialog = true
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
            // Stats input search
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Search farmer or description") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Query") },
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (activeSubTab) {
                    // ADVANCES LIST
                    0 -> {
                        val filteredAdvances = advances.filter {
                            it.farmerName?.contains(searchQuery, ignoreCase = true) == true ||
                            it.remarks?.contains(searchQuery, ignoreCase = true) == true
                        }

                        if (filteredAdvances.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No advancements found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filteredAdvances) { adv ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = adv.farmerName ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(text = "Given: ₹${adv.advanceAmount ?: 0.0}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                IconButton(onClick = { viewModel.deleteAdvance(adv.advanceId ?: 0) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Outstanding Advance: ₹${adv.advanceAmount ?: 0.0}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                                Text(text = DateUtils.formatCompactDate(adv.advanceDate), fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // OUTSTANDING DEBTS
                    1 -> {
                        val filteredDues = dues.filter {
                            it.farmerName?.contains(searchQuery, ignoreCase = true) == true ||
                            it.dueType?.contains(searchQuery, ignoreCase = true) == true
                        }

                        if (filteredDues.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No registered feed dues found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filteredDues) { fd ->
                                    val outstanding = (fd.totalAmount ?: 0.0) - (fd.recoveredAmount ?: 0.0)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = fd.farmerName ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(text = "[${fd.dueType}] | Charge: ₹${fd.totalAmount} | Recovered: ₹${fd.recoveredAmount ?: 0.0}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (outstanding > 0) {
                                                        Button(
                                                            onClick = {
                                                                showRecoverDialog = fd
                                                                recoveryAmount = outstanding.toString()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("Collect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    IconButton(onClick = { viewModel.deleteDue(fd.dueId ?: 0) }) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Outstanding Due: ₹$outstanding", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                                Text(text = DateUtils.formatCompactDate(fd.dueDate), fontSize = 10.sp, color = Color.Gray)
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

        // Add Advance Dialog Form
        if (showAddAdvanceDialog) {
            AlertDialog(
                onDismissRequest = { showAddAdvanceDialog = false },
                title = { Text(text = "Advance Payment Dispatch", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Select Recipient Farmer *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    farmerTargetRole = "adv"
                                    showFarmerSelectDialog = true
                                }
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = advSelectedFarmer?.let { if (isMarathi) it.farmerNameMr.orEmpty().ifEmpty { it.farmerNameEn } ?: "" else it.farmerNameEn ?: "" } ?: "Tap to choose farmer",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = advAmount, onValueChange = { advAmount = it }, label = { Text("Advance Amount (₹) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag("advance_amount_input"))
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = advRemarks, onValueChange = { advRemarks = it }, label = { Text("Remarks/Purpose") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = advAmount.toDoubleOrNull() ?: 0.0
                            val f = advSelectedFarmer
                            if (amt > 0 && f != null) {
                                viewModel.addAdvance(f.farmerId ?: 0, amt, advRemarks)
                                showAddAdvanceDialog = false
                                advAmount = ""; advRemarks = ""; advSelectedFarmer = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("Give Advance", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAdvanceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add Due Dialog Form
        if (showAddDueDialog) {
            AlertDialog(
                onDismissRequest = { showAddDueDialog = false },
                title = { Text(text = "Create Dues / Feed Order", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Select Debtor Farmer *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    farmerTargetRole = "due"
                                    showFarmerSelectDialog = true
                                }
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = dueSelectedFarmer?.let { if (isMarathi) it.farmerNameMr.orEmpty().ifEmpty { it.farmerNameEn } ?: "" else it.farmerNameEn ?: "" } ?: "Tap to choose farmer",
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = dueAmount, onValueChange = { dueAmount = it }, label = { Text("Charge Amount (₹) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag("due_amount_input"))
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Dropdown choice type
                        listOf("Feed Charge", "Fodder Purchase", "Medicine charges", "Other debit").forEach { choice ->
                            val active = dueType == choice
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dueType = choice }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = active, onClick = { dueType = choice })
                                Text(text = choice, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(value = dueRemarks, onValueChange = { dueRemarks = it }, label = { Text("Remarks/Notes") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = dueAmount.toDoubleOrNull() ?: 0.0
                            val f = dueSelectedFarmer
                            if (amt > 0 && f != null) {
                                viewModel.addDue(f.farmerId ?: 0, amt, dueType, dueRemarks)
                                showAddDueDialog = false
                                dueAmount = ""; dueRemarks = ""; dueSelectedFarmer = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text("Record Debt", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDueDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Recover Due Dialog form
        showRecoverDialog?.let { row ->
            AlertDialog(
                onDismissRequest = { showRecoverDialog = null },
                title = { Text(text = "Recover/Collect Farmer Debts") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Creditor: ${row.farmerName}", fontSize = 14.sp)
                        Text("Category: ${row.dueType}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(value = recoveryAmount, onValueChange = { recoveryAmount = it }, label = { Text("Recovered Payment (₹) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Cash", "UPI", "Bank").forEach { mode ->
                                val active = recoveryMode == mode
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                        .clickable { recoveryMode = mode },
                                    colors = CardDefaults.cardColors(containerColor = if (active) Color(0xFF0D9488).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(text = mode, modifier = Modifier.padding(10.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = recoveryAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.recoverDue(row.dueId ?: 0, row.farmerId ?: 0, amt, recoveryMode)
                                showRecoverDialog = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Commit Recovery", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecoverDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Shared Farmer popup search list
        if (showFarmerSelectDialog) {
            AlertDialog(
                onDismissRequest = { showFarmerSelectDialog = false },
                title = { Text(text = "Select Member Profile") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextField(value = farmerSearchQuery, onValueChange = { farmerSearchQuery = it }, placeholder = { Text("Search Code or Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val fList = farmers.filter {
                                it.farmerNameEn?.contains(farmerSearchQuery, ignoreCase = true) == true ||
                                it.farmerCode?.contains(farmerSearchQuery, ignoreCase = true) == true
                            }
                            fList.forEach { f ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (farmerTargetRole == "adv") advSelectedFarmer = f else dueSelectedFarmer = f
                                            showFarmerSelectDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = f.farmerNameEn ?: "")
                                    Text(text = "Code: ${f.farmerCode}", fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFarmerSelectDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
