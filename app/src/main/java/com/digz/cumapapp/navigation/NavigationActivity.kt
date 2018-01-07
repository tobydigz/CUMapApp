package com.digz.cumapapp.navigation

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.digz.cumapapp.R
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_direction.*

class NavigationActivity : AppCompatActivity(), View.OnClickListener, NavigationContract.View, OnMapReadyCallback {

    lateinit var presenter: NavigationPresenter

    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        presenter = NavigationPresenter(this)
        presenter.onAttach(this)
        presenter.setupGoogleServices()
        setupMaps()


        startTrip.setOnClickListener(this)
        send.setOnClickListener(this)
        setClickListeners()

        setTextWatchers()

    }

    private fun setTextWatchers() {

        /* These text watchers set the start and end points to null because once there's
         * a change after a value has been selected from the dropdown
         * then the value has to reselected from dropdown to get
         * the correct location.
         */
        startAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, startNum: Int, before: Int, count: Int) {
                presenter.setStartToNull()

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        endAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                presenter.setEndToNull()


            }

            override fun afterTextChanged(s: Editable) {

            }
        })


    }

    private fun setClickListeners() {
        startAutoComplete.setOnItemClickListener { _, _, position, _ ->

            presenter.onStartAutocompleteClicked(position)

        }

        endAutoComplete.setOnItemClickListener { _, _, position, _ ->
            presenter.onDestinationAutocompleteClicked(position)

        }

    }

    private fun setupMaps() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.send -> if (presenter.determineIfOnline()) {
                presenter.route()
            } else {
                Toast.makeText(this, "No internet connectivity", Toast.LENGTH_SHORT).show()
            }
            R.id.startTrip -> {
                progress_bar.visibility = View.VISIBLE
                presenter.startTrip()
            }
        }
    }

    override fun showProgressDialog(title: String, message: String) {
        progress_bar.visibility = View.VISIBLE

    }

    override fun dismissProgressDialog() {
        progress_bar.visibility = View.GONE
    }


    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun setStartTripVisibility(visibility: Int) {
        startTrip.visibility = visibility
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        presenter.setUpPlaceAutoCompleteAdapter()

        val CU = LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))

        map.setLatLngBoundsForCameraTarget(CU)
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(CU, 0))
        presenter.setAdapterBounds(CU)
    }

    override fun setPlaceAdapterToView(adapter: PlaceAutoCompleteAdapter) {
        startAutoComplete.setAdapter(adapter)
        endAutoComplete.setAdapter(adapter)
    }

    override fun setErrorOnOriginTextField(error: String?) {
        startAutoComplete.setError(error)
    }

    override fun setErrorOnDestinationTextField(error: String?) {
        endAutoComplete.setError(error)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS ->
                // Check the location settings again and continue
                presenter.checkLocationSettings()
            else -> {
            }
        }
    }


    override fun centerCamera(center: CameraUpdate) {
        map!!.moveCamera(center)
    }

    override fun zoomCamera(zoom: CameraUpdate) {
        map!!.moveCamera(zoom)
    }

    override fun drawOnMap(polylineOptions: PolylineOptions): Polyline {
        return map!!.addPolyline(polylineOptions)
    }

    override fun addMapMarker(markerOptions: MarkerOptions) {
        map!!.addMarker(markerOptions)
    }

    override val textOfOriginField: String
        get() = startAutoComplete.getText().toString()
    override val textOfDestinationField: String
        get() = endAutoComplete.getText().toString()

    companion object {

        private val REQUEST_CHECK_SETTINGS = 100
    }
}
