package com.digz.cumapapp.navigation


import android.content.Context
import android.os.Bundle
import android.util.Log
import com.digz.cumapapp.R
import com.digz.cumapapp.Util
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class NavigationPresenter(private val context: Context) : NavigationContract.Presenter, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status> {

    private var start: DoubleArray? = null
    private var end: DoubleArray? = null
    lateinit var view: NavigationActivity
    private var googleApiClient: GoogleApiClient? = null
    private var adapter: PlaceAutoCompleteAdapter? = null
    private var connectedToMapzen = false


    override fun onAttach(view: NavigationActivity) {
        this.view = view
    }

    private fun startRouting() {
    }

    override fun setStartToNull() {
        if (start != null) start = null
    }

    override fun setEndToNull() {
        if (end != null) end = null
    }

    override fun determineIfOnline(): Boolean {
        return Util.Operations.isOnline(context)
    }


    override fun setLocationListener() {


    }

    override fun checkLocationSettings() {

    }


    override fun startTrip() {
        if (connectedToMapzen)
            setLocationListener()
    }

    override fun setupGoogleServices() {
        googleApiClient = GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        googleApiClient!!.connect()
    }

    override fun setUpPlaceAutoCompleteAdapter() {
        adapter = PlaceAutoCompleteAdapter(context, android.R.layout.simple_list_item_1, googleApiClient, BOUNDS_NIGERIA, null)
        view.setPlaceAdapterToView(adapter!!)
    }

    override fun route() {
        if (!validateStart() || !validateEnd()) return

        view.showProgressDialog("Please wait.", "Fetching route information.")
        view.showToast("Please wait. Fetching route information.")
        startRouting()
    }

    private fun validateEnd(): Boolean {
        val state = end != null
        if (end != null) {
            view.setErrorOnDestinationTextField(null)
        }

        if (view.textOfDestinationField.isNotEmpty()) {
            view.setErrorOnOriginTextField("Choose location from dropdown.")

        } else {
            view.setErrorOnDestinationTextField("Please type in a destination.")
            view.showToast("Please choose a destination.")

        }
        return state
    }

    private fun validateStart(): Boolean {
        val state = start != null
        if (start != null) {
            view.setErrorOnOriginTextField(null)
        }

        if (view.textOfOriginField.isNotEmpty()) {
            view.setErrorOnOriginTextField("Choose location from dropdown.")

        } else {
            view.setErrorOnOriginTextField("Please type in a starting point.")
            view.showToast("Please choose a starting point.")

        }
        return state

    }

    override fun onDestinationAutocompleteClicked(position: Int) {
        val item = adapter!!.getItem(position)
        val placeId = item!!.placeId.toString()
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description)

        /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        val placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId)
        placeResult.setResultCallback(ResultCallback { places ->
            if (!places.status.isSuccess) {
                // Request did not complete successfully
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.status.toString())
                places.release()
                return@ResultCallback
            }
            // Get the Place object from the buffer.
            val place = places.get(0)
            end = DoubleArray(2)

            end!![0] = place.latLng.latitude
            end!![1] = place.latLng.longitude
            view.addMapMarker(createMarker(end, false))
        })
    }

    override fun onStartAutocompleteClicked(position: Int) {
        val item = adapter!!.getItem(position)
        val placeId = item!!.placeId.toString()
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description)

        /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        val placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId)
        placeResult.setResultCallback(ResultCallback { places ->
            if (!places.status.isSuccess) {
                // Request did not complete successfully
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.status.toString())
                places.release()
                return@ResultCallback
            }
            // Get the Place object from the buffer.
            val place = places.get(0)
            start = DoubleArray(2)
            start!![0] = place.latLng.latitude
            start!![1] = place.latLng.longitude
            view.addMapMarker(createMarker(start, true))
        })
    }

    private fun createMarker(position: DoubleArray?, startMarker: Boolean): MarkerOptions {
        val options = MarkerOptions()
        options.position(LatLng(position!![0], position[1]))
        if (startMarker)
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
        else
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
        return options
    }


    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onResult(status: Status) {

    }

    override fun setAdapterBounds(bounds: LatLngBounds) {
        adapter!!.setBounds(BOUNDS_NIGERIA)
    }

    companion object {
        private val LOG_TAG = "MyActivity"
        private val BOUNDS_NIGERIA = LatLngBounds(LatLng(5.065341647205726, 2.9987719580531),
                LatLng(9.9, 5.9))
    }
}
