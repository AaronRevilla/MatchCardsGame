package revilla.aaron.showtime.di

import dagger.Component
import revilla.aaron.showtime.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
}