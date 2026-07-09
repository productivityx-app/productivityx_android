package com.oussama_chatri.productivityx.core.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import com.oussama_chatri.productivityx.R
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class NotificationHelperTest {

    private lateinit var context: Context
    private lateinit var prefs: PreferencesDataStore
    private lateinit var helper: NotificationHelper

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        
        val notificationManager = mockk<android.app.NotificationManager>(relaxed = true)
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.applicationContext } returns context

        // Mock default quiet hours (22 to 8)
        coEvery { prefs.quietHoursStart } returns flowOf(22)
        coEvery { prefs.quietHoursEnd } returns flowOf(8)

        helper = NotificationHelper(context, prefs)
    }

    @Test
    fun `isQuietHours returns true when current hour is within overnight range`() = runBlocking {
        // Range: 22 to 8
        // Hour: 23 (11 PM) -> Should be quiet
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23) }
        assertTrue(helper.isQuietHours(calendar))

        // Hour: 2 (2 AM) -> Should be quiet
        calendar.apply { set(Calendar.HOUR_OF_DAY, 2) }
        assertTrue(helper.isQuietHours(calendar))
        
        // Hour: 22 (10 PM) -> Should be quiet
        calendar.apply { set(Calendar.HOUR_OF_DAY, 22) }
        assertTrue(helper.isQuietHours(calendar))
    }

    @Test
    fun `isQuietHours returns false when current hour is outside overnight range`() = runBlocking {
        // Range: 22 to 8
        // Hour: 12 (12 PM) -> Should NOT be quiet
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12) }
        assertFalse(helper.isQuietHours(calendar))
        
        // Hour: 8 (8 AM) -> Should NOT be quiet (end is exclusive in standard logic, but let's just test a clear one)
        calendar.apply { set(Calendar.HOUR_OF_DAY, 9) }
        assertFalse(helper.isQuietHours(calendar))
    }
    
    @Test
    fun `isQuietHours returns true when current hour is within daytime range`() = runBlocking {
        // Mock daytime quiet hours (13 to 15) e.g., afternoon nap
        coEvery { prefs.quietHoursStart } returns flowOf(13)
        coEvery { prefs.quietHoursEnd } returns flowOf(15)
        
        // Hour: 14 (2 PM) -> Should be quiet
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 14) }
        assertTrue(helper.isQuietHours(calendar))
    }

    @Test
    fun `isQuietHours returns false when current hour is outside daytime range`() = runBlocking {
        // Mock daytime quiet hours (13 to 15)
        coEvery { prefs.quietHoursStart } returns flowOf(13)
        coEvery { prefs.quietHoursEnd } returns flowOf(15)
        
        // Hour: 11 (11 AM) -> Should NOT be quiet
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 11) }
        assertFalse(helper.isQuietHours(calendar))
        
        // Hour: 16 (4 PM) -> Should NOT be quiet
        calendar.apply { set(Calendar.HOUR_OF_DAY, 16) }
        assertFalse(helper.isQuietHours(calendar))
    }
}
