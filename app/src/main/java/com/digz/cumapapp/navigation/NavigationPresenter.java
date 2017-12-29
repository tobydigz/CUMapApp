package com.digz.cumapapp.navigation;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.digz.cumapapp.R;
import com.digz.cumapapp.Util;
import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter;
import com.digz.cumapapp.navigation.directioning.DetermineDirection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.lost.api.LocationSettingsRequest;
import com.mapzen.android.lost.api.LocationSettingsResult;
import com.mapzen.android.lost.api.LocationSettingsStates;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.routing.MapzenRouter;
import com.mapzen.helpers.RouteEngine;
import com.mapzen.helpers.RouteListener;
import com.mapzen.model.ValhallaLocation;
import com.mapzen.valhalla.Route;
import com.mapzen.valhalla.RouteCallback;

import java.util.ArrayList;
import java.util.List;

public class NavigationPresenter implements NavigationContract.Presenter, RouteCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, LostApiClient.ConnectionCallbacks {
    private static final String LOG_TAG = "MyActivity";
    private static final LatLngBounds BOUNDS_NIGERIA = new LatLngBounds(new LatLng(5.065341647205726, 2.9987719580531),
            new LatLng(9.9, 5.9));
    private static final int[] COLORS = new int[]{
            R.color.colorPrimaryDark,
            R.color.colorPrimary,
            R.color.colorPrimaryLight,
            R.color.colorAccent,
            R.color.primary_dark_material_light
    };
    public MapzenRouter mapzenRouter;
    public RouteEngine routeEngine;
    public DetermineDirection determineDirection;
    private double[] start, end;
    private LostApiClient lostApiClient;
    private Context context;
    private NavigationActivity view;
    private boolean bluetoothModuleInitialised = false;
    private BluetoothDevice device;
    private GoogleApiClient googleApiClient;
    private PlaceAutoCompleteAdapter adapter;
    private LocationRequest request;
    private LocationListener listener;
    private boolean connectedToMapzen = false;
    private List<Polyline> polylines = new ArrayList<>();

    public NavigationPresenter(Context context) {
        this.context = context;
    }


    @Override
    public void setView(NavigationActivity view) {
        this.view = view;
    }

    private void startRouting() {
        mapzenRouter = view.getRouter();

        mapzenRouter.setWalking();
        mapzenRouter.setLocation(start);
        mapzenRouter.setLocation(end);
        mapzenRouter.setCallback(this);
        mapzenRouter.fetch();
        createLostApiClient();

    }


    @Override
    public void setStartToNull() {
        if (start != null) start = null;
    }

    @Override
    public void setEndToNull() {
        if (end != null) end = null;
    }

    @Override
    public boolean determineIfOnline() {
        return Util.Operations.isOnline(context);
    }


