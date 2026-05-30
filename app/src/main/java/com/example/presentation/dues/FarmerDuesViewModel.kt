package com.example.presentation.dues

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.*
import com.example.util.DateUtils
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FarmerDuesViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NetworkClient.getDataStore(application)
    private val apiService = NetworkClient.getApiService(application)

    private val _advances = MutableStateFlow<List<AdvancePayment>>(emptyList())
    val advances: StateFlow<List<AdvancePayment>> = _advances

    private val _dues = MutableStateFlow<List<FarmerDue>>(emptyList())
    val dues: StateFlow<List<FarmerDue>> = _dues

    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

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
                // Dropdown farmers
                val farmersRes = apiService.getFarmerDropdown()
                if (farmersRes.isSuccessful) _farmers.value = farmersRes.body()?.finalData ?: emptyList()

                // Advances
                val advancesRes = apiService.getAdvancePayments()
                if (advancesRes.isSuccessful) _advances.value = advancesRes.body()?.finalData ?: emptyList()

                // Dues
                val dairyId = dataStore.sessionFlow.firstOrNull()?.dairyId ?: 0
                val duesRes = apiService.getOutstandingDues(dairyId = dairyId, pageNumber = 1, pageSize = 100)
                if (duesRes.isSuccessful) _dues.value = duesRes.body()?.finalData ?: emptyList()
            } catch (e: Exception) {
                // Offline
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ADVANCE TRANSACTIONS
    fun addAdvance(farmerId: Int, amt: Double, remarks: String) {
        if (farmerId == 0 || amt <= 0) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dairyId = dataStore.sessionFlow.firstOrNull()?.dairyId ?: 0
                val adv = AdvancePayment(
                    advanceId = 0,
                    dairyId = dairyId,
                    farmerId = farmerId,
                    advanceDate = DateUtils.getTodayIsoString(),
                    advanceAmount = amt,
                    remarks = remarks
                )
                val res = apiService.createAdvancePayment(adv)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Advance payment allocated successfully / अ‍ॅडव्हान्स जमा केला.")
                    loadData()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Advance failed.")
                }
            } catch (e: Exception) {
                _message.emit("Advance save error.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAdvance(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteAdvancePayment(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Advance record removed.")
                    loadData()
                }
            } catch (e: Exception) {
                _message.emit("Delete failure.")
            }
        }
    }

    // FARMER DUES TRANSACTIONS
    fun addDue(farmerId: Int, amt: Double, type: String, remarks: String) {
        if (farmerId == 0 || amt <= 0) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dairyId = dataStore.sessionFlow.firstOrNull()?.dairyId ?: 0
                val fd = FarmerDue(
                    dueId = 0,
                    dairyId = dairyId,
                    farmerId = farmerId,
                    dueDate = DateUtils.getTodayIsoString(),
                    totalAmount = amt,
                    recoveredAmount = 0.0,
                    dueType = type,
                    remarks = remarks
                )
                val res = apiService.createFarmerDue(fd)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Dues profile recorded / थकबाकी नोंदली गेली.")
                    loadData()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Dues record failed.")
                }
            } catch (e: Exception) {
                _message.emit("Connection issues.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun recoverDue(dueId: Int, farmerId: Int, amt: Double, mode: String) {
        if (farmerId == 0 || amt <= 0) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dairyId = dataStore.sessionFlow.firstOrNull()?.dairyId ?: 0
                val req = RecoverDueRequest(
                    dairyId = dairyId,
                    farmerId = farmerId,
                    recoveryAmount = amt,
                    recoveryDate = DateUtils.getTodayIsoString(),
                    remarks = "Collected in app dueId:$dueId"
                )
                val res = apiService.recoverDue(req)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Dues recovered successfully / थकबाकी वसूल झाली.")
                    loadData()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Recovery failed.")
                }
            } catch (e: Exception) {
                _message.emit("Connection offline.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDue(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteFarmerDue(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Outstanding due deleted / थकबाकी रेकॉर्ड हटवला.")
                    loadData()
                }
            } catch (e: Exception) {
                _message.emit("Unable to delete.")
            }
        }
    }
}
