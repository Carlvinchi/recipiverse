package s3154679.tees.ac.uk.recipiverse.screens

import android.Manifest
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader
import s3154679.tees.ac.uk.recipiverse.viewmodels.UploadState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
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

    //context and scope variables
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    //navigate to login page if unauthenticated
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate(LoginScreen)
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else ->Unit
        }
    }


    //profile details variables
    var name by remember {
        mutableStateOf(userState.value?.name?: "")
    }

    var isNameError by remember { mutableStateOf(false) }


    var email by remember {
        mutableStateOf(userState.value?.email?: "")
    }

    var isEmailError by remember {
        mutableStateOf(false)
    }


    // Request camera permissions
    val cameraPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA
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


    //variables for displaying profile image
    val profileImage = userState.value?. profileImageUrl.toString()
    val imageUrl = if (profileImage != "") profileImage else R.drawable.blob_3


    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(

        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Profile Details", fontSize = 32.sp
                )
            }

        },
        containerColor = Color.White,
    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(

                modifier = Modifier
                    .size(320.dp)
                    .border(width = 2.dp, color = Color.White, shape = CircleShape)
                    .background(Color.Gray, shape = CircleShape),

                ) {

                // Display selected or camera taken profile image if user uploaded new one
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

                // Display default profile image if no image uploaded
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

                //take photo button
                Button(
                    modifier = Modifier.padding(end = 10.dp),
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

                //choose photo button
                Button(
                    modifier = Modifier.padding(start = 10.dp),
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

            // show progress when uploading new profile image
            if(uploadState.value == UploadState.Loading){
                CircularProgressIndicator()
            }


            Spacer(modifier = Modifier.height(40.dp))


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

            //update profile button
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
                enabled = !isNameError && !isEmailError
            ) {
                Text(text = "Update Profile")
            }

            Spacer(modifier = Modifier.height(15.dp))

            // show progress when updating profile
            if(loaderState.value == Loader.Loading){
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(15.dp))

            //update delete account button
            Button(
                onClick = {
                    showDeleteConfirmationDialog = true
                },
                colors = ButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
            ) {
                Text(text = "Delete Account")
            }


            //show confirmation dialog when delete button is clicked
            if (showDeleteConfirmationDialog) {

                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                cameraViewModel.deletePostsByUserId(scope)
                                authViewModel.deleteUserFromFirestore(scope)
                                showDeleteConfirmationDialog = false
                            }
                        ){
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirmationDialog = false }

                        ) {
                            Text("Cancel")
                        }

                    },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete your account and all related posts?") },
                )
            }

        }
    }

}
