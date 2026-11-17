package com.android_ca3.lostandfoundv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android_ca3.lostandfoundv2.ui.components.FindlyTheme
import com.android_ca3.lostandfoundv2.ui.navigation.AppNavigator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindlyTheme {
                AppNavigator()
            }
        }
    }
}
