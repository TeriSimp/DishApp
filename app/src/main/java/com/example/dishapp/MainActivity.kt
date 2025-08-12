package com.example.dishapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.dishapp.models.Data
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Data.load(this)

        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val navHostView = findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        ViewCompat.setOnApplyWindowInsetsListener(navHostView) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        val navController = navHostFragment?.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        if (navController != null && bottomNav != null) {
            NavigationUI.setupWithNavController(bottomNav, navController)

            val topLevel = setOf(R.id.listFragment, R.id.accountFragment)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                bottomNav.isVisible = destination.id in topLevel
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()
        return navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}
