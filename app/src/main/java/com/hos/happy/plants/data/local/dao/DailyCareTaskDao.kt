package com.hos.happy.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hos.happy.plants.data.local.entity.DailyCareTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCareTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyCareTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyCareTaskEntity)

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey ORDER BY priority ASC, urgencyDays DESC, createdAt ASC")
    fun getTasksForDateFlow(profileId: String, dateKey: String): Flow<List<DailyCareTaskEntity>>

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey AND isCompleted = 0 AND isSkipped = 0 ORDER BY priority ASC, urgencyDays DESC, createdAt ASC")
    fun getPendingTasksForDateFlow(profileId: String, dateKey: String): Flow<List<DailyCareTaskEntity>>

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey AND isCompleted = 1 ORDER BY completedAt DESC, createdAt DESC")
    fun getCompletedTasksForDateFlow(profileId: String, dateKey: String): Flow<List<DailyCareTaskEntity>>

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey AND isCompleted = 0 ORDER BY priority ASC, urgencyDays DESC, createdAt ASC")
    fun getIncompleteTasksForDateFlow(profileId: String, dateKey: String): Flow<List<DailyCareTaskEntity>>

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey")
    suspend fun getTasksForDate(profileId: String, dateKey: String): List<DailyCareTaskEntity>

    @Query("SELECT * FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey AND isCompleted = 0 AND isSkipped = 0 ORDER BY priority ASC")
    suspend fun getPendingTasksForDate(profileId: String, dateKey: String): List<DailyCareTaskEntity>

    @Query("SELECT COUNT(*) FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey")
    suspend fun getTaskCountForDate(profileId: String, dateKey: String): Int

    @Query("SELECT COUNT(*) FROM daily_care_tasks WHERE profileId = :profileId AND (isCompleted = 1 OR isSkipped = 1)")
    fun getCompletedOrSkippedCountFlow(profileId: String): Flow<Int>

    @Query("UPDATE daily_care_tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE daily_care_tasks SET isCompleted = 0, completedAt = NULL WHERE id = :taskId")
    suspend fun unmarkTaskCompleted(taskId: String)

    @Query("UPDATE daily_care_tasks SET isSkipped = 1 WHERE id = :taskId")
    suspend fun markTaskSkipped(taskId: String)

    @Query("UPDATE daily_care_tasks SET isCompleted = 1, completedAt = :completedAt WHERE profileId = :profileId AND dateKey = :dateKey AND taskType = :taskType AND spaceName = :spaceName AND isCompleted = 0")
    suspend fun markAllCompletedForSpace(
        profileId: String,
        dateKey: String,
        taskType: String,
        spaceName: String,
        completedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE daily_care_tasks SET isCompleted = 1, completedAt = :completedAt WHERE profileId = :profileId AND dateKey = :dateKey AND taskType = 'SNAPSHOT' AND (LOWER(targetId) = LOWER(:roomTag) OR LOWER(spaceName) = LOWER(:roomTag)) AND isCompleted = 0")
    suspend fun markSnapshotTaskCompleted(
        profileId: String,
        dateKey: String,
        roomTag: String,
        completedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE daily_care_tasks SET isCompleted = 1, completedAt = :completedAt WHERE profileId = :profileId AND dateKey = :dateKey AND taskType = 'GARDEN_PHOTO' AND isCompleted = 0")
    suspend fun markGardenPhotoTaskCompleted(
        profileId: String,
        dateKey: String,
        completedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM daily_care_tasks WHERE profileId = :profileId AND dateKey = :dateKey AND isCompleted = 0")
    suspend fun deleteIncompleteTasksForDate(profileId: String, dateKey: String)

    @Query("DELETE FROM daily_care_tasks WHERE targetId = :targetId")
    suspend fun deleteTasksForTarget(targetId: String)
}
