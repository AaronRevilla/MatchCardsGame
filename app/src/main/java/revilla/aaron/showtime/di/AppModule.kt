package revilla.aaron.showtime.di

import android.content.Context
import dagger.Module
import dagger.Provides
import revilla.aaron.showtime.MatchCardGameApplication
import revilla.aaron.showtime.datasources.CardsImagesDS
import revilla.aaron.showtime.network.RestAPIImplementation
import revilla.aaron.showtime.network.RestAPI
import revilla.aaron.showtime.repositories.CardsRepository
import javax.inject.Singleton

@Module
open class AppModule(private val application: MatchCardGameApplication) {

    @Provides
    @Singleton
    open fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    open fun provideRestApiCall() : RestAPI {
        return RestAPIImplementation()
    }

    @Provides
    @Singleton
    open fun provideCardsDS() : CardsImagesDS {
        return CardsImagesDS(provideRestApiCall())
    }

    @Provides
    @Singleton
    open fun provideCardsRepository() : CardsRepository {
        return CardsRepository(provideCardsDS())
    }


}