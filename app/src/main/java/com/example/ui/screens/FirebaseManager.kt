package com.example.ui.screens

import android.app.Activity
import android.content.Context
import com.example.model.JobPost
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

object FirebaseManager {
    
    const val DATABASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com" // पुरानी फाइल्स के लिए रखा है
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var storedVerificationId: String = ""

    // ==========================================
    // 1. NEW AUTH & OTP LOGIC (असली SMS के लिए)
    // ==========================================
    
    fun sendOtp(activity: Activity, phoneNumber: String, onCodeSent: () -> Unit, onFailed: (String) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential, activity) { success, _ -> if(success) onCodeSent() }
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    onFailed(e.message ?: "OTP failed")
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    storedVerificationId = verificationId
                    onCodeSent()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(activity: Activity, otpCode: String, onResult: (Boolean, String) -> Unit) {
        if (storedVerificationId.isEmpty()) {
            onResult(false, "OTP expired")
            return
        }
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
        signInWithPhoneAuthCredential(credential, activity, onResult)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, activity: Activity, onResult: (Boolean, String) -> Unit) {
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) onResult(true, "Success") else onResult(false, "Invalid OTP")
        }
    }

    fun saveWorkerProfile(name: String, phone: String, skills: String, location: String, onSuccess: (Boolean) -> Unit) {
        val data = hashMapOf("name" to name, "phone" to phone, "skills" to skills, "location" to location, "role" to "WORKER", "createdAt" to System.currentTimeMillis())
        db.collection("workers").document(phone).set(data).addOnCompleteListener { onSuccess(it.isSuccessful) }
    }

    // ==========================================
    // 2. RESTORED OLD FUNCTIONS (Firestore में अपग्रेड किए गए)
    // ==========================================

    // जिस एरर से बिल्ड फेल हुआ, उसे फिक्स किया (logAdminAudit)
    fun logAdminAudit(adminIdentifier: String, action: String, target: String) {
        val logData = hashMapOf(
            "adminIdentifier" to adminIdentifier,
            "action" to action,
            "target" to target,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("admin_audit_logs").add(logData)
    }

    fun checkIfEmailIsAdminOnCloud(email: String, onResult: (Boolean, String) -> Unit) {
        // असली Firestore चेक
        db.collection("admins").whereEqualTo("email", email.trim().lowercase()).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) onResult(true, "SUPER_ADMIN") else onResult(false, "NONE")
            }
            .addOnFailureListener { onResult(false, "NONE") }
    }

    fun checkIfEmailIsAdminOnCloud(context: Context, email: String, onResult: (Boolean, String) -> Unit) {
        checkIfEmailIsAdminOnCloud(email, onResult)
    }

    fun syncUserToFirebase(context: Context, name: String, email: String, phone: String, password: String, role: String) {
        val cleanPhone = phone.filter { it.isDigit() }.takeLast(10).ifBlank { "0000000000" }
        val data = hashMapOf(
            "name" to name, "email" to email, "phone" to phone, "role" to role,
            "accountStatus" to "ACTIVE", "updatedAt" to System.currentTimeMillis()
        )
        db.collection("users").document(cleanPhone).set(data)
    }

    fun createBooking(customerPhone: String, customerName: String, workerPhone: String, workerName: String, jobTitle: String, category: String, dailyRate: Int, location: String, onSuccess: (String) -> Unit = {}) {
        val bookingId = "bk_${System.currentTimeMillis()}"
        val data = hashMapOf(
            "bookingId" to bookingId, "customerPhone" to customerPhone, "customerName" to customerName,
            "workerPhone" to workerPhone, "workerName" to workerName, "jobTitle" to jobTitle,
            "category" to category, "dailyRate" to dailyRate, "location" to location, "status" to "PENDING",
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("bookings").document(bookingId).set(data).addOnSuccessListener { onSuccess(bookingId) }
    }

    fun updateBookingStatus(bookingId: String, newStatus: String, cancelReason: String = "") {
        val updates = mutableMapOf<String, Any>("status" to newStatus, "updatedAt" to System.currentTimeMillis())
        if (cancelReason.isNotBlank()) updates["cancelReason"] = cancelReason
        db.collection("bookings").document(bookingId).update(updates)
    }

    fun submitReport(reporterPhone: String, reportedPhone: String, reportedName: String, reason: String, description: String, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "reporterPhone" to reporterPhone, "reportedPhone" to reportedPhone,
            "reportedName" to reportedName, "reason" to reason, "description" to description,
            "status" to "PENDING_REVIEW", "timestamp" to System.currentTimeMillis()
        )
        db.collection("reports").add(data).addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun setBlockStatus(myPhone: String, targetPhone: String, isBlocked: Boolean, onDone: () -> Unit = {}) {
        val myClean = myPhone.filter { it.isDigit() }.takeLast(10)
        val targetClean = targetPhone.filter { it.isDigit() }.takeLast(10)
        if (isBlocked) {
            db.collection("blocked_users").document("u_$myClean").collection("blocked").document("u_$targetClean")
                .set(hashMapOf("blocked" to true, "timestamp" to System.currentTimeMillis()))
                .addOnCompleteListener { onDone() }
        } else {
            db.collection("blocked_users").document("u_$myClean").collection("blocked").document("u_$targetClean")
                .delete().addOnCompleteListener { onDone() }
        }
    }

    fun submitReview(bookingId: String, reviewerPhone: String, reviewerName: String, targetPhone: String, rating: Int, comment: String, onDone: (Boolean) -> Unit) {
        val data = hashMapOf(
            "bookingId" to bookingId, "reviewerPhone" to reviewerPhone, "reviewerName" to reviewerName,
            "targetPhone" to targetPhone, "rating" to rating.coerceIn(1, 5), "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reviews").add(data).addOnCompleteListener { onDone(it.isSuccessful) }
    }

    fun submitKycDocument(userPhone: String, docType: String, docNumber: String, docBase64: String, onDone: (Boolean) -> Unit) {
        val cleanPhone = userPhone.filter { it.isDigit() }.takeLast(10)
        val data = hashMapOf(
            "userPhone" to userPhone, "docType" to docType, "docNumber" to docNumber,
            "docBase64" to docBase64, "status" to "VERIFICATION_PENDING", "timestamp" to System.currentTimeMillis()
        )
        db.collection("kyc_verifications").document("u_$cleanPhone").set(data).addOnCompleteListener { onDone(it.isSuccessful) }
    }

    fun deleteAndAnonymizeAccount(userPhone: String, onDone: (Boolean) -> Unit) {
        val cleanPhone = userPhone.filter { it.isDigit() }.takeLast(10)
        db.collection("workers").document(cleanPhone).delete()
        val data = hashMapOf("name" to "Deleted User", "email" to "deleted@workora.in", "accountStatus" to "DELETED")
        db.collection("users").document(cleanPhone).update(data as Map<String, Any>).addOnCompleteListener { onDone(it.isSuccessful) }
    }

    fun postJobToFirebase(context: Context? = null, title: String = "Work Needed", category: String = "Mason", description: String = "", dailyRate: Int = 600, location: String = "Silwani", workersNeeded: Int = 1, urgency: String = "Immediate", dateTime: String = "", customerName: String = "", customerPhone: String = "", onSuccess: (Boolean) -> Unit = {}) {
        val data = hashMapOf(
            "title" to title, "category" to category, "description" to description, "dailyRate" to dailyRate,
            "location" to location, "workersNeeded" to workersNeeded, "urgency" to urgency, "dateTime" to dateTime,
            "customerName" to customerName, "customerPhone" to customerPhone, "status" to "OPEN", "createdAt" to System.currentTimeMillis()
        )
        db.collection("jobs").add(data).addOnCompleteListener { onSuccess(it.isSuccessful) }
    }

    fun postJobToFirebase(job: JobPost, onSuccess: (Boolean) -> Unit = {}) {
        postJobToFirebase(
            title = job.title, category = job.category, description = job.description, dailyRate = job.dailyRate,
            location = job.location, workersNeeded = job.workersNeeded, urgency = job.urgency, dateTime = job.dateTime,
            customerName = job.customerName, customerPhone = job.customerPhone, onSuccess = onSuccess
        )
    }
}
