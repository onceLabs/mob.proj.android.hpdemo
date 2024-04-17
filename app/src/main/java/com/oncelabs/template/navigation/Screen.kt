package com.oncelabs.template.navigation

/**
 * All possible routes & associated args should be defined here
 */
sealed class Screen(val route: String) {
    object HomeScreen : Screen("login")
}