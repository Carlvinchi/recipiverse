package s3154679.tees.ac.uk.recipiverse.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import s3154679.tees.ac.uk.recipiverse.screens.HomeScreen
import s3154679.tees.ac.uk.recipiverse.screens.LoginScreen
import s3154679.tees.ac.uk.recipiverse.screens.SignUpScreen
import s3154679.tees.ac.uk.recipiverse.screens.TermsScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = LoginScreen) {


        composable<LoginScreen> {
            LoginScreen(modifier, navController, authViewModel)
        }

        composable<SignupScreen> {
            SignUpScreen(modifier, navController, authViewModel)
        }

        composable<HomeScreen> {
            HomeScreen(modifier, navController, authViewModel)
        }

        composable<TermsScreen> {
            TermsScreen(navController)
        }
    }
}