package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

object FirebaseManager {
    
    // असली Firebase SDKs
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var storedVerificationId: String = ""

    // 1. असली OTP भेजने का फंक्शन (SMS)
    fun sendOtp(
        activity: Activity, 
        phoneNumber: String, // format: "+919876543210"
        onCodeSent: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) 
            .setTimeout(60L, TimeUnit.SECONDS) 
            .setActivity(activity) 
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification (अगर सिम उसी फ़ोन में है)
                    signInWithPhoneAuthCredential(credential, activity) { success, _ ->
                        if(success) onCodeSent()
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onFailed(e.message ?: "OTP भेजने में फेल हुआ")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    onCodeSent()
                }
            }).build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 2. OTP वेरीफाई करने का फंक्शन
    fun verifyOtp(
        activity: Activity,
        otpCode: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (storedVerificationId.isEmpty()) {
            onResult(false, "OTP एक्सपायर हो गया है। दोबारा भेजें।")
            return
        }
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otpCode)
        signInWithPhoneAuthCredential(credential, activity, onResult)
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential, 
        activity: Activity,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onResult(true, "OTP Verified Successfully")
                } else {
                    onResult(false, task.exception?.message ?: "गलत OTP")
                }
            }
    }

    // 3. Firestore में वर्कर का डेटा सेव करना (नया, फ़ास्ट और सिक्योर तरीका)
    fun saveWorkerProfile(
        name: String,
        phone: String,
        skills: String,
        location: String,
        onSuccess: (Boolean) -> Unit
    ) {
        val workerData = hashMapOf(
            "name" to name,
            "phone" to phone,
            "skills" to skills,
            "location" to location,
            "role" to "WORKER",
            "createdAt" to System.currentTimeMillis()
        )

        // Firestore में 'workers' नाम के कलेक्शन में डेटा सेव होगा
        db.collection("workers").document(phone)
            .set(workerData)
            .addOnSuccessListener { onSuccess(true) }
            .addOnFailureListener { onSuccess(false) }
    }

    // 4. Job पोस्ट करने का नया तरीका (Firestore)
    fun postJobToFirestore(
        title: String,
        category: String,
        dailyRate: Int,
        location: String,
        onSuccess: (Boolean) -> Unit
    ) {
        val jobData = hashMapOf(
            "title" to title,
            "category" to category,
            "dailyRate" to dailyRate,
            "location" to location,
            "status" to "OPEN",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("jobs")
            .add(jobData)
            .addOnSuccessListener { onSuccess(true) }
            .addOnFailureListener { onSuccess(false) }
    }
}
