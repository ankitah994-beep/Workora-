<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name="com.workora.app.WorkoraApplication"
        android:allowBackup="true"
        android:label="Workora"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <!-- Disables Firebase auto-initialization (no-op if Firebase is not in the project). -->
        <provider
            android:name="com.google.firebase.provider.FirebaseInitProvider"
            android:authorities="${applicationId}.firebaseinitprovider"
            tools:node="remove" />

        <activity
            android:name="com.workora.app.MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Crash report screen: separate process, framework theme only. -->
        <activity
            android:name="com.workora.app.CrashActivity"
            android:exported="false"
            android:excludeFromRecents="true"
            android:process=":crash"
            android:theme="@android:style/Theme.DeviceDefault.Light.NoActionBar" />

    </application>

</manifest>
