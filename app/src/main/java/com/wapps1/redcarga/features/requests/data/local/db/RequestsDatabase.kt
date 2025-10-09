package com.wapps1.redcarga.features.requests.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao
import com.wapps1.redcarga.features.requests.data.local.entities.*

@Database(
    entities = [
        RequestEntity::class,
        RequestItemEntity::class,
        RequestImageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters()
abstract class RequestsDatabase : RoomDatabase() {
    abstract fun requestsDao(): RequestsDao
    
    companion object {
        @Volatile
        private var INSTANCE: RequestsDatabase? = null
        
        fun getDatabase(context: Context): RequestsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RequestsDatabase::class.java,
                    "requests_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
