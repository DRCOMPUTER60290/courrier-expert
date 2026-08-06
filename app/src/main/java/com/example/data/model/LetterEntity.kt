package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "letters")
data class LetterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val senderName: String,
    val senderAddress: String,
    val senderZipCode: String,
    val senderCity: String,
    val senderPhone: String,
    val senderEmail: String,
    val recipientName: String,
    val recipientAddress: String,
    val recipientZipCode: String,
    val recipientCity: String,
    val cityDate: String,
    val subject: String,
    val referencesText: String = "",
    val body: String,
    val politeForm: String = "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées.",
    val status: String = "Brouillon", // Brouillon, Finalisé, Envoyé
    val dateCreated: Long = System.currentTimeMillis(),
    val lrarTrackingNumber: String = "",
    val deadlineDate: String = "",
    val signaturePath: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "",
    val address: String = "",
    val zipCode: String = "",
    val city: String = "",
    val phone: String = "",
    val email: String = "",
    val signatureSvg: String = ""
)

@Entity(tableName = "saved_recipients")
data class RecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String = "Autre",
    val address: String,
    val zipCode: String,
    val city: String
)
