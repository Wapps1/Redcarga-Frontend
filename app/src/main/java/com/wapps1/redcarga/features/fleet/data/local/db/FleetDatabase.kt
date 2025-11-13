package com.wapps1.redcarga.features.fleet.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wapps1.redcarga.features.fleet.data.local.dao.*
import com.wapps1.redcarga.features.fleet.data.local.entities.*

@Database(
    entities = [
        DriverEntity::class,
        VehicleEntity::class,
        RouteEntity::class,
        DepartmentEntity::class,
        ProvinceEntity::class,
        MetaEntity::class,
        PendingOperationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FleetDatabase : RoomDatabase() {
    abstract fun driversDao(): DriversDao
    abstract fun vehiclesDao(): VehiclesDao
    abstract fun routesDao(): RoutesDao
    abstract fun geoDao(): GeoDao
    abstract fun metaDao(): MetaDao
    abstract fun pendingOperationDao(): PendingOperationDao
}


