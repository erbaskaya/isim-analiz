package com.baskaya.isimanaliz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.baskaya.isimanaliz.ui.NameAnalysisApp
import com.baskaya.isimanaliz.ui.theme.IsimAnalizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IsimAnalizTheme {
                NameAnalysisApp()
            }
        }
    }
}
