package xyz.nowaha.chengetawildlife

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.room.Room
import kotlinx.coroutines.*
import xyz.nowaha.chengetawildlife.data.AppDatabase
import xyz.nowaha.chengetawildlife.data.SessionManager
import xyz.nowaha.chengetawildlife.data.http.APIClient
import xyz.nowaha.chengetawildlife.data.http.APIInterface
import xyz.nowaha.chengetawildlife.data.repos.Repositories.isNetworkAvailable
import xyz.nowaha.chengetawildlife.databinding.ActivityMainBinding
import xyz.nowaha.chengetawildlife.util.SoftInputUtils.hideSoftInput

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        var appDatabase: AppDatabase? = null
        val offlineMode = MutableLiveData(true)
        fun offlineModePrecise(context: Context) = !context.isNetworkAvailable()

        val hasLocationPermission = MutableLiveData(false)
    }

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment.findNavController()
    }

    private val topLevelDestinations = setOf(R.id.loginFragmentNav, R.id.eventMapFragment)

    @SuppressLint("MissingPermission")
    val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (permissions.getOrDefault(
                        android.Manifest.permission.ACCESS_FINE_LOCATION, false
                    ) || permissions.getOrDefault(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, false
                    )
                ) {
                    hasLocationPermission.postValue(true)
                } else {
                    hasLocationPermission.postValue(false)
                }
            }
        }

    @SuppressLint("MissingPermission")
    fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            hasLocationPermission.postValue(true)
        }

        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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

                navController.graph = graph
            } else {
                // Logged in

                lifecycleScope.launch(Dispatchers.IO) {
                    var sessionIsValid = false
                    if (offlineModePrecise(this@MainActivity)) {
                        sessionIsValid = true
                    } else {
                        try {
                            val req = APIClient.getAPIInterface().validateSession().execute()
                            sessionIsValid = req.body()?.success == true && req.body()?.valid == true
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                    if (sessionIsValid) {
                        withContext(Dispatchers.Main) {
                            hideSoftInput(binding.root)
                            graph.setStartDestination(R.id.eventMapFragment)
                            setupActionBarWithNavController()

                            navController.graph = graph
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@MainActivity,
                                "Your session expired. Please log in again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        SessionManager.logOut()
                    }
                }
            }
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

