package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Share
import com.example.R
import com.example.ui.components.PostalCodeCityInputGroup
import com.example.utils.PostalCodeService
import com.example.ui.viewmodel.LetterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterEditorScreen(
    viewModel: LetterViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLetter by viewModel.currentLetter.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Édition Formulaire", "Aperçu AFNOR", "Booster IA")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1785323456846),
                            contentDescription = "Courrier Expert Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentLetter.title.ifEmpty { "Courrier" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Courrier", "${currentLetter.subject}\n\n${currentLetter.body}\n\n${currentLetter.politeForm}")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Texte copié dans le presse-papier !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_text_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copier")
                    }

                    IconButton(
                        onClick = { viewModel.exportAndSharePdf(context) },
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Exporter PDF", tint = MaterialTheme.colorScheme.secondary)
                    }

                    Button(
                        onClick = {
                            viewModel.saveCurrentLetter { id ->
                                Toast.makeText(context, "Courrier sauvegardé !", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_letter_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sauvegarder", style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when (index) {
                                    0 -> Icons.Default.Edit
                                    1 -> Icons.Default.Description
                                    else -> Icons.Default.AutoAwesome
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        modifier = Modifier.testTag("editor_tab_$index")
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> EditorFormTab(
                    letter = currentLetter,
                    userProfile = userProfile,
                    onUpdate = { updated -> viewModel.updateCurrentLetter(updated) }
                )
                1 -> AfnorPreviewTab(
                    letter = currentLetter,
                    onExportPdf = { viewModel.exportAndSharePdf(context) },
                    onSendEmailPdf = { viewModel.sendEmailPdf(context) }
                )
                2 -> AiBoosterTab(
                    isGenerating = isAiGenerating,
                    onRewrite = { instruction ->
                        viewModel.rewriteTextWithAi(instruction) {
                            Toast.makeText(context, "Texte amélioré par l'IA !", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EditorFormTab(
    letter: com.example.data.model.LetterEntity,
    userProfile: com.example.data.model.UserProfileEntity?,
    onUpdate: (com.example.data.model.LetterEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Expéditeur
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Vos coordonnées (Expéditeur)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (userProfile != null) {
                        TextButton(
                            onClick = {
                                onUpdate(
                                    letter.copy(
                                        senderName = userProfile.fullName,
                                        senderAddress = userProfile.address,
                                        senderZipCode = userProfile.zipCode,
                                        senderCity = userProfile.city,
                                        senderPhone = userProfile.phone,
                                        senderEmail = userProfile.email
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-remplir profil", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.senderName,
                    onValueChange = { onUpdate(letter.copy(senderName = it)) },
                    label = { Text("Nom & Prénom") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sender_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.senderAddress,
                    onValueChange = { onUpdate(letter.copy(senderAddress = it)) },
                    label = { Text("Adresse postale") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PostalCodeCityInputGroup(
                    zipCode = letter.senderZipCode,
                    onZipCodeChange = { newZip -> onUpdate(letter.copy(senderZipCode = newZip)) },
                    city = letter.senderCity,
                    onCityChange = { newCity ->
                        val updatedCityDate = if (letter.cityDate.isBlank() || letter.cityDate.startsWith("Fait à")) {
                            PostalCodeService.formatCityDate(newCity)
                        } else {
                            letter.cityDate
                        }
                        onUpdate(letter.copy(senderCity = newCity, cityDate = updatedCityDate))
                    },
                    testTagPrefix = "editor_sender"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = letter.senderPhone,
                        onValueChange = { onUpdate(letter.copy(senderPhone = it)) },
                        label = { Text("Téléphone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = letter.senderEmail,
                        onValueChange = { onUpdate(letter.copy(senderEmail = it)) },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        // Section Destinataire
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Destinataire",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.recipientName,
                    onValueChange = { onUpdate(letter.copy(recipientName = it)) },
                    label = { Text("Nom de l'organisme / Société / Destinataire") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipient_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.recipientAddress,
                    onValueChange = { onUpdate(letter.copy(recipientAddress = it)) },
                    label = { Text("Adresse du destinataire") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PostalCodeCityInputGroup(
                    zipCode = letter.recipientZipCode,
                    onZipCodeChange = { newZip -> onUpdate(letter.copy(recipientZipCode = newZip)) },
                    city = letter.recipientCity,
                    onCityChange = { newCity -> onUpdate(letter.copy(recipientCity = newCity)) },
                    testTagPrefix = "editor_recipient"
                )
            }
        }

        // Section Document (Lieu, Objet, Réf)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Entête & Objet du courrier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.cityDate,
                    onValueChange = { onUpdate(letter.copy(cityDate = it)) },
                    label = { Text("Lieu et date (ex: Fait à Paris, le 29 Juillet 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.subject,
                    onValueChange = { onUpdate(letter.copy(subject = it)) },
                    label = { Text("Objet de la lettre") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("letter_subject_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.referencesText,
                    onValueChange = { onUpdate(letter.copy(referencesText = it)) },
                    label = { Text("Références / Pièces jointes (Facultatif)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section Corps du Texte
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Contenu du courrier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = letter.body,
                    onValueChange = { onUpdate(letter.copy(body = it)) },
                    label = { Text("Corps de la lettre (Paragraphes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("letter_body_input"),
                    maxLines = 15
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = letter.politeForm,
                    onValueChange = { onUpdate(letter.copy(politeForm = it)) },
                    label = { Text("Formule de politesse finale") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun AfnorPreviewTab(
    letter: com.example.data.model.LetterEntity,
    onExportPdf: () -> Unit,
    onSendEmailPdf: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aperçu de mise en page (Norme AFNOR NF Z 11-001)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Top Quick Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExportPdf,
                modifier = Modifier
                    .weight(1f)
                    .testTag("afnor_top_export_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Partager PDF", fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Button(
                onClick = onSendEmailPdf,
                modifier = Modifier
                    .weight(1f)
                    .testTag("afnor_top_email_pdf_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Envoyer Email", fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // Paper Canvas Sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(4.dp)),
            color = Color.White,
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                // Top Row: Sender (Left) & Recipient (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sender Block
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = letter.senderName.ifEmpty { "[Nom Expéditeur]" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (letter.senderAddress.isNotEmpty()) {
                            Text(text = letter.senderAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        val zipCity = "${letter.senderZipCode} ${letter.senderCity}".trim()
                        if (zipCity.isNotEmpty()) {
                            Text(text = zipCity, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        if (letter.senderPhone.isNotEmpty()) {
                            Text(text = "Tél: ${letter.senderPhone}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Recipient Block (Top Right)
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(top = 20.dp)
                    ) {
                        Text(
                            text = letter.recipientName.ifEmpty { "[Nom Destinataire]" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        if (letter.recipientAddress.isNotEmpty()) {
                            Text(text = letter.recipientAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        val recZipCity = "${letter.recipientZipCode} ${letter.recipientCity}".trim()
                        if (recZipCity.isNotEmpty()) {
                            Text(text = recZipCity, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Date Block (Right)
                Text(
                    text = letter.cityDate.ifEmpty { "Fait à ..., le ..." },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Objet
                Text(
                    text = "Objet : ${letter.subject}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )

                if (letter.referencesText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Réf : ${letter.referencesText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Body Text Paragraphs
                Text(
                    text = letter.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Formule de Politesse
                Text(
                    text = letter.politeForm,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Signature Block (Right)
                Column(modifier = Modifier.align(Alignment.End)) {
                    Text(
                        text = "Signature :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = letter.senderName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Share & Action Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Options d'Envoi & Partage Rapide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exportez votre courrier au format vectoriel PDF haute qualité et transmettez-le directement par mail ou vers vos applications installées.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSendEmailPdf,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("afnor_bottom_email_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Envoyer par Email", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onExportPdf,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("afnor_bottom_share_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Autre Partage", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AiBoosterTab(
    isGenerating: Boolean,
    onRewrite: (String) -> Unit
) {
    val options = listOf(
        "Rendre le style plus formel et administratif",
        "Ajouter des références légales et articles de loi français",
        "Rendre la lettre plus ferme (Mise en demeure)",
        "Corriger la grammaire et l'orthographe",
        "Raccourcir et aller à l'essentiel"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Booster IA & Correction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Améliorez instantanément le texte avec l'intelligence artificielle", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGenerating) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Réécriture par l'IA en cours...")
                }
            }
        } else {
            options.forEach { option ->
                Card(
                    onClick = { onRewrite(option) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
