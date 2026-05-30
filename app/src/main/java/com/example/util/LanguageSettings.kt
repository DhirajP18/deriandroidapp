package com.example.util

object LanguageSettings {

    fun translate(en: String, mr: String, isMarathi: Boolean): String {
        return if (isMarathi) mr else en
    }

    // Common Phrases Dictionary
    val appName = Pair("DERISET", "डेअरीसेट")
    val appTagline = Pair("Your Dairy Partner", "तुमचा दुग्ध व्यवसाय भागीदार")
    val loginTitle = Pair("Welcome Back", "पुन्हा स्वागत आहे")
    val loginSubtitle = Pair("Sign in to manage your dairy", "तुमची डेअरी व्यवस्थापित करण्यासाठी लॉग इन करा")
    val username = Pair("Username", "वापरकर्तानाव")
    val password = Pair("Password", "पासवर्ड")
    val signIn = Pair("Sign In", "लॉग इन करा")
    val languageToggle = Pair("मराठी", "English")
    val themeToggle = Pair("Theme", "थीम")
    
    // Bottom Nav Tabs
    val navDashboard = Pair("Dashboard", "डॅशबोर्ड")
    val navCollection = Pair("Collection", "दूध संकलन")
    val navPayments = Pair("Payments", "पेमेंट")
    val navMore = Pair("More", "अधिक")

    // Dashboard Cards
    val todayColl = Pair("Today's Collection", "आजचे दूध संकलन")
    val todayPurch = Pair("Today's Purchase", "आजची खरेदी")
    val farmerStats = Pair("Farmer Stats", "शेतकरी आकडेवारी")
    val topFarmers = Pair("Top Farmers", "शीर्ष शेतकरी")
    val recentActivity = Pair("Recent Activity", "अलीकडील क्रियाकलाप")
    val weeklySummary = Pair("Weekly Payment Summary", "साप्ताहिक पेमेंट सारांश")
    val pendingDues = Pair("Pending Dues", "प्रलंबित देणी")
    val currSubscription = Pair("Dairy Subscription", "डेअरी सबस्क्रिप्शन")
    val daysRemaining = Pair("days remaining", "दिवस शिल्लक")
    val selectDairy = Pair("Select Dairy", "डेअरी निवडा")
    
    // Masters and navigation Drawers
    val masFarmers = Pair("Farmers & Members", "शेतकरी आणि सदस्य")
    val masVillages = Pair("Villages & Nodes", "गाव व्यवस्थापन")
    val masMilkTypes = Pair("Milk Types", "दूध प्रकार")
    val masMilkShifts = Pair("Milk Shifts", "दूध शिफ्ट")
    val masFeedItems = Pair("Feed Items", "चारा/खाद्य आयटम")
    val masMilkRates = Pair("Milk Rate Master", "दूध दर चार्ट")
    val masPurchaseRates = Pair("Purchase Rate Master", "खरेदी दर मास्टर")
    
    // Transactions
    val txMorningColl = Pair("Morning Collection", "सकाळचे संकलन")
    val txEveningColl = Pair("Evening Collection", "संध्याकाळचे संकलन")
    val txMilkPurch = Pair("Milk Purchase Entry", "दूध खरेदी नोंद")
    val txAdvancePay = Pair("Advance Payment", "अ‍ॅडव्हान्स पेमेंट")
    val txFarmerDues = Pair("Farmer Outstanding Dues", "शेतकरी थकबाकी")
    val reports = Pair("Reports", "अहवाल")
    val settings = Pair("App Settings", "अ‍ॅप सेटिंग्ज")

    // Stats Definitions
    val totalLitres = Pair("Total Litres", "एकूण लिटर")
    val totalAmount = Pair("Total Amount", "एकूण रक्कम")
    val cowLitres = Pair("Cow Litres", "गाय दूध लिटर")
    val buffaloLitres = Pair("Buffalo Litres", "म्हैस दूध लिटर")
    val totalFarmersLabel = Pair("Total Farmers", "एकूण शेतकरी")
    val activeFarmersLabel = Pair("Active Farmers", "सक्रिय शेतकरी")
    
