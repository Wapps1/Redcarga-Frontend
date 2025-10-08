package com.wapps1.redcarga.features.fleet.data.di

import android.content.Context
import androidx.room.Room
import com.wapps1.redcarga.features.fleet.data.local.db.FleetDatabase
import com.wapps1.redcarga.features.fleet.data.remote.services.*
import com.wapps1.redcarga.features.fleet.domain.repositories.*
import com.wapps1.redcarga.features.fleet.data.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FleetDataModule {

    @Provides @Singleton
    fun provideFleetDriversService(@Named("backend") r: Retrofit): FleetDriversService =
        r.create(FleetDriversService::class.java)

    @Provides @Singleton
    fun provideFleetVehiclesService(@Named("backend") r: Retrofit): FleetVehiclesService =
        r.create(FleetVehiclesService::class.java)

    @Provides @Singleton
    fun providePlanningRoutesService(@Named("backend") r: Retrofit): PlanningRoutesService =
        r.create(PlanningRoutesService::class.java)

    @Provides @Singleton
    fun provideGeoService(@Named("backend") r: Retrofit): GeoService =
        r.create(GeoService::class.java)

    @Provides @Singleton
    fun provideFleetDatabase(@ApplicationContext context: Context): FleetDatabase =
        Room.databaseBuilder(context, FleetDatabase::class.java, "fleet.db").build()

    @Provides fun provideDriversDao(db: FleetDatabase) = db.driversDao()
    @Provides fun provideVehiclesDao(db: FleetDatabase) = db.vehiclesDao()
    @Provides fun provideRoutesDao(db: FleetDatabase) = db.routesDao()
    @Provides fun provideGeoDao(db: FleetDatabase) = db.geoDao()
    @Provides fun provideMetaDao(db: FleetDatabase) = db.metaDao()
    @Provides fun providePendingOpsDao(db: FleetDatabase) = db.pendingOperationDao()

    @Provides @Singleton
    fun bindFleetDriversRepository(impl: FleetDriversRepositoryImpl): FleetDriversRepository = impl

    @Provides @Singleton
    fun bindFleetVehiclesRepository(impl: FleetVehiclesRepositoryImpl): FleetVehiclesRepository = impl

    @Provides @Singleton
    fun bindPlanningRoutesRepository(impl: PlanningRoutesRepositoryImpl): PlanningRoutesRepository = impl

    @Provides @Singleton
    fun bindGeoRepository(impl: GeoRepositoryImpl): GeoRepository = impl
}


