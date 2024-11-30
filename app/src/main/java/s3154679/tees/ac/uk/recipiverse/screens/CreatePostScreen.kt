package s3154679.tees.ac.uk.recipiverse.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import s3154679.tees.ac.uk.recipiverse.navigation.HomeScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader
import s3154679.tees.ac.uk.recipiverse.viewmodels.LocationViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.UploadState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CreatePostScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel,
    cameraViewModel: CameraViewModel

) {

    //context and coroutine scope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    //observe all states from viewmodel
    val loaderState = locationViewModel.loaderState.observeAsState()
    val uploadState = cameraViewModel.uploadState.observeAsState()
    val userState = authViewModel.user.observeAsState()


    //observe image media uri and video media uri
    val imageMediaUri by cameraViewModel.imageMediaUri.collectAsState()
    val videoMediaUri by cameraViewModel.videoMediaUri.collectAsState()


    //show some message when post creation is triggered
    LaunchedEffect(uploadState.value) {
        when(uploadState.value) {
            is UploadState.Uploaded -> {
                Toast.makeText(context, (uploadState.value as UploadState.Uploaded).message, Toast.LENGTH_SHORT).show()
                navController.navigate(HomeScreen)
            }
            is UploadState.Error -> {
                Toast.makeText(context, (uploadState.value as UploadState.Error).message, Toast.LENGTH_LONG).show()
            }
            else ->Unit
        }
    }


    //location related variables
    val placesList = remember { mutableStateOf<List<Place>>(emptyList()) }
    val location = locationViewModel.selectedLocation.collectAsStateWithLifecycle().value
    var selectedOption by remember { mutableStateOf("") }
    var showLocation by remember { mutableStateOf(false) }


    //location access permissions
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )


    // Request camera and microphone permissions for recording video
    val cameraPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )



    // Image and video related variables
    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var cameraVideoUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var showImageHolder by remember { mutableStateOf(false) }
    var showVideoHolder by remember { mutableStateOf(false) }


    // Take Photo launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { cameraViewModel.setImageMediaUri(it) }
        }

    }

    // Record Video launcher
    val recordVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            cameraVideoUri?.let { cameraViewModel.setVideoMediaUri(it) }
        }

    }

    // Set up gallery launcher for choosing an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { cameraViewModel.setImageMediaUri(it) }
    }


    // Set up video gallery launcher for choosing a video from the gallery
    val videoGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { cameraViewModel.setVideoMediaUri(it) }
    }


    //post related variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    val categoriesList = listOf(
        "AFRICAN",
        "ASIAN",
        "EUROPEAN",
        "AMERICAN"
    )

    //for controlling the post creation button
    var disableButton by remember { mutableStateOf(false) }


    Scaffold(

        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Post", fontSize = 32.sp
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

            OutlinedTextField(
                modifier = Modifier.padding(8.dp),
                value = title,
                onValueChange = {
                    title = it
                },
                label = {
                    Text(text = "Title")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SelectCategory(
                options = categoriesList,
                selectedOption = selectedCategory,
                onOptionSelected = { selectedCategory = it }
            )


            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                modifier = Modifier.padding(8.dp).height(80.dp),
                value = description,
                onValueChange = {
                    description = it
                },
                label = {
                    Text(text = "Description")
                }
            )

            Spacer(modifier = Modifier.height(10.dp))


            //Start for  camera and gallery image
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                //take photo with camera button
                Button(
                    modifier = Modifier.padding(end = 10.dp),
                    onClick = {

                        // Request Camera permissions if it is not already granted
                        if (!cameraPermissionState.allPermissionsGranted) {
                            cameraPermissionState.launchMultiplePermissionRequest()
                            val cameraFile = cameraViewModel.createImageFile(context)

                            cameraImageUri = cameraViewModel.getFileUri(context, cameraFile)

                            cameraImageUri?.let {

                                cameraLauncher.launch(it)
                            }
                            showImageHolder = true
                        }
                        else {
                            val cameraFile = cameraViewModel.createImageFile(context)

                            cameraImageUri = cameraViewModel.getFileUri(context, cameraFile)

                            cameraImageUri?.let {

                                cameraLauncher.launch(it)
                            }

                            showImageHolder = true
                        }

                    },
                ) {

                    Text(text = "Take Photo")


                }


                //choose from gallery button
                Button(
                    modifier = Modifier.padding(start = 10.dp),
                    onClick = {
                        galleryLauncher.launch("image/*")
                        showImageHolder = true
                    },
                ) {

                    Text(text = "Choose Photo")

                }

            }

            Spacer(modifier = Modifier.height(10.dp))


            //show image if picture is taken or selected from gallery
            if(showImageHolder){

                imageMediaUri?.let { uri ->

                    DisplayImage(imageUri = uri)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            //end for camera and gallery image



            //Start for  camera and gallery video
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                //record video with camera button
                Button(
                    modifier = Modifier.padding(end = 10.dp),
                    onClick = {

                        // Request Camera permissions if it is not already granted
                        if (!cameraPermissionState.allPermissionsGranted) {
                            cameraPermissionState.launchMultiplePermissionRequest()
                            val videoFile = cameraViewModel.createVideoFile(context)

                            cameraVideoUri = cameraViewModel.getFileUri(context, videoFile)

                            cameraVideoUri?.let {

                                recordVideoLauncher.launch(it)
                            }
                            showVideoHolder = true
                        }
                        else {
                            val videoFile = cameraViewModel.createVideoFile(context)

                            cameraVideoUri = cameraViewModel.getFileUri(context, videoFile)

                            cameraVideoUri?.let {

                                recordVideoLauncher.launch(it)
                            }

                            showVideoHolder = true
                        }

                    },
                ) {

                    Text(text = "Record Video")

                }

                //choose video from gallery button
                Button(
                    modifier = Modifier.padding(start = 10.dp),
                    onClick = {
                        videoGalleryLauncher.launch("video/*")
                        showVideoHolder = true
                    }
                ) {

                    Text(text = "Choose Video")

                }

            }

            Spacer(modifier = Modifier.height(10.dp))


            //show video if video is recorded or selected from gallery
            if(showVideoHolder){

                videoMediaUri?.let { uri ->
                    VidPlayer(context = context, videoUri = uri)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            //end for camera and gallery video


            //start for location
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Add location button
                Button(
                    onClick = {

                        // Request Camera permissions if it is not already granted
                        if (!locationPermissions.allPermissionsGranted) {
                            locationPermissions.launchMultiplePermissionRequest()

                        }
                        else {

                            locationViewModel.fetchNearbyPlaces(context) { places ->
                                placesList.value = places
                                showLocation = true
                            }
                        }

                    },
                ) {
                    Text(text = "Add Location")

                }

                Spacer(modifier = Modifier.height(10.dp))

                // show progress when state is loading
                if(loaderState.value == Loader.Loading){
                    CircularProgressIndicator()
                }


                //shows a list of locations if add location button has been pressed
                if(showLocation){

                    SelectInput(
                        options = placesList.value,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        locationViewModel = locationViewModel
                    )
                }


                Spacer(modifier = Modifier.height(10.dp))
            }
            //end for location


            //create post button, will send post to firestore
            Button(
                modifier = Modifier.padding(bottom = 15.dp, top = 15.dp),
                onClick = {

                    cameraViewModel.addPost(
                        title = title,
                        description = description,
                        image = imageMediaUri?: Uri.EMPTY,
                        video = videoMediaUri?: Uri.EMPTY,
                        userName = userState.value?.name ?: "",
                        locName = location?.name ?: "",
                        latLng = location?.latLng?: LatLng(0.0, 0.0),
                        scope = scope,
                        category = selectedCategory
                    )

                },
                colors = ButtonColors(
                    containerColor = Color(0xFF00BFA6),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
                enabled = !disableButton
            ) {

                Text(text = "Create Post")

            }


            Box(
                modifier = Modifier.padding(bottom = 25.dp)
            ){
                // show progress when state is loading
                if(uploadState.value == UploadState.Loading){
                    disableButton = true

                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

        }

    }


}


//composable for displaying image
@Composable
fun DisplayImage(
    imageUri: Uri?
) {

    Box(
        modifier = Modifier
            .size(350.dp)
            .border(width = 2.dp, color = Color.White, shape = RectangleShape)
            .background(Color.Gray, shape = RectangleShape),
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Feature Image",
            modifier = Modifier
                .size(350.dp)
                .clip(shape = RectangleShape),
            contentScale = ContentScale.Crop
        )
    }

}


//composable for displaying video
@Composable
fun VidPlayer(
    context: Context,
    videoUri: Uri?
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Set MediaSource to ExoPlayer
    LaunchedEffect(videoUri) {
        val mediaSource = videoUri?.let { MediaItem.fromUri(it) }
        if (mediaSource != null) {
            exoPlayer.setMediaItem(mediaSource)
        }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .size(350.dp)
            .border(width = 2.dp, color = Color.White, shape = RectangleShape)
            .background(Color.Gray, shape = RectangleShape),
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier
                .size(350.dp)
        )
    }

}


//composable for selecting location
@Composable
fun SelectInput(
    options: List<Place>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    locationViewModel: LocationViewModel
) {

    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(selectedOption)) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier
                .clickable { expanded = true },
            label = { Text("Select your location") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            readOnly = true
        )
        DropdownMenu(
            modifier = Modifier.height(350.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        locationViewModel.updateLocation(option)

                        textFieldValue = TextFieldValue(option.name ?: "")
                        onOptionSelected(option.name?: "")
                        expanded = false
                    },
                    text = {
                        Text(option.name?: "")
                    }
                )
            }
        }
    }
}


//composable for selecting category
@Composable
fun SelectCategory(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(selectedOption)) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier
                .clickable { expanded = true },
            label = { Text("Select category") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            readOnly = true
        )
        DropdownMenu(
            modifier = Modifier.height(300.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {

                        textFieldValue = TextFieldValue(option)
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = {
                        Text(option)
                    }
                )
            }
        }
    }
}
