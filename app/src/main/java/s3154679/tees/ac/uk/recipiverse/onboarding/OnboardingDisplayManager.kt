package s3154679.tees.ac.uk.recipiverse.onboarding

import android.content.Context

//The utility code used to check if the user has completed the onboarding screen

class OnboardingDisplayManager(val context: Context) {

    fun isOnboardingFinished(): Boolean {
        val sharedPreferences = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("finished", false)
    }

    fun setOnboardingFinished() {
        val sharedPreferences = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("finished", true).apply()
    }
}