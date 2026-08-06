package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LetterEntity
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusFinalized
import com.example.ui.theme.StatusSent
import com.example.ui.viewmodel.LetterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField

@Composable
fun MyLettersScreen(
    viewModel: LetterViewModel,
    onEditLetter: (LetterEntity) -> Unit,
    onNavigateToNewLetter: () -> Unit
) {
    val context = LocalContext.current
    val savedLetters by viewModel.savedLetters.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var historySearchQuery by remember { mutableStateOf("") }
    val tabTitles = listOf("Tous les courriers", "Brouillons", "Finalisés", "Suivi LRAR")

    var letterToDelete by remember { mutableStateOf<LetterEntity?>(null) }
    var trackingLetterToEdit by remember { mutableStateOf<LetterEntity?>(null) }
    var trackingNumberInput by remember { mutableStateOf("") }

    val filteredLetters = savedLetters.filter { letter ->
        val matchesTab = when (selectedTabIndex) {
            1 -> letter.status == "Brouillon"
            2 -> letter.status == "Finalisé"
            3 -> letter.lrarTrackingNumber.isNotEmpty()
            else -> true
        }
        val matchesSearch = historySearchQuery.isBlank() ||
                letter.title.contains(historySearchQuery, ignoreCase = true) ||
                letter.recipientName.contains(historySearchQuery, ignoreCase = true) ||
                letter.subject.contains(historySearchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    val draftCount = savedLetters.count { it.status == "Brouillon" }
    val finalizedCount = savedLetters.count { it.status == "Finalisé" }
    val lrarCount = savedLetters.count { it.lrarTrackingNumber.isNotEmpty() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Dark Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historique des Courriers",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${savedLetters.size} courrier(s) archivé(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleDarkTheme() },
                    modifier = Modifier.testTag("dark_mode_toggle_history")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Basculer Mode Sombre",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // History Stats Summary Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${savedLetters.size}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$draftCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Brouillons",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$lrarCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Suivis LRAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Search Bar for History
            OutlinedTextField(
                value = historySearchQuery,
                onValueChange = { historySearchQuery = it },
                placeholder = { Text("Rechercher dans l'historique...") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (historySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { historySearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("history_search_input")
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_my_letters_$index")
                    )
                }
            }

            // List
            if (filteredLetters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (historySearchQuery.isNotEmpty()) "Aucun résultat pour '$historySearchQuery'." else "Aucun courrier dans cet historique.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToNewLetter,
                            modifier = Modifier.testTag("empty_state_create_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Créer un nouveau courrier")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLetters, key = { it.id }) { letter ->
                        SavedLetterCard(
                            letter = letter,
                            onEdit = {
                                viewModel.loadLetterForEditing(letter)
                                onEditLetter(letter)
                            },
                            onExportPdf = {
                                viewModel.exportAndShareLetterPdf(context, letter)
                            },
                            onSendEmail = {
                                viewModel.sendEmailLetterPdf(context, letter)
                            },
                            onDuplicate = {
                                viewModel.duplicateLetter(letter)
                            },
                            onAddTracking = {
                                trackingLetterToEdit = letter
                                trackingNumberInput = letter.lrarTrackingNumber
                            },
                            onDelete = { letterToDelete = letter }
                        )
                    }
                }
            }
        }

        // FAB to add new letter
        FloatingActionButton(
            onClick = onNavigateToNewLetter,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("fab_create_new_letter"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nouveau courrier")
        }
    }

    // Delete Confirmation Dialog
    letterToDelete?.let { letter ->
        AlertDialog(
            onDismissRequest = { letterToDelete = null },
            title = { Text("Supprimer le courrier") },
            text = { Text("Voulez-vous vraiment supprimer '${letter.title}' ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLetter(letter.id)
                        letterToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { letterToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // LRAR Tracking Dialog
    trackingLetterToEdit?.let { letter ->
        AlertDialog(
            onDismissRequest = { trackingLetterToEdit = null },
            title = { Text("Numéro de Suivi LRAR") },
            text = {
                Column {
                    Text("Ajoutez le numéro de suivi de la lettre recommandée avec accusé de réception (La Poste) :")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = trackingNumberInput,
                        onValueChange = { trackingNumberInput = it },
                        label = { Text("N° de suivi (ex: 1A09876543210)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = letter.copy(
                            lrarTrackingNumber = trackingNumberInput,
                            status = if (trackingNumberInput.isNotEmpty()) "Envoyé" else letter.status
                        )
                        viewModel.updateLetterDirectly(updated)
                        trackingLetterToEdit = null
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackingLetterToEdit = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SavedLetterCard(
    letter: LetterEntity,
    onEdit: () -> Unit,
    onExportPdf: () -> Unit,
    onSendEmail: () -> Unit,
    onDuplicate: () -> Unit,
    onAddTracking: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(letter.dateCreated))
    val statusColor = when (letter.status) {
        "Finalisé" -> StatusFinalized
        "Envoyé" -> StatusSent
        else -> StatusDraft
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_letter_card_${letter.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = letter.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = letter.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (letter.recipientName.isNotEmpty()) {
                Text(
                    text = "Destinataire : ${letter.recipientName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (letter.lrarTrackingNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = "LRAR",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Suivi LRAR : ${letter.lrarTrackingNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Éditer", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onSendEmail) {
                        Icon(Icons.Default.Email, contentDescription = "Envoyer par E-mail", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onExportPdf) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Partager PDF", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Dupliquer", tint = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = onAddTracking) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Ajouter Suivi", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
