package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.InventoryRepository
import com.example.ui.InventoryViewModel
import com.example.ui.TerroirStockApp
import com.example.ui.theme.TerroirStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = InventoryRepository(database.inventoryDao())
        val viewModel = ViewModelProvider(
            this,
            InventoryViewModel.factory(repository)
        )[InventoryViewModel::class.java]

        setContent {
            TerroirStockTheme {
                TerroirStockApp(viewModel = viewModel)
            }
        }
    }
}
