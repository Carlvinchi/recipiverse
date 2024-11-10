package s3154679.tees.ac.uk.recipiverse.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import s3154679.tees.ac.uk.recipiverse.navigation.EditPostScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel


@Composable
fun CreatePostScreen(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel

) {
    val authState = authViewModel.authState.observeAsState()

    //navigate to login page if unauthenticated
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate(s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen)
            }
            else ->Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Post Page", fontSize = 32.sp)

        Text(text = "Email is: ${authViewModel.auth.currentUser?.email}")

        TextButton(
            onClick = {

                authViewModel.signout()
            }

        ) {
            Text(text="Sign Out")
        }

        Button(
            onClick = {
                navController.navigate(EditPostScreen)
            },
            colors = ButtonColors(
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
        ) {
            Text(text = "Go To Edit Create Post")
        }

        // show progress when state is loading
        if(authState.value == AuthState.Loading){
            CircularProgressIndicator()
        }

    }
}