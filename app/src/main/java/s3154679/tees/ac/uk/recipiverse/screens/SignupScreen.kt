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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import s3154679.tees.ac.uk.recipiverse.R
import s3154679.tees.ac.uk.recipiverse.navigation.HomeScreen
import s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen
import s3154679.tees.ac.uk.recipiverse.navigation.TermsScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader


@Composable
fun SignUpScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    //variables for text fields and signup form
    var name by remember {
        mutableStateOf("")
    }

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

    var isNameError by remember {
        mutableStateOf(false)
    }

    var isChecked by remember {
        mutableStateOf(false)
    }

    //we observe the authentication state
    val authState = authViewModel.authState.observeAsState()
    val loaderState = authViewModel.loaderState.observeAsState()


    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    //the code below ensures we navigate to homepage after account creation
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> navController.navigate(HomeScreen)
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RECIPIVERSE",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BFA6)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Image(
            painter = painterResource(id = R.drawable.tasting),
            contentDescription = "Sign up Image",
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Welcome!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(text = "Sign up for an account")
        Spacer(modifier = Modifier.height(5.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                isNameError = it.isEmpty()
            },
            label = {
                Text(text = "Name")
            },
            isError = isNameError,
            supportingText = {
                if (isNameError) {
                    Text(
                        text = "Name cannot be empty",
                        color = Color.Red
                    )
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Name Icon"
                )
            }
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it.trim()
                isEmailError = !Patterns.EMAIL_ADDRESS.matcher(it).matches() || it.isEmpty()
            },
            label = {
                Text(text = "Email")
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

        OutlinedTextField(
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

        Spacer(modifier = Modifier.height(2.dp))

        Row {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {isChecked = it}
            )

            //terms and conditions Text
            val termsText = buildAnnotatedString {
                append("I agree to the ")
                pushStringAnnotation(tag = "TERMS", annotation = "terms")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ){
                    append("Terms and Conditions")
                }
                pop()
            }

            Text(
                text = termsText,
                modifier = Modifier.padding(8.dp)
                    .clickable {
                        navController.navigate(TermsScreen)
                        //or navigate to terms page
                    }
            )
        }

        //Sign up button
        Button(
            onClick = {
                authViewModel.signup(email, password, name, scope)
            },
            colors = ButtonColors(
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
            enabled = isChecked

        ) {
            Text(text = "Sign Up")

        }

        // show progress when state is loading
        if(loaderState.value == Loader.Loading){
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Or sign up with",
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(8.dp))


        //navigate to login page
        Row {
            Text(text = "Already have an account? ")
            Text(
                text = "Login",
                modifier = Modifier
                    .clickable {
                        navController.navigate(LoginScreen)
                    },
                fontWeight = FontWeight.Bold
            )
        }


    }
}