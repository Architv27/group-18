package com.google.mediapipe.examples.fluenthands

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.fluenthands.databinding.ActivityBottomNavBinding

class BottomNavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Find the navigation controller associated with the nav host fragment
        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_nav)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_reults, R.id.navigation_settings, R.id.navigation_help
            )
        )
        // Set up the toolbar defined in the binding as the action bar
        setSupportActionBar(binding.toolbar)

        // Set up the action bar with the navigation controller and the appBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect the BottomNavigationView with the NavController
        navView.setupWithNavController(navController)
    }
}