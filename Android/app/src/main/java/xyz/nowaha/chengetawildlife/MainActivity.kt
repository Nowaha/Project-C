package xyz.nowaha.chengetawildlife

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import xyz.nowaha.chengetawildlife.databinding.ActivityMainBinding
import xyz.nowaha.chengetawildlife.util.SoftInputUtils.hideSoftInput

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

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

        SessionManager.initDatabase(applicationContext)
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
    }

    private fun setupActionBarWithNavController() {
        setupActionBarWithNavController(navController, AppBarConfiguration(topLevelDestinations))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

