package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hos.happy.plants.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles ORDER BY lastLoginAt DESC")
    fun getAllProfilesFlow(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY lastLoginAt DESC")
    suspend fun getAllProfiles(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getProfileByEmail(email: String): UserProfileEntity?

    @Query("UPDATE user_profiles SET lastLoginAt = :timestamp WHERE id = :profileId")
    suspend fun updateLastLogin(profileId: String, timestamp: Long = System.currentTimeMillis())
}
