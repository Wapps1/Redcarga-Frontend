package com.wapps1.redcarga.features.requests.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.wapps1.redcarga.features.requests.data.local.dao.RequestsDao
import com.wapps1.redcarga.features.requests.data.local.dao.IncomingRequestsDao
import com.wapps1.redcarga.features.requests.data.local.entities.*

@Database(
    entities = [
        RequestEntity::class,
        RequestItemEntity::class,
        RequestImageEntity::class,
        IncomingRequestEntity::class
    ],
    version = 2, // Incrementar versión por nueva entidad
    exportSchema = false
)
@TypeConverters()
abstract class RequestsDatabase : RoomDatabase() {
    abstract fun requestsDao(): RequestsDao
    abstract fun incomingRequestsDao(): IncomingRequestsDao

    companion object {
        @Volatile
        private var INSTANCE: RequestsDatabase? = null

        fun getDatabase(context: Context): RequestsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RequestsDatabase::class.java,
                    "requests_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true) // Por el cambio de versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
