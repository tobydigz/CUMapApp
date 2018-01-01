package com.digz.cumapapp.navigation

import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapzen.android.lost.api.Status
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.helpers.RouteEngine

interface NavigationContract {
    interface View {

        val textOfOriginField: String

        val textOfDestinationField: String

        val router: MapzenRouter

        val routeEngine: RouteEngine

        fun showProgressDialog(title: String, message: String)

        fun dismissProgressDialog()

        fun showToast(message: String)

        fun setStartTripVisibility(visibility: Int)

        fun setErrorOnOriginTextField(textField: String)

        fun setErrorOnDestinationTextField(textField: String)

        fun setPlaceAdapterToView(adapter: PlaceAutoCompleteAdapter)

        fun getResultForLost(status: Status)

        fun centerCamera(center: CameraUpdate)

        fun zoomCamera(zoom: CameraUpdate)

        fun drawOnMap(polylineOptions: PolylineOptions): Polyline

        fun addMapMarker(markerOptions: MarkerOptions)

    }

    interface Presenter {
        fun setView(view: NavigationActivity)

        fun setStartToNull()

        fun setEndToNull()

        fun determineIfOnline(): Boolean

        fun setLocationListener()

        fun setupGoogleServices()

        fun setUpPlaceAutoCompleteAdapter()

        fun route()

        fun onDestinationAutocompleteClicked(position: Int)

        fun onStartAutocompleteClicked(position: Int)

        fun createLostApiClient()

        fun checkLocationSettings()

        fun startTrip()

        fun setAdapterBounds(bounds: LatLngBounds)
    }
}
