package com.monasoftware.pascher.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.monasoftware.pascher.ui.navigation.NavKey

object LastRoute {
    var route: NavKey? = null
    var currentSessionId by mutableStateOf("")
}