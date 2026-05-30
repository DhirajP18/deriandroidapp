package com.example.data.api

import com.squareup.moshi.Json
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// Resilient API Response wrapper mapping both Camel & Pascal variations
data class ApiResponse<T>(
    @Json(name = "success") val successCamel: Boolean? = null,
    @Json(name = "Success") val successPascal: Boolean? = null,
    @Json(name = "isSuccess") val isSuccessCamel: Boolean? = null,
    @Json(name = "IsSuccess") val isSuccessPascal: Boolean? = null,

    @Json(name = "message") val messageCamel: String? = null,
    @Json(name = "Message") val messagePascal: String? = null,
    @Json(name = "resMsg") val resMsgCamel: String? = null,
    @Json(name = "ResMsg") val resMsgPascal: String? = null,

    @Json(name = "data") val dataCamel: T? = null,
    @Json(name = "Data") val dataPascal: T? = null,
    @Json(name = "result") val resultCamel: T? = null,
    @Json(name = "Result") val resultPascal: T? = null
) {
    val finalSuccess: Boolean get() = successCamel == true || successPascal == true || isSuccessCamel == true || isSuccessPascal == true
    val finalMessage: String get() = messageCamel ?: messagePascal ?: resMsgCamel ?: resMsgPascal ?: ""
    val finalData: T? get() = dataCamel ?: dataPascal ?: resultCamel ?: resultPascal
}

// REST Requests & DTOs
data class LoginRequest(
    @Json(name = "UserName") val userName: String,
    @Json(name = "Password") val password: String
)

data class LoginResponse(
    @Json(name = "accessToken") val accessToken: String? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "dairyName") val dairyName: String? = null,
    @Json(name = "dairyNameMr") val dairyNameMr: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "fullNameMr") val fullNameMr: String? = null,
    @Json(name = "isAdmin") val isAdmin: Boolean? = null,
    @Json(name = "isSuperAdmin") val isSuperAdmin: Boolean? = null,
    @Json(name = "roleId") val roleId: Int? = null,
    @Json(name = "roleName") val roleName: String? = null,
    @Json(name = "sidebar") val sidebar: List<SidebarMenu>? = null,
    @Json(name = "myWidgets") val myWidgets: List<Any>? = null,
    @Json(name = "tokenExpiry") val tokenExpiry: String? = null,
    @Json(name = "userId") val userId: Int? = null,
    @Json(name = "userName") val userName: String? = null
)

data class SidebarMenu(
    @Json(name = "menuId") val menuId: Int? = null,
    @Json(name = "menuName") val menuName: String? = null,
    @Json(name = "menuNameMr") val menuNameMr: String? = null,
    @Json(name = "menuUrl") val menuUrl: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "parentMenuId") val parentMenuId: Int? = null,
    @Json(name = "childMenus") val childMenus: List<SidebarMenu>? = null,
    @Json(name = "moduleName") val moduleName: String? = null,
    @Json(name = "moduleNameMr") val moduleNameMr: String? = null,
    @Json(name = "isHeader") val isHeader: Boolean? = null
)

data class DashboardAll(
    @Json(name = "todayCollection") val todayCollection: DashboardStats? = null,
    @Json(name = "todayPurchase") val todayPurchase: DashboardStats? = null,
    @Json(name = "farmerStats") val farmerStats: FarmerStats? = null,
    @Json(name = "topFarmers") val topFarmers: List<TopFarmer>? = null,
    @Json(name = "recentActivity") val recentActivity: List<ActivityLog>? = null,
    @Json(name = "collectionChart") val collectionChart: List<ChartPoint>? = null,
    @Json(name = "weeklyPaymentSummary") val weeklyPaymentSummary: WeeklySummary? = null,
    @Json(name = "pendingDues") val pendingDues: Double? = null,
    @Json(name = "subscriptionStatus") val subscriptionStatus: SubscriptionStatus? = null
)

data class DashboardStats(
    @Json(name = "totalLitres") val totalLitres: Double? = null,
    @Json(name = "totalAmount") val totalAmount: Double? = null,
    @Json(name = "buffaloLitres") val buffaloLitres: Double? = null,
    @Json(name = "cowLitres") val cowLitres: Double? = null
)

data class FarmerStats(
    @Json(name = "totalFarmers") val totalFarmers: Int? = null,
    @Json(name = "activeFarmers") val activeFarmers: Int? = null
)

