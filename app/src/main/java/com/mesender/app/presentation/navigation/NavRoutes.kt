package com.mesender.app.presentation.navigation

object NavRoutes {
    const val HOME = "home"
    const val THREAD = "thread/{inboxId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun thread(inboxId: Long): String = "thread/$inboxId"
}