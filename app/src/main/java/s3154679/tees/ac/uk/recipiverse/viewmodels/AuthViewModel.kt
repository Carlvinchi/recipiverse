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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import s3154679.tees.ac.uk.recipiverse.R
import java.security.MessageDigest
import java.util.UUID

class AuthViewModel : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val usersCollection = "users"

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

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
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password can't be empty")
            return
        }

        _authState.value = AuthState.Loading

        scope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task->
                    if (task.isSuccessful){

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



}


sealed class AuthState {
    object  Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    object StopLoading: AuthState()
    data class Error(val message: String): AuthState()
}