data class TopFarmer(
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "farmerNameMr") val farmerNameMr: String? = null,
    @Json(name = "totalQuantity") val totalQuantity: Double? = null,
    @Json(name = "totalAmount") val totalAmount: Double? = null
)

data class ActivityLog(
    @Json(name = "activityId") val activityId: Int? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "descriptionMr") val descriptionMr: String? = null,
    @Json(name = "logDate") val logDate: String? = null
)

data class ChartPoint(
    @Json(name = "label") val label: String? = null,
    @Json(name = "cowQuantity") val cowQuantity: Double? = null,
    @Json(name = "buffaloQuantity") val buffaloQuantity: Double? = null,
    @Json(name = "totalQuantity") val totalQuantity: Double? = null
)

data class WeeklySummary(
    @Json(name = "totalMilkAmount") val totalMilkAmount: Double? = null,
    @Json(name = "totalDeductions") val totalDeductions: Double? = null,
    @Json(name = "netPayable") val netPayable: Double? = null
)

data class SubscriptionStatus(
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "planName") val planName: String? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "daysRemaining") val daysRemaining: Int? = null
)

// Main Domain DTO definitions
data class Village(
    @Json(name = "villageId") val villageId: Int? = null,
    @Json(name = "villageName") val villageName: String? = null,
    @Json(name = "villageNameMarathi") val villageNameMarathi: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class Farmer(
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "farmerNameEn") val farmerNameEn: String? = null,
    @Json(name = "farmerNameMr") val farmerNameMr: String? = null,
    @Json(name = "mobileNumber") val mobileNumber: String? = null,
    @Json(name = "whatsAppNumber") val whatsAppNumber: String? = null,
    @Json(name = "emailid") val emailid: String? = null,
    @Json(name = "villageId") val villageId: Int? = null,
    @Json(name = "villageName") val villageName: String? = null,
    @Json(name = "addressEn") val addressEn: String? = null,
    @Json(name = "addressMr") val addressMr: String? = null,
    @Json(name = "adharNumber") val adharNumber: String? = null,
    @Json(name = "joinDate") val joinDate: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class MilkType(
    @Json(name = "milkTypeId") val milkTypeId: Int? = null,
    @Json(name = "milkTypeName") val milkTypeName: String? = null,
    @Json(name = "milkTypeNameMarathi") val milkTypeNameMarathi: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class MilkShift(
    @Json(name = "shiftId") val shiftId: Int? = null,
    @Json(name = "shiftName") val shiftName: String? = null,
    @Json(name = "shiftNameMarathi") val shiftNameMarathi: String? = null,
    @Json(name = "shiftCode") val shiftCode: String? = null
)

data class FeedItem(
    @Json(name = "feedItemId") val feedItemId: Int? = null,
    @Json(name = "feedItemName") val feedItemName: String? = null,
    @Json(name = "feedItemNameMarathi") val feedItemNameMarathi: String? = null,
    @Json(name = "defaultRatePerUnit") val defaultRatePerUnit: Double? = null,
    @Json(name = "unit") val unit: String? = null
)

data class MilkRate(
    @Json(name = "rateId") val rateId: Int? = null,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null,
    @Json(name = "milkTypeName") val milkTypeName: String? = null,
    @Json(name = "faTFrom") val fATFrom: Double? = null,
    @Json(name = "faTTo") val fATTo: Double? = null,
    @Json(name = "rate") val rate: Double? = null,
    @Json(name = "rateFixDate") val rateFixDate: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class MilkPurchaseRate(
    @Json(name = "purchaseRateId") val purchaseRateId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null,
    @Json(name = "isBuffalo") val isBuffalo: Boolean? = null,
    @Json(name = "fatFrom") val fatFrom: Double? = null,
    @Json(name = "fatTo") val fatTo: Double? = null,
    @Json(name = "ratePerLitre") val ratePerLitre: Double? = null,
    @Json(name = "effectiveFrom") val effectiveFrom: String? = null,
    @Json(name = "effectiveTo") val effectiveTo: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class MilkCollectionEntry(
    @Json(name = "entryId") val entryId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "shiftId") val shiftId: Int? = null,
    @Json(name = "shiftName") val shiftName: String? = null,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null,
    @Json(name = "milkTypeName") val milkTypeName: String? = null,
    @Json(name = "collectionDate") val collectionDate: String? = null,
    @Json(name = "quantityLiters") val quantityLiters: Int? = null,
    @Json(name = "quantityMilliliters") val quantityMilliliters: Int? = null,
    @Json(name = "fat") val fat: Double? = null,
    @Json(name = "ratePerLitermili") val ratePerLitermili: Double? = null,
    @Json(name = "remarks") val remarks: String? = null
)

data class MilkPurchaseEntry(
    @Json(name = "purchaseId") val purchaseId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null,
    @Json(name = "milkTypeName") val milkTypeName: String? = null,
    @Json(name = "shiftId") val shiftId: Int? = null,
    @Json(name = "shiftName") val shiftName: String? = null,
    @Json(name = "purchaseDate") val purchaseDate: String? = null,
    @Json(name = "quantityLitres") val quantityLitres: Double? = null,
    @Json(name = "fat") val fat: Double? = null,
    @Json(name = "snf") val snf: Double? = null,
    @Json(name = "isBuffalo") val isBuffalo: Boolean? = null,
    @Json(name = "manualRatePerLitre") val manualRatePerLitre: Double? = null
)

data class AdvancePayment(
    @Json(name = "advanceId") val advanceId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "advanceAmount") val advanceAmount: Double? = null,
    @Json(name = "advanceDate") val advanceDate: String? = null,
    @Json(name = "purpose") val purpose: String? = null,
    @Json(name = "purposeMarathi") val purposeMarathi: String? = null,
    @Json(name = "remarks") val remarks: String? = null
)

data class DeductAdvanceRequest(
    @Json(name = "advanceId") val advanceId: Int,
    @Json(name = "deductionAmount") val deductionAmount: Double,
    @Json(name = "deductionDate") val deductionDate: String,
    @Json(name = "remarks") val remarks: String? = null
)

data class FarmerDue(
    @Json(name = "dueId") val dueId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "dueType") val dueType: String? = null, // Advance or FeedItem
    @Json(name = "totalAmount") val totalAmount: Double? = null,
    @Json(name = "dueDate") val dueDate: String? = null,
    @Json(name = "remarks") val remarks: String? = null,
    @Json(name = "feedItemId") val feedItemId: Int? = null,
    @Json(name = "feedItemName") val feedItemName: String? = null,
    @Json(name = "quantity") val quantity: Double? = null,
    @Json(name = "ratePerUnit") val ratePerUnit: Double? = null,
    @Json(name = "recoveredAmount") val recoveredAmount: Double? = null,
    @Json(name = "pendingAmount") val pendingAmount: Double? = null
)

data class RecoverDueRequest(
    @Json(name = "dairyId") val dairyId: Int,
    @Json(name = "farmerId") val farmerId: Int,
    @Json(name = "recoveryAmount") val recoveryAmount: Double,
    @Json(name = "recoveryDate") val recoveryDate: String,
    @Json(name = "remarks") val remarks: String? = null
)

data class FarmerLedgerEntry(
    @Json(name = "date") val date: String? = null,
    @Json(name = "particulars") val particulars: String? = null,
    @Json(name = "particularsMr") val particularsMr: String? = null,
    @Json(name = "debit") val debit: Double? = null,
    @Json(name = "credit") val credit: Double? = null,
    @Json(name = "balance") val balance: Double? = null
)

data class WeeklyPayment(
    @Json(name = "paymentId") val paymentId: Int? = null,
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "startDate") val startDate: String? = null,
    @Json(name = "endDate") val endDate: String? = null,
    @Json(name = "milkType") val milkType: String? = null,
    @Json(name = "totalLitres") val totalLitres: Double? = null,
    @Json(name = "milkAmount") val milkAmount: Double? = null,
    @Json(name = "deductionsAmount") val deductionsAmount: Double? = null,
    @Json(name = "netPayable") val netPayable: Double? = null,
    @Json(name = "paidAmount") val paidAmount: Double? = null,
    @Json(name = "status") val status: String? = null, // e.g., "Pending" or "Paid"
    @Json(name = "paymentMode") val paymentMode: String? = null,
    @Json(name = "paymentDate") val paymentDate: String? = null
)

