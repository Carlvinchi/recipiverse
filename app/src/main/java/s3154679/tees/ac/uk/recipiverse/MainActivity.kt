package s3154679.tees.ac.uk.recipiverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import s3154679.tees.ac.uk.recipiverse.navigation.AppNavigation
import s3154679.tees.ac.uk.recipiverse.ui.theme.RecipiverseTheme
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the splash screen
        installSplashScreen()


        enableEdgeToEdge()

        // Initialize the AuthViewModel
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            RecipiverseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
