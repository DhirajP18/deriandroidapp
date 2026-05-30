package com.example.presentation.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.*
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val list: List<WeeklyPayment>) : PaymentUiState()
    data class Error(val msg: String) : PaymentUiState()
}

class WeeklyPaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = NetworkClient.getApiService(application)

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    // Cache drop downs
    private val _farmers = MutableStateFlow<List<Farmer>>(emptyList())
    val farmers: StateFlow<List<Farmer>> = _farmers

    private val _milkTypes = MutableStateFlow<List<MilkType>>(emptyList())
    val milkTypes: StateFlow<List<MilkType>> = _milkTypes

    // Selected generating filters
    val selectedFarmer = MutableStateFlow<Farmer?>(null)
    val selectedMilkType = MutableStateFlow<MilkType?>(null)
    val startDate = MutableStateFlow(DateUtils.getDaysAgoIso(7))
    val endDate = MutableStateFlow(DateUtils.getTodayIsoString())

    // Preview billing data
    private val _previewData = MutableStateFlow<WeeklySummary?>(null)
    val previewData: StateFlow<WeeklySummary?> = _previewData

    private val _billReport = MutableStateFlow<BillReportData?>(null)
    val billReport: StateFlow<BillReportData?> = _billReport

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    init {
        loadFilters()
        fetchPayments()
    }

    fun loadFilters() {
        viewModelScope.launch {
            try {
                val fRes = apiService.getFarmerDropdown()
                if (fRes.isSuccessful) _farmers.value = fRes.body()?.finalData ?: emptyList()

                val mRes = apiService.getMilkTypes()
                if (mRes.isSuccessful) _milkTypes.value = mRes.body()?.finalData ?: emptyList()
            } catch (e: Exception) {
                // Ignore silent config issues
            }
        }
    }

    fun fetchPayments() {
        fetchPayments(pageNumber = 1, pageSize = 50)
    }

    fun fetchPayments(
        pageNumber: Int,
        pageSize: Int,
        farmerId: Int? = null,
        status: String? = null,
        milkTypeId: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                val res = apiService.getWeeklyPayments(
                    page = pageNumber,
                    size = pageSize,
                    farmerId = farmerId,
                    status = status,
                    milkTypeId = milkTypeId
                )
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    if (body.finalSuccess) {
                        _uiState.value = PaymentUiState.Success(body.finalData ?: emptyList())
                    } else {
                        _uiState.value = PaymentUiState.Error(body.finalMessage)
                    }
                } else {
                    _uiState.value = PaymentUiState.Error("Server error loading payments list")
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error("Server unavailable: offline.")
            }
        }
    }

    fun loadBillReport(paymentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = apiService.getWeeklyBillReport(paymentId)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _billReport.value = res.body()?.finalData
                } else {
                    _message.emit("Failed to load invoice layout detail from server.")
                }
            } catch (e: Exception) {
                _message.emit("Unable to contact payment endpoints.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun previewPayment() {
        val farmer = selectedFarmer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val req = GeneratePaymentRequest(
                    farmerId = farmer.farmerId ?: 0,
                    startDate = startDate.value,
                    endDate = endDate.value,
                    milkTypeId = selectedMilkType.value?.milkTypeId
                )
                val res = apiService.previewWeeklyPayment(req)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _previewData.value = res.body()?.finalData
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Unable to preview. Verify dates overlap.")
                }
            } catch (e: Exception) {
                _message.emit("API error during invoice preview calculation.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateSinglePayment() {
        val farmer = selectedFarmer.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val req = GeneratePaymentRequest(
                    farmerId = farmer.farmerId ?: 0,
                    startDate = startDate.value,
                    endDate = endDate.value,
                    milkTypeId = selectedMilkType.value?.milkTypeId
                )
                val res = apiService.generateWeeklyPayment(req)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Generated payment record successfully! / शेतकरी बिल तयार झाले.")
                    _previewData.value = null
                    fetchPayments()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Server declined payment generation.")
                }
            } catch (e: Exception) {
                _message.emit("Generate API connection failed.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateBulkPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val req = GenerateBulkRequest(
                    startDate = startDate.value,
                    endDate = endDate.value,
                    milkTypeId = selectedMilkType.value?.milkTypeId
                )
                val res = apiService.generateWeeklyPaymentBulk(req)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Bulk billing completed for active members! / सर्व सक्रिय शेतकऱ्यांचे बिल तयार झाले.")
                    fetchPayments()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Unable to run bulk sequence.")
                }
            } catch (e: Exception) {
                _message.emit("Bulk generation failure.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markPaid(paymentId: Int, amt: Double, mode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val req = MarkPaidRequest(
                    paymentId = paymentId,
                    paidAmount = amt,
                    paymentMode = mode,
                    paymentDate = DateUtils.getTodayIsoString()
                )
                val res = apiService.markWeeklyPaymentPaid(req)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Marked paid successfully! / पेमेंट नोंदवले गेले.")
                    fetchPayments()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Server declined marking payment paid.")
                }
            } catch (e: Exception) {
                _message.emit("Mark paid offline failure.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendEmailBill(paymentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = apiService.sendBillReportMail(SendSingleEmailRequest(paymentId))
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Bill email sent successfully! / बिल शेतकऱ्याला ईमेल केले.")
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Premium subscription required on mail services.")
                }
            } catch (e: Exception) {
                _message.emit("Unable to deliver bill mail.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendBulkEmails(paymentIds: List<Int>) {
        if (paymentIds.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = apiService.sendBulkBillReportsMail(SendBulkEmailsRequest(paymentIds))
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Bulk SMTP emails scheduled successfully / एकत्रित ईमेल पाठवले.")
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Bulk mail limits or plan constraints.")
                }
            } catch (e: Exception) {
                _message.emit("Unable to dispatch bulk schedules.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayment(id: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.deleteWeeklyPayment(id)
                if (res.isSuccessful && res.body()?.finalSuccess == true) {
                    _message.emit("Payment block deleted / बिल पेमेंट हटवले गेले.")
                    fetchPayments()
                } else {
                    _message.emit(res.body()?.finalMessage ?: "Unable to delete.")
                }
            } catch (e: Exception) {
                _message.emit("Deletion api connection issues.")
            }
        }
    }
}
