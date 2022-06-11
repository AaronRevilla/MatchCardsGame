package revilla.aaron.showtime

import android.app.Application
import revilla.aaron.showtime.di.AppComponent

class MatchCardGameApplication: Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        //component = Dagger
    }
}