package com.monasoftware.pascher.data.remote

/**
 * Hand-verified mapping of TMDB movie IDs to confirmed public-domain
 * Archive.org identifiers. Only add entries here after manually verifying
 * copyright status (e.g. via Wikipedia's public-domain film lists, or
 * expired-copyright databases like the NYPL/Stanford copyright renewal records).
 */
object PublicDomainCatalog {
    data class Entry(val tmdbId: Int, val archiveIdentifier: String, val title: String)

    val entries = listOf(
        Entry(tmdbId = 3082, archiveIdentifier = "night_of_the_living_dead", title = "Night of the Living Dead (1968)"),
        Entry(tmdbId = 933, archiveIdentifier = "HisGirlFriday1940", title = "His Girl Friday (1940)"),
        Entry(tmdbId = 964, archiveIdentifier = "TheGeneral1926", title = "The General (1926)"),
        Entry(tmdbId = 11599, archiveIdentifier = "ReeferMadness1936", title = "Reefer Madness (1936)"),
        Entry(tmdbId = 33521, archiveIdentifier = "Metropolis1927", title = "Metropolis (1927)"),
        // Add more only after individually verifying public-domain status
    )

    fun archiveIdFor(tmdbId: Int): String? = entries.find { it.tmdbId == tmdbId }?.archiveIdentifier
}