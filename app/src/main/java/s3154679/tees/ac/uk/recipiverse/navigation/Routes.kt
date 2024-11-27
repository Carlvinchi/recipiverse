package s3154679.tees.ac.uk.recipiverse.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginScreen

@Serializable
object SignupScreen

@Serializable
object HomeScreen

@Serializable
object TermsScreen

@Serializable
object OnboardingScreen

@Serializable
object ProfileScreen

@Serializable
object CreatePostScreen

@Serializable
object UserPostsScreen

@Serializable
data class  PostDetailsScreen(
    val postId: String,
    val postTitle: String,
    val postDescription: String,
    val postImage: String,
    val postVideo: String,
    val postLocLat: Double,
    val postLocLng: Double,
    val postDate: String,
    val userName: String,
    val userLocName: String,
)

@Serializable
object EditPostScreen