package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.PostalCodeService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostalCodeCityInputGroup(
    zipCode: String,
    onZipCodeChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "postal_code"
) {
    var citySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingSuggestions by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }

    // Trigger lookup when zipCode changes
    LaunchedEffect(zipCode) {
        val clean = zipCode.trim().filter { it.isDigit() }
        if (clean.length == 5) {
            isLoadingSuggestions = true
            delay(150) // slight debounce
            val cities = PostalCodeService.fetchCities(clean)
            citySuggestions = cities
            isLoadingSuggestions = false

            if (cities.isNotEmpty()) {
                expandedMenu = true
            }

            // Auto-fill city if empty and exactly 1 city found
            if (city.isBlank() && cities.size == 1) {
                onCityChange(cities.first())
            }
        } else {
            citySuggestions = emptyList()
            isLoadingSuggestions = false
            expandedMenu = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zip Code Input - Fixed clean width (135dp) to avoid any layout squishing
            OutlinedTextField(
                value = zipCode,
                onValueChange = { newZip ->
                    if (newZip.length <= 5 && newZip.all { it.isDigit() }) {
                        onZipCodeChange(newZip)
                    }
                },
                label = { Text("Code Postal", maxLines = 1) },
                placeholder = { Text("60290", maxLines = 1) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.PinDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (isLoadingSuggestions) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .width(135.dp)
                    .testTag("${testTagPrefix}_zip_input")
            )

            // City Input with Zone de Liste Déroulante (ZDLM / Dropdown Menu) - Fills remaining width
            ExposedDropdownMenuBox(
                expanded = expandedMenu && citySuggestions.isNotEmpty(),
                onExpandedChange = { expandedMenu = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { newCity ->
                        onCityChange(newCity)
                        expandedMenu = true
                    },
                    label = { Text("Ville", maxLines = 1) },
                    placeholder = { Text("Sélectionnez...", maxLines = 1) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMenu && citySuggestions.isNotEmpty())
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                        .fillMaxWidth()
                        .testTag("${testTagPrefix}_city_input")
                )

                // Zone de Liste Déroulante (ZDLM)
                ExposedDropdownMenu(
                    expanded = expandedMenu && citySuggestions.isNotEmpty(),
                    onDismissRequest = { expandedMenu = false }
                ) {
                    citySuggestions.forEach { suggestion ->
                        val isSelected = city.equals(suggestion, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                onCityChange(suggestion)
                                expandedMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Horizontal Quick-Access Suggestion Chips below ZDLM
        AnimatedVisibility(
            visible = citySuggestions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = "Villes trouvées pour le CP $zipCode (${citySuggestions.size}) :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    citySuggestions.forEach { suggestion ->
                        val isSelected = city.equals(suggestion, ignoreCase = true)
                        AssistChip(
                            onClick = {
                                onCityChange(suggestion)
                                expandedMenu = false
                            },
                            label = {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationCity,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