data class GeneratePaymentRequest(
    @Json(name = "farmerId") val farmerId: Int? = null,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null
)

data class GenerateBulkRequest(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "milkTypeId") val milkTypeId: Int? = null
)

data class MarkPaidRequest(
    @Json(name = "paymentId") val paymentId: Int,
    @Json(name = "paidAmount") val paidAmount: Double,
    @Json(name = "paymentMode") val paymentMode: String,
    @Json(name = "paymentDate") val paymentDate: String
)

data class SendBulkEmailsRequest(
    @Json(name = "paymentIds") val paymentIds: List<Int>
)

data class SendSingleEmailRequest(
    @Json(name = "paymentId") val paymentId: Int
)

// Bill Report Data Format (from server json payload)
data class BillReportData(
    @Json(name = "dairyName") val dairyName: String? = null,
    @Json(name = "dairyNameMr") val dairyNameMr: String? = null,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "farmerName") val farmerName: String? = null,
    @Json(name = "farmerCode") val farmerCode: String? = null,
    @Json(name = "period") val period: String? = null,
    @Json(name = "rows") val rows: List<BillReportRow>? = null,
    @Json(name = "totalLitres") val totalLitres: Double? = null,
    @Json(name = "totalAmount") val totalAmount: Double? = null,
    @Json(name = "deductions") val deductions: List<BillDeduction>? = null,
    @Json(name = "netPayable") val netPayable: Double? = null
)

