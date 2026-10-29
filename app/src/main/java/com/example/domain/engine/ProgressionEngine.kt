package com.example.domain.engine

import kotlin.math.max

data class LevelInfo(
    val level: Int,
    val title: String,
    val xpThreshold: Int, // Cumulative XP needed
    val description: String,
    val activityId: String
)

data class ProgressionState(
    val activityId: String,
    val currentXp: Int,
    val currentLevel: Int,
    val currentTitle: String,
    val nextLevelXp: Int,
    val prevLevelXp: Int,
    val progressToNextLevel: Float, // 0.0 to 1.0
    val protectedFloorXp: Int,
    val isMaxLevel: Boolean
)

object ProgressionEngine {

    val LEVEL_THRESHOLDS = intArrayOf(
        0,     // Level 0 (starter before L1)
        100,   // Level 1
        400,   // Level 2
        900,   // Level 3
        1600,  // Level 4
        2500,  // Level 5
        3600,  // Level 6
        4900,  // Level 7
        6400,  // Level 8
        8100,  // Level 9
        10000, // Level 10
        12100, // Level 11
        14400, // Level 12
        16900  // Level 13
    )

    // Habit Streak Milestones (consecutive scheduled days / clean days)
    val HABIT_STREAK_THRESHOLDS = intArrayOf(
        0,   // Level 0
        1,   // Level 1: 1 Day
        3,   // Level 2: 3 Days
        7,   // Level 3: 7 Days (1 Week)
        14,  // Level 4: 14 Days (2 Weeks)
        21,  // Level 5: 21 Days (3 Weeks / Habit Formed)
        30,  // Level 6: 30 Days (1 Month)
        45,  // Level 7: 45 Days
        60,  // Level 8: 60 Days (2 Months)
        90,  // Level 9: 90 Days (Quarter)
        120, // Level 10: 120 Days
        180, // Level 11: 180 Days (Half Year)
        270, // Level 12: 270 Days
        365  // Level 13: 365 Days (1 Full Year of Absolute Consistency)
    )

    private val MEDITATION_TITLES = listOf(
        "Initiate" to "Taking the first breath of conscious presence.",
        "Quiet Mind" to "Subtle quietude settles thoughts into background awareness.",
        "Breath Keeper" to "Anchoring awareness in rhythmic vital breath.",
        "Inner Seeker" to "Exploring deep stillness beneath surface chatter.",
        "Stillness Walker" to "Carrying serene composure into daily life.",
        "Serenity Builder" to "Constructing an unbreakable sanctuary of inner calm.",
        "Mindful Warrior" to "Equanimity under pressure; presence fortified.",
        "Silent Sage" to "Finding boundless clarity in conscious stillness.",
        "Conscious One" to "Witnessing reality without attachment or fear.",
        "Awakened Mind" to "Perceiving the fundamental unity of being.",
        "Enlightened Soul" to "Radiating effortless tranquility and lucidity.",
        "Mystic Sage" to "Dwelling in the timeless stillness prior to concept.",
        "Nirvana Walker" to "Dissolving dualities into sovereign awareness.",
        "Transcendent" to "Absolute mastery of inner space; supreme stillness realized."
    )

    private val STUDY_TITLES = listOf(
        "Initiate" to "Opening the first page of intentional learning.",
        "Novice" to "Cultivating curiosity and systematic inquiry.",
        "Apprentice" to "Mastering foundational principles and cognitive endurance.",
        "Learner" to "Building mental models and connecting multidisciplinary insights.",
        "Seeker" to "Relentlessly pursuing depth beyond superficial facts.",
        "Scholar" to "Synthesizing complex knowledge into clear comprehension.",
        "Disciple" to "Rigorous intellectual discipline and deep work immersion.",
        "Sage" to "Intuitive understanding cutting directly to first principles.",
        "Knowledge Keeper" to "Preserving, structuring, and refining vast domains.",
        "Master" to "Fluid cognitive agility; creating original insights.",
        "Grandmaster" to "Architecting comprehensive systems of thought.",
        "Illuminated Scholar" to "Illuminating obscure truths with profound clarity.",
        "Visionary" to "Synthesizing timeless wisdom to anticipate future frontiers.",
        "Omniscient" to "The pinnacle of intellectual mastery; cognitive transcendence."
    )

    private val WORKOUT_TITLES = listOf(
        "Initiate" to "The first repetition on the path of physical transformation.",
        "Iron Beginner" to "Establishing foundational movement patterns and grit.",
        "Disciplined" to "Showing up without hesitation regardless of motivation.",
        "Ironbound" to "Forging resilient sinew, tendon strength, and unyielding posture.",
        "Warrior" to "Embracing voluntary hardship and pushing through discomfort.",
        "Battle Forged" to "Tested against heavy resistance and sculpted by recovery.",
        "Vanguard" to "Leading the standard of unrelenting physical output.",
        "Titan" to "Commanding immense physical power and explosive capacity.",
        "Warlord" to "Total mastery over muscular endurance and heart rate.",
        "Apex" to "Reaching elite kinetic harmony between power and mobility.",
        "Colossus" to "An immovable pillar of physical discipline and endurance.",
        "Ascendant" to "Transcendence of bodily fatigue through invincible willpower.",
        "Living Legend" to "A living monument of supreme athletic discipline and power."
    )

    private val SLEEP_TITLES = listOf(
        "Initiate" to "Acknowledging sleep as the cornerstone of human optimization.",
        "Restless" to "Taming nocturnal restlessness and nighttime stimulation.",
        "Rest Seeker" to "Establishing consistent pre-sleep rituals and darkness hygiene.",
        "Balanced Sleeper" to "Syncing the body clock to the natural circadian cadence.",
        "Rhythm Keeper" to "Maintaining strict bedtime and wake-up consistency.",
        "Deep Restorer" to "Maximizing slow-wave physical repair and nervous recovery.",
        "Night Disciplined" to "Treating the sleep window as a sacred non-negotiable duty.",
        "Sleep Guardian" to "Shielding sleep architecture against late-night stress.",
        "Circadian Master" to "Flawless hormonal synchronization and waking vitality.",
        "Rest Sage" to "Entering deep restorative states with instant serenity.",
        "Dream Walker" to "Unlocking vivid REM memory consolidation and mental clarity.",
        "Lunar Guardian" to "Harmonizing nocturnal recovery with daytime peak performance.",
        "Eternal Rest" to "Supreme cellular regeneration and biological renewal.",
        "Perfect Rhythm" to "The absolute harmony of biological rhythm; circadian mastery."
    )

    private val HABIT_BUILD_TITLES = listOf(
        "Sprout" to "Planting the seed of a powerful positive habit.",
        "Rhythm" to "Establishing initial daily momentum and consistency.",
        "Habitual" to "Completing the first continuous week of discipline.",
        "Consistent" to "Two weeks unbroken; neurological pathways strengthening.",
        "Iron Will" to "21 days: the scientific tipping point into automaticity.",
        "Steadfast" to "One full month of unbroken execution.",
        "Fortified" to "Surpassing resistance on days when motivation waned.",
        "Centurion" to "60 days of relentless, unyielding ritual execution.",
        "Unyielding" to "90 days: habit has evolved into enduring identity.",
        "Grand Practitioner" to "Effortless integration into daily character and virtue.",
        "Unbreakable" to "Half a year of continuous mastery; second nature.",
        "Living Discipline" to "An inspiration to self and living proof of daily resolve.",
        "Sovereign Mastery" to "One full year of total unbroken commitment; supreme triumph."
    )

    private val HABIT_QUIT_TITLES = listOf(
        "Sovereign Choice" to "Drawing the line in the sand; conscious reclamation of sovereignty.",
        "Breaking Shackles" to "Weathering the critical first 72 hours of neurochemical craving.",
        "Clear Horizon" to "1 full week clean; the mental fog begins to lift.",
        "Fortress Mind" to "14 days abstinent; resisting high-pressure temptation triggers.",
        "Iron Resolve" to "21 days free; the old impulse begins to starve and lose grip.",
        "Clean Slate" to "30 days clean; genuine chemical reset and restored dopamine balance.",
        "Unshakable" to "45 days unbroken; establishing emotional autonomy without the vice.",
        "Shield of Honor" to "60 days of absolute cleanliness and moral fortitude.",
        "Vanquisher" to "90 days: the old habit pattern is completely broken down.",
        "Apex Clarity" to "120 days of pure, untainted sovereignty and clear mind.",
        "Pure Sovereign" to "6 months clean; total transformation of physical and mental baseline.",
        "Liberated Soul" to "9 months free; the temptation has lost all gravitational pull.",
        "Absolute Freedom" to "One full year of pure victory; complete emancipation of the soul."
    )

    private val CUSTOM_TITLES = listOf(
        "Initiate" to "Beginning the journey of deliberate practice.",
        "Practitioner" to "Establishing regular cadence and dedication.",
        "Apprentice" to "Sharpening core fundamentals with focused repetition.",
        "Dedicated" to "Consistent execution without compromise.",
        "Artisan" to "Demonstrating refined technique and attention to detail.",
        "Expert" to "High degree of proficiency and autonomous problem solving.",
        "Adept" to "Smooth execution under varying demands.",
        "Virtuoso" to "Exceptional speed, finesse, and command.",
        "Specialist" to "Deep specialized domain competence.",
        "Master" to "Fluid execution and creative expression.",
        "Grandmaster" to "Effortless command and strategic vision.",
        "Luminary" to "Inspiring standard of excellence and depth.",
        "Apex Master" to "Supreme mastery and timeless legacy in this craft."
    )

    fun getLevelTitle(activityId: String, level: Int, isHabitQuit: Boolean = false, isHabitBuild: Boolean = false): Pair<String, String> {
        val clampedLevel = level.coerceIn(0, 13)
        val list = when {
            isHabitQuit -> HABIT_QUIT_TITLES
            isHabitBuild -> HABIT_BUILD_TITLES
            activityId.lowercase() == "meditation" -> MEDITATION_TITLES
            activityId.lowercase() == "study" -> STUDY_TITLES
            activityId.lowercase() == "workout" -> WORKOUT_TITLES
            activityId.lowercase() == "sleep" -> SLEEP_TITLES
            else -> CUSTOM_TITLES
        }
        return list.getOrElse(clampedLevel) { list.last() }
    }

    fun getAllLevelsForActivity(activityId: String, isHabitQuit: Boolean = false, isHabitBuild: Boolean = false): List<LevelInfo> {
        val thresholds = if (isHabitQuit || isHabitBuild) HABIT_STREAK_THRESHOLDS else LEVEL_THRESHOLDS
        return (1..13).map { level ->
            val (title, desc) = getLevelTitle(activityId, level, isHabitQuit, isHabitBuild)
            LevelInfo(
                level = level,
                title = title,
                xpThreshold = thresholds[level],
                description = desc,
                activityId = activityId
            )
        }
    }

    /**
     * Calculates current level from total accumulated XP (for time-based activities).
     */
    fun calculateLevelFromXp(xp: Int): Int {
        if (xp < LEVEL_THRESHOLDS[1]) return 0
        for (lvl in 13 downTo 1) {
            if (xp >= LEVEL_THRESHOLDS[lvl]) {
                return lvl
            }
        }
        return 0
    }

    /**
     * Calculates current level from streak (for habits).
     */
    fun calculateLevelFromStreak(streak: Int): Int {
        if (streak < HABIT_STREAK_THRESHOLDS[1]) return 0
        for (lvl in 13 downTo 1) {
            if (streak >= HABIT_STREAK_THRESHOLDS[lvl]) {
                return lvl
            }
        }
        return 0
    }

    /**
     * Calculates progression state for a habit activity.
     */
    fun calculateHabitProgression(
        activityId: String,
        streak: Int,
        totalCompletions: Int,
        isHabitQuit: Boolean
    ): ProgressionState {
        val currentLevel = calculateLevelFromStreak(streak)
        val (title, _) = getLevelTitle(activityId, currentLevel, isHabitQuit = isHabitQuit, isHabitBuild = !isHabitQuit)

        val prevStreakThreshold = if (currentLevel == 0) 0 else HABIT_STREAK_THRESHOLDS[currentLevel]
        val isMax = currentLevel >= 13
        val nextStreakThreshold = if (isMax) HABIT_STREAK_THRESHOLDS[13] else HABIT_STREAK_THRESHOLDS[currentLevel + 1]

        val progressToNext = if (isMax) {
            1.0f
        } else {
            val needed = nextStreakThreshold - prevStreakThreshold
            val currentInLevel = streak - prevStreakThreshold
            if (needed > 0) (currentInLevel.toFloat() / needed).coerceIn(0f, 1f) else 1.0f
        }

        // Decay floor: XP threshold of current level
        val protectedFloor = HABIT_STREAK_THRESHOLDS[currentLevel.coerceIn(0, 13)]

        return ProgressionState(
            activityId = activityId,
            currentXp = streak,
            currentLevel = currentLevel,
            currentTitle = title,
            nextLevelXp = nextStreakThreshold,
            prevLevelXp = prevStreakThreshold,
            progressToNextLevel = progressToNext,
            protectedFloorXp = protectedFloor,
            isMaxLevel = isMax
        )
    }

    /**
     * Calculates comprehensive progression state including progress % to next level.
     * Note: Requirement 1: The decay floor must be the XP threshold of the user's current level,
     * not the previous level!
     */
    fun calculateProgression(activityId: String, rawXp: Int): ProgressionState {
        val currentLevel = calculateLevelFromXp(rawXp)
        val (title, _) = getLevelTitle(activityId, currentLevel)

        val prevLevelXp = if (currentLevel == 0) 0 else LEVEL_THRESHOLDS[currentLevel]
        val isMax = currentLevel >= 13
        val nextLevelXp = if (isMax) LEVEL_THRESHOLDS[13] else LEVEL_THRESHOLDS[currentLevel + 1]

        val progressToNext = if (isMax) {
            1.0f
        } else {
            val needed = nextLevelXp - prevLevelXp
            val currentInLevel = rawXp - prevLevelXp
            if (needed > 0) (currentInLevel.toFloat() / needed).coerceIn(0f, 1f) else 1.0f
        }

        // Protected floor is the XP threshold of currentLevel (NOT currentLevel - 1)
        val protectedFloor = LEVEL_THRESHOLDS[currentLevel.coerceIn(0, 13)]

        return ProgressionState(
            activityId = activityId,
            currentXp = rawXp,
            currentLevel = currentLevel,
            currentTitle = title,
            nextLevelXp = nextLevelXp,
            prevLevelXp = prevLevelXp,
            progressToNextLevel = progressToNext,
            protectedFloorXp = protectedFloor,
            isMaxLevel = isMax
        )
    }

    /**
     * Calculates daily XP earned from active minutes versus daily goal minutes.
     * 100% of goal = 100 XP. Not capped, e.g. 150% = 150 XP.
     */
    fun calculateDailyXp(activeMinutes: Int, dailyGoalMinutes: Int): Int {
        if (dailyGoalMinutes <= 0 || activeMinutes <= 0) return 0
        val ratio = activeMinutes.toDouble() / dailyGoalMinutes.toDouble()
        return (ratio * 100.0).toInt()
    }

    /**
     * Applies soft decay.
     * Requirement 1: The decay floor must be the XP threshold of the user's current level,
     * not the previous level!
     * Default decay rate: 50 XP per missed day.
     */
    fun applySoftDecay(accumulatedXp: Int, currentLevel: Int, inactiveDays: Int, decayRatePerDay: Int = 50): Int {
        if (inactiveDays <= 0) return accumulatedXp
        // Protected floor is strictly the XP threshold of currentLevel
        val protectedFloor = LEVEL_THRESHOLDS[currentLevel.coerceIn(0, 13)]
        val totalDecay = inactiveDays * decayRatePerDay
        return max(protectedFloor, accumulatedXp - totalDecay)
    }
}
