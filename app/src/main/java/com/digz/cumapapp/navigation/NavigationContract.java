package com.digz.cumapapp.navigation;

import android.bluetooth.BluetoothDevice;

import com.digz.cumapapp.adapter.PlaceAutoCompleteAdapter;
import com.digz.cumapapp.navigation.directioning.DetermineDirection;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapzen.android.lost.api.Status;
import com.mapzen.android.routing.MapzenRouter;
import com.mapzen.helpers.RouteEngine;

public interface NavigationContract {
    interface View{
        void showProgressDialog(String title, String message);
        void dismissProgressDialog();
        void showToast(String message);
        void setStartTripVisibility(int visibility);
        String getTextOfOriginField();
        String getTextOfDestinationField();
        void setErrorOnOriginTextField(String textField);
        void setErrorOnDestinationTextField(String textField);
        void setPlaceAdapterToView(PlaceAutoCompleteAdapter adapter);
        void getResultForLost(Status status);
        void setLeftRightVisibility(int visibility);
        void centerCamera(CameraUpdate center);
        void zoomCamera(CameraUpdate zoom);
        Polyline drawOnMap(PolylineOptions polylineOptions);
        void addMapMarker(MarkerOptions markerOptions);
        MapzenRouter getRouter();
        RouteEngine getRouteEngine();
        DetermineDirection determineDirection();
    }

    interface Presenter{
        void setView(NavigationActivity view);
        void setStartToNull();
        void setEndToNull();
        boolean determineIfOnline();
        void setLocationListener();
        void setupGoogleServices();
        void setUpPlaceAutoCompleteAdapter();
        void route();
        void onDestinationAutocompleteClicked(int position);
        void onStartAutocompleteClicked(int position);
        void createLostApiClient();
        void checkLocationSettings();
        void startTrip();
        void setAdapterBounds(LatLngBounds bounds);
    }
}
