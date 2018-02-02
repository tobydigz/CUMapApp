package com.digz.cumapapp.navigation

import android.location.Location
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions

interface NavigationContract {
    interface View {

        val textOfOriginField: String

        val textOfDestinationField: String

        fun showProgressDialog(title: String, message: String)

        fun dismissProgressDialog()

        fun showToast(message: String)

        fun setStartTripVisibility(visibility: Int)

        fun setErrorOnOriginTextField(error: String?)

        fun setErrorOnDestinationTextField(error: String?)

        fun setPlaceAdapterToView(adapter: PlaceAutoCompleteAdapter)

        fun centerCamera(center: CameraUpdate)

        fun zoomCamera(zoom: CameraUpdate)

        fun drawOnMap(polylineOptions: PolylineOptions): Polyline

        fun setStartAutoCompleteText()

        fun addMapMarker(markerOptions: MarkerOptions)

        fun startNavigation(options:NavigationViewOptions)

    }

    interface Presenter {
        fun onAttach(view: NavigationActivity)

        fun setStartToNull()

        fun setEndToNull()

        fun setStart(location:Location)

        fun determineIfOnline(): Boolean

        fun setLocationListener()

        fun setupGoogleServices()

        fun setUpPlaceAutoCompleteAdapter()

        fun route()

        fun onDestinationAutocompleteClicked(position: Int)

        fun onStartAutocompleteClicked(position: Int)

        fun checkLocationSettings()

        fun startTrip()

        fun setAdapterBounds(bounds: LatLngBounds)
    }
}