data class BillReportRow(
    @Json(name = "date") val date: String? = null,
    @Json(name = "morningLitres") val morningLitres: Double? = null,
    @Json(name = "morningFat") val morningFat: Double? = null,
    @Json(name = "morningRate") val morningRate: Double? = null,
    @Json(name = "morningAmount") val morningAmount: Double? = null,
    @Json(name = "eveningLitres") val eveningLitres: Double? = null,
    @Json(name = "eveningFat") val eveningFat: Double? = null,
    @Json(name = "eveningRate") val eveningRate: Double? = null,
    @Json(name = "eveningAmount") val eveningAmount: Double? = null
)

data class BillDeduction(
    @Json(name = "type") val type: String? = null,
    @Json(name = "amount") val amount: Double? = null,
    @Json(name = "remarks") val remarks: String? = null
)

// SuperAdmin and UserManagement
data class Dairy(
    @Json(name = "dairyId") val dairyId: Int? = null,
    @Json(name = "dairyName") val dairyName: String? = null,
    @Json(name = "dairyNameMr") val dairyNameMr: String? = null,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "ownerName") val ownerName: String? = null,
    @Json(name = "mobileNumber") val mobileNumber: String? = null,
    @Json(name = "subscriptionEndDate") val subscriptionEndDate: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class ActiveToggleResponse(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "isActive") val isActive: Boolean? = null
)

data class CreateDairyRequest(
    @Json(name = "dairyName") val dairyName: String,
    @Json(name = "dairyNameMr") val dairyNameMr: String,
    @Json(name = "address") val address: String,
    @Json(name = "ownerName") val ownerName: String,
    @Json(name = "ownerUserName") val ownerUserName: String,
    @Json(name = "ownerEmail") val ownerEmail: String,
    @Json(name = "mobileNumber") val mobileNumber: String,
    @Json(name = "subscriptionPlanId") val subscriptionPlanId: Int,
    @Json(name = "assignedMenuIds") val assignedMenuIds: List<Int>,
    @Json(name = "assignedWidgetIds") val assignedWidgetIds: List<Int>
)

data class UpdateSubscriptionRequest(
    @Json(name = "dairyId") val dairyId: Int,
    @Json(name = "subscriptionPlanId") val subscriptionPlanId: Int,
    @Json(name = "subscriptionStart") val subscriptionStart: String,
    @Json(name = "subscriptionEnd") val subscriptionEnd: String,
    @Json(name = "assignedMenuIds") val assignedMenuIds: List<Int>,
    @Json(name = "assignedWidgetIds") val assignedWidgetIds: List<Int>
)

data class SubscriptionPlan(
    @Json(name = "planId") val planId: Int? = null,
    @Json(name = "planName") val planName: String? = null,
    @Json(name = "amount") val amount: Double? = null,
    @Json(name = "durationMonths") val durationMonths: Int? = null
)

data class EmailSettings(
    @Json(name = "smtpHost") val smtpHost: String? = null,
    @Json(name = "smtpPort") val smtpPort: Int? = null,
    @Json(name = "enableSsl") val enableSsl: Boolean? = null,
    @Json(name = "senderEmail") val senderEmail: String? = null,
    @Json(name = "senderName") val senderName: String? = null,
    @Json(name = "smtpUserName") val smtpUserName: String? = null,
    @Json(name = "smtpPassword") val smtpPassword: String? = null
)

