package s3154679.tees.ac.uk.recipiverse.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraViewModel: ViewModel() {

    //variables for setting upload states
    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState


    //variables for setting loader states
    private val _loaderState = MutableLiveData<Loader>()
    val loaderState: LiveData<Loader> = _loaderState


    // get storage reference for firebase
    var imageStorageRef : StorageReference = FirebaseStorage.getInstance().reference.child("images")
    var videoStorageRef : StorageReference = FirebaseStorage.getInstance().reference.child("videos")
    val firestore = FirebaseFirestore.getInstance()


    //get details required to update user profile image
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val usersCollection = "users"

    //get details required to create post
    val postsCollection = "posts"

    private val _post = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _post

    //post media va
    private val _postVideoUrl = mutableStateOf("")
    val postVideoUrl = _postVideoUrl


    private val _postImageUrl = mutableStateOf("")
    val postImageUrl = _postImageUrl




    // State variables for video and image files and URIs
    private val _imageMediaUri = MutableStateFlow<Uri?>(null)
    val imageMediaUri: MutableStateFlow<Uri?> = _imageMediaUri

    fun setImageMediaUri(uri: Uri) {
        _imageMediaUri.value = uri

    }

    private val _videoMediaUri = MutableStateFlow<Uri?>(null)
    val videoMediaUri: MutableStateFlow<Uri?> = _videoMediaUri

    fun setVideoMediaUri(uri: Uri) {
        _videoMediaUri.value = uri

    }


    private val _videoUriState = mutableStateOf("")
    val videoUriState = _videoUriState


    private val _imageUriState = mutableStateOf("")
    val imageUriState = _imageUriState


    // State variables for profile image files and URL
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: MutableStateFlow<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri

        //upload image to firebase storage
        uploadProfileImage(uri)
    }

    private val _profileImageUrlState = mutableStateOf("")
    val profileImageUrlState = _profileImageUrlState


    //Function to create a temporary image file
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss" , Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile(
            "IMAGE_${timeStamp}_",
            ".JPEG",
            storageDir
        )

        //_mediaUri.value = tempFile.getUri(context)
        return tempFile

    }

    //Function to create a temporary video file
    fun createVideoFile(context: Context) : File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss" , Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "VIDEO_${timeStamp}_",
            ".mp4",
            storageDir
        )
    }


    //Function to get the URI for a file
    fun getFileUri(context: Context, file: File): Uri? {
        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".fileprovider",
            file
        )
    }



    //Function to upload profile image to firebase storage
    fun uploadProfileImage(uri: Uri) {

        _uploadState.value = UploadState.Loading
        _loaderState.value = Loader.Loading

        Log.i("Image URI", uri.toString())

        try {

            imageStorageRef = imageStorageRef.child(System.currentTimeMillis().toString())
            imageStorageRef.putFile(uri)
                .addOnSuccessListener {
                    Log.i("Image", "Image uploaded successfully")
                    imageStorageRef.downloadUrl.addOnSuccessListener {

                        _profileImageUrlState.value = it.toString()

                        //save the url to user collection in firestore
                        updateUserProfileImage(it.toString())
                    }
                }
                .addOnFailureListener {

                    Log.i("Image URI", "Image failed to upload")

                    _loaderState.value = Loader.StopLoading
                    _uploadState.value = UploadState.Error("Image Upload Failed")

                }

        }catch (e: Exception){

            _loaderState.value = Loader.StopLoading
            _uploadState.value = UploadState.Error("Image Upload Failed")
        }


    }


    //for updating user profile image in the user collection at firestore database
    fun updateUserProfileImage(profileImageUrl: String) {

        val userId = auth.currentUser?.uid

        try {
            firestore.collection(usersCollection).document(userId!!).update("profileImageUrl", profileImageUrl)
                .addOnSuccessListener {

                    _loaderState.value = Loader.StopLoading
                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener {

                    _loaderState.value = Loader.StopLoading
                    _uploadState.value = UploadState.Error("User profile image update failed")
                }
        }catch (e: Exception){

            Log.i("Image Failed", e.message.toString())
            _loaderState.value = Loader.StopLoading
            _uploadState.value = UploadState.Error("User profile image update failed")
        }
    }


    //Function to add post to firestore database
    fun addPost(title: String, description: String, image: Uri, video: Uri, userName: String, locName: String, latLng: LatLng, category: String, scope: CoroutineScope){

        _uploadState.value = UploadState.Loading

        //uploadPostImage(image)
        // uploadPostVideo(video)

        val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
        scope.launch {

            try {


                imageStorageRef = imageStorageRef.child(System.currentTimeMillis().toString())
                imageStorageRef.putFile(image)
                    .addOnSuccessListener {
                        Log.i("Post", "Image uploaded successfully")
                        imageStorageRef.downloadUrl.addOnSuccessListener {

                            _postImageUrl.value = it.toString()

                            videoStorageRef = videoStorageRef.child(System.currentTimeMillis().toString())
                            videoStorageRef.putFile(video)
                                .addOnSuccessListener {
                                    Log.i("Post", "Video uploaded successfully")
                                    videoStorageRef.downloadUrl.addOnSuccessListener { url ->

                                        _postVideoUrl.value = url.toString()

                                        val keywords = title.lowercase().split(" ")
                                        val postObject = hashMapOf(
                                            "title" to title,
                                            "description" to description,
                                            "image" to _postImageUrl.value,
                                            "video" to _postVideoUrl.value,
                                            "userId" to auth.currentUser?.uid,
                                            "userDisplayName" to userName,
                                            "userLocationName" to locName,
                                            "userLocation" to geoPoint,
                                            "dateCreated" to Timestamp.now(),
                                            "category" to category,
                                            "keywords" to keywords
                                        )


                                        firestore.collection(postsCollection).add(postObject)
                                            .addOnSuccessListener {

                                                Log.i("Post", "Post created successfully")
                                                _uploadState.value = UploadState.Uploaded("Post created successfully")

                                            }
                                            .addOnFailureListener{
                                                _uploadState.value = UploadState.Error("Post creation failed")
                                            }

                                    }
                                }
                                .addOnFailureListener {
                                    _uploadState.value = UploadState.Error("Video Upload Failed")

                                }

                        }
                    }
                    .addOnFailureListener {
                        _uploadState.value = UploadState.Error("Image Upload Failed")

                    }

            }catch (e : Exception){
                _uploadState.value = UploadState.Error(e.message.toString())

            }
        }


    }


    //function to fetch posts from firestore database
    fun fetchPosts() {

        _uploadState.value = UploadState.Loading

        firestore.collection(postsCollection)
            .get()
            .addOnSuccessListener {
                Log.i("Posts", "Posts fetched successfully")

                val posts =  it.documents.map { document ->
                    Post(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        image = document.getString("image") ?: "",
                        video = document.getString("video") ?: "",
                        userLocationName = document.getString("userLocationName") ?: "",
                        userDisplayName = document.getString("userDisplayName") ?: "",
                        userLocation = document.getGeoPoint("userLocation")?.let { geoPoint ->
                            LatLng(geoPoint.latitude, geoPoint.longitude)
                        },
                        dateCreated = document.getTimestamp("dateCreated")?.toDate().toString() ?: Date().toString(),
                        category = document.getString("category") ?: ""
                    )
                }

                _post.value = posts

                _uploadState.value = UploadState.StopLoading
            }
            .addOnFailureListener{
                Log.i("Posts", "Posts fetched failed")
                _uploadState.value = UploadState.Error("Posts fetched failed")
            }
    }


    //function to fetch posts by category from firestore database
    fun fetchPostsByCategory(category: String) {

        _uploadState.value = UploadState.Loading

        firestore.collection(postsCollection).whereEqualTo("category", category)
            .get()
            .addOnSuccessListener {
                Log.i("Posts", "Posts fetched successfully")

                val posts =  it.documents.map { document ->
                    Post(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        image = document.getString("image") ?: "",
                        video = document.getString("video") ?: "",
                        userLocationName = document.getString("userLocationName") ?: "",
                        userDisplayName = document.getString("userDisplayName") ?: "",
                        userLocation = document.getGeoPoint("userLocation")?.let { geoPoint ->
                            LatLng(geoPoint.latitude, geoPoint.longitude)
                        },
                        dateCreated = document.getTimestamp("dateCreated")?.toDate().toString() ?: Date().toString(),
                        category = document.getString("category") ?: ""
                    )
                }

                _post.value = posts

                _uploadState.value = UploadState.StopLoading
            }
            .addOnFailureListener{
                Log.i("Posts", "Posts fetched failed")
                _uploadState.value = UploadState.Error("Posts fetched failed")
            }
    }

    //function to fetch posts by category from firestore database
    fun fetchPostsBySearch(sententce: String) {

        _uploadState.value = UploadState.Loading

        val searchKeywords = sententce.lowercase().split(" ")

        firestore.collection(postsCollection)
            .get()
            .addOnSuccessListener {snapshot ->


                val documents = snapshot.documents.filter { document ->
                    val keywords = document.get("keywords") as List<String>
                    keywords.any { searchKeywords.contains(it) }
                }

                val posts =  documents.map { document ->
                    Post(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        image = document.getString("image") ?: "",
                        video = document.getString("video") ?: "",
                        userLocationName = document.getString("userLocationName") ?: "",
                        userDisplayName = document.getString("userDisplayName") ?: "",
                        userLocation = document.getGeoPoint("userLocation")?.let { geoPoint ->
                            LatLng(geoPoint.latitude, geoPoint.longitude)
                        },
                        dateCreated = document.getTimestamp("dateCreated")?.toDate().toString() ?: Date().toString(),
                        category = document.getString("category") ?: ""
                    )
                }

                _post.value = posts

                _uploadState.value = UploadState.StopLoading
            }
            .addOnFailureListener{
                Log.i("Posts", "Posts fetched failed")
                _uploadState.value = UploadState.Error("Posts fetched failed")
            }
    }




}

sealed class UploadState {
    data class  Uploaded(val message: String): UploadState()
    object Loading: UploadState()
    object DownloadedImage: UploadState()
    object DownloadedVideo: UploadState()
    object StopLoading: UploadState()
    data class Error(val message: String): UploadState()
}

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val video: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val userLocationName: String = "",
    val userLocation: LatLng?,
    val dateCreated: String = "",
    val category: String = "",
    val keywords: List<String> = emptyList()

)