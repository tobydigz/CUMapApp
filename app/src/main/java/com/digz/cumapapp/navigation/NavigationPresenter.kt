package com.digz.cumapapp.navigation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.digz.cumapapp.R
import com.digz.cumapapp.Util
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.mapzen.android.lost.api.LocationListener
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LocationSettingsRequest
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.helpers.RouteEngine
import com.mapzen.helpers.RouteListener
import com.mapzen.model.ValhallaLocation
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import java.util.*

class NavigationPresenter(private val context: Context) : NavigationContract.Presenter, RouteCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, LostApiClient.ConnectionCallbacks {

    private lateinit var mapzenRouter: MapzenRouter
    private lateinit var routeEngine: RouteEngine
    private var start: DoubleArray? = null
    private var end: DoubleArray? = null
    private var lostApiClient: LostApiClient? = null
    lateinit var view: NavigationActivity
    private var googleApiClient: GoogleApiClient? = null
    private var adapter: PlaceAutoCompleteAdapter? = null
    private var request: LocationRequest? = null
    private var listener: LocationListener? = null
    private var connectedToMapzen = false


    override fun onAttach(view: NavigationActivity) {
        this.view = view
    }

    private fun startRouting() {
        mapzenRouter = view.router

        mapzenRouter.setWalking()
        mapzenRouter.setLocation(start)
        mapzenRouter.setLocation(end)
        mapzenRouter.setCallback(this)
        mapzenRouter.fetch()
        createLostApiClient()
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
        request = LocationRequest.create()
                .setInterval(5000)
                .setSmallestDisplacement(10f)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        listener = LocationListener { location ->
            val valhallaLocation = ValhallaLocation()
            valhallaLocation.bearing = location.bearing
            valhallaLocation.latitude = location.latitude
            valhallaLocation.longitude = location.longitude

            routeEngine.onLocationChanged(valhallaLocation)
        }

        checkLocationSettings()
    }

    override fun checkLocationSettings() {
        val requests = ArrayList<LocationRequest>()
        val highAccuracy = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        requests.add(highAccuracy)
        val needBle = false
        val settingsRequest = LocationSettingsRequest.Builder()
                .addAllLocationRequests(requests)
                .setNeedBle(needBle)
                .build()
        val result = LocationServices.SettingsApi.checkLocationSettings(lostApiClient, settingsRequest)

        val locationSettingsResult = result.await()
        val status = locationSettingsResult.status
        checkStatusApi(status)
    }

    private fun checkStatusApi(status: com.mapzen.android.lost.api.Status) {
        when (status.statusCode) {
            com.mapzen.android.lost.api.Status.SUCCESS -> {
                // All location and BLE settings are satisfied. The client can initialize location
                // requests here.
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, listener)
            }
            com.mapzen.android.lost.api.Status.RESOLUTION_REQUIRED ->
                // Location settings are not satisfied but can be resolved by show the user the Location Settings activity
                view.getResultForLost(status)
            com.mapzen.android.lost.api.Status.INTERNAL_ERROR, com.mapzen.android.lost.api.Status.INTERRUPTED, com.mapzen.android.lost.api.Status.TIMEOUT, com.mapzen.android.lost.api.Status.CANCELLED -> {
            }
            else -> {
            }
        }// Location settings are not satisfied but cannot be resolved
    }

    override fun startTrip() {
        if (connectedToMapzen)
            setLocationListener()
        else
            createLostApiClient()
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

    override fun success(route: Route) {
        view.dismissProgressDialog()
        view.showToast("route info success")
        view.setStartTripVisibility(View.VISIBLE)
        val center = CameraUpdateFactory.newLatLng(LatLng(start!![0], start!![1]))
        val zoom = CameraUpdateFactory.zoomTo(16f)
        view.centerCamera(center)
        view.zoomCamera(zoom)
        createPolyline(route)
        listenerForRoute(route)
    }

    private fun createPolyline(route: Route) {
        Log.d("DIGZ", route.rawRoute.toString())
        val routes = route.getGeometry()
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLUE)
        polylineOptions.width(13f)
        for (i in routes.indices) {
            val latitude = routes[i].latitude
            val longitude = routes[i].longitude
            val point = LatLng(latitude, longitude)
            polylineOptions.add(point)
        }
        view.drawOnMap(polylineOptions)
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

    override fun failure(statusCode: Int) {
        view.dismissProgressDialog()
        view.showToast("Error getting route info")
    }

    private fun listenerForRoute(route: Route) {
        val routeListener = object : RouteListener {
            override fun onRouteStart() {

            }

            override fun onRecalculate(location: ValhallaLocation) {

            }

            override fun onSnapLocation(originalLocation: ValhallaLocation, snapLocation: ValhallaLocation) {

            }

            override fun onMilestoneReached(index: Int, milestone: RouteEngine.Milestone) {

            }

            override fun onApproachInstruction(index: Int) {}

            override fun onInstructionComplete(index: Int) {

            }

            override fun onUpdateDistance(distanceToNextInstruction: Int, distanceToDestination: Int) {

            }

            override fun onRouteComplete() {

            }
        }
        routeEngine = view.routeEngine
        routeEngine.setListener(routeListener)
        routeEngine.route = route

    }


    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    override fun onResult(status: Status) {

    }

    override fun createLostApiClient() {
        lostApiClient = LostApiClient.Builder(context).addConnectionCallbacks(this).build()
        lostApiClient!!.connect()
    }

    override fun onConnected() {
        connectedToMapzen = true
        view.showToast("Connected successfully")
    }

    override fun onConnectionSuspended() {
        connectedToMapzen = false
        view.showToast("Connected suspended")
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