data class MenuOrWidgetDef(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "nameMr") val nameMr: String? = null,
    @Json(name = "key") val key: String? = null
)

data class User(
    @Json(name = "userId") val userId: Int? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "userName") val userName: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "mobileNumber") val mobileNumber: String? = null,
    @Json(name = "roleName") val roleName: String? = null,
    @Json(name = "isActive") val isActive: Boolean? = null,
    @Json(name = "roleId") val roleId: Int? = null,
    @Json(name = "assignedWidgetIds") val assignedWidgetIds: List<Int>? = null
)

data class Role(
    @Json(name = "roleId") val roleId: Int? = null,
    @Json(name = "roleName") val roleName: String? = null
)

data class CreateUserRequest(
    @Json(name = "fullName") val fullName: String,
    @Json(name = "userName") val userName: String,
    @Json(name = "password") val password: String,
    @Json(name = "email") val email: String,
    @Json(name = "mobileNumber") val mobileNumber: String,
    @Json(name = "roleId") val roleId: Int,
    @Json(name = "assignedWidgetIds") val assignedWidgetIds: List<Int>,
    @Json(name = "assignedMenuIds") val assignedMenuIds: List<Int> = emptyList()
)

data class UpdateUserRequest(
    @Json(name = "userId") val userId: Int,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "userName") val userName: String,
    @Json(name = "email") val email: String,
    @Json(name = "mobileNumber") val mobileNumber: String,
    @Json(name = "roleId") val roleId: Int,
    @Json(name = "newPassword") val newPassword: String? = null,
    @Json(name = "assignedWidgetIds") val assignedWidgetIds: List<Int>
)

interface ApiService {