    @Override
    public void setLocationListener() {
        request = LocationRequest.create()
                .setInterval(5000)
                .setSmallestDisplacement(10)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                ValhallaLocation valhallaLocation = new ValhallaLocation();
                valhallaLocation.setBearing(location.getBearing());
                valhallaLocation.setLatitude(location.getLatitude());
                valhallaLocation.setLongitude(location.getLongitude());

                routeEngine.onLocationChanged(valhallaLocation);
            }

        };

        checkLocationSettings();

    }

    @Override
    public void checkLocationSettings() {
        ArrayList<LocationRequest> requests = new ArrayList<>();
        LocationRequest highAccuracy = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requests.add(highAccuracy);
        boolean needBle = false;
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addAllLocationRequests(requests)
                .setNeedBle(needBle)
                .build();
        com.mapzen.android.lost.api.PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(lostApiClient, settingsRequest);

        LocationSettingsResult locationSettingsResult = result.await();
        LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
        com.mapzen.android.lost.api.Status status = locationSettingsResult.getStatus();
        checkStatusApi(status);


    }

    public void checkStatusApi(com.mapzen.android.lost.api.Status status) {
        switch (status.getStatusCode()) {
            case com.mapzen.android.lost.api.Status.SUCCESS:
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
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(lostApiClient, request, listener);
                break;
            case com.mapzen.android.lost.api.Status.RESOLUTION_REQUIRED:
                // Location settings are not satisfied but can be resolved by show the user the Location Settings activity
                view.getResultForLost(status);
                break;
            case com.mapzen.android.lost.api.Status.INTERNAL_ERROR:
            case com.mapzen.android.lost.api.Status.INTERRUPTED:
            case com.mapzen.android.lost.api.Status.TIMEOUT:
            case com.mapzen.android.lost.api.Status.CANCELLED:
                // Location settings are not satisfied but cannot be resolved
                break;
            default:
                break;
        }
    }

    @Override
    public void startTrip() {
        if (connectedToMapzen)
            setLocationListener();
        else createLostApiClient();
    }

    @Override
    public void setupGoogleServices() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void setUpPlaceAutoCompleteAdapter() {
        adapter = new PlaceAutoCompleteAdapter(context, android.R.layout.simple_list_item_1, googleApiClient, BOUNDS_NIGERIA, null);
        view.setPlaceAdapterToView(adapter);
    }


    @Override
    public void route() {
        if (start == null || end == null) {
            if (start == null) {
                if (view.getTextOfOriginField().length() > 0) {
                    view.setErrorOnOriginTextField("Choose location from dropdown.");
                } else {
                    view.showToast("Please choose a starting point.");
                }
            }
            if (end == null) {
                if (view.getTextOfDestinationField().length() > 0) {
                    view.setErrorOnDestinationTextField("Choose location from dropdown.");
                } else {
                    view.showToast("Please choose a destination.");
                }
            }
        } else {
            view.showProgressDialog("Please wait.", "Fetching route information.");
            startRouting();
        }
    }

    @Override
    public void onDestinationAutocompleteClicked(int position) {
        final PlaceAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    // Request did not complete successfully
                    Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                // Get the Place object from the buffer.
                final Place place = places.get(0);
                end = new double[2];
                end[0] = place.getLatLng().latitude;
                end[1] = place.getLatLng().longitude;
            }
        });
    }

    @Override
    public void onStartAutocompleteClicked(int position) {
        final PlaceAutoCompleteAdapter.PlaceAutocomplete item = adapter.getItem(position);
        final String placeId = String.valueOf(item.placeId);
        Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    // Request did not complete successfully
                    Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                // Get the Place object from the buffer.
                final Place place = places.get(0);
                start = new double[2];
                start[0] = place.getLatLng().latitude;
                start[1] = place.getLatLng().longitude;

            }
        });
    }

    @Override
    public void success(Route route) {
        view.dismissProgressDialog();
        view.showToast("route info success");
        view.setStartTripVisibility(View.VISIBLE);
        view.addMapMarker(createMarker(start, true));
        view.addMapMarker(createMarker(end, false));
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(start[0], start[1]));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        view.centerCamera(center);
        view.zoomCamera(zoom);
        createPolyline(route);
        listenerForRoute(route);
    }

    private void createPolyline(Route route) {
        Log.d("DIGZ", route.getRawRoute().toString());
        ArrayList<ValhallaLocation> routes = route.getGeometry();
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(13);
        for (int i = 0; i < routes.size(); i++) {
            double latitude = routes.get(i).getLatitude();
            double longitude = routes.get(i).getLongitude();
            LatLng point = new LatLng(latitude, longitude);
            polylineOptions.add(point);
        }
        view.drawOnMap(polylineOptions);
    }

    private MarkerOptions createMarker(double[] position, boolean startMarker) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(position[0], position[1]));
        if (startMarker)
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        else options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        return options;
    }

    @Override
    public void failure(int i) {
        view.dismissProgressDialog();
        view.showToast("Error getting route info");
    }


    private void listenerForRoute(final Route route) {
        RouteListener routeListener = new RouteListener() {
            @Override
            public void onRouteStart() {

            }

            @Override
            public void onRecalculate(ValhallaLocation location) {

            }

            @Override
            public void onSnapLocation(ValhallaLocation originalLocation, ValhallaLocation snapLocation) {

            }

            @Override
            public void onMilestoneReached(int index, RouteEngine.Milestone milestone) {

            }

            @Override
            public void onApproachInstruction(int index) {
            }

            @Override
            public void onInstructionComplete(int index) {

            }

            @Override
            public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination) {

            }

            @Override
            public void onRouteComplete() {

            }
        };
        routeEngine = view.getRouteEngine();
        routeEngine.setListener(routeListener);
        routeEngine.setRoute(route);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void createLostApiClient() {
        lostApiClient = new LostApiClient.Builder(context).addConnectionCallbacks(this).build();
        lostApiClient.connect();
    }

    @Override
    public void onConnected() {
        connectedToMapzen = true;
        view.showToast("Connected successfully");
    }

    @Override
    public void onConnectionSuspended() {
        connectedToMapzen = false;
        view.showToast("Connected suspended");
    }

    @Override
    public void setAdapterBounds(LatLngBounds bounds) {
        adapter.setBounds(BOUNDS_NIGERIA);
    }
}
