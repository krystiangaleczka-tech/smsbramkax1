package com.example.smsbramkax1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset

data class Country(
    val code: String,        // "+48"
    val name: String,        // "Polska"
    val flag: String,        // "🇵🇱"
    val phoneLength: Int,    // 9
    val placeholder: String   // "123456789"
)

val defaultCountries = listOf(
    Country("+48", "Polska", "🇵🇱", 9, "123456789"),
    Country("+49", "Niemcy", "🇩🇪", 10, "1234567890"),
    Country("+380", "Ukraina", "🇺🇦", 9, "123456789"),
    Country("+44", "Wielka Brytania", "🇬🇧", 10, "1234567890"),
    Country("+1", "USA", "🇺🇸", 10, "1234567890"),
    Country("+33", "Francja", "🇫🇷", 9, "123456789"),
    Country("+39", "Włochy", "🇮🇹", 9, "123456789"),
    Country("+34", "Hiszpania", "🇪🇸", 9, "123456789"),
    Country("+31", "Holandia", "🇳🇱", 9, "123456789"),
    Country("+43", "Austria", "🇦🇹", 9, "123456789"),
    Country("+41", "Szwajcaria", "🇨🇭", 9, "123456789"),
    Country("+46", "Szwecja", "🇸🇪", 9, "123456789"),
    Country("+47", "Norwegia", "🇳🇴", 8, "12345678"),
    Country("+358", "Finlandia", "🇫🇮", 9, "123456789"),
    Country("+45", "Dania", "🇩🇰", 8, "12345678"),
    Country("+351", "Portugalia", "🇵🇹", 9, "123456789"),
    Country("+30", "Grecja", "🇬🇷", 10, "1234567890"),
    Country("+90", "Turcja", "🇹🇷", 10, "1234567890"),
    Country("+7", "Rosja", "🇷🇺", 10, "1234567890"),
    Country("+420", "Czechy", "🇨🇿", 9, "123456789"),
    Country("+421", "Słowacja", "🇸🇰", 9, "123456789"),
    Country("+36", "Węgry", "🇭🇺", 9, "123456789"),
    Country("+40", "Rumunia", "🇷🇴", 9, "123456789"),
    Country("+359", "Bułgaria", "🇧🇬", 9, "123456789"),
    Country("+381", "Serbia", "🇷🇸", 9, "123456789"),
    Country("+385", "Chorwacja", "🇭🇷", 9, "123456789"),
    Country("+386", "Słowenia", "🇸🇮", 8, "12345678")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodePhoneField(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newValue ->
                // Allow only digits and limit to country's phone length
                val filteredValue = newValue.filter { it.isDigit() }
                    .take(selectedCountry.phoneLength)
                onPhoneNumberChanged(filteredValue)
            },
            label = { Text("Numer telefonu") },
            placeholder = { Text(selectedCountry.placeholder) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCountry.flag} ${selectedCountry.code}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Wybierz kraj",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 48.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            defaultCountries.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${country.flag} ${country.code}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onCountrySelected(country)
                        expanded = false
                    }
                )
            }
        }
    }
}