    // Buttons and actions
    val btnSave = Pair("Save Entry", "नोंदणी जतन करा")
    val btnEdit = Pair("Edit", "संपादित करा")
    val btnDelete = Pair("Delete", "हटवा")
    val btnCancel = Pair("Cancel", "रद्द करा")
    val btnPreview = Pair("Preview Bill", "बिल पूर्वावलोकन")
    val btnGenerate = Pair("Generate Bill", "बिल तयार करा")
    val btnGenerateBulk = Pair("Generate Bulk Bills", "एकत्रित बिले तयार करा")
    val btnMarkPaid = Pair("Mark Paid", "भरलेले म्हणून चिन्हांकित करा")
    val btnSendEmail = Pair("Send Bill Email", "बिल ईमेलवर पाठवा")
    val btnPrint = Pair("Share PDF", "पीडीएफ शेअर करा")
    val btnConfigure = Pair("Configure", "कॉन्फिगर करा")
    val btnTest = Pair("Test SMTP", "SMTP चाचणी घ्या")
    val btnAddFarmer = Pair("Add New Farmer", "नवीन शेतकरी जोडा")
    val btnAddVillage = Pair("Add Village", "नवीन गाव जोडा")
    val btnAddMilkType = Pair("Add Milk Type", "नवीन दूध प्रकार जोडा")
    val btnAddShift = Pair("Add Shift", "नवीन शिफ्ट जोडा")
    val btnAddFeedItem = Pair("Add Feed Item", "नवीन खाद्य आयटम जोडा")
    val btnAddRate = Pair("Add Rate Slot", "नवीन दर स्लॉट जोडा")
    val btnAddPurchaseRate = Pair("Add Purchase Rate", "नवीन खरेदी दर जोडा")
    val btnAddAdvance = Pair("Give Advance", "अ‍ॅडव्हान्स द्या")
    val btnAddDue = Pair("Create Due/Feed Charge", "देणी/चारा भार जोडा")
    val btnRecoverDue = Pair("Recover Due Amount", "देणी रक्कम वसूल करा")
    val btnDeductAdvance = Pair("Deduct From Advance", "अ‍ॅडव्हान्स कपात")

    // Field labels
    val fldFarmerNo = Pair("Farmer Code/No", "शेतकरी कोड/क्रमांक")
    val fldFarmerEn = Pair("Farmer Name (English)", "शेतकरी नाव (इंग्रजी)")
    val fldFarmerMr = Pair("Farmer Name (Marathi)", "शेतकरी नाव (मराठी)")
    val fldMobile = Pair("Mobile Number", "मोबाईल नंबर")
    val fldWhatsapp = Pair("WhatsApp Number", "व्हाट्सएप नंबर")
    val fldEmail = Pair("Email address", "ईमेल आयडी")
    val fldAadhar = Pair("Aadhar Card Number", "आधार कार्ड नंबर")
    val fldAddressEn = Pair("Address (English)", "पत्ता (इंग्रजी)")
    val fldAddressMr = Pair("Address (Marathi)", "पत्ता (मराठी)")
    val fldVillage = Pair("Village Name", "गाव")
    val fldShift = Pair("Selected Shift", "शिफ्ट")
    val fldMilkType = Pair("Milk Category", "दूध संवर्ग")
    val fldQtyLtr = Pair("Quantity Litres", "दूध लिटर")
    val fldQtyMl = Pair("Quantity Milliliters", "दूध मिली")
    val fldFat = Pair("FAT % Percentage", "फॅट % प्रमाण")
    val fldSnf = Pair("SNF % Percentage", "एसएनएफ % प्रमाण")
    val fldRate = Pair("Calculated Rate/Ltr", "प्रति लिटर दर")
    val fldRemarks = Pair("Remarks/Notes", "रिमार्क्स/नोंद")
    val fldAmount = Pair("Total Amount Due", "एकूण रक्कम")
    val fldPurpose = Pair("Purpose (English)", "हेतू (इंग्रजी)")
    val fldPurposeMr = Pair("Purpose (Marathi)", "हेतू (मराठी)")
    val fldDueType = Pair("Due category (Advance/Feed)", "देणी श्रेणी")
}
