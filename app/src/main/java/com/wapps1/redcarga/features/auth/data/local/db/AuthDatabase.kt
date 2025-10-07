package com.wapps1.redcarga.features.auth.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wapps1.redcarga.features.auth.data.local.dao.*
import com.wapps1.redcarga.features.auth.data.local.entities.*

@Database(
    entities = [
        AccountSnapshotEntity::class,
        SignupIntentEntity::class,
        PersonDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuthDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountSnapshotDao
    abstract fun intentDao(): SignupIntentDao
    abstract fun personDraftDao(): PersonDraftDao
}
