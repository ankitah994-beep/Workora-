package com.example.ui.screens

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class WorkoraRealOtpEngine(private val activity: Activity) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storedVerificationId: String = ""

    // 1. Naam update karke sendRealOtp kar diya
    fun sendRealOtp(phoneNumber: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verify
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

    // 2. Naam update karke verifyRealOtp kar diya
    fun verifyRealOtp(otp: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (storedVerificationId.isEmpty()) {
            onError("Pehle OTP bhejiye.")
            return
        }

        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess() 
                } else {
                    onError(task.exception?.localizedMessage ?: "Galat OTP! Kripya sahi code daalein.")
                }
            }
    }
}
