package com.example.artsyapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.artsyapp.ui.theme.ArtsyAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        HttpClientProvider.init(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color(0xFFE5ECFA).value.toInt()

        setContent {
            ArtsyAppTheme {
                val navController = rememberNavController()


                AppStartup(navController)
            }
        }
    }
}

@Composable
fun AppStartup(navController: androidx.navigation.NavHostController) {

    LaunchedEffect(Unit) {
        try {
            val me = AuthApiClient.create().getMe()
            Log.d("Logged in", me.toString())
            Log.d("Logged in user", "$me")
            UserSession.currentUser = me
        } catch (e: Exception) {
            Log.e("getMe error", "Exception in /me: ${e.localizedMessage}", e)
        }
    }

    Nav(navController = navController)
}