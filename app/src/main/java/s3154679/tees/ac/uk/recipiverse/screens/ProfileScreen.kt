package s3154679.tees.ac.uk.recipiverse.screens

import android.Manifest
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import s3154679.tees.ac.uk.recipiverse.R
import s3154679.tees.ac.uk.recipiverse.navigation.HomeScreen
import s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen
import s3154679.tees.ac.uk.recipiverse.navigation.UserPostsScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader
import s3154679.tees.ac.uk.recipiverse.viewmodels.UploadState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel

) {

    //observe all states
    val authState = authViewModel.authState.observeAsState()
    val userState = authViewModel.user.observeAsState()
    val loaderState = authViewModel.loaderState.observeAsState()
    val profileImageUri by cameraViewModel.profileImageUri.collectAsState()
    val uploadState = cameraViewModel.uploadState.observeAsState()


    //navigate to login page if unauthenticated
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate(LoginScreen)
            }
            else ->Unit
        }
    }


    //context and scope variables
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    var name by remember {
        mutableStateOf(userState.value?.name?: "")
    }

    var email by remember {
        mutableStateOf(userState.value?.email?: "")
    }

    var isEmailError by remember {
        mutableStateOf(false)
    }


    // Request camera and microphone permissions
    val cameraPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    //initial uri of image
    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    // Take Photo launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { cameraViewModel.setProfileImageUri(it) }
        }

    }

    // Set up gallery launcher for choosing an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { cameraViewModel.setProfileImageUri(it) }
    }


    val profileImage = userState.value?. profileImageUrl.toString()
    var imageUrl = if (profileImage != "") profileImage else R.drawable.blob_3


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Page", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(30.dp))

        Box(

            modifier = Modifier
                .size(320.dp)
                .border(width = 2.dp, color = Color.White, shape = CircleShape)
                .background(Color.Gray, shape = CircleShape),

            ) {

            if (profileImageUri != null) {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(320.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(320.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    scope.launch {
                        // Request Camera permissions if it is not already granted
                        if (!cameraPermissionState.allPermissionsGranted) {
                            cameraPermissionState.launchMultiplePermissionRequest()
                        }
                        else {
                            val cameraFile = cameraViewModel.createImageFile(context)

                            cameraImageUri = cameraViewModel.getFileUri(context, cameraFile)

                            cameraImageUri?.let {

                                cameraLauncher.launch(it)
                            }
                        }
                    }
                },
                colors = ButtonColors(
                    containerColor = Color(0xFF00BFA6),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
            ) {
                Text(text = "Take Photo")
            }

            Button(
                onClick = {

                    scope.launch {
                        galleryLauncher.launch("image/*")
                    }

                },
                colors = ButtonColors(
                    containerColor = Color(0xFF00BFA6),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Choose Photo")
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        // show progress when state is loading
        if(uploadState.value == UploadState.Loading){
            CircularProgressIndicator()
        }


        Spacer(modifier = Modifier.height(40.dp))


        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text(text = "Name")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                scope.launch {
                    authViewModel.updateUserInFirestore(name, email)
                }

            },
            colors = ButtonColors(
                containerColor = Color(0xFF00BFA6),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
        ) {
            Text(text = "Save")
        }

        Spacer(modifier = Modifier.height(15.dp))

        // show progress when state is loading
        if(loaderState.value == Loader.Loading){
            CircularProgressIndicator()
        }

    }
}