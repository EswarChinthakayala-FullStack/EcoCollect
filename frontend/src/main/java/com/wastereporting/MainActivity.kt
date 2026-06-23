package com.wastereporting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.maplibre.android.MapLibre
import androidx.navigation.navOptions
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize MapLibre Native SDK
        MapLibre.getInstance(this)
        
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Dynamically set start destination based on Auth State
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)

        if (com.wastereporting.network.TokenManager.isLoggedIn()) {
            when (com.wastereporting.network.TokenManager.userRole) {
                "supervisor" -> graph.setStartDestination(R.id.supervisorDashboardFragment)
                "admin" -> graph.setStartDestination(R.id.adminDashboardFragment)
                else -> graph.setStartDestination(R.id.homeFragment)
            }
        } else {
            graph.setStartDestination(R.id.onboardingFragment)
        }
        navController.graph = graph

        // Setup bottom navigation bar
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Observe login session changes to force redirect if logged out (e.g. on 401 response)
        lifecycleScope.launch {
            com.wastereporting.network.TokenManager.isLoggedInFlow.collect { loggedIn ->
                if (!loggedIn) {
                    val currentDest = navController.currentDestination
                    if (currentDest != null) {
                        val isPublic = currentDest.id == R.id.onboardingFragment ||
                                       currentDest.id == R.id.welcomeBackFragment ||
                                       currentDest.id == R.id.adminLoginFragment ||
                                       currentDest.id == R.id.supervisorLoginFragment ||
                                       currentDest.id == R.id.createAccountFragment ||
                                       currentDest.id == R.id.forgotPasswordFragment ||
                                       currentDest.id == R.id.resetPasswordFragment ||
                                       currentDest.id == R.id.otpVerificationFragment
                        if (!isPublic) {
                            val redirectDest = when {
                                isAdminDestination(currentDest.id) -> R.id.adminLoginFragment
                                isSupervisorDestination(currentDest.id) -> R.id.supervisorLoginFragment
                                else -> R.id.welcomeBackFragment
                            }
                            if (currentDest.id != redirectDest) {
                                navController.navigate(redirectDest, null, navOptions {
                                    popUpTo(currentDest.id) { inclusive = true }
                                })
                            }
                        }
                    }
                }
            }
        }

        // Route Guarding and Bottom Nav visibility controller
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val loggedIn = com.wastereporting.network.TokenManager.isLoggedIn()
            val role = com.wastereporting.network.TokenManager.userRole

            val isPublic = destination.id == R.id.onboardingFragment ||
                           destination.id == R.id.welcomeBackFragment ||
                           destination.id == R.id.adminLoginFragment ||
                           destination.id == R.id.supervisorLoginFragment ||
                           destination.id == R.id.createAccountFragment ||
                           destination.id == R.id.forgotPasswordFragment ||
                           destination.id == R.id.resetPasswordFragment ||
                           destination.id == R.id.otpVerificationFragment

            if (!loggedIn) {
                if (!isPublic) {
                    val redirectDest = when {
                        isAdminDestination(destination.id) -> R.id.adminLoginFragment
                        isSupervisorDestination(destination.id) -> R.id.supervisorLoginFragment
                        else -> R.id.welcomeBackFragment
                    }
                    if (destination.id != redirectDest) {
                        navController.navigate(redirectDest, null, navOptions {
                            popUpTo(destination.id) { inclusive = true }
                        })
                        return@addOnDestinationChangedListener
                    }
                }
            } else {
                val isSignupProgress = destination.id == R.id.profileSetupFragment ||
                                       destination.id == R.id.enableLocationFragment ||
                                       destination.id == R.id.stayUpdatedFragment ||
                                       destination.id == R.id.allSetFragment

                if (isPublic && !isSignupProgress) {
                    val homeDest = when (role) {
                        "admin" -> R.id.adminDashboardFragment
                        "supervisor" -> R.id.supervisorDashboardFragment
                        else -> R.id.homeFragment
                    }
                    if (destination.id != homeDest) {
                        navController.navigate(homeDest, null, navOptions {
                            popUpTo(destination.id) { inclusive = true }
                        })
                        return@addOnDestinationChangedListener
                    }
                } else {
                    if (isAdminDestination(destination.id) && role != "admin") {
                        if (destination.id != R.id.adminLoginFragment) {
                            navController.navigate(R.id.adminLoginFragment, null, navOptions {
                                popUpTo(destination.id) { inclusive = true }
                            })
                            return@addOnDestinationChangedListener
                        }
                    } else if (isSupervisorDestination(destination.id) && role != "supervisor") {
                        if (destination.id != R.id.supervisorLoginFragment) {
                            navController.navigate(R.id.supervisorLoginFragment, null, navOptions {
                                popUpTo(destination.id) { inclusive = true }
                            })
                            return@addOnDestinationChangedListener
                        }
                    } else if (isCitizenDestination(destination.id) && (role == "admin" || role == "supervisor")) {
                        val homeDest = when (role) {
                            "admin" -> R.id.adminDashboardFragment
                            "supervisor" -> R.id.supervisorDashboardFragment
                            else -> R.id.homeFragment
                        }
                        if (destination.id != homeDest) {
                            navController.navigate(homeDest, null, navOptions {
                                popUpTo(destination.id) { inclusive = true }
                            })
                            return@addOnDestinationChangedListener
                        }
                    }
                }
            }

            // Set visibility of bottom navigation bar
            if (destination.id == R.id.homeFragment ||
                destination.id == R.id.historyFragment ||
                destination.id == R.id.reportFragment ||
                destination.id == R.id.profileFragment) {
                bottomNav.visibility = View.VISIBLE
            } else {
                bottomNav.visibility = View.GONE
            }
        }
    }

    private fun isAdminDestination(id: Int): Boolean {
        return id == R.id.adminDashboardFragment ||
               id == R.id.adminReportsFragment ||
               id == R.id.adminReportDetailsFragment ||
               id == R.id.adminSupervisorsFragment ||
               id == R.id.adminAddSupervisorFragment ||
               id == R.id.adminSupervisorDetailsFragment ||
               id == R.id.adminEditSupervisorFragment ||
               id == R.id.adminProfileFragment ||
               id == R.id.adminEditProfileFragment ||
               id == R.id.adminSettingsFragment
    }

    private fun isSupervisorDestination(id: Int): Boolean {
        return id == R.id.supervisorDashboardFragment ||
               id == R.id.supervisorPendingReportsFragment ||
               id == R.id.supervisorCompletedReportsFragment ||
               id == R.id.supervisorProfileFragment ||
               id == R.id.supervisorEditProfileFragment ||
               id == R.id.supervisorReportDetailsFragment ||
               id == R.id.supervisorSettingsFragment
    }

    private fun isCitizenDestination(id: Int): Boolean {
        val isPublic = id == R.id.onboardingFragment ||
                       id == R.id.welcomeBackFragment ||
                       id == R.id.adminLoginFragment ||
                       id == R.id.supervisorLoginFragment ||
                       id == R.id.createAccountFragment ||
                       id == R.id.forgotPasswordFragment ||
                       id == R.id.resetPasswordFragment ||
                       id == R.id.otpVerificationFragment
        return !isPublic && !isAdminDestination(id) && !isSupervisorDestination(id) && 
                id != R.id.notificationsFragment && id != R.id.aboutAppFragment
    }
}
