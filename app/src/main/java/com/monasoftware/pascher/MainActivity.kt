package com.monasoftware.pascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monasoftware.pascher.ui.navigation.NavApp
import com.monasoftware.pascher.ui.theme.PasCherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasCherTheme {
                NavApp()
            }
        }
    }
}
