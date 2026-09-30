package com.example.ui.screens

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class WorkoraRealOtpEngine(private val activity: Activity) {
    
    // Firebase Auth ka instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storedVerificationId: String = ""

    // 1. Real OTP Bhejne ka function
    fun sendOtp(phoneNumber: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // India ke numbers ke liye +91 lagana zaroori hai (agar user ne nahi lagaya)
        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS) // 60 seconds ka timeout
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Agar sim phone mein hi hai, toh auto-verify ho jayega
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    onError(e.localizedMessage ?: "OTP bhejne mein error aayi. Number check karein.")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // SMS sach mein chala gaya hai!
                    storedVerificationId = verificationId
                    onSuccess()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 2. Real OTP Verify karne ka function
    fun verifyOtp(otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (storedVerificationId.isEmpty()) {
            onError("Pehle OTP bhejiye.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
        
        // Firebase ke server par OTP match karna
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess() // OTP bilkul sahi hai
                } else {
                    onError(task.exception?.localizedMessage ?: "Galat OTP! Kripya sahi code daalein.")
                }
            }
    }
}
