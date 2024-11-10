package s3154679.tees.ac.uk.recipiverse.onboarding

import androidx.annotation.DrawableRes
import s3154679.tees.ac.uk.recipiverse.R

sealed class OnboardingContentModel(
    @DrawableRes val image: Int,
    val title: String,
    val description: String
) {
    data object First : OnboardingContentModel(

        image = R.drawable.community,
        title = "Community For Food Lovers",
        description = "Recipiverse is a community for sharing and discovering recipes and ingredients from around the world."
    )

    data object Second : OnboardingContentModel(
        image = R.drawable.sharing,
        title = "Share Your Favourite Recipes",
        description = "Quickly and easily share your recipes with the world. Let others enjoy your favorite dishes as well as share their own."
    )

    data object Third : OnboardingContentModel(
        image = R.drawable.cooking,
        title = "Easily Learn To Cook",
        description = "Learn to prepare your favorite meals and dishes through video guides using the right ingredients and techniques."
    )
}