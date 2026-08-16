package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val repository: String,
    val datePeriod: String,
    val language: String,
    val scriptType: String, // e.g. "Naskh", "Kufic", "Maghrebi", "Thuluth", "Diwani", "Nastaliq"
    val totalFolios: Int,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "folios",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"])]
)
data class FolioEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val folioNumber: String, // e.g. "12r", "12v", "1a"
    val title: String,
    val imageUrl: String? = null,
    val totalLines: Int = 0
)

@Entity(
    tableName = "lines",
    foreignKeys = [
        ForeignKey(
            entity = FolioEntity::class,
            parentColumns = ["id"],
            childColumns = ["folioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folioId"]), Index(value = ["folioId", "lineNumber"])]
)
data class LineEntity(
    @PrimaryKey val id: String,
    val folioId: String,
    val lineNumber: Int,
    val originalScriptText: String, // Ancient manuscript line representation / calligraphy text
    val contextTranslation: String = "",
    val scriptStyle: String = "Naskh",
    val bboxTop: Float = 0f,
    val bboxLeft: Float = 0f,
    val bboxWidth: Float = 1f,
    val bboxHeight: Float = 0.1f,
    val imagePath: String? = null
)

@Entity(
    tableName = "transcriptions",
    foreignKeys = [
        ForeignKey(
            entity = LineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"]), Index(value = ["syncedAt"])]
)
data class TranscriptionEntity(
    @PrimaryKey val lineId: String,
    val text: String,
    val status: String = "draft", // "draft", "completed", "unclear", "annotated"
    val notes: String = "",
    val confidence: Float = 0.0f,
    val alternativeReadings: String = "", // Comma-separated or JSON list of alternative readings
    val annotator: String = "Transcriber",
    val htrResult: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)

@Entity(
    tableName = "sync_queue",
    indices = [Index(value = ["createdAt"])]
)
data class SyncQueueEntity(
    @PrimaryKey val lineId: String, // PK = lineId (bukan id auto-increment) supaya edit berulang pada baris yang sama otomatis dedupe lewat OnConflictStrategy.REPLACE, bukan menumpuk row baru
    val action: String, // "update", "delete"
    val payload: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

data class LineWithTranscription(
    @Embedded val line: LineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lineId"
    )
    val transcription: TranscriptionEntity?
)

data class DocumentWithFolios(
    @Embedded val document: DocumentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val folios: List<FolioEntity>
)
