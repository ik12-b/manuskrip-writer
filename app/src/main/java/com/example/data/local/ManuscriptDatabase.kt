package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TranscriptionEntity

@Database(
    entities = [
        DocumentEntity::class,
        FolioEntity::class,
        LineEntity::class,
        TranscriptionEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ManuscriptDatabase : RoomDatabase() {

    abstract fun manuscriptDao(): ManuscriptDao

    companion object {
        @Volatile
        private var INSTANCE: ManuscriptDatabase? = null

        fun getInstance(context: Context): ManuscriptDatabase {
            // Database mulai KOSONG — tidak ada lagi auto-seed manuskrip contoh
            // (Ibn Sina, Sahih Bukhari, dst). Dokumen hanya muncul lewat alur
            // "Tambah Naskah" yang mengunggah foto asli (lihat AddDocumentDialog).
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ManuscriptDatabase::class.java,
                    "manuscribe_arab.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