    @POST("Auth/Login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @POST("Account/Logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("Auth/Refresh")
    suspend fun refresh(): Response<ResponseBody>

    // Sidebar
    @GET("Sidebar/GetSidebar")
    suspend fun getSidebar(): Response<ApiResponse<List<SidebarMenu>>>

    @GET("Sidebar/GetAllModulesMenus")
    suspend fun getAllModulesMenus(): Response<ApiResponse<List<SidebarMenu>>>

    // Dashboard
    @GET("Dashboard/GetAll")
    suspend fun getDashboardAll(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<DashboardAll>>

    @GET("Dashboard/GetTodayCollection")
    suspend fun getTodayCollection(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<DashboardStats>>

    @GET("Dashboard/GetTodayPurchase")
    suspend fun getTodayPurchase(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<DashboardStats>>

    @GET("Dashboard/GetFarmerStats")
    suspend fun getFarmerStats(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<FarmerStats>>

    @GET("Dashboard/GetTopFarmers")
    suspend fun getTopFarmers(
        @Query("period") period: String,
        @Query("dairyId") dairyId: Int? = null
    ): Response<ApiResponse<List<TopFarmer>>>

    @GET("Dashboard/GetRecentActivity")
    suspend fun getRecentActivity(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<List<ActivityLog>>>

    @GET("Dashboard/GetCollectionChart")
    suspend fun getCollectionChart(
        @Query("days") days: Int = 7,
        @Query("dairyId") dairyId: Int? = null
    ): Response<ApiResponse<List<ChartPoint>>>

    @GET("Dashboard/GetWeeklyPaymentSummary")
    suspend fun getWeeklyPaymentSummary(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<WeeklySummary>>

    @GET("Dashboard/GetPendingDues")
    suspend fun getPendingDues(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<Double>>

    @GET("Dashboard/GetSubscriptionStatus")
    suspend fun getSubscriptionStatus(@Query("dairyId") dairyId: Int? = null): Response<ApiResponse<SubscriptionStatus>>

    @GET("Dashboard/GetDairySubscription")
    suspend fun getDairySubscription(
        @Query("dairyId") dairyId: Int? = null,
        @Query("DairyId") altDairyId: Int? = null
    ): Response<ApiResponse<SubscriptionStatus>>

    // Village
    @GET("Village/GetAll")
    suspend fun getVillages(): Response<ApiResponse<List<Village>>>

    @GET("Village/GetDropdown")
    suspend fun getVillageDropdown(): Response<ApiResponse<List<Village>>>

    @POST("Village/Create")
    suspend fun createVillage(@Body body: Village): Response<ApiResponse<Unit>>

    @PUT("Village/Update")
    suspend fun updateVillage(@Body body: Village): Response<ApiResponse<Unit>>

    @PATCH("Village/ToggleActive/{id}")
    suspend fun toggleVillageActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("Village/Delete/{id}")
    suspend fun deleteVillage(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Farmer
    @GET("Farmer/GetAll")
    suspend fun getFarmers(@Query("Search") search: String? = null): Response<ApiResponse<List<Farmer>>>

    @GET("Farmer/GetDropdown")
    suspend fun getFarmerDropdown(): Response<ApiResponse<List<Farmer>>>

    @POST("Farmer/Create")
    suspend fun createFarmer(@Body body: Farmer): Response<ApiResponse<Unit>>

    @PUT("Farmer/Update")
    suspend fun updateFarmer(@Body body: Farmer): Response<ApiResponse<Unit>>

    @PATCH("Farmer/ToggleActive/{id}")
    suspend fun toggleFarmerActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("Farmer/Delete/{id}")
    suspend fun deleteFarmer(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Type
    @GET("MilkType/GetAll")
    suspend fun getMilkTypes(): Response<ApiResponse<List<MilkType>>>

    @POST("MilkType/Create")
    suspend fun createMilkType(@Body body: MilkType): Response<ApiResponse<Unit>>

    @PUT("MilkType/Update")
    suspend fun updateMilkType(@Body body: MilkType): Response<ApiResponse<Unit>>

    @PATCH("MilkType/ToggleActive/{id}")
    suspend fun toggleMilkTypeActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("MilkType/Delete/{id}")
    suspend fun deleteMilkType(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Shift
    @GET("MilkShift/GetAll")
    suspend fun getMilkShifts(): Response<ApiResponse<List<MilkShift>>>

    @POST("MilkShift/Create")
    suspend fun createMilkShift(@Body body: MilkShift): Response<ApiResponse<Unit>>

    @PUT("MilkShift/Update")
    suspend fun updateMilkShift(@Body body: MilkShift): Response<ApiResponse<Unit>>

    @DELETE("MilkShift/Delete/{id}")
    suspend fun deleteMilkShift(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Feed Item
    @GET("FeedItem/GetAll")
    suspend fun getFeedItems(): Response<ApiResponse<List<FeedItem>>>

    @POST("FeedItem/Create")
    suspend fun createFeedItem(@Body body: FeedItem): Response<ApiResponse<Unit>>

    @PUT("FeedItem/Update")
    suspend fun updateFeedItem(@Body body: FeedItem): Response<ApiResponse<Unit>>

    @DELETE("FeedItem/Delete/{id}")
    suspend fun deleteFeedItem(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Rate
    @GET("MilkRate/GetAll")
    suspend fun getMilkRates(): Response<ApiResponse<List<MilkRate>>>

    @POST("MilkRate/Create")
    suspend fun createMilkRate(@Body body: MilkRate): Response<ApiResponse<Unit>>

    @PUT("MilkRate/Update")
    suspend fun updateMilkRate(@Body body: MilkRate): Response<ApiResponse<Unit>>

    @PATCH("MilkRate/ToggleActive/{id}")
    suspend fun toggleMilkRateActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("MilkRate/Delete/{id}")
    suspend fun deleteMilkRate(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Purchase Rate Master
    @GET("MilkPurchase/GetRates/{dairyId}")
    suspend fun getPurchaseRates(@Path("dairyId") dairyId: Int): Response<ApiResponse<List<MilkPurchaseRate>>>

    @POST("MilkPurchase/SaveRate")
    suspend fun savePurchaseRate(@Body body: MilkPurchaseRate): Response<ApiResponse<Unit>>

    @PATCH("MilkPurchase/ToggleRateActive/{id}")
    suspend fun togglePurchaseRateActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("MilkPurchase/DeleteRate/{id}")
    suspend fun deletePurchaseRate(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Collection Entry
    @GET("MilkCollectionEntry/GetAll")
    suspend fun getCollectionEntries(): Response<ApiResponse<List<MilkCollectionEntry>>>

    @POST("MilkCollectionEntry/Create")
    suspend fun createCollectionEntry(@Body body: MilkCollectionEntry): Response<ApiResponse<Unit>>

    @PUT("MilkCollectionEntry/Update")
    suspend fun updateCollectionEntry(@Body body: MilkCollectionEntry): Response<ApiResponse<Unit>>

    @DELETE("MilkCollectionEntry/Delete/{id}")
    suspend fun deleteCollectionEntry(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Milk Purchase Entry
    @GET("MilkPurchase/GetAll")
    suspend fun getPurchaseEntries(
        @Query("PageNumber") page: Int? = null,
        @Query("PageSize") size: Int? = null,
        @Query("milkTypeId") milkTypeId: Int? = null
    ): Response<ApiResponse<List<MilkPurchaseEntry>>>

    @POST("MilkPurchase/Create")
    suspend fun createPurchaseEntry(@Body body: MilkPurchaseEntry): Response<ApiResponse<Unit>>

    @PUT("MilkPurchase/Update")
    suspend fun updatePurchaseEntry(@Body body: MilkPurchaseEntry): Response<ApiResponse<Unit>>

    @GET("MilkPurchase/GetPurchaseBillReport")
    suspend fun getPurchaseBillReport(
        @Query("farmerId") farmerId: Int,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String,
        @Query("milkTypeId") milkTypeId: Int? = null
    ): Response<ApiResponse<BillReportData>>

    @DELETE("MilkPurchase/Delete/{id}")
    suspend fun deletePurchaseEntry(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Advance Payment
    @GET("AdvancePayment/GetAll")
    suspend fun getAdvancePayments(
        @Query("PageNumber") pageNumber: Int? = null,
        @Query("PageSize") pageSize: Int? = null,
        @Query("farmerId") farmerId: Int? = null,
        @Query("startDate") start: String? = null,
        @Query("endDate") end: String? = null
    ): Response<ApiResponse<List<AdvancePayment>>>

    @POST("AdvancePayment/Create")
    suspend fun createAdvancePayment(@Body body: AdvancePayment): Response<ApiResponse<Unit>>

    @POST("AdvancePayment/Update") // Note: create/update are often post per prompt, handle safely
    suspend fun updateAdvancePayment(@Body body: AdvancePayment): Response<ApiResponse<Unit>>

    @POST("AdvancePayment/Deduct")
    suspend fun deductAdvancePayment(@Body body: DeductAdvanceRequest): Response<ApiResponse<Unit>>

    @DELETE("AdvancePayment/Delete/{id}")
    suspend fun deleteAdvancePayment(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Farmer Due
    @GET("FarmerDue/GetOutstanding")
    suspend fun getOutstandingDues(
        @Query("DairyId") dairyId: Int,
        @Query("PageNumber") pageNumber: Int,
        @Query("PageSize") pageSize: Int,
        @Query("Search") search: String? = null
    ): Response<ApiResponse<List<FarmerDue>>>

    @POST("FarmerDue/Create")
    suspend fun createFarmerDue(@Body body: FarmerDue): Response<ApiResponse<Unit>>

    @GET("FarmerDue/GetDuesByFarmer")
    suspend fun getDuesByFarmer(
        @Query("dairyId") dairyId: Int,
        @Query("farmerId") farmerId: Int,
        @Query("pendingOnly") pendingOnly: Boolean = true
    ): Response<ApiResponse<List<FarmerDue>>>

    @POST("FarmerDue/Recover")
    suspend fun recoverDue(@Body body: RecoverDueRequest): Response<ApiResponse<Unit>>

    @GET("FarmerDue/GetLedger")
    suspend fun getFarmerLedger(
        @Query("dairyId") dairyId: Int,
        @Query("farmerId") farmerId: Int
    ): Response<ApiResponse<List<FarmerLedgerEntry>>>

    @DELETE("FarmerDue/Delete/{id}")
    suspend fun deleteFarmerDue(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // Weekly Payment
    @GET("WeeklyPayment/GetAll")
    suspend fun getWeeklyPayments(
        @Query("PageNumber") page: Int? = null,
        @Query("PageSize") size: Int? = null,
        @Query("FarmerId") farmerId: Int? = null,
        @Query("Status") status: String? = null,
        @Query("milkTypeId") milkTypeId: Int? = null
    ): Response<ApiResponse<List<WeeklyPayment>>>

    @POST("WeeklyPayment/Preview")
    suspend fun previewWeeklyPayment(@Body body: GeneratePaymentRequest): Response<ApiResponse<WeeklySummary>>

    @POST("WeeklyPayment/Generate")
    suspend fun generateWeeklyPayment(@Body body: GeneratePaymentRequest): Response<ApiResponse<Unit>>

    @POST("WeeklyPayment/GenerateBulk")
    suspend fun generateWeeklyPaymentBulk(@Body body: GenerateBulkRequest): Response<ApiResponse<Unit>>

    @GET("WeeklyPayment/GetBillReport/{paymentId}")
    suspend fun getWeeklyBillReport(@Path("paymentId") paymentId: Int): Response<ApiResponse<BillReportData>>

    @POST("Email/SendBillReport")
    suspend fun sendBillReportMail(@Body body: SendSingleEmailRequest): Response<ApiResponse<Unit>>

    @POST("Email/SendBulkBillReports")
    suspend fun sendBulkBillReportsMail(@Body body: SendBulkEmailsRequest): Response<ApiResponse<Unit>>

    @PATCH("WeeklyPayment/MarkPaid")
    suspend fun markWeeklyPaymentPaid(@Body body: MarkPaidRequest): Response<ApiResponse<Unit>>

    @DELETE("WeeklyPayment/Delete/{id}")
    suspend fun deleteWeeklyPayment(@Path("id") id: Int): Response<ApiResponse<Unit>>

    // SuperAdmin
    @GET("SuperAdmin/GetAllDairies")
    suspend fun getAllDairies(): Response<ApiResponse<List<Dairy>>>

    @POST("SuperAdmin/CreateDairy")
    suspend fun createDairy(@Body body: CreateDairyRequest): Response<ApiResponse<Unit>>

    @PUT("SuperAdmin/UpdateSubscription")
    suspend fun updateSubscription(@Body body: UpdateSubscriptionRequest): Response<ApiResponse<Unit>>

    @PATCH("SuperAdmin/ToggleActive/{id}")
    suspend fun toggleDairyActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("SuperAdmin/GetAllSubscriptionPlans")
    suspend fun getAllSubscriptionPlans(): Response<ApiResponse<List<SubscriptionPlan>>>

    @GET("SuperAdmin/GetAssignedMenus/{dairyId}")
    suspend fun getAssignedMenus(@Path("dairyId") dairyId: Int): Response<ApiResponse<List<SidebarMenu>>>

    @GET("SuperAdmin/GetEmailSettings/{dairyId}")
    suspend fun getEmailSettings(@Path("dairyId") dairyId: Int): Response<ApiResponse<EmailSettings>>

    @POST("SuperAdmin/SaveEmailSettings/{dairyId}")
    suspend fun saveEmailSettings(
        @Path("dairyId") dairyId: Int,
        @Body body: EmailSettings
    ): Response<ApiResponse<Unit>>

    @POST("SuperAdmin/TestEmailConnection/{dairyId}")
    suspend fun testEmailConnection(@Path("dairyId") dairyId: Int): Response<ApiResponse<Unit>>

    @Multipart
    @POST("SuperAdmin/UploadLogo/{dairyId}")
    suspend fun uploadLogo(
        @Path("dairyId") dairyId: Int,
        @Part logo: MultipartBody.Part
    ): Response<ApiResponse<Unit>>

    // Widgets definitions
    @GET("DashboardWidget/GetAll")
    suspend fun getAllWidgets(): Response<ApiResponse<List<MenuOrWidgetDef>>>

    @GET("DashboardWidget/GetDairyAllowedWidgets")
    suspend fun getDairyAllowedWidgets(): Response<ApiResponse<List<MenuOrWidgetDef>>>

    @GET("DashboardWidget/GetAssignedWidgets/{dairyId}")
    suspend fun getAssignedWidgets(@Path("dairyId") dairyId: Int): Response<ApiResponse<List<MenuOrWidgetDef>>>

    // User Management
    @GET("UserManagment/GetAll")
    suspend fun getUsers(): Response<ApiResponse<List<User>>>

    @GET("UserManagment/GetRoles")
    suspend fun getUserRoles(): Response<ApiResponse<List<Role>>>

    @GET("UserManagment/GetForEdit/{userId}")
    suspend fun getUserForEdit(@Path("userId") userId: Int): Response<ApiResponse<User>>

    @POST("UserManagment/Create")
    suspend fun createUser(@Body body: CreateUserRequest): Response<ApiResponse<Unit>>

    @PUT("UserManagment/Update")
    suspend fun updateUser(@Body body: UpdateUserRequest): Response<ApiResponse<Unit>>

    @PATCH("UserManagment/ToggleActive/{id}")
    suspend fun toggleUserActive(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @DELETE("UserManagment/Delete/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ApiResponse<Unit>>
}
