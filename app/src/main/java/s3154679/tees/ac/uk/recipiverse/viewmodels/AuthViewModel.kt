package s3154679.tees.ac.uk.recipiverse.viewmodels

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import s3154679.tees.ac.uk.recipiverse.R
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel : ViewModel() {

    //firebase variables
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore = Firebase.firestore
    val usersCollection = "users"


    //variables for authentication states
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState


    //variables for setting loader states
    private val _loaderState = MutableLiveData<Loader>()
    val loaderState: LiveData<Loader> = _loaderState


    //variables for setting up user object
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user


    //Automatically calls the method when app is launched
    init {
        checkAuthStatus()
    }


    //called to check if user is signed in or not
    private fun checkAuthStatus() {
        if (auth.currentUser ==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated

        }
    }


    //for login process
    fun login(email: String, password: String, scope: CoroutineScope) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _loaderState.value = Loader.Loading

        scope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        //fetch user details from firestore
                        getUserFromFirestore()

                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Authenticated


                    } else {

                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")

                    }

                }
            } catch (e: Exception) {

                //update loader state and authentication state
                _loaderState.value = Loader.StopLoading
                _authState.value = e.message?.let { AuthState.Error(it) }
            }
        }


    }


    //for signup process
    fun signup(email: String, password: String, name: String, scope: CoroutineScope) {
        if(email.isEmpty() || password.isEmpty() || name.isEmpty()){
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _loaderState.value = Loader.Loading

        scope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task->
                    if (task.isSuccessful){

                        //add user details to firestore when signup is successful
                        addUserToFirestore(name, email, auth.currentUser?.uid ?: "no uid")

                        //fetch user details from firestore
                        getUserFromFirestore()


                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Authenticated

                    }else{

                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                    }

                }
            }catch (e: Exception) {

                //update loader state and authentication state
                _loaderState.value = Loader.StopLoading
                _authState.value = e.message?.let { AuthState.Error(it) }
            }
        }

    }

    //create nonce for google signin
    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold(""){str, it ->
            str + "%02x".format(it)}
    }


    //for login/signup with Google
    fun signInWithGoogle(context: Context, scope: CoroutineScope) {

        _loaderState.value = Loader.Loading

        val credentialManger = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(createNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {

            try {
                val result = credentialManger.getCredential(context = context, request = request)
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                Log.i("fire cred :", firebaseCredential.toString())
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener{task->
                    if (task.isSuccessful){

                        //fetch user details from firestore when signin is successful
                        getUserFromFirestore()

                        //add user details to firestore  if it does not already exist when signin/signup with Google is successful
                        if(user.value?.email == null || user.value?.name == null){
                            addUserToFirestore(
                                auth.currentUser?.displayName ?: "no name",
                                auth.currentUser?.email ?: "no email",
                                auth.currentUser?.uid ?: "no uid"
                            )

                            //fetch user details from firestore after it has been added to firestore
                            getUserFromFirestore()
                        }

                        //getUserFromFirestore()

                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Authenticated

                    }else{

                        //update loader state and authentication state
                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                    }

                }

            }catch (e: Exception){

                //update loader state and authentication state
                _loaderState.value = Loader.StopLoading
                _authState.value = e.message?.let { AuthState.Error(it) }
            }
        }


    }

    //for signout process
    fun signout() {

        _loaderState.value = Loader.Loading

        auth.signOut()

        //update loader state and authentication state
        _authState.value = AuthState.Unauthenticated
        _loaderState.value = Loader.StopLoading
    }


    //for adding user details to firestore
    fun addUserToFirestore(
        name: String,
        email: String,
        userId: String
    ) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "profileImageUrl" to ""
        )

        try {
            firestore.collection(usersCollection).document(userId).set(user)
                .addOnSuccessListener {
                    _user.value = User(name, email)

                }
                .addOnFailureListener {
                    _user.value = User(
                        "no name",
                        "no email"
                    )
                }

        }catch (e: Exception){
            _user.value = User(
                "no name",
                "no email"
            )
        }

    }


    //for fetching user details from firestore
    fun getUserFromFirestore() {

        val userId = auth.currentUser?.uid

        try {
            firestore.collection(usersCollection).document(userId!!).get()
                .addOnSuccessListener {

                    val name = it.get("name").toString()
                    val email = it.get("email").toString()
                    val profileImage = it.get("profileImageUrl").toString()

                    Log.i("Fetched Name", name)
                    Log.i("Fetched Email", email)

                    _user.value = User(name, email, profileImage)

                }
                .addOnFailureListener {
                    _user.value = User(
                        "no name",
                        "no email"
                    )
                }

        }catch (e: Exception) {
            _user.value = User(
                "no name",
                "no email"
            )
        }

    }

    //for updating user details in firestore
    fun updateUserInFirestore(name: String, email: String) {

        if(name.isEmpty() || email.isEmpty()){
            _authState.value = AuthState.Error("Name or Email can't be empty")
            return
        }

        _loaderState.value = Loader.Loading

        val userId = auth.currentUser?.uid
        val user = hashMapOf(
            "name" to name,
            "email" to email
        )

        try {
            firestore.collection(usersCollection).document(userId!!).update(user as Map<String, Any>)
                .addOnSuccessListener {

                    getUserFromFirestore()

                    _loaderState.value = Loader.StopLoading
                }
                .addOnFailureListener {
                    _user.value = User(
                        "no name",
                        "no email"
                    )

                    _loaderState.value = Loader.StopLoading
                }
        }catch (e: Exception){
            _user.value = User(
                "no name",
                "no email"
            )

            _loaderState.value = Loader.StopLoading
        }
    }



    //for deleting user details in firestore
    fun deleteUserFromFirestore(scope: CoroutineScope) {

        _loaderState.value = Loader.Loading

        val userId = auth.currentUser?.uid

        scope.launch {
            try {
                firestore.collection(usersCollection).document(userId!!).delete()
                    .addOnSuccessListener {

                        _loaderState.value = Loader.StopLoading
                    }
                    .addOnFailureListener {

                        _loaderState.value = Loader.StopLoading
                        _authState.value = AuthState.Error("Could not delete user")
                    }
            }catch (e: Exception){

                _loaderState.value = Loader.StopLoading
                _authState.value = AuthState.Error(e.message ?:"could not delete user")
            }
        }
    }



}

//class for different authentication states
sealed class AuthState {
    object  Authenticated: AuthState()
    object Unauthenticated: AuthState()
    data class Error(val message: String): AuthState()
}

//class for user details
data class User(val name: String, val email: String, val profileImageUrl: String = "")