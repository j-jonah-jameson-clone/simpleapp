package com.example.simpleapp.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simpleapp.algorithm.sortingAlgorithm



@Composable
fun ApplicationMainScreen(modifier: Modifier = Modifier) {
    var input by remember {mutableStateOf("")}
    var output by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { newValue ->
                input = newValue
                output = sortingAlgorithm(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Буква") },
        )

        OutlinedTextField(
            value = output,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Результат") },
            minLines = 2
        )

        Button(
            onClick = {
                output = sortingAlgorithm(input)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Отсортировать")
        }
    }
}
