package com.example.presentation.masters

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.Farmer
import com.example.data.api.NetworkClient
import com.example.data.api.Village
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MastersViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = NetworkClient.getApiService(application)

    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

    private val _villages = MutableStateFlow<List<Village>>(emptyList())
    val villages: StateFlow<List<Village>> = _villages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    init {
        loadAllMasters()
    }

    fun loadAllMasters() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val villagesRes = apiService.getVillageDropdown()
                if (villagesRes.isSuccessful) {
                    _villages.value = villagesRes.body()?.finalData ?: emptyList()
                }

                val farmersRes = apiService.getFarmers()
                if (farmersRes.isSuccessful) {
                    _farmers.value = farmersRes.body()?.finalData ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently hold offline cache state
            } finally {
                _isLoading.value = false
            }
        }
    }

    // VILLAGE CRUD
    fun addVillage(nameEn: String, nameMr: String) {
        if (nameEn.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val vil = Village(villageId = 0, villageName = nameEn, villageNameMarathi = nameMr, isActive = true)
                val res = apiService.createVillage(vil)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Village saved successfully! / गाव जोडले गेले.")
                    loadAllMasters()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Failed to save village")
                }
            } catch (e: Exception) {
                _message.emit("Village save backend error.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVillage(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteVillage(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Village deleted / गाव हटवले गेले.")
                    loadAllMasters()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Failed.")
                }
            } catch (e: Exception) {
                _message.emit("Connection issues.")
            }
        }
    }

    // FARMER CRUD
    fun addFarmer(
        code: String, nameEn: String, nameMr: String, mob: String, wa: String, email: String,
        villageId: Int, aad: String, addrEn: String, addrMr: String
    ) {
        if (nameEn.isEmpty() || code.isEmpty() || villageId == 0) {
            viewModelScope.launch { _message.emit("Name, Code, and Village are required fields") }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val farm = Farmer(
                    farmerId = 0,
                    farmerCode = code,
                    farmerNameEn = nameEn,
                    farmerNameMr = nameMr,
                    mobileNumber = mob,
                    whatsAppNumber = wa,
                    emailid = email,
                    villageId = villageId,
                    addressEn = addrEn,
                    addressMr = addrMr,
                    adharNumber = aad,
                    joinDate = DateUtils.getTodayIsoString(),
                    isActive = true
                )

                val res = apiService.createFarmer(farm)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Farmer registered successfully / नवीन शेतकरी जोडला गेला.")
                    loadAllMasters()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Farmer registration rejected.")
                }
            } catch (e: Exception) {
                _message.emit("Farmer save error.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFarmer(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteFarmer(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Farmer deleted / शेतकरी हटवला गेला.")
                    loadAllMasters()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Failed to delete.")
                }
            } catch (e: Exception) {
                _message.emit("Connection error.")
            }
        }
    }

    fun toggleFarmerActive(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.toggleFarmerActive(id)
                if (res.isSuccessful) {
                    _message.emit("Farmer active state toggled.")
                    loadAllMasters()
                }
            } catch (e: Exception) {
                // Ignore silent toggle failures
            }
        }
    }
}
