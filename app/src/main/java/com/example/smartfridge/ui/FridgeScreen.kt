package com.example.smartfridge.ui

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun FridgeScreen(
    onNavigateToRecipe: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: FridgeViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )
    val uiState by viewModel.recipeUiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Upload or Take Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.ingredientsText,
                onValueChange = viewModel::onIngredientsTextChanged,
                label = { Text("Enter ingredients (e.g., milk, eggs)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.getRecipe() },
                enabled = uiState !is RecipeUiState.Loading
            ) {
                Text("Get Recipe")
            }

            when (val state = uiState) {
                is RecipeUiState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
                is RecipeUiState.Success -> {
                    LaunchedEffect(state) {
                        onNavigateToRecipe(state.recipe)
                        viewModel.resetUiState()
                    }
                }
                is RecipeUiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUiState()
                }
                else -> {}
            }
        }
    }
}
