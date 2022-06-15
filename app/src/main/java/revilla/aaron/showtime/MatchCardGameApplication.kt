package revilla.aaron.showtime

import android.app.Application
import revilla.aaron.showtime.di.AppComponent
import revilla.aaron.showtime.di.AppModule
import revilla.aaron.showtime.di.DaggerAppComponent

class MatchCardGameApplication : Application() {

    lateinit var component: AppComponent

    /*
    * Initialize dagger for DI when the project loads
    * */
    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }
}