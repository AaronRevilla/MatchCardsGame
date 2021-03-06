package revilla.aaron.showtime

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import revilla.aaron.showtime.customviews.CustomCardView
import revilla.aaron.showtime.databinding.ActivityMainBinding
import revilla.aaron.showtime.repositories.CardsRepository
import revilla.aaron.showtime.repositories.GameScoreRepository
import revilla.aaron.showtime.viewmodels.MainActivityModelFactory
import revilla.aaron.showtime.viewmodels.MainActivityViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity(), GameBoardAdapter.ItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    @Inject
    lateinit var cardsRepository: CardsRepository

    @Inject
    lateinit var gameScoreRepository: GameScoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as MatchCardGameApplication).component.inject(this)

        //initialize view model
        viewModel = ViewModelProvider(
            this,
            MainActivityModelFactory(cardsRepository, gameScoreRepository)
        ).get(
            MainActivityViewModel::class.java
        )

        //initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //Setup the Recycler View
        setupRecyclerView()

        //Initialize observers
        viewModel.loadingObserver.observe(this, Observer { isLoading ->
            isLoading?.let {
                showLoadingScreen(it)
            } ?: kotlin.run { showLoadingScreen(false) }
        })

        viewModel.cardBoardObserver.observe(this, Observer {
            binding.gameBoardRv.adapter =
                GameBoardAdapter(it, clickListener = this)
        })

        viewModel.gameObserver.observe(this, Observer {
            binding.winsValue.text = "${it.wins}"
            binding.flipsValue.text = "${it.flips}"
        })

        viewModel.cardObserver.observe(this, Observer { responseList ->
            for (currentCard in responseList) {
                val position = currentCard.first
                val card = currentCard.second
                val viewHolder = binding.gameBoardRv.findViewHolderForAdapterPosition(position)
                Handler(Looper.getMainLooper()).postDelayed({
                    viewHolder?.let {
                        if(!card.isFrontSideUp) {
                            (it.itemView as CustomCardView).flipCard()
                        }
                        if(card.hasFoundThePair)
                            it.itemView.setOnClickListener(null)
                    }
                }, 700)
            }
        })

        viewModel.messagesObservable.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        viewModel.gameIsOverObserver.observe(this, Observer { isOver ->
            if(isOver) {
                throwKonfetti()
            }
        })

        binding.playAgain.setOnClickListener {
            if(viewModel.gameIsOverObserver.value ?: false)
                viewModel.newGame()
            else
                Toast.makeText(this, "Please finish the current game before starting a new one.", Toast.LENGTH_LONG).show()
        }
    }

    /*
    * When the app goes to the background the game will be save it automatically
    * When the user resumes the app or open after being killed
    * it will load the latest state of the game
    * */
    override fun onPause() {
        super.onPause()
        viewModel.saveGame()
    }

    /*
    * Set up recycler view
    * */
    private fun setupRecyclerView() {
        binding.gameBoardRv.adapter =
            GameBoardAdapter(viewModel.cardBoardObserver.value ?: listOf(), clickListener = this)
        binding.gameBoardRv.layoutManager = GridLayoutManager(this, viewModel.getGridNumber(), RecyclerView.VERTICAL, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /*
    * Method to block the main screen while the game is loading
    * */
    private fun showLoadingScreen(show: Boolean) {
        if (show)
            binding.loaderIndicator.visibility = View.VISIBLE
        else
            binding.loaderIndicator.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_reset_game -> {
                viewModel.resetGame()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        val cardView = view as? CustomCardView
        cardView?.flipCard()
        viewModel.cardFliped(position, Looper.getMainLooper())
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    private fun isInternetAvailable(): Boolean {
        var result = false
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }

    private fun throwKonfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 2000, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
        binding.konfetti.start(party)
    }
}