package com.example.domain.engine

import java.time.LocalDate

data class Quote(
    val quote: String,
    val author: String,
    val tradition: String,
    val theme: String // "mountains", "sunrise", "stillness", "warrior", "cosmos", "temple", "forest"
)

object MotivationProvider {

    private val QUOTES = listOf(
        Quote(
            quote = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            author = "Marcus Aurelius",
            tradition = "Stoicism",
            theme = "mountains"
        ),
        Quote(
            quote = "He who conquers himself is the mightiest warrior.",
            author = "Confucius",
            tradition = "Eastern Philosophy",
            theme = "warrior"
        ),
        Quote(
            quote = "Yoga is the stilling of the fluctuations of the mind. Then the witness abides in its own true nature.",
            author = "Patanjali",
            tradition = "Yoga Sutras",
            theme = "stillness"
        ),
        Quote(
            quote = "Perform your duty equipoised, abandoning all attachment to success or failure. Such equanimity is called Yoga.",
            author = "Bhagavad Gita",
            tradition = "Vedanta",
            theme = "sunrise"
        ),
        Quote(
            quote = "We suffer more often in imagination than in reality.",
            author = "Seneca",
            tradition = "Stoicism",
            theme = "forest"
        ),
        Quote(
            quote = "Silence is the place where knowledge is born.",
            author = "Lao Tzu",
            tradition = "Taoism",
            theme = "stillness"
        ),
        Quote(
            quote = "There is nothing outside of yourself that can ever enable you to get better, stronger, richer, quicker, or smarter. Everything is within.",
            author = "Miyamoto Musashi",
            tradition = "The Book of Five Rings",
            theme = "warrior"
        ),
        Quote(
            quote = "No man is free who is not master of himself.",
            author = "Epictetus",
            tradition = "Stoicism",
            theme = "mountains"
        ),
        Quote(
            quote = "Arise, awake, and stop not until the goal is reached.",
            author = "Swami Vivekananda",
            tradition = "Vedanta",
            theme = "sunrise"
        ),
        Quote(
            quote = "Quiet the mind, and the soul will speak.",
            author = "Jaya Ram",
            tradition = "Mindfulness",
            theme = "cosmos"
        ),
        Quote(
            quote = "Do not seek to follow in the footsteps of the wise; seek what they sought.",
            author = "Matsuo Basho",
            tradition = "Zen",
            theme = "forest"
        ),
        Quote(
            quote = "The soul becomes dyed with the color of its thoughts.",
            author = "Marcus Aurelius",
            tradition = "Stoicism",
            theme = "cosmos"
        ),
        Quote(
            quote = "To a mind that is still, the whole universe surrenders.",
            author = "Zhuangzi",
            tradition = "Taoism",
            theme = "stillness"
        ),
        Quote(
            quote = "A disciplined mind brings true happiness and enduring sovereignty.",
            author = "The Dhammapada",
            tradition = "Buddhism",
            theme = "temple"
        ),
        Quote(
            quote = "When you arise in the morning, think of what a precious privilege it is to be alive—to breathe, to think, to enjoy, to love.",
            author = "Marcus Aurelius",
            tradition = "Stoicism",
            theme = "sunrise"
        ),
        Quote(
            quote = "The impediment to action advances action. What stands in the way becomes the way.",
            author = "Marcus Aurelius",
            tradition = "Stoicism",
            theme = "mountains"
        ),
        Quote(
            quote = "Mastery is not a function of genius or luck, but the result of time and intense focus applied to any field.",
            author = "Robert Greene",
            tradition = "Self-Mastery",
            theme = "temple"
        ),
        Quote(
            quote = "In the depth of winter, I finally learned that within me there lay an invincible summer.",
            author = "Albert Camus",
            tradition = "Existentialism",
            theme = "mountains"
        ),
        Quote(
            quote = "Purity of mind, resolute effort, and self-restraint lead to supreme freedom.",
            author = "Upanishads",
            tradition = "Vedanta",
            theme = "cosmos"
        ),
        Quote(
            quote = "Today not tomorrow. If you fail today, you fail tomorrow. Live each day as if it is your magnum opus.",
            author = "Miyamoto Musashi",
            tradition = "Bushido",
            theme = "warrior"
        )
    )

    fun getDailyQuote(date: LocalDate = LocalDate.now()): Quote {
        val dayOfYear = date.dayOfYear
        val index = (dayOfYear + date.year) % QUOTES.size
        return QUOTES[index]
    }
}
