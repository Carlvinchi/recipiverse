package s3154679.tees.ac.uk.recipiverse.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import s3154679.tees.ac.uk.recipiverse.navigation.HomeScreen
import s3154679.tees.ac.uk.recipiverse.viewmodels.AuthViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.CameraViewModel
import s3154679.tees.ac.uk.recipiverse.viewmodels.Loader
import s3154679.tees.ac.uk.recipiverse.viewmodels.Post
import s3154679.tees.ac.uk.recipiverse.viewmodels.UploadState

@Composable
fun UserPostsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cameraViewModel: CameraViewModel

) {
    //observe states from viewmodel
    val loaderState = authViewModel.loaderState.observeAsState()
    val uploadState = cameraViewModel.uploadState.observeAsState()
    val postList = cameraViewModel.postList.observeAsState()


   val context = LocalContext.current

    //used to fetch posts when screen is visited
    LaunchedEffect(Unit) {
        cameraViewModel.fetchPostsByUserId()

    }

    //used to update user when a post is deleted
    LaunchedEffect(uploadState.value) {
        when(uploadState.value) {
            is UploadState.Uploaded -> {
                Toast.makeText(context, (uploadState.value as UploadState.Uploaded).message, Toast.LENGTH_SHORT).show()
                navController.navigate(HomeScreen)
            }
            is UploadState.Error -> {
                Toast.makeText(context, (uploadState.value as UploadState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Scaffold(

        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "User Posts", fontSize = 32.sp
                )
            }

        },
        containerColor = Color.White,
    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding)
                .padding(top = 10.dp)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
                    UserPostItem(post, navController, cameraViewModel)
                }
            }


            // show progress when state is loading
            if(loaderState.value == Loader.Loading){
                CircularProgressIndicator()
            }

        }
    }

}

@Composable
fun UserPostItem(post: Post, navController: NavHostController, cameraViewModel: CameraViewModel) {

    val scope = rememberCoroutineScope()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),

        onClick = {
            // navigate to post details screen when card is clicked
            navController.navigate(
                s3154679.tees.ac.uk.recipiverse.navigation.PostDetailsScreen(
                    post.id,
                    post.title,
                    post.description,
                    post.image,
                    post.video,
                    post.userLocation?.latitude ?: 0.0,
                    post.userLocation?.longitude ?: 0.0,
                    post.dateCreated,
                    post.userDisplayName,
                    post.userLocationName
                )
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //will display image if there is one
            if(post.image != ""){

                AsyncImage(
                    model = post.image,
                    contentDescription = "Feature Image",
                    modifier = Modifier
                        .size(350.dp)
                        .padding(top = 10.dp)
                        .clip(shape = RectangleShape),
                    contentScale = ContentScale.Crop
                )

            }

            Text(
                text = post.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(
                    top = 10.dp,
                    start = 5.dp,
                    end = 2.dp
                ).align(Alignment.Start)

            )

            HorizontalDivider()

            Text(
                text = "Category: " + post.category,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Left,
                modifier = Modifier.padding(
                    top = 2.dp,
                    start = 5.dp,
                    end = 2.dp
                ).align(Alignment.Start)

            )

            //delete post button
            TextButton(
                onClick = {

                    showDeleteConfirmationDialog = true

                }

            ) {
                Text(text = "Delete Post")

            }

            //show confirmation dialog when delete button is clicked
            if (showDeleteConfirmationDialog) {

                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                cameraViewModel.deletePostById(post.id, scope)
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
                    text = { Text("Are you sure you want to delete this post?") },
                )
            }
        }
    }

}