package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentWithFolios
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.LineWithTranscription
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TranscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ManuscriptDao {

    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun getDocumentWithFolios(documentId: String): Flow<DocumentWithFolios?>

    @Query("SELECT * FROM folios WHERE documentId = :documentId ORDER BY folioNumber ASC")
    fun getFoliosForDocument(documentId: String): Flow<List<FolioEntity>>

    @Transaction
    @Query("SELECT * FROM lines WHERE folioId = :folioId ORDER BY lineNumber ASC")
    fun getLinesWithTranscriptions(folioId: String): Flow<List<LineWithTranscription>>

    @Query("SELECT * FROM transcriptions WHERE lineId = :lineId")
    suspend fun getTranscription(lineId: String): TranscriptionEntity?

    @Query("SELECT * FROM transcriptions WHERE lineId = :lineId")
    fun observeTranscription(lineId: String): Flow<TranscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolios(folios: List<FolioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<LineEntity>)

    @Query("SELECT * FROM lines WHERE folioId = :folioId ORDER BY lineNumber ASC")
    suspend fun getLinesForFolioOnce(folioId: String): List<LineEntity>

    // @Update (bukan insert-or-replace) SENGAJA dipakai untuk menata ulang nomor/bbox
    // baris yang SUDAH ADA: INSERT OR REPLACE di SQLite = delete lalu insert ulang di
    // belakang layar, dan itu akan memicu CASCADE DELETE ke tabel transcriptions
    // (foreign key LineEntity->TranscriptionEntity onDelete=CASCADE) — bisa menghapus
    // diam-diam transkripsi yang sudah diketik user setiap kali ada baris ditambah/
    // dihapus di folio yang sama. @Update murni UPDATE SQL, tidak memicu cascade itu.
    @Update
    suspend fun updateLines(lines: List<LineEntity>)

    @Query("DELETE FROM lines WHERE id = :lineId")
    suspend fun deleteLine(lineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTranscription(transcription: TranscriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToSyncQueue(syncQueueItem: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC LIMIT :count")
    suspend fun getPendingSyncQueue(count: Int = 10): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingSyncCount(): Flow<Int>

    @Query("DELETE FROM sync_queue WHERE lineId IN (:lineIds)")
    suspend fun deleteFromSyncQueue(lineIds: List<String>)

    @Query("UPDATE transcriptions SET syncedAt = :syncedAt WHERE lineId = :lineId")
    suspend fun markTranscriptionSynced(lineId: String, syncedAt: Long)

    @Query("UPDATE folios SET imageUrl = :imageUrl WHERE id = :folioId")
    suspend fun updateFolioImage(folioId: String, imageUrl: String)

    @Query("SELECT COUNT(*) FROM transcriptions WHERE status = 'completed'")
    fun getCompletedTranscriptionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lines")
    fun getTotalLinesCount(): Flow<Int>

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocument(documentId: String)
}
