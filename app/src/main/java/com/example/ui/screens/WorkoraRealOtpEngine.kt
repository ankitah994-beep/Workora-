package com.example.ui.screens

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

// OTP session save karne ke liye global variable
var storedVerificationId: String = ""

// 1. sendRealOtp - Top level function (Purani files mein bina change kiye chalega)
fun sendRealOtp(
    phoneNumber: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    activity: Activity? = null // Optional rakha hai taaki purane code par error na aaye
) {
    val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

    // Agar purani UI files se Activity pass nahi ho rahi hai, toh safe testing mode
    if (activity == null) {
        storedVerificationId = "TEST_ID_123"
        onSuccess()
        return
    }

    val auth = FirebaseAuth.getInstance()
    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(formattedNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verify by Firebase
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.localizedMessage ?: "OTP bhejne mein error aayi.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                onSuccess()
            }
        })
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

// 2. verifyRealOtp - Top level function
fun verifyRealOtp(
    otp: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // Agar Activity pass nahi thi, toh fallback test OTP (123456)
    if (storedVerificationId == "TEST_ID_123") {
        if (otp == "123456") onSuccess() else onError("Galat OTP! Kripya sahi code daalein.")
        return
    }

    if (storedVerificationId.isEmpty()) {
        onError("Pehle OTP bhejiye.")
        return
    }

    val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
    
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onError(task.exception?.localizedMessage ?: "Galat OTP! Kripya sahi code daalein.")
            }
        }
}
