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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
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

    //Function to upload image to firebase storage and save the uri in firestore
    fun uploadImage(context: Context, uri: Uri) {

        _loaderState.value = Loader.Loading

        imageStorageRef = imageStorageRef.child(System.currentTimeMillis().toString())
        imageStorageRef.putFile(uri)
            .addOnSuccessListener {
                Log.i("Image", "Image uploaded successfully")
                imageStorageRef.downloadUrl.addOnSuccessListener {
                    val image = hashMapOf(
                        "imageUrl" to it.toString()
                    )
                    firestore.collection("images").add(image)
                        .addOnSuccessListener {
                            Log.i("Image", "Image added to firestore successfully")
                            _loaderState.value = Loader.StopLoading
                            _uploadState.value = UploadState.Uploaded("Image uploaded successfully")
                        }
                        .addOnFailureListener {
                            Log.i("Image", "Image added to firestore failed")
                            _loaderState.value = Loader.StopLoading
                            _uploadState.value = UploadState.Error("Image added to firestore failed")
                        }

                }
            }
            .addOnFailureListener {
                Log.i("Image", "Image uploaded failed")
                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.Error("Image added to firestore failed")
            }

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

    //function fetch image from firestore
    fun fetchImage() {
        _uploadState.value = UploadState.Loading
        _loaderState.value = Loader.Loading

        firestore.collection("images")
            .get()
            .addOnSuccessListener {

                _imageUriState.value = it.documents[0].data?.get("imageUrl").toString()
                Log.i("Image URI", imageUriState.value)

                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.DownloadedImage
            }
            .addOnFailureListener {
                Log.i("Image", "Image fetched failed")

                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.Error("Image fetched failed")
            }

    }

    //Function to upload video to firebase storage and save the uri in firestore
    fun uploadVideo(context: Context, uri: Uri) {

        _uploadState.value = UploadState.Loading
        _loaderState.value = Loader.Loading

        videoStorageRef = videoStorageRef.child(System.currentTimeMillis().toString())
        videoStorageRef.putFile(uri)
            .addOnSuccessListener {
                Log.i("Video", "Image uploaded successfully")
                videoStorageRef.downloadUrl.addOnSuccessListener {
                    val video = hashMapOf(
                        "videoUrl" to it.toString()
                    )
                    firestore.collection("videos").add(video)
                        .addOnSuccessListener {
                            Log.i("Video", "Video added to firestore successfully")

                            _loaderState.value = Loader.StopLoading
                            _uploadState.value = UploadState.Uploaded("Video uploaded successfully")
                        }
                        .addOnFailureListener {
                            Log.i("Video", "Video added to firestore failed")

                            _loaderState.value = Loader.StopLoading
                            _uploadState.value = UploadState.Error("Video added to firestore failed")
                        }

                }
            }
            .addOnFailureListener {
                Log.i("Video", "Video uploaded failed")

                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.Error("Video added to firestore failed")
            }

    }

    //function fetch video from firestore
    fun fetchVideo() {
        _uploadState.value = UploadState.Loading
        _loaderState.value = Loader.Loading

        firestore.collection("videos")
            .get()
            .addOnSuccessListener {

                _videoUriState.value = it.documents[0].data?.get("videoUrl").toString()
                Log.i("Video URI", videoUriState.value)

                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.DownloadedVideo
            }
            .addOnFailureListener {
                Log.i("Video", "Video fetched failed")

                _loaderState.value = Loader.StopLoading
                _uploadState.value = UploadState.Error("Video fetched failed")
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

}

sealed class UploadState {
    data class  Uploaded(val message: String): UploadState()
    object Loading: UploadState()
    object DownloadedImage: UploadState()
    object DownloadedVideo: UploadState()
    object StopLoading: UploadState()
    data class Error(val message: String): UploadState()
}