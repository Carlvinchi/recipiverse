package s3154679.tees.ac.uk.recipiverse.viewmodels

sealed class Loader {
    object Loading : Loader()
    object StopLoading : Loader()

}