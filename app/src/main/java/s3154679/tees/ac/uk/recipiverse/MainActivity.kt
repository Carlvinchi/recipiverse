package s3154679.tees.ac.uk.recipiverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import s3154679.tees.ac.uk.recipiverse.screens.LoginScreen
import s3154679.tees.ac.uk.recipiverse.screens.SignUpScreen
import s3154679.tees.ac.uk.recipiverse.ui.theme.RecipiverseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipiverseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    LoginScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
