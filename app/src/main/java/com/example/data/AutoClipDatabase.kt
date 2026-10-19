package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoClipDao {
    @Query("SELECT * FROM autoclip_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<VideoProjectEntity>>

    @Query("SELECT * FROM autoclip_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: Long): VideoProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProjectEntity): Long

    @Update
    suspend fun updateProject(project: VideoProjectEntity)

    @Query("DELETE FROM autoclip_projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)

    @Query("SELECT * FROM autoclip_segments WHERE projectId = :projectId ORDER BY finalScore DESC")
    fun getSegmentsForProject(projectId: Long): Flow<List<ClipSegmentEntity>>

    @Query("SELECT * FROM autoclip_segments ORDER BY finalScore DESC")
    fun getAllSegments(): Flow<List<ClipSegmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<ClipSegmentEntity>): List<Long>

    @Update
    suspend fun updateSegment(segment: ClipSegmentEntity)

    @Query("DELETE FROM autoclip_segments WHERE projectId = :projectId")
    suspend fun deleteSegmentsForProject(projectId: Long)

    @Query("SELECT * FROM autoclip_collections WHERE projectId = :projectId ORDER BY avgViralScore DESC")
    fun getCollectionsForProject(projectId: Long): Flow<List<ClipCollectionEntity>>

    @Query("SELECT * FROM autoclip_collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<ClipCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollections(collections: List<ClipCollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: ClipCollectionEntity): Long

    @Query("DELETE FROM autoclip_collections WHERE projectId = :projectId")
    suspend fun deleteCollectionsForProject(projectId: Long)

    @Query("DELETE FROM autoclip_collections WHERE id = :collectionId")
    suspend fun deleteCollectionById(collectionId: Long)
}

@Database(
    entities = [
        VideoProjectEntity::class,
        ClipSegmentEntity::class,
        ClipCollectionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AutoClipDatabase : RoomDatabase() {
    abstract fun dao(): AutoClipDao

    companion object {
        @Volatile
        private var INSTANCE: AutoClipDatabase? = null

        fun getInstance(context: Context): AutoClipDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AutoClipDatabase::class.java,
                    "autoclip_studio.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
