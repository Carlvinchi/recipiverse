package s3154679.tees.ac.uk.recipiverse.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import s3154679.tees.ac.uk.recipiverse.navigation.PostDetailsScreen
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

    Scaffold(
        topBar = {

            MyTopBar(cameraViewModel)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Categories(cameraViewModel)


            Spacer(modifier = Modifier.height(10.dp))

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
                    PostItem(post, navController)
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
fun PostItem(post: Post, navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),

        onClick = {
            // Handle item click
            navController.navigate(
                PostDetailsScreen(
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
            }

        }
    }

}

@Composable
fun MyTopBar(cameraViewModel: CameraViewModel) {
    var searchQuery by remember {
        mutableStateOf("")
    }

    // Hides the keyboard after typing
    val keyBoardController = LocalSoftwareKeyboardController.current


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(8.dp)
                .height(55.dp)
                .border(1.dp, Color.Gray, CircleShape)
                .clip(CircleShape),
            value = searchQuery,
            onValueChange = {
                searchQuery = it
            },
            trailingIcon = {
                IconButton(
                    onClick = {

                        if (searchQuery.isNotEmpty()) {
                            cameraViewModel.fetchPostsBySearch(searchQuery)
                            keyBoardController?.hide()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                }

            }
        )
    }

}

@Composable
fun Categories(cameraViewModel: CameraViewModel) {

    val categoriesList = listOf(
        "AFRICAN",
        "ASIAN",
        "EUROPEAN",
        "AMERICAN"
    )

    Row(
        modifier = Modifier.fillMaxWidth().
        horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {

        categoriesList.forEach{category ->
            Button(
                onClick = {
                    cameraViewModel.fetchPostsByCategory(category)
                },
                modifier = Modifier.padding(4.dp),
                colors = ButtonColors(
                    containerColor = Color(0xFF00BFA6),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = category)
            }

        }
    }


}
