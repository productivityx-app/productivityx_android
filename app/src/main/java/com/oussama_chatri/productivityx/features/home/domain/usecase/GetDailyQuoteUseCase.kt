package com.oussama_chatri.productivityx.features.home.domain.usecase

import com.oussama_chatri.productivityx.features.home.domain.model.DashboardSummary
import javax.inject.Inject

class GetDailyQuoteUseCase @Inject constructor() {
    private val quotes = listOf(
        "The secret of getting ahead is getting started." to "Mark Twain",
        "Don't watch the clock; do what it does. Keep going." to "Sam Levenson",
        "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese Proverb",
        "You don't have to be great to start, but you have to start to be great." to "Zig Ziglar",
        "Small daily improvements over time lead to stunning results." to "Robin Sharma",
        "Focus on being productive instead of busy." to "Tim Ferriss",
        "The key is not to prioritize what's on your schedule, but to schedule your priorities." to "Stephen Covey",
        "It's not about having time. It's about making time." to "Anonymous",
        "Success is the sum of small efforts repeated day in and day out." to "Robert Collier",
        "The only way to do great work is to love what you do." to "Steve Jobs",
        "Your focus determines your reality." to "Qui-Gon Jinn",
        "Do the hard jobs first. The easy jobs will take care of themselves." to "Dale Carnegie",
        "Either you run the day, or the day runs you." to "Jim Rohn",
        "The most difficult thing is the decision to act, the rest is merely tenacity." to "Amelia Earhart",
        "Start where you are. Use what you have. Do what you can." to "Arthur Ashe",
    )

    operator fun invoke(summary: DashboardSummary): DashboardSummary {
        val dayIndex = java.time.LocalDate.now().dayOfYear
        val quote = quotes[dayIndex % quotes.size]
        return summary.copy(
            dailyQuote = quote.first,
            dailyQuoteAuthor = quote.second,
        )
    }
}
