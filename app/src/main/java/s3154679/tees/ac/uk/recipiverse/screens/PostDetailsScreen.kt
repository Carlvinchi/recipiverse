package s3154679.tees.ac.uk.recipiverse.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthState
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel

@Composable
fun PostDetailsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postId: String,
    postTitle: String,
    postDescription: String,
    postImage: String,
    postVideo: String,
    postLocLat: Double,
    postLocLng: Double,
    postDate: String,
    userName: String,
    userLocName: String

) {

    //we observe the authentication
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

    Scaffold(

        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Post Details", fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

        },
        containerColor = Color.White,
    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding).padding(top = 10.dp)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //display post details
            Item(
                postId,
                postTitle,
                postDescription,
                postImage,
                postVideo,
                postLocLat,
                postLocLng,
                postDate,
                userName,
                userLocName
            )


        }

    }

}


@Composable
fun Item(
    postId: String,
    postTitle: String,
    postDescription: String,
    postImage: String,
    postVideo: String,
    postLocLat: Double,
    postLocLng: Double,
    postDate: String,
    userName: String,
    userLocName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        var showMap  by remember {
            mutableStateOf(false)
        }

        Text(
            text = postTitle,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)

        )

        //display image if available
        if(postImage != ""){

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),

            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = postImage,
                        contentDescription = "Feature Image",
                        modifier = Modifier
                            .size(350.dp)
                            .clip(shape = RectangleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }


        Text(
            text = postDescription,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 15.dp)

        )

        //display video if available
        if(postVideo != ""){

            Player(context = LocalContext.current, videoUri = postVideo)

            Spacer(modifier = Modifier.height(18.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Date Posted: $postDate",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp),

        )

        Text(
            text = "Created By: $userName",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp),

        )



        //display map if location is available
        if(showMap){
            DisplayMap(location = LatLng(postLocLat, postLocLng))
        }


        if(postLocLat != 0.0){

            Text(
                text = "Posted From: $userLocName",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 5.dp),
                color = Color.Black,

            )

            TextButton(
                modifier = Modifier.padding(top = 5.dp),
                onClick = {
                    showMap = !showMap
                },

                ) {
                Text(
                    text = "View Location",

                    fontWeight = FontWeight.Bold,
                )
            }
        }





    }
}


@Composable
fun Player(
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
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