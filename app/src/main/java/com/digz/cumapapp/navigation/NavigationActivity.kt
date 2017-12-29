package com.digz.cumapapp.navigation

import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.digz.cumapapp.R
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter
import com.digz.cumapapp.navigation.directioning.DetermineDirection
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.mapzen.android.lost.api.Status
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.helpers.RouteEngine

class NavigationActivity : AppCompatActivity(), View.OnClickListener, NavigationContract.View, OnMapReadyCallback {

    lateinit var presenter: NavigationPresenter

    private var progressDialog: ProgressDialog? = null
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction)
        router
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        presenter = NavigationPresenter(this)
        presenter.setView(this)
        presenter.setupGoogleServices()
        setupMaps()


        //        binding.startTrip.setOnClickListener(this);
        //        binding.send.setOnClickListener(this);
        //        binding.goLeft.setOnClickListener(this);
        //        binding.goRight.setOnClickListener(this);
        setClickListeners()

        //        setTextWatchers();

        progressDialog = ProgressDialog(this)
        val intent = intent
        val bundle = intent.extras
    }

    /*  private void setTextWatchers() {
        *//*
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * *//*
        binding.startAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                presenter.setStartToNull();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.endAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                presenter.setEndToNull();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }*/

    private fun setClickListeners() {
        /*
        * Sets the start and destination points based on the values selected
        * from the autocomplete text views.
        * */
        /*binding.startAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                presenter.onStartAutocompleteClicked(position);

            }
        });*/
        /*binding.endAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                presenter.onDestinationAutocompleteClicked(position);

            }
        });*/
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
                progressDialog!!.show()
                presenter.startTrip()
            }
            R.id.goLeft -> {
            }
            R.id.goRight -> {
            }
        }
    }

    override fun showProgressDialog(title: String, message: String) {
        progressDialog!!.setTitle(title)
        progressDialog!!.setMessage(message)
        progressDialog!!.show()
    }

    override fun dismissProgressDialog() {
        progressDialog!!.dismiss()
    }


    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun setStartTripVisibility(visibility: Int) {
        //        binding.startTrip.setVisibility(visibility);
    }


    override fun onMapReady(map: GoogleMap) {
        this.map = map
        presenter.setUpPlaceAutoCompleteAdapter()
        map.setOnCameraChangeListener {
            val bounds = this@NavigationActivity.map!!.projection.visibleRegion.latLngBounds
            presenter.setAdapterBounds(bounds)
        }

        val center = CameraUpdateFactory.newLatLng(LatLng(6.667876, 3.151196))
        val zoom = CameraUpdateFactory.zoomTo(8f)

        map.moveCamera(center)
        map.animateCamera(zoom)
    }

    override fun setPlaceAdapterToView(adapter: PlaceAutoCompleteAdapter) {
        //        binding.startAutoComplete.setAdapter(adapter);
        //        binding.endAutoComplete.setAdapter(adapter);

    }

    override fun getResultForLost(status: Status) {
        try {
            status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }

    }

    override fun setLeftRightVisibility(visibility: Int) {
        //        binding.goLeft.setVisibility(visibility);
        //        binding.goRight.setVisibility(visibility);
    }

    override fun getTextOfOriginField(): String {
        //        startAutoComplete.getText().toString();
        return ""
    }

    override fun getTextOfDestinationField(): String {
        //        binding.endAutoComplete.getText().toString();
        return ""
    }

    override fun setErrorOnOriginTextField(error: String) {
        //        binding.startAutoComplete.setError(error);
    }

    override fun setErrorOnDestinationTextField(error: String) {
        //        binding.endAutoComplete.setError(error);
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

    override fun getRouter(): MapzenRouter {
        return MapzenRouter(this/*, "mapzen-4DXdxtn"*/)
    }

    override fun getRouteEngine(): RouteEngine {
        return RouteEngine()
    }

    override fun determineDirection(): DetermineDirection {
        return DetermineDirection()
    }

    companion object {

        private val REQUEST_CHECK_SETTINGS = 100
    }
}
