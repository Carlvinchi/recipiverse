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

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val firestore = Firebase.firestore

    val usersCollection = "users"

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

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

        _authState.value = AuthState.Loading

        scope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        //fetch user details from firestore
                        getUserFromFirestore()


                        _authState.value = AuthState.Authenticated

                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.message ?: "Something went wrong")

                    }

                }
            } catch (e: Exception) {
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

        _authState.value = AuthState.Loading

        scope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task->
                    if (task.isSuccessful){

                        //add user details to firestore when signup is successful
                        addUserToFirestore(name, email, auth.currentUser?.uid ?: "no uid")

                        //fetch user details from firestore
                        getUserFromFirestore()


                        _authState.value = AuthState.Authenticated

                    }else{
                        _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                    }

                }
            }catch (e: Exception) {
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
        _authState.value = AuthState.Loading

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

                        getUserFromFirestore()
                        _authState.value = AuthState.Authenticated

                    }else{
                        _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")

                    }

                }

            }catch (e: Exception){
                _authState.value = e.message?.let { AuthState.Error(it) }
            }
        }


    }

    //for signout process
    fun signout() {
        _authState.value = AuthState.Loading

        auth.signOut()
        _authState.value = AuthState.Unauthenticated
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
            "profileImage" to ""
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

                    Log.i("Fetched Name", name)
                    Log.i("Fetched Email", email)

                    _user.value = User(name, email)

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
    fun updateUserInFirestore(name: String, email: String, scope: CoroutineScope) {

        if(name.isEmpty() || email.isEmpty()){
            _authState.value = AuthState.Error("Name or Email can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        val userId = auth.currentUser?.uid
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "profileImage" to ""
        )

        scope.launch {
            try {
                firestore.collection(usersCollection).document(userId!!).update(user as Map<String, Any>)
                    .addOnSuccessListener {
                        getUserFromFirestore()
                        _authState.value = AuthState.StopLoading
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
    }

    //for deleting user details in firestore
    fun deleteUserFromFirestore(scope: CoroutineScope) {

        _authState.value = AuthState.Loading

        val userId = auth.currentUser?.uid

        scope.launch {
            try {
                firestore.collection(usersCollection).document(userId!!).delete()
                    .addOnSuccessListener {
                        _authState.value = AuthState.StopLoading
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.StopLoading
                    }
            }catch (e: Exception){
                _authState.value = AuthState.StopLoading
            }
        }
    }



}

//class for different authentication states
sealed class AuthState {
    object  Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    object StopLoading: AuthState()
    data class Error(val message: String): AuthState()
}

//class for user details
data class User(val name: String, val email: String, val profileImage: String = "")