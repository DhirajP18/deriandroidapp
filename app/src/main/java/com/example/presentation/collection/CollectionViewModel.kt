package com.example.presentation.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Farmer
import com.example.data.api.MilkCollectionEntry
import com.example.data.api.MilkRate
import com.example.data.api.MilkShift
import com.example.data.api.MilkType
import com.example.data.api.NetworkClient
import kotlinx.coroutines.flow.firstOrNull
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NetworkClient.getDataStore(application)
    private val apiService = NetworkClient.getApiService(application)

    // Master list elements
    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

    private val _milkTypes = MutableStateFlow<List<MilkType>>(emptyList())
    val milkTypes: StateFlow<List<MilkType>> = _milkTypes

    private val _shifts = MutableStateFlow<List<MilkShift>>(emptyList())
    val shifts: StateFlow<List<MilkShift>> = _shifts

    private val _rates = MutableStateFlow<List<MilkRate>>(emptyList())
    val rates: StateFlow<List<MilkRate>> = _rates

    private val _entries = MutableStateFlow<List<MilkCollectionEntry>>(emptyList())
    val entries: StateFlow<List<MilkCollectionEntry>> = _entries

    // Selected states
    val collectionDate = MutableStateFlow(DateUtils.getTodayIsoString())
    val selectedFarmer = MutableStateFlow<Farmer?>(null)
    val selectedMilkType = MutableStateFlow<MilkType?>(null)
    val selectedShift = MutableStateFlow<MilkShift?>(null)

    // Numbers input
    val quantityLiters = MutableStateFlow("")
    val quantityMl = MutableStateFlow("0")
    val fat = MutableStateFlow("")
    val rate = MutableStateFlow("")
    val remarks = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parallel fetching of drop selections
                val farmersRes = apiService.getFarmerDropdown()
                if (farmersRes.isSuccessful) {
                    _farmers.value = farmersRes.body()?.finalData ?: emptyList()
                }

                val typesRes = apiService.getMilkTypes()
                if (typesRes.isSuccessful) {
                    _milkTypes.value = typesRes.body()?.finalData ?: emptyList()
                    if (_milkTypes.value.isNotEmpty() && selectedMilkType.value == null) {
                        selectedMilkType.value = _milkTypes.value.find { it.isActive == true } ?: _milkTypes.value.first()
                    }
                }

                val shiftsRes = apiService.getMilkShifts()
                if (shiftsRes.isSuccessful) {
                    _shifts.value = shiftsRes.body()?.finalData ?: emptyList()
                }

                val ratesRes = apiService.getMilkRates()
                if (ratesRes.isSuccessful) {
                    _rates.value = ratesRes.body()?.finalData ?: emptyList()
                }

                loadTodaysEntries()
            } catch (e: Exception) {
                _message.emit("Authentication expired or remote API offline. Unable to sync options.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTodaysEntries() {
        viewModelScope.launch {
            try {
                val res = apiService.getCollectionEntries()
                if (res.isSuccessful) {
                    _entries.value = res.body()?.finalData ?: emptyList()
                }
            } catch (e: Exception) {
                // Ignore silent list fetching errors during entries refresh
            }
        }
    }

    fun selectShiftByMode(isEvening: Boolean) {
        viewModelScope.launch {
            // Pre-select shifts once loaded
            if (_shifts.value.isNotEmpty()) {
                val index = if (isEvening) 1 else 0
                if (index < _shifts.value.size) {
                    selectedShift.value = _shifts.value[index]
                } else {
                    selectedShift.value = _shifts.value.first()
                }
            }
        }
    }

    // Dynamic rate computation based on MilkType and FAT.
    fun recalculateRate(fatVal: String, milkTypeId: Int?) {
        if (fatVal.isEmpty() || milkTypeId == null) {
            rate.value = ""
            return
        }
        val fatDouble = fatVal.toDoubleOrNull() ?: return
        
        // Find rate inside constraints
        val matchedRate = _rates.value.find { r ->
            r.milkTypeId == milkTypeId &&
            r.isActive == true &&
            fatDouble >= (r.fATFrom ?: 0.0) &&
            fatDouble <= (r.fATTo ?: 99.0)
        }

        if (matchedRate != null) {
            rate.value = matchedRate.rate.toString()
        } else {
            rate.value = ""
        }
    }

    fun saveEntry() {
        val farmer = selectedFarmer.value
        val shift = selectedShift.value
        val mType = selectedMilkType.value
        val lit = quantityLiters.value.toIntOrNull() ?: 0
        val mlVal = quantityMl.value.toIntOrNull() ?: 0
        val fatVal = fat.value.toDoubleOrNull() ?: 0.0
        val rateVal = rate.value.toDoubleOrNull() ?: 0.0

        if (farmer == null || shift == null || mType == null || (lit <= 0 && mlVal <= 0)) {
            viewModelScope.launch {
                _message.emit("Please select Farmer and enter a valid quantity volume")
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val session = dataStore.sessionFlow.firstOrNull()
                val entry = MilkCollectionEntry(
                    entryId = 0,
                    dairyId = session?.dairyId ?: 0,
                    farmerId = farmer.farmerId ?: 0,
                    shiftId = shift.shiftId ?: 0,
                    milkTypeId = mType.milkTypeId ?: 0,
                    collectionDate = collectionDate.value,
                    quantityLiters = lit,
                    quantityMilliliters = mlVal,
                    fat = fatVal,
                    ratePerLitermili = rateVal,
                    remarks = remarks.value
                )

                val response = apiService.createCollectionEntry(entry)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.finalSuccess) {
                        _message.emit(body.finalMessage.ifEmpty { "Entry saved successfully" })
                        // Clean inputs except Farmer to help rapid sequential entry insertions!
                        quantityLiters.value = ""
                        fat.value = ""
                        rate.value = ""
                        remarks.value = ""
                        loadTodaysEntries()
                    } else {
                        _message.emit(body.finalMessage.ifEmpty { "Save failed. Server rejected operation." })
                    }
                } else {
                    _message.emit("Save failed: Code ${response.code()}")
                }
            } catch (e: Exception) {
                _message.emit("Network out. Check connection to database details.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteCollectionEntry(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Record deleted successfully / नोंदणी रद्द केली.")
                    loadTodaysEntries()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Unable to delete records")
                }
            } catch (e: Exception) {
                _message.emit("Check networks, delete action failed.")
            }
        }
    }
}
