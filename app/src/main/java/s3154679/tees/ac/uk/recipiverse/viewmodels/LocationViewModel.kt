package s3154679.tees.ac.uk.recipiverse.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import s3154679.tees.ac.uk.recipiverse.R

class LocationViewModel: ViewModel() {

    //variables for setting loader states
    private val _loaderState = MutableLiveData<Loader>()
    val loaderState: LiveData<Loader> = _loaderState

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> get() = _selectedLocation

    fun updateLocation(location: LatLng) {
        viewModelScope.launch {
            _selectedLocation.emit(location)
        }
    }

    fun fetchNearbyPlaces(context: Context, locationCallback: (List<Place>) -> Unit) {

        _loaderState.value = Loader.Loading

        // Initialize the Places API with your API key
        if(!Places.isInitialized()){
            Places.initialize(context, context.getString(R.string.maps_key))

        }

        val placesClient = Places.createClient(context)

        // Create a FindCurrentPlaceRequest
        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)


        // check for location permission
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            //I am 100% sure that it has been granted so I can ignore
            return
        }

        // fetch places and invoke callback with the result
        placesClient.findCurrentPlace(request)
            .addOnSuccessListener { response ->
                val places = response.placeLikelihoods.map { it.place }

                _loaderState.value = Loader.StopLoading
                locationCallback(places)
            }
            .addOnFailureListener { exception ->
                _loaderState.value = Loader.StopLoading
                // Handle the failure case
                exception.printStackTrace()

                locationCallback(emptyList())
            }

    }
}