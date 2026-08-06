package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LetterEntity
import com.example.data.model.RecipientEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LetterDao {
    // Letters
    @Query("SELECT * FROM letters ORDER BY dateCreated DESC")
    fun getAllLetters(): Flow<List<LetterEntity>>

    @Query("SELECT * FROM letters WHERE id = :id")
    suspend fun getLetterById(id: Int): LetterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LetterEntity): Long

    @Update
    suspend fun updateLetter(letter: LetterEntity)

    @Query("DELETE FROM letters WHERE id = :id")
    suspend fun deleteLetterById(id: Int)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Saved Recipients
    @Query("SELECT * FROM saved_recipients ORDER BY name ASC")
    fun getAllRecipients(): Flow<List<RecipientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: RecipientEntity): Long

    @Query("DELETE FROM saved_recipients WHERE id = :id")
    suspend fun deleteRecipientById(id: Int)
}
