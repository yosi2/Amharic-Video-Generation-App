package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ScriptRepository
import com.example.ui.screens.MainScriptCreatorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScriptViewModel
import com.example.ui.viewmodel.ScriptViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val scriptDao = database.scriptDao()
        val repository = ScriptRepository(scriptDao)
        
        // Setup ViewModel
        val factory = ScriptViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[ScriptViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScriptCreatorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

