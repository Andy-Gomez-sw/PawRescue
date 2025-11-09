package com.refugio.pawrescue.ui.theme.main

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // ELIMINA O COMENTA ESTE MÉTODO, YA NO ES NECESARIO SI USAS @Inject EN EL REPOSITORIO
    // @Provides
    // @Singleton
    // fun provideDonacionesRepository(firestore: FirebaseFirestore): DonacionesRepository {
    //     return DonacionesRepository(firestore)
    // }
}