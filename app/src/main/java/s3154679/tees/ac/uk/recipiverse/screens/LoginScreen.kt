package s3154679.tees.ac.uk.recipiverse.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import s3154679.tees.ac.uk.recipiverse.R
import s3154679.tees.ac.uk.recipiverse.navigation.HomeScreen
import s3154679.tees.ac.uk.recipiverse.navigation.SignupScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader


@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    //login form variables

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var isError by remember {
        mutableStateOf(false)
    }

    var isEmailError by remember {
        mutableStateOf(false)
    }

    //observe the authentication and load state
    val authState = authViewModel.authState.observeAsState()
    val loaderState = authViewModel.loaderState.observeAsState()

    //context and scope variables
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    //ensures we navigate to home page when authenticated
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate(HomeScreen)
            }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else ->Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RECIPIVERSE",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFA6)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.street_food),
            contentDescription = "Login Image",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Welcome Back",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black

        )


        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            textStyle = TextStyle(Color.Black),
            value = email,
            onValueChange = {
                email = it.trim()
                isEmailError = !Patterns.EMAIL_ADDRESS.matcher(it).matches() || it.isEmpty()
            },
            label = {
                Text(
                    text = "Email"
                )
            },
            isError = isEmailError,
            supportingText = {
                if (isEmailError) {
                    Text(
                        text = "Invalid Email",
                        color = Color.Red
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Email Icon"
                )
            }
        )

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            textStyle = TextStyle(Color.Black),
            value = password,
            onValueChange = {
                password = it
                isError = it.length < 8 || it.isEmpty()
            },
            label = {
                Text(text = "Password")
            },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = "Password must be at least 8 characters",
                        color = Color.Red
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Password Icon"
                )
            },
            visualTransformation = PasswordVisualTransformation()
        )

        Text(
            text = "Forgot Password?",
            modifier = Modifier
                .padding(end = 15.dp)
                .align(Alignment.End)
                .clickable {  },
            fontWeight = FontWeight.Bold,
            color = Color.Black

        )

        Spacer(modifier = Modifier.height(8.dp))

        //login button
        Button(
            onClick = {
                authViewModel.login(email, password, scope)
            },
            colors = ButtonColors(
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF00BFA6),
                disabledContentColor = Color.White
            )

        ) {
            Text(text = "Login")

        }

        // show progress when state is loading
        if(loaderState.value == Loader.Loading){
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Or sign in with",
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(15.dp))


        //google sign in button
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Google Icon",
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    authViewModel.signInWithGoogle(context, scope)
                }

        )

        Spacer(modifier = Modifier.height(10.dp))


        //navigate to sign up screen when clicked
        Row {
            Text(
                text = "Don't have an account? ",
                color = Color.Black
            )
            Text(
                text = "Sign Up",
                modifier = Modifier
                    .clickable {
                        navController.navigate(SignupScreen)

                    },
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }


    }
}