package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BookRepository
import com.example.ui.BookyApp
import com.example.ui.BookyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = BookRepository(applicationContext)

        setContent {
            val viewModel: BookyViewModel = viewModel(
                factory = BookyViewModelFactory(repository, applicationContext)
            )
            BookyApp(viewModel = viewModel)
        }
    }
}

class BookyViewModelFactory(
    private val repository: BookRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookyViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
