package com.example.presentation.collection

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.Farmer
import com.example.util.DateUtils
import com.example.util.LanguageSettings
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionEntryScreen(
    viewModel: CollectionViewModel,
    isEvening: Boolean,
    isMarathi: Boolean
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val farmers by viewModel.farmers.collectAsState()
    val milkTypes by viewModel.milkTypes.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val entries by viewModel.entries.collectAsState()

    val dateVal by viewModel.collectionDate.collectAsState()
    val selectedFarmer by viewModel.selectedFarmer.collectAsState()
    val selectedMilkType by viewModel.selectedMilkType.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()

    val litVal by viewModel.quantityLiters.collectAsState()
    val mlVal by viewModel.quantityMl.collectAsState()
    val fatVal by viewModel.fat.collectAsState()
    val rateVal by viewModel.rate.collectAsState()
    val remarksVal by viewModel.remarks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showFarmerSearchDialog by remember { mutableStateOf(false) }
    var farmerQuery by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shifts) {
        viewModel.selectShiftByMode(isEvening)
    }

    LaunchedEffect(Unit) {
        viewModel.message.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEvening) {
                            LanguageSettings.translate(LanguageSettings.txEveningColl.first, LanguageSettings.txEveningColl.second, isMarathi)
                        } else {
                            LanguageSettings.translate(LanguageSettings.txMorningColl.first, LanguageSettings.txMorningColl.second, isMarathi)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                // Primary collection inputs
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Column Form Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Raw 1: Date & Shift Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Date picker clicker
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .clickable {
                                            val cal = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, d ->
                                                    viewModel.collectionDate.value = DateUtils.toIso8601String(y, m, d)
                                                },
                                                cal.get(Calendar.YEAR),
                                                cal.get(Calendar.MONTH),
                                                cal.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DateUtils.formatCompactDate(dateVal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Shift label
                                selectedShift?.let { s ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
//                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                            .background(if (isEvening) Color(0xFF6366F1).copy(alpha = 0.15f) else Color(0xFFFFB03A).copy(alpha = 0.15f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isMarathi) s.shiftNameMarathi ?: s.shiftName ?: "" else s.shiftName ?: "",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEvening) Color(0xFF6366F1) else Color(0xFFB45309)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Static Farmer Selection Card
                            Text(
                                text = LanguageSettings.translate(LanguageSettings.masFarmers.first, LanguageSettings.masFarmers.second, isMarathi),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFarmerSearchDialog = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = if (selectedFarmer == null) ButtonDefaults.outlinedButtonBorder else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.PersonSearch, contentDescription = "Find", tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = selectedFarmer?.let { if (isMarathi) it.farmerNameMr.orEmpty().ifEmpty { it.farmerNameEn } else it.farmerNameEn } 
                                                    ?: (if (isMarathi) "शेतकरी निवडा" else "Select Farmer / Search Code"),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedFarmer == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                                            )
                                            selectedFarmer?.let {
                                                Text("Farmer Code: ${it.farmerCode} | Village: ${it.villageName ?: "N/A"}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "down")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Milk category selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                milkTypes.forEach { type ->
                                    val checked = selectedMilkType?.milkTypeId == type.milkTypeId
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable {
                                                viewModel.selectedMilkType.value = type
                                                viewModel.recalculateRate(fatVal, type.milkTypeId)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = checked,
                                            onClick = {
                                                viewModel.selectedMilkType.value = type
                                                viewModel.recalculateRate(fatVal, type.milkTypeId)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMarathi) type.milkTypeNameMarathi ?: type.milkTypeName ?: "" else type.milkTypeName ?: "",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. Numeric Litres and ML inputs side by side
                            Row(modifier = Modifier.fillMaxWidth()) {
                                TextField(
                                    value = litVal,
                                    onValueChange = { viewModel.quantityLiters.value = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("litres_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    placeholder = { Text(LanguageSettings.translate(LanguageSettings.fldQtyLtr.first, LanguageSettings.fldQtyLtr.second, isMarathi)) },
                                    label = { Text(LanguageSettings.translate(LanguageSettings.fldQtyLtr.first, LanguageSettings.fldQtyLtr.second, isMarathi), fontSize = 11.sp) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
                                )

                                TextField(
                                    value = mlVal,
                                    onValueChange = { viewModel.quantityMl.value = it },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    placeholder = { Text(LanguageSettings.translate(LanguageSettings.fldQtyMl.first, LanguageSettings.fldQtyMl.second, isMarathi)) },
                                    label = { Text(LanguageSettings.translate(LanguageSettings.fldQtyMl.first, LanguageSettings.fldQtyMl.second, isMarathi), fontSize = 11.sp) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. FAT and Computed Rate side by side
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // FAT Input
                                TextField(
                                    value = fatVal,
                                    onValueChange = {
                                        viewModel.fat.value = it
                                        viewModel.recalculateRate(it, selectedMilkType?.milkTypeId)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                        .testTag("fat_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                    placeholder = { Text(LanguageSettings.translate(LanguageSettings.fldFat.first, LanguageSettings.fldFat.second, isMarathi)) },
                                    label = { Text(LanguageSettings.translate(LanguageSettings.fldFat.first, LanguageSettings.fldFat.second, isMarathi), fontSize = 11.sp) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
                                )

                                // Computed / Editable Rate
                                TextField(
                                    value = rateVal,
                                    onValueChange = { viewModel.rate.value = it },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                    placeholder = { Text(LanguageSettings.translate(LanguageSettings.fldRate.first, LanguageSettings.fldRate.second, isMarathi)) },
                                    label = { Text(LanguageSettings.translate(LanguageSettings.fldRate.first, LanguageSettings.fldRate.second, isMarathi), fontSize = 11.sp) },
                                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Remarks
                            TextField(
                                value = remarksVal,
                                onValueChange = { viewModel.remarks.value = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(LanguageSettings.translate(LanguageSettings.fldRemarks.first, LanguageSettings.fldRemarks.second, isMarathi)) },
                                label = { Text(LanguageSettings.translate(LanguageSettings.fldRemarks.first, LanguageSettings.fldRemarks.second, isMarathi), fontSize = 11.sp) },
                                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Save Submit action Button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.saveEntry()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_collection_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = LanguageSettings.translate(LanguageSettings.btnSave.first, LanguageSettings.btnSave.second, isMarathi),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Saved entries subtitle section header
                    Text(
                        text = if (isMarathi) "आजच्या दूध संग्रह नोंदी" else "Today's Collection Register",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Table Listing entries
                val todayEntriesFiltered = entries.filter {
                    DateUtils.formatCompactDate(it.collectionDate) == DateUtils.formatCompactDate(dateVal) &&
                    it.shiftId == selectedShift?.shiftId
                }

                if (todayEntriesFiltered.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = if (isMarathi) "या शिफ्टसाठी आज अद्याप नोंदी नाहीत." else "No entries captured for this shift today.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(todayEntriesFiltered) { row ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${row.farmerName} (${row.farmerCode})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "${row.milkTypeName} | ${row.quantityLiters ?: 0}L ${row.quantityMilliliters ?: 0}ml", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("FAT: ${row.fat}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val amt = (row.quantityLiters ?: 0) * (row.ratePerLitermili ?: 0.0)
                                    Text(
                                        text = String.format("₹%.1f", amt),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteEntry(row.entryId ?: 0) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Farmer Drawer AlertDialog
        if (showFarmerSearchDialog) {
            AlertDialog(
                onDismissRequest = { showFarmerSearchDialog = false },
                title = { Text(text = if (isMarathi) "शेतकरी शोध" else "Select Farmer Account") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = farmerQuery,
                            onValueChange = { farmerQuery = it },
                            placeholder = { Text("Search by Name or Code") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Query") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        
                        val filteredFarmers = farmers.filter {
                            it.farmerNameEn?.contains(farmerQuery, ignoreCase = true) == true ||
                            it.farmerNameMr?.contains(farmerQuery, ignoreCase = true) == true ||
                            it.farmerCode?.contains(farmerQuery, ignoreCase = true) == true
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            if (filteredFarmers.isEmpty()) {
                                Text(text = "No farmers found.", color = Color.Gray, modifier = Modifier.padding(8.dp))
                            } else {
                                filteredFarmers.forEach { farm ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectedFarmer.value = farm
                                                showFarmerSearchDialog = false
                                                farmerQuery = ""
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isMarathi) farm.farmerNameMr?.ifEmpty { farm.farmerNameEn } ?: "" else farm.farmerNameEn ?: "",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(text = "Code: ${farm.farmerCode}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFarmerSearchDialog = false }) {
                        Text(text = LanguageSettings.translate(LanguageSettings.btnCancel.first, LanguageSettings.btnCancel.second, isMarathi))
                    }
                }
            )
        }
    }
}
