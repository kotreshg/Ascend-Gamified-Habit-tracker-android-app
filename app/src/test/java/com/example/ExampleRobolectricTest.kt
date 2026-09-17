package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.engine.ProgressionEngine
import com.example.domain.engine.SleepEngine
import com.example.domain.engine.StreakEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ascend", appName)
    }

    @Test
    fun `test quadratic progression engine calculations`() {
        // Level 1 threshold: 0 XP
        assertEquals(0, ProgressionEngine.getXpThresholdForLevel(1))
        // Level 2 threshold: 100 * 2^2 = 400 XP
        assertEquals(400, ProgressionEngine.getXpThresholdForLevel(2))
        // Level 13 threshold: 100 * 13^2 = 16900 XP
        assertEquals(16900, ProgressionEngine.getXpThresholdForLevel(13))

        val prog = ProgressionEngine.calculateProgression("meditation", 1200)
        assertEquals(3, prog.currentLevel) // Level 3 is 900 to 1600 XP
        assertEquals("Stillness Practitioner", prog.currentTitle)
        assertEquals(400, prog.protectedFloorXp) // Level 2 floor
    }

    @Test
    fun `test streak and intensity tiers`() {
        assertEquals(4, StreakEngine.calculateIntensityTier(1.0f))
        assertEquals(3, StreakEngine.calculateIntensityTier(0.85f))
        assertEquals(2, StreakEngine.calculateIntensityTier(0.60f))
        assertEquals(1, StreakEngine.calculateIntensityTier(0.30f))
        assertEquals(0, StreakEngine.calculateIntensityTier(0.0f))
    }

    @Test
    fun `test sleep circadian engine`() {
        val zone = java.time.ZoneId.systemDefault()
        val date = java.time.LocalDate.of(2026, 8, 30)
        val bedtime = date.minusDays(1).atTime(22, 30).atZone(zone).toInstant().toEpochMilli()
        val wakeTime = date.atTime(6, 30).atZone(zone).toInstant().toEpochMilli()

        val analysis = SleepEngine.evaluateSleep(
            durationMinutes = 480,
            goalMinutes = 480,
            sessionStartMillis = bedtime,
            sessionEndMillis = wakeTime,
            bedtimeWindowStartMin = 22 * 60,
            bedtimeWindowEndMin = 23 * 60 + 30,
            wakeWindowStartMin = 5 * 60 + 30,
            wakeWindowEndMin = 7 * 60 + 30
        )

        assertEquals(100, analysis.durationScore)
        assertEquals(100, analysis.timingScore)
        assertEquals(100, analysis.overallSleepScore)
        assertTrue(analysis.isWithinBedtimeWindow)
        assertTrue(analysis.isWithinWakeWindow)
    }
}
