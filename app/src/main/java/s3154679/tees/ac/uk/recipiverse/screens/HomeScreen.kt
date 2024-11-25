package s3154679.tees.ac.uk.recipiverse.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import s3154679.tees.ac.uk.recipiverse.navigation.CreatePostScreen
import s3154679.tees.ac.uk.recipiverse.navigation.LoginScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader
import s3154679.tees.ac.uk.recipiverse.viewmodels.LocationViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Post
import s3154679.tees.ac.uk.recipiverse.viewmodels.UploadState

@Composable
fun HomeScreen(
    modifier: Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel,
    locationViewModel: LocationViewModel

) {
    //observe auth state and loader state from viewmodel
    val authState = authViewModel.authState.observeAsState()
    val userState = authViewModel.user.observeAsState()
    val loaderState = authViewModel.loaderState.observeAsState()
    val uploadState = cameraViewModel.uploadState.observeAsState()
    val postList = cameraViewModel.postList.observeAsState()

    authViewModel.getUserFromFirestore()


    //navigate to login page if unauthenticated
    LaunchedEffect(Unit) {
        cameraViewModel.fetchPosts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Page", fontSize = 32.sp)

        Spacer(modifier = Modifier.height(15.dp))

        Box {

            // show progress when state is loading
            if(uploadState.value == UploadState.Loading){
                CircularProgressIndicator()
            }
        }

        // Display the list of posts in a LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            items(postList.value ?: emptyList()) { post ->
                PostItem(post)
            }
        }


        // show progress when state is loading
        if(loaderState.value == Loader.Loading){
            CircularProgressIndicator()
        }

    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {}),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            //Text(text = "ID: ${post.id}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = post.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
                )
            //Text(text = "Description: ${post.description}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(10.dp))

//            if(post.image != ""){
//
//                Box(
//                    modifier = Modifier
//                        .size(350.dp)
//                        .border(width = 2.dp, color = Color.White, shape = RectangleShape)
//                        .background(Color.Gray, shape = RectangleShape),
//                )
//                {
//                    AsyncImage(
//                        model = post.image,
//                        contentDescription = "Feature Image",
//                        modifier = Modifier
//                            .size(350.dp)
//                            .clip(shape = RectangleShape),
//                        contentScale = ContentScale.Crop
//                    )
//
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//            }

            if(post.video != ""){

                Box(
                    modifier = Modifier
                        .size(350.dp)
                        .border(width = 2.dp, color = Color.White, shape = RectangleShape)
                        .background(Color.Gray, shape = RectangleShape),
                )
                {
                    Player(context = LocalContext.current, videoUri = post.video)

                }

                Spacer(modifier = Modifier.height(18.dp))
            }


            if(post.userLocation?.latitude != 0.0){
                DisplayMap(location = post.userLocation!!)
            }

            Text(
                text = "Date Posted: ${
                    post.dateCreated
                }",
                style = MaterialTheme.typography.bodySmall
            )

           // HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }

}

@Composable
fun Player(
    modifier: Modifier = Modifier,
    context: Context,
    videoUri: String?
) {

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // Set MediaSource to ExoPlayer
    val mediaSource = videoUri?.let { MediaItem.fromUri(it) }
    if (mediaSource != null) {
        exoPlayer.setMediaItem(mediaSource)
    }
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier
            .size(350.dp)// Set your desired height
    )
}

@Composable
fun DisplayMap(
    location: LatLng
) {
    val markerState = rememberMarkerState(position = location)

    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(location, 10f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth().height(300.dp)
            .padding(bottom = 10.dp),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = markerState,
            title = "Posted From"
        )
    }

}