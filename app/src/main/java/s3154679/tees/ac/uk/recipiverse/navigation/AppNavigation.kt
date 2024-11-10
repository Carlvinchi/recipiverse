package s3154679.tees.ac.uk.recipiverse.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingDisplayManager
import s3154679.tees.ac.uk.recipiverse.screens.HomeScreen
import s3154679.tees.ac.uk.recipiverse.screens.LoginScreen
import s3154679.tees.ac.uk.recipiverse.screens.OnboardingScreen
import s3154679.tees.ac.uk.recipiverse.screens.SignUpScreen
import s3154679.tees.ac.uk.recipiverse.screens.TermsScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if (OnboardingDisplayManager(context).isOnboardingFinished()) LoginScreen else OnboardingScreen){

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

        composable<OnboardingScreen> {
            OnboardingScreen(navController = navController)
        }
    }

}