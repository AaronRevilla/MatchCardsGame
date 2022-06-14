package revilla.aaron.showtime

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
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
import revilla.aaron.showtime.customviews.CustomCardView
import revilla.aaron.showtime.databinding.ActivityMainBinding
import revilla.aaron.showtime.repositories.CardsRepository
import revilla.aaron.showtime.repositories.GameScoreRepository
import revilla.aaron.showtime.viewmodels.MainActivityModelFactory
import revilla.aaron.showtime.viewmodels.MainActivityViewModel
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
            (binding.gameBoardRv.adapter as? GameBoardAdapter)?.updateCardList(it)
        })

        viewModel.gameObserver.observe(this, Observer {
            binding.winsValue.text = "${it.wins}"
            binding.flipsValue.text = "${it.flips}"
        })

        viewModel.cardObserver.observe(this, Observer { responseList ->
            for (currentCard in responseList) {
                val position = currentCard.first
                val card = currentCard.second
                Log.d("Aaron flip method", "$position - hasFoundThePair=${card.hasFoundThePair}, isFrontSideUp=${card.isFrontSideUp} : ${card.imgURL}")
                val viewHolder = binding.gameBoardRv.findViewHolderForAdapterPosition(position)
                runOnUiThread {
                    viewHolder?.let {
                        if(!card.isFrontSideUp) {
                            (it.itemView as CustomCardView).flipCard()
                            binding.gameBoardRv.scheduleLayoutAnimation()
                        }
                        if(card.hasFoundThePair)
                            it.itemView.setOnClickListener(null)
                    }
                }
            }
        })

        viewModel.messagesObservable.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        binding.playAgain.isEnabled = false
        binding.playAgain.setOnClickListener {
            viewModel.playAgain()
        }
    }

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
            R.id.action_settings -> true
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
}