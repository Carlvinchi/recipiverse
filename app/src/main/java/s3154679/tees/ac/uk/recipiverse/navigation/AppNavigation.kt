package s3154679.tees.ac.uk.recipiverse.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import s3154679.tees.ac.uk.recipiverse.onboarding.OnboardingDisplayManager
import s3154679.tees.ac.uk.recipiverse.screens.CreatePostScreen
import s3154679.tees.ac.uk.recipiverse.screens.EditPostScreen
import s3154679.tees.ac.uk.recipiverse.screens.HomeScreen
import s3154679.tees.ac.uk.recipiverse.screens.LoginScreen
import s3154679.tees.ac.uk.recipiverse.screens.OnboardingScreen
import s3154679.tees.ac.uk.recipiverse.screens.PostDetailsScreen
import s3154679.tees.ac.uk.recipiverse.screens.ProfileScreen
import s3154679.tees.ac.uk.recipiverse.screens.SignUpScreen
import s3154679.tees.ac.uk.recipiverse.screens.TermsScreen
import s3154679.tees.ac.uk.recipiverse.screens.UserPostsScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.LocationViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    locationViewModel: LocationViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    //val userState = authViewModel.user.observeAsState()
    val context = LocalContext.current

    val navController = rememberNavController()

    val navBottomItems = listOf(
        BottomNavigationItems("Home", Icons.Default.Home),
        BottomNavigationItems("Create Post", Icons.Default.Add),
        BottomNavigationItems("User Posts", Icons.Default.Favorite),
        BottomNavigationItems("Profile", Icons.Default.Person),
        BottomNavigationItems("Logout", Icons.AutoMirrored.Filled.ExitToApp)
    )

    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }



    Scaffold(
        modifier = Modifier.fillMaxSize().padding(top = 20.dp),

        bottomBar = {
            if(authState.value == AuthState.Authenticated){
                NavigationBar {
                    navBottomItems.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                when (index) {
                                    0 -> {
                                        navController.navigate(HomeScreen){
                                           popUpTo(navController.graph.findStartDestination().id){inclusive = true}

                                        }
                                    }
                                    1 -> navController.navigate(CreatePostScreen)
                                    2 -> navController.navigate(UserPostsScreen)
                                    3 -> navController.navigate(ProfileScreen)
                                    4 -> {
                                        authViewModel.signout()
                                        navController.navigate(LoginScreen) {
                                            popUpTo(navController.graph.findStartDestination().id){inclusive = true}
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                            },
                            label = {
                                Text(text = navItem.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = if (OnboardingDisplayManager(context).isOnboardingFinished()) LoginScreen else OnboardingScreen
        ){

            composable<LoginScreen> {
                LoginScreen(modifier, navController, authViewModel)
            }

            composable<SignupScreen> {
                SignUpScreen(modifier, navController, authViewModel)
            }

            composable<HomeScreen> {
                HomeScreen(modifier, navController, authViewModel, cameraViewModel, locationViewModel)
            }

            composable<TermsScreen> {
                TermsScreen(navController)
            }

            composable<OnboardingScreen> {
                OnboardingScreen(navController = navController)
            }

            composable<ProfileScreen> {
                ProfileScreen(modifier, navController, authViewModel, cameraViewModel)

            }

            composable<CreatePostScreen> {
                CreatePostScreen(modifier, navController, authViewModel, locationViewModel, cameraViewModel)
            }

            composable<UserPostsScreen> {
                UserPostsScreen(modifier, navController, authViewModel)
            }

            composable<PostDetailsScreen> {
                val args = it.toRoute<PostDetailsScreen>()

                PostDetailsScreen(
                    modifier,
                    navController,
                    authViewModel,
                    args.postId,
                    args.postTitle,
                    args.postDescription,
                    args.postImage,
                    args.postVideo,
                    args.postLocLat,
                    args.postLocLng,
                    args.postDate,
                    args.userName,
                    args.userLocName
                )
            }

            composable<EditPostScreen> {
                EditPostScreen(modifier, navController, authViewModel)
            }

        }
    }
}