package com.example.data.repository

import com.example.data.local.LetterDao
import com.example.data.model.LetterEntity
import com.example.data.model.RecipientEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class LetterRepository(private val letterDao: LetterDao) {
    val allLetters: Flow<List<LetterEntity>> = letterDao.getAllLetters()
    val userProfile: Flow<UserProfileEntity?> = letterDao.getUserProfile()
    val allRecipients: Flow<List<RecipientEntity>> = letterDao.getAllRecipients()

    suspend fun getLetterById(id: Int): LetterEntity? = letterDao.getLetterById(id)

    suspend fun saveLetter(letter: LetterEntity): Long = letterDao.insertLetter(letter)

    suspend fun updateLetter(letter: LetterEntity) = letterDao.updateLetter(letter)

    suspend fun deleteLetter(id: Int) = letterDao.deleteLetterById(id)

    suspend fun getUserProfileOnce(): UserProfileEntity? = letterDao.getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfileEntity) = letterDao.saveUserProfile(profile)

    suspend fun saveRecipient(recipient: RecipientEntity): Long = letterDao.insertRecipient(recipient)

    suspend fun deleteRecipient(id: Int) = letterDao.deleteRecipientById(id)
}
