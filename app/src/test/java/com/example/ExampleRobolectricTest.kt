package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.WorkoraDao
import com.example.data.WorkoraDatabase
import com.example.data.WorkoraRepository
import com.example.model.ScreenState
import com.example.model.UserAccount
import com.example.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: WorkoraDatabase
    private lateinit var dao: WorkoraDao
    private lateinit var repository: WorkoraRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WorkoraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.workoraDao()
        repository = WorkoraRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Workora", appName)
    }

    @Test
    fun prototypeScreenNavigationStatesExist() {
        val splash = ScreenState.SPLASH
        val accountSelection = ScreenState.ACCOUNT_SELECTION
        val customerHome = ScreenState.CUSTOMER_HOME
        val labourHome = ScreenState.LABOUR_HOME
        val profile = ScreenState.PROFILE

        assertNotNull(splash)
        assertNotNull(accountSelection)
        assertNotNull(customerHome)
        assertNotNull(labourHome)
        assertNotNull(profile)
    }

    @Test
    fun sampleWorkerCardRetrieval() = runBlocking {
        val workers = repository.getWorkersByTrade("All").first()
        // Ensure worker profile queries work smoothly
        assertNotNull(workers)
    }

    @Test
    fun userProfileAccountDetailsWork() = runBlocking {
        val user = UserAccount(
            fullName = "Ramesh Verma",
            mobileNumber = "+91 98765 43210",
            email = "ramesh.verma@workora.com",
            password = "demoPassword123",
            location = "Sector 14, Gurugram",
            role = "CUSTOMER",
            isLoggedIn = true
        )
        val id = repository.registerUser(user)
        assertTrue(id > 0)

        val retrieved = repository.getLoggedInUserSync()
        assertNotNull(retrieved)
        assertEquals("Ramesh Verma", retrieved?.fullName)
        assertEquals("Sector 14, Gurugram", retrieved?.location)
    }
}
