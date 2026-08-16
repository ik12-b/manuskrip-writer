package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DocumentEntity
import com.example.data.model.FolioEntity
import com.example.data.model.LineEntity
import com.example.data.model.SyncQueueEntity
import com.example.data.model.TranscriptionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ManuscriptDatabase::class.java,
                    "manuscribe_arab.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed initial historical manuscripts asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).manuscriptDao()
                                dao.insertDocuments(InitialManuscriptData.sampleDocuments)
                                dao.insertFolios(InitialManuscriptData.sampleFolios)
                                dao.insertLines(InitialManuscriptData.sampleLines)
                                InitialManuscriptData.sampleTranscriptions.forEach {
                                    dao.saveTranscription(it)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
