package xyz.nowaha.chengetawildlife

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.room.Room
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.nowaha.chengetawildlife.data.AppDatabase
import xyz.nowaha.chengetawildlife.data.SessionManager
import xyz.nowaha.chengetawildlife.data.repos.Repositories.isNetworkAvailable
import xyz.nowaha.chengetawildlife.databinding.ActivityMainBinding
import xyz.nowaha.chengetawildlife.util.SoftInputUtils.hideSoftInput

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        var appDatabase: AppDatabase? = null
        val offlineMode = MutableLiveData(true)
        fun offlineModePrecise(context: Context) = !context.isNetworkAvailable()
    }

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment.findNavController()
    }

    private val topLevelDestinations = setOf(R.id.loginFragmentNav, R.id.eventMapFragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "chengeta-db")
                .fallbackToDestructiveMigration().build()
        }

        SessionManager.getCurrentSessionLiveData().observe(this) { session ->
            val graph = navController.navInflater.inflate(R.navigation.nav_graph_main)

            if (session == null) {
                // Not logged in
                hideSoftInput(binding.root)
                graph.setStartDestination(R.id.loginFragmentNav)
                setupActionBarWithNavController()
            } else {
                // Logged in
                hideSoftInput(binding.root)
                graph.setStartDestination(R.id.eventMapFragment)
                setupActionBarWithNavController()
            }

            navController.graph = graph
        }

        lifecycleScope.launch {
            while (true) {
                if (offlineModePrecise(this@MainActivity)) {
                    if (offlineMode.value == false) {
                        offlineMode.postValue(true)
                    }
                } else {
                    if (offlineMode.value == true) {
                        offlineMode.postValue(false)
                    }
                }
                delay(2500)
            }
        }

        offlineMode.observe(this) {
            if (it) {
                binding.offlineNotice.visibility = View.VISIBLE
            } else {
                binding.offlineNotice.visibility = View.GONE
            }
        }
    }

    private fun setupActionBarWithNavController() {
        setupActionBarWithNavController(navController, AppBarConfiguration(topLevelDestinations))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

