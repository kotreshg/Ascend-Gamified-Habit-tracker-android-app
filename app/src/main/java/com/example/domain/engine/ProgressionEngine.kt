package com.example.domain.engine

import kotlin.math.max
import kotlin.math.sqrt

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

    private val MEDITATION_TITLES = listOf(
        "Initiate" to "Taking the first breath of conscious presence.",
        "Quiet Mind" to "Subtle quietude emerges as thoughts settle into background awareness.",
        "Breath Keeper" to "Anchoring awareness in the rhythmic tide of prana and vital breath.",
        "Inner Seeker" to "Diving beneath surface chatter to explore the stillness within.",
        "Stillness Walker" to "Carrying serene composure from the cushion into waking life.",
        "Serenity Builder" to "Constructing an unbreakable sanctuary of calm amidst chaos.",
        "Mindful Warrior" to "Equanimity under pressure; presence fortified against distraction.",
        "Silent Sage" to "Finding boundless clarity in the silent depths of conscious space.",
        "Conscious One" to "Witnessing the flux of reality without attachment or resistance.",
        "Awakened Mind" to "Perceiving the fundamental unity of observer and experience.",
        "Enlightened Soul" to "Radiating effortless tranquility, compassion, and lucid presence.",
        "Mystic Sage" to "Dwelling in the timeless stillness prior to all concepts and form.",
        "Nirvana Walker" to "Dissolving dualities in uninterrupted sovereign awareness.",
        "Transcendent" to "Absolute mastery of inner space; supreme stillness realized."
    )

    private val STUDY_TITLES = listOf(
        "Initiate" to "Opening the first page of intentional learning.",
        "Novice" to "Cultivating curiosity and systematic inquiry.",
        "Apprentice" to "Mastering foundational principles and focused cognitive endurance.",
        "Learner" to "Building mental models and connecting multidisciplinary insights.",
        "Seeker" to "Relentlessly pursuing depth beyond superficial facts.",
        "Scholar" to "Synthesizing complex knowledge into clear, actionable comprehension.",
        "Disciple" to "Rigorous intellectual discipline and deep work immersion.",
        "Sage" to "Intuitive understanding that cuts directly to first principles.",
        "Knowledge Keeper" to "Preserving, structuring, and refining vast domains of mastery.",
        "Master" to "Fluid cognitive agility; creating original insights and novel concepts.",
        "Grandmaster" to "Architecting comprehensive systems of thought with effortless precision.",
        "Illuminated Scholar" to "Illuminating obscure truths with profound clarity and insight.",
        "Visionary" to "Synthesizing timeless wisdom to anticipate future frontiers.",
        "Omniscient" to "The pinnacle of intellectual mastery; complete cognitive transcendence."
    )

    private val WORKOUT_TITLES = listOf(
        "Initiate" to "The first repetition on the path of physical transformation.",
        "Iron Beginner" to "Establishing foundational movement patterns and grit.",
        "Disciplined" to "Showing up without hesitation regardless of motivation.",
        "Ironbound" to "Forging resilient sinew, tendon strength, and unyielding posture.",
        "Warrior" to "Embracing voluntary hardship and pushing through discomfort.",
        "Battle Forged" to "Tested against heavy resistance and sculpted by recovery.",
        "Vanguard" to "Leading the standard of unrelenting physical output and vigor.",
        "Titan" to "Commanding immense physical power and explosive capacity.",
        "Warlord" to "Total mastery over muscular endurance, stamina, and heart rate.",
        "Apex" to "Reaching elite kinetic harmony between power and mobility.",
        "Colossus" to "An immovable pillar of physical discipline and endurance.",
        "Ascendant" to "Transcendence of bodily fatigue through invincible willpower.",
        "Living Legend" to "A living monument of supreme athletic discipline and raw power."
    )

    private val SLEEP_TITLES = listOf(
        "Initiate" to "Acknowledging sleep as the cornerstone of human optimization.",
        "Restless" to "Taming nocturnal restlessness and nighttime overstimulation.",
        "Rest Seeker" to "Establishing consistent pre-sleep rituals and darkness hygiene.",
        "Balanced Sleeper" to "Syncing the body clock to the natural circadian cadence.",
        "Rhythm Keeper" to "Maintaining strict bedtime and wake-up consistency.",
        "Deep Restorer" to "Maximizing slow-wave physical repair and nervous system recovery.",
        "Night Disciplined" to "Treating the sleep window as a sacred non-negotiable duty.",
        "Sleep Guardian" to "Shielding sleep architecture against blue light and late stress.",
        "Circadian Master" to "Flawless hormonal synchronization and effortless waking vitality.",
        "Rest Sage" to "Entering deep restorative states with instant serenity.",
        "Dream Walker" to "Unlocking vivid REM memory consolidation and mental clarity.",
        "Lunar Guardian" to "Harmonizing nocturnal recovery with daytime peak performance.",
        "Eternal Rest" to "Supreme cellular regeneration and perpetual biological renewal.",
        "Perfect Rhythm" to "The absolute harmony of biological rhythm; pristine circadian mastery."
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

    fun getLevelTitle(activityId: String, level: Int): Pair<String, String> {
        val clampedLevel = level.coerceIn(0, 13)
        val list = when (activityId.lowercase()) {
            "meditation" -> MEDITATION_TITLES
            "study" -> STUDY_TITLES
            "workout" -> WORKOUT_TITLES
            "sleep" -> SLEEP_TITLES
            else -> CUSTOM_TITLES
        }
        return list.getOrElse(clampedLevel) { list.last() }
    }

    fun getAllLevelsForActivity(activityId: String): List<LevelInfo> {
        return (1..13).map { level ->
            val (title, desc) = getLevelTitle(activityId, level)
            LevelInfo(
                level = level,
                title = title,
                xpThreshold = LEVEL_THRESHOLDS[level],
                description = desc,
                activityId = activityId
            )
        }
    }

    /**
     * Calculates current level from total accumulated XP.
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
     * Calculates comprehensive progression state including progress % to next level.
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

        // Protected floor is the XP threshold of (currentLevel - 1)
        val protectedFloor = if (currentLevel <= 1) 0 else LEVEL_THRESHOLDS[currentLevel - 1]

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
     * Applies soft decay with 1-level protected floor.
     * Default decay rate: 50 XP per missed day.
     */
    fun applySoftDecay(accumulatedXp: Int, highestLevelReached: Int, inactiveDays: Int, decayRatePerDay: Int = 50): Int {
        if (inactiveDays <= 0) return accumulatedXp
        val protectedFloor = if (highestLevelReached <= 1) 0 else LEVEL_THRESHOLDS[highestLevelReached - 1]
        val totalDecay = inactiveDays * decayRatePerDay
        return max(protectedFloor, accumulatedXp - totalDecay)
    }
}
