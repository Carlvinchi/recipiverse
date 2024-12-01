package s3154679.tees.ac.uk.recipiverse.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Environment
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


    // get storage reference for firebase
    private var imageStorageRef : StorageReference = FirebaseStorage.getInstance().reference.child("images")
    private var videoStorageRef : StorageReference = FirebaseStorage.getInstance().reference.child("videos")
    private val firestore = FirebaseFirestore.getInstance()
    private val batch = firestore.batch()


    //get details required to update user profile image
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersCollection = "users"

    //get details required to create post
    private val postsCollection = "posts"

    //post list
    private val _post = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _post

    //post media url variables
    private val _postVideoUrl = mutableStateOf("")
    private val _postImageUrl = mutableStateOf("")


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


    // State variables for profile image files and URL
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: MutableStateFlow<Uri?> = _profileImageUri

    fun setProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri

        //upload image to firebase storage
        uploadProfileImage(uri)
    }


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
    private fun uploadProfileImage(uri: Uri) {

        _uploadState.value = UploadState.Loading

        try {

            imageStorageRef = imageStorageRef.child(System.currentTimeMillis().toString())
            imageStorageRef.putFile(uri)
                .addOnSuccessListener {

                    imageStorageRef.downloadUrl.addOnSuccessListener {

                        //save the url to user collection in firestore
                        updateUserProfileImage(it.toString())
                    }
                }
                .addOnFailureListener {

                    _uploadState.value = UploadState.Error("Image Upload Failed")

                }

        }catch (e: Exception){

            _uploadState.value = UploadState.Error(e.message.toString())
        }

    }


    //for updating user profile image in the user collection at firestore database
    private fun updateUserProfileImage(profileImageUrl: String) {

        val userId = auth.currentUser?.uid

        try {
            firestore.collection(usersCollection).document(userId!!).update("profileImageUrl", profileImageUrl)
                .addOnSuccessListener {


                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener {

                    _uploadState.value = UploadState.Error("User profile image update failed")
                }
        }catch (e: Exception){

            _uploadState.value = UploadState.Error(e.message.toString())
        }
    }


    //Function to add post to firestore database
    fun addPost(title: String, description: String, image: Uri, video: Uri, userName: String, locName: String, latLng: LatLng, category: String, scope: CoroutineScope){

        if(title.isEmpty() || description.isEmpty() || image == Uri.EMPTY || video == Uri.EMPTY || category.isEmpty()){
            _uploadState.value = UploadState.Error("Please title, category, description, image and video fields cannot be empty")
            return
        }
        _uploadState.value = UploadState.Loading

        //uploadPostImage(image)
        // uploadPostVideo(video)

        val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
        scope.launch {

            try {


                imageStorageRef = imageStorageRef.child(System.currentTimeMillis().toString())
                imageStorageRef.putFile(image)
                    .addOnSuccessListener {

                        imageStorageRef.downloadUrl.addOnSuccessListener {

                            _postImageUrl.value = it.toString()

                            videoStorageRef = videoStorageRef.child(System.currentTimeMillis().toString())
                            videoStorageRef.putFile(video)
                                .addOnSuccessListener {

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

        try {

            firestore.collection(postsCollection)
                .get()
                .addOnSuccessListener {

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
                            dateCreated = document.getTimestamp("dateCreated")?.toDate().toString(),
                            category = document.getString("category") ?: ""
                        )
                    }

                    _post.value = posts.reversed() //so new posts are at the top

                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener{

                    _uploadState.value = UploadState.Error("Posts fetched failed")
                }

        }catch (e: Exception){

            _uploadState.value = UploadState.Error(e.message.toString())
        }

    }


    //function to fetch posts by category from firestore database
    fun fetchPostsByCategory(category: String) {

        _uploadState.value = UploadState.Loading

        try {

            firestore.collection(postsCollection).whereEqualTo("category", category)
                .get()
                .addOnSuccessListener {

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
                            dateCreated = document.getTimestamp("dateCreated")?.toDate().toString(),
                            category = document.getString("category") ?: ""
                        )
                    }

                    _post.value = posts.reversed() //so new posts are at the top

                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener{

                    _uploadState.value = UploadState.Error("Posts fetched failed")
                }

        }catch (e: Exception){
            _uploadState.value = UploadState.Error(e.message.toString())
        }


    }

    //function to fetch posts by search term from firestore database
    fun fetchPostsBySearch(sentence: String) {
        if(sentence.isEmpty()){
            _uploadState.value = UploadState.Error("Please enter a search term")
        }

        _uploadState.value = UploadState.Loading

        val searchKeywords = sentence.lowercase().split(" ")

        try {

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
                            dateCreated = document.getTimestamp("dateCreated")?.toDate().toString(),
                            category = document.getString("category") ?: ""
                        )
                    }

                    _post.value = posts.reversed() //so new posts are at the top

                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener{

                    _uploadState.value = UploadState.Error("Posts fetched failed")
                }

        }catch (e: Exception){
            _uploadState.value = UploadState.Error(e.message.toString())
        }

    }

    //function to fetch posts by userId from firestore database
    fun fetchPostsByUserId() {

        val userId = auth.currentUser?.uid

        _uploadState.value = UploadState.Loading

        try {

            firestore.collection(postsCollection).whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener {


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
                            dateCreated = document.getTimestamp("dateCreated")?.toDate().toString(),
                            category = document.getString("category") ?: ""
                        )
                    }

                    _post.value = posts.reversed() //so new posts are at the top

                    _uploadState.value = UploadState.StopLoading
                }
                .addOnFailureListener{

                    _uploadState.value = UploadState.Error("Posts fetched failed")
                }

        }catch (e: Exception){
            _uploadState.value = UploadState.Error(e.message.toString())
        }


    }

    //function to delete posts by Id from firestore database
    fun deletePostById(postId: String, image: String, video: String, scope: CoroutineScope) {

        val imageStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(image)
        val videoStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(video)

        _uploadState.value = UploadState.Loading

        scope.launch {

            try {
                videoStorageRef.delete()
                    .addOnSuccessListener {
                        imageStorageRef.delete().addOnSuccessListener {

                            firestore.collection(postsCollection).document(postId).delete()
                                .addOnSuccessListener {

                                    _uploadState.value = UploadState.Uploaded("Post deleted successfully")
                                }
                                .addOnFailureListener{
                                    _uploadState.value = UploadState.Error("Posts delete failed")
                                }
                        }
                            .addOnFailureListener {
                                _uploadState.value = UploadState.Error("Posts image delete failed")
                            }
                    }
                    .addOnFailureListener {
                        _uploadState.value = UploadState.Error("Posts video delete failed")
                    }

            }catch (e: Exception){
                _uploadState.value = UploadState.Error(e.message.toString())
            }
        }
    }

    //function to delete posts by userId from firestore database
    fun deletePostsByUserId(scope: CoroutineScope) {

        val userId = auth.currentUser?.uid

        _uploadState.value = UploadState.Loading

        scope.launch {
            try {

                firestore.collection(postsCollection).whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        for (document in snapshot.documents) {
                            batch.delete(document.reference)
                        }

                        batch.commit()

                        _uploadState.value = UploadState.StopLoading
                    }
                    .addOnFailureListener{

                        _uploadState.value = UploadState.Error("Posts delete failed")
                    }

            }catch (e: Exception){
                _uploadState.value = UploadState.Error(e.message.toString())
            }

        }

    }




}

sealed class UploadState {
    data class  Uploaded(val message: String): UploadState()
    object Loading: UploadState()
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