package itp341.sposto.lorraine.walkwithme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by LorraineSposto on 4/22/16.
 */
public class MapPaneFragment extends Fragment
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = MapPaneFragment.class.getSimpleName();
    private static final int PERMISSION_FINE_LOCATION = 0;
    private static final String MAPS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationUpdates = true;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mMarker;
    private Button mButtonStartJourney;
    private TextView mTextJourneySummary;
    private CardView mSummaryCardView;

    private Route mRoute;

    SupportPlaceAutocompleteFragment mAutocompleteFragment;

    public static MapPaneFragment newInstance() {
        
        Bundle args = new Bundle();
        
        MapPaneFragment fragment = new MapPaneFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_pane_fragment, container, false);
        FragmentManager fm = getChildFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map_fragment);

        if (f == null ) {
            f = SupportMapFragment.newInstance();
        }
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.map_fragment, f);
        fragmentTransaction.commit();

        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        mAutocompleteFragment  = (SupportPlaceAutocompleteFragment)
                fm.findFragmentById(R.id.place_autocomplete_fragment);

        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                getDirections(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mButtonStartJourney = (Button) v.findViewById(R.id.startJourneyButton);
        mTextJourneySummary = (TextView) v.findViewById(R.id.journeySummary);
        mSummaryCardView = (CardView) v.findViewById(R.id.summaryCardView);

        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady");
        mMap = map;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission granted");

            mMap.setMyLocationEnabled(true);
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "Requesting verbose location");

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                Log.d(TAG, "Requesting location");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (permissions.length == 1 &&
                        permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted");
                    mMap.setMyLocationEnabled(true);

                } else {
                    Log.d(TAG, "Location permission denied");
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            status.startResolutionForResult(
//                                    OuterClass.this,
//                                    REQUEST_CHECK_SETTINGS);
//                        } catch (SendIntentException e) {
//                            // Ignore the error.
//                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        if (mLocationUpdates) {
            try {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (lastLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13));
//                    mMarker = mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).title("Destination").draggable(true));
                }
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
            startLocationUpdates();
        }
    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.d(TAG, "StartLocationUpdates error -- " + e.getMessage());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//        updateUI();
    }

    public void onStart() {
        Log.d(TAG, "onStart, connecting to google api client");
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        Log.d(TAG, "onStop, disconnecting from google api client");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private String generateDirectionsRequestUrl(Place dest) {
        String url = MAPS_API_URL;
        url += "origin=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
        url += "&destination=place_id:" + dest.getId();
        url += "&mode=walking";
        url += "&key=" + getString(R.string.maps_api_key);
        Log.d(TAG, "url: " + url);
        return url;
    }

    private void getDirections(Place dest) {
        String url = generateDirectionsRequestUrl(dest);
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            mRoute = new Route(new JSONObject(response));
                            // get all coordinates for polyline
                            ArrayList<LatLng> coords = mRoute.getPolylineCoordinates();
                            drawRoute(coords);
                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());
            }
        });
        queue.add(stringRequest);
    }

    private void drawRoute(ArrayList<LatLng> coords) {
        PolylineOptions line = new PolylineOptions().width(10).color(Color.GREEN);

        for (LatLng latLng : coords) {
            line.add(latLng);
        }
        Polyline polyline = mMap.addPolyline(line);
        mMarker = mMap.addMarker(new MarkerOptions().position(mRoute.getmDestinationCoords()).title(mRoute.getmDestinationAddress()).draggable(false));

        // Show summary and button
        mTextJourneySummary.setText(getResources().getString(R.string.summary_template, mRoute.getDistanceInKm(), mRoute.getDurationinMinutes()));
        mSummaryCardView.setVisibility(View.VISIBLE);
    }

}
