package com.monasoftware.pascher.domain.model

enum class MovieCategory(val displayTitle: String, val genreSlugs: List<String>) {
    TRENDING("🔥 Trending", emptyList()),
    SCIENCE_FICTION("🚀 Science Fiction", listOf("scifi")),
    HORROR("👻 Horror", listOf("horror")),
    ROMANCE("❤️ Romance", listOf("romance")),
    DRAMA("🎭 Drama", listOf("drama")),
    THRILLER("🕵️ Thriller", listOf("thriller")),
    ACTION_SUPERHERO("🦸 Action", listOf("action", "adventure")),
    COMEDY("😂 Comedy", listOf("comedy")),
    ANIMATION("🎬 Animation", listOf("animated")),
    OTHER("🎞 Other", emptyList()) // catch-all, uses changes list
}