package itp341.sposto.lorraine.walkwithme;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.digits.sdk.android.Digits;
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
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.fabric.sdk.android.Fabric;
import itp341.sposto.lorraine.walkwithme.dialogs.FinishJourneyDialogFragment;
import itp341.sposto.lorraine.walkwithme.dialogs.GetNameDialogFragment;
import itp341.sposto.lorraine.walkwithme.dialogs.StartJourneyDialogFragment;
import itp341.sposto.lorraine.walkwithme.dialogs.WelcomeDialogFragment;
import itp341.sposto.lorraine.walkwithme.models.Contact;
import itp341.sposto.lorraine.walkwithme.models.MyPlace;
import itp341.sposto.lorraine.walkwithme.models.Route;

/**
 * Created by LorraineSposto on 4/22/16.
 */
public class MapPaneFragment extends Fragment
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = MapPaneFragment.class.getSimpleName();
    private static final String PLACES_FILE  = TAG + ".recent_places";

    /**
     * Google Maps Keys and Fields
     */
    private static final int PERMISSION_FINE_LOCATION = 0;
    private static final String MAPS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    /**
     * Twitter Fabric keys and fields
     */
    // TODO Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "kB8WPjexllbSj05v6uszyKx3u";
    private static final String TWITTER_SECRET = "yXXGtJGWVdQsqCr41mlaeFNV48L6M90auKaHmwp27hvb4kIRgh";
    private boolean mIsAuthenticated;

    /**
     * Intent request codes
     */
    private static final int REQUEST_CODE_AUTHENTICATE = 1;
    private static final int REQUEST_CODE_ADD_WATCHER = 2;
    private static final int REQUEST_CODE_START_JOURNEY = 3;
    private static final int REQUEST_CODE_SEND_SMS = 4;

    /**
     * Location & Map member variables
     */
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation; // current location set onLocationChanged
    private boolean mLocationUpdates = true;
    private boolean mJourneyInProgress;

    private boolean mShowWelcome;

    /**
     * View member variables
     */
//    private MapView mMapView;
    private GoogleMap mMap;
    SupportPlaceAutocompleteFragment mAutocompleteFragment;
//    private Marker mMarker;
    private Button mButtonStartJourney, mButtonFinishJourney;
    private FloatingActionButton mButtonAddWatcher;
    private TextView mTextJourneySummary;
    private CardView mSummaryCardView;
    private ImageView mClearRoute;

    /**
     * Shared pref storage members
     */
    private ArrayList<Contact> mContacts;
    private Route mRoute;
    private Place mPlace;
    private String mOwnPhoneNumber;
    private String mUserName;
    private ArrayList<MyPlace> mPlaces;

    private AlarmManager mSMSAlarmManager;


    public MapPaneFragment() {
        mContacts = new ArrayList<>();
        mPlaces = new ArrayList<>();
    }

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

        mJourneyInProgress = false;

        // Authenticate with Fabric, if information missing, launch intent
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(getActivity(), new TwitterCore(authConfig), new Digits());

        SharedPreferences settings = getActivity().getSharedPreferences(Keys.SharedPref.SHARED_PREF_APP_NAME, 0);
        mIsAuthenticated = settings.getBoolean(Keys.SharedPref.KEY_DIGITS_IS_AUTHENTICATED, false);
        mOwnPhoneNumber = settings.getString(Keys.SharedPref.KEY_DIGITS_PHONE_NUMBER, null);
        mUserName = settings.getString(Keys.SharedPref.KEY_USERNAME, null);
        mShowWelcome = settings.getBoolean(Keys.SharedPref.KEY_SHOW_WELCOME, true);

        if (!mIsAuthenticated || mOwnPhoneNumber == null) {
            Intent i = new Intent(getActivity(), AuthenticateActivity.class);
            startActivityForResult(i, REQUEST_CODE_AUTHENTICATE);
        }

        // Check if their name was not saved
        if (mUserName == null) {
            new GetNameDialogFragment().show(getChildFragmentManager(), "GetNameDialog");
        }

        if (mShowWelcome) WelcomeDialogFragment.newInstance(mUserName).show(getChildFragmentManager(), "WelcomeDialog");

        // read recent places
        readRecentPlaces();

        // Set up Google API Client
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();
        }

        mSMSAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate fragment
        View v = inflater.inflate(R.layout.map_pane_fragment, container, false);
        FragmentManager fm = getChildFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map_fragment);

        if (f == null ) {
            f = SupportMapFragment.newInstance();
        }
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.map_fragment, f);
        fragmentTransaction.commit();

        // Initialize Google Map & Autocomplete field
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        // Draw directions when autocomplete fragment
        mAutocompleteFragment  = (SupportPlaceAutocompleteFragment)
                fm.findFragmentById(R.id.place_autocomplete_fragment);

        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "onPlaceSelected: " + place.getName());
                mPlace = place;
                getDirections(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "onPlaceSelectedListener, An error occurred: " + status);
            }
        });

        mAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) mAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
//                view.setVisibility(View.GONE);
                mMap.clear();
                mSummaryCardView.setVisibility(View.GONE);
                zoomToDevice(mCurrentLocation, 15);
            }
        });

        // Handle intent to add contact to watcher list
        mButtonAddWatcher = (FloatingActionButton) v.findViewById(R.id.addWatcherButton);
        mButtonAddWatcher.setOnClickListener(addWatcherOnClickListener);

        // Handle functionality to start Journey
        // Sets up SMS on timer to notify contacts in 1/2 the travel time and at the end of travel time
        // Swaps button to Finish Journey
        mButtonStartJourney = (Button) v.findViewById(R.id.startJourneyButton);
        mButtonStartJourney.setOnClickListener(startJourneyOnClickListener);

        // Handle functionality to finish journey
        // Sends SMS to each contact notifying finished journey
        // TODO UI Update and save route?
        mButtonFinishJourney = (Button) v.findViewById(R.id.finishJourneyButton);
        mButtonFinishJourney.setOnClickListener(finishJourneyOnClickListener);

        mTextJourneySummary = (TextView) v.findViewById(R.id.journeySummary);
        mSummaryCardView = (CardView) v.findViewById(R.id.summaryCardView);
        mClearRoute = (ImageView) v.findViewById(R.id.clearRouteButton);
        mClearRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetJourney();
            }
        });

        return v;
    }

    private void launchSMSAlarm(int when, String message) {
        Log.d(TAG, "launchSMSAlarm for minutes: " + when);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.MINUTE, when);

        Intent intent = new Intent(getContext(), NotifyReceiver.class);
        intent.putExtra(Keys.INTENT_CODE, Keys.SMS_CODE);
        intent.putExtra(Keys.KEY_MY_NUMBER, mOwnPhoneNumber);
        intent.putExtra(Keys.KEY_MESSAGE, message);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        mSMSAlarmManager.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5, alarmIntent);
    }


    private void setTimedNotification(int minutes, String message) {
        Log.d(TAG, "setTimedNotification for minutes: " + minutes);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.MINUTE, minutes);
        Intent i = new Intent(getContext(), NotifyReceiver.class);
        i.putExtra(Keys.INTENT_CODE, Keys.NOTIF_CODE);
        i.putExtra(Keys.KEY_MESSAGE, message);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), (int) System.currentTimeMillis(), i, 0);

        mSMSAlarmManager.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5, pi);
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
                Log.d(TAG, "Requesting location with rantionale");

                /**
                 * Show an expanation to the user *asynchronously* -- don't block
                 this thread waiting for the user's response! After the user
                 sees the explanation, try again to request the permission.
                 */

            } else {
                Log.d(TAG, "Requesting location");
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_FINE_LOCATION);
                /**
                 * MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                 app-defined int constant. The callback method gets the
                 result of the request.
                 */
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (permissions.length == 1 &&
                        permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission granted, enabling myLocation in Map");
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
                        Log.d(TAG, "LocationSettingsStatusCodes.SUCCESS");
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
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
                        Log.d(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        // Move camera to last known location
        if (mLocationUpdates) {
            try {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                zoomToDevice(lastLocation, 15);
//                    mMarker = mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).title("Destination").draggable(true));
            } catch (SecurityException e) {
                Log.d(TAG, e.getMessage());
            }
            startLocationUpdates();
        }
    }

    private void zoomToDevice(Location location, int size) {
        if (location == null) return;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), size));
    }

    protected void createLocationRequest() {
        // starts polling device location
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
        mCurrentLocation = location;
        if (mJourneyInProgress) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())
                )) {
                zoomToDevice(mCurrentLocation, 16);
            }
        }
    }

    public void onStart() {
        Log.d(TAG, "onStart, connecting to google api client");
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        Log.d(TAG, "onStop, disconnecting from google api client");
        mGoogleApiClient.disconnect();
        writeRecentPlaces();
        super.onStop();
    }

    private void writeRecentPlaces() {
        Log.d(TAG, "writeRecentPlaces");
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getActivity().openFileOutput(PLACES_FILE, 0));
            outputStream.writeObject(mPlaces);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            Log.d(TAG, "Could not write recent places");
            e.printStackTrace();
        }
    }

    private void readRecentPlaces() {
        Log.d(TAG, "readRecentPlaces");
//        File f = new File(getActivity().getFilesDir(), PLACES_FILE);
//        f.delete();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(getActivity().openFileInput(PLACES_FILE));
            mPlaces = (ArrayList<MyPlace>) inputStream.readObject();
        } catch (IOException e) {
            Log.d(TAG, "Could not open recent places");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Could not open recent places");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MyPlace> getRecentPlaces() {
        return mPlaces;
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

    /**
     * Queries Google Directions API to get route from current location to destination.
     * Draws the polyline route on the map.
     * @param dest Place destination
     */
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

    /**
     * Draws an arraylist of latlng coordinates on the Map view
     * @param coords polyline coordations (latlng) from google api route
     */
    private void drawRoute(ArrayList<LatLng> coords) {
        mMap.clear();
        PolylineOptions line = new PolylineOptions().width(10).color(Color.GREEN);

        for (LatLng latLng : coords) {
            line.add(latLng);
        }
        Polyline polyline = mMap.addPolyline(line);
        Marker marker = mMap.addMarker(new MarkerOptions().position(mRoute.getmDestinationCoords()).title(mRoute.getmDestinationAddress()).draggable(false));

        // zoom camera to fit route
        LatLngBounds bounds = LatLngBounds.builder().include(mRoute.getmOriginCoords()).include(mRoute.getmDestinationCoords()).build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 400));

        // Show summary and button
        mTextJourneySummary.setText(getResources().getString(R.string.summary_template, mRoute.getDistanceInKm(), mRoute.getDurationinMinutes()));
        mSummaryCardView.setVisibility(View.VISIBLE);
        mButtonAddWatcher.setVisibility(View.VISIBLE);
    }

    private void sendSMS(String phoneNumber, String message) throws Exception {
        Log.d(TAG, "sendSMS: Sending SMS to phone number: " + phoneNumber + "; message: " + message);
        phoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
//            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), REQUEST_CODE_SEND_SMS, new Intent(getActivity(), MapPaneFragment.class), 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void setUsernameDialogResult(String username) {
        mUserName = username;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Keys.SharedPref.SHARED_PREF_APP_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Keys.SharedPref.KEY_USERNAME, username);
        editor.apply();
        Log.d(TAG, "Set username to " + mUserName);
    }

    public void setPlaceGetDirections(String placeID) {
        Log.d(TAG, "setPlaceGetDirections");
        DrawerLayout menu = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        menu.closeDrawers();

        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeID).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                    final Place myPlace = places.get(0);
                    Log.i(TAG, "Place found: " + myPlace.getName());
                    mPlace = myPlace.freeze();
                    getDirections(mPlace);
                } else {
                    Log.e(TAG, "Place not found");
                }
                places.release();
            }
        });
    }

    private void resetJourney() {
        mSummaryCardView.setVisibility(View.GONE);
        mButtonFinishJourney.setVisibility(View.GONE);
        mButtonStartJourney.setVisibility(View.VISIBLE);
        mButtonAddWatcher.setVisibility(View.GONE);
        mClearRoute.setVisibility(View.VISIBLE);
        ((EditText) mAutocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("");
        mMap.clear();
        zoomToDevice(mCurrentLocation, 15);
    }

    public void safePreferenceNoWelcome(boolean show) {
        SharedPreferences pref = getActivity().getSharedPreferences(Keys.SharedPref.SHARED_PREF_APP_NAME, 0);
        SharedPreferences.Editor e = pref.edit();
        e.putBoolean(Keys.SharedPref.KEY_SHOW_WELCOME, show);
        e.apply();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_CODE_AUTHENTICATE:
                    Log.d(TAG, "onActivityResult, REQUEST_CODE_AUTHENTICATE");
                    long sessionId = data.getLongExtra(Keys.SharedPref.KEY_DIGITS_ID, -1);
                    String phoneNumber = data.getStringExtra(Keys.SharedPref.KEY_DIGITS_PHONE_NUMBER);

                    SharedPreferences settings = getActivity().getSharedPreferences(Keys.SharedPref.SHARED_PREF_APP_NAME, 0);
                    SharedPreferences.Editor e = settings.edit();
                    e.putBoolean(Keys.SharedPref.KEY_DIGITS_IS_AUTHENTICATED, true);
                    e.putString(Keys.SharedPref.KEY_DIGITS_PHONE_NUMBER, phoneNumber);
                    e.apply();
                    Toast.makeText(getActivity().getApplicationContext(), "Authentication successful for "
                            + "id: " + sessionId + ", #: " + phoneNumber, Toast.LENGTH_LONG).show();
                    mOwnPhoneNumber = phoneNumber;
                    break;
                case REQUEST_CODE_ADD_WATCHER:
                    Log.d(TAG, "onActivityResult, REQUEST_CODE_ADD_WATCHER");
                    mContacts = data.getParcelableArrayListExtra(Keys.KEY_PHONE_CONTACTS);
                    break;
                case REQUEST_CODE_START_JOURNEY:
                    break;
                default:
                    Log.d(TAG, "onActivityResult. Request code: " + requestCode);
                    break;
            }
        } else {
            Log.d(TAG, "onActivityResult, cancelled");
        }
    }

    View.OnClickListener addWatcherOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "addWatcher onClick");
            Intent i = new Intent(getActivity(), AddWatcherActivity.class);
            i.putExtra(Keys.KEY_DEST_ADDRESS, mRoute.getmOriginAddress());
            i.putExtra(Keys.KEY_DEST_ADDRESS, mRoute.getmDestinationAddress());
            i.putExtra(Keys.KEY_DURATION, mRoute.getDurationinMinutes() + " minutes");
            i.putExtra(Keys.KEY_DISTANCE, mRoute.getDistanceInKm() + " km");
            i.putExtra(Keys.KEY_PHONE_CONTACTS, mContacts);
            startActivityForResult(i, REQUEST_CODE_ADD_WATCHER);
        }
    };

    View.OnClickListener startJourneyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "startJourney onClick");

            if (mContacts.isEmpty()) {
                new AlertDialog.Builder(getContext()).setTitle(getString(R.string.dialog_no_contacts_title)).setMessage(R.string.dialog_no_contacts_message).show();
                return;
            }

            mJourneyInProgress = true;
            String halfSMSMessage = getString(R.string.message_notify_halfway_sms, mUserName);
            String fullSMSMessage = getString(R.string.message_notify_fullway_sms, mUserName);
            String halfNotifMessage = getString(R.string.notification_halfway_message);
            String fullNotifMessage = getString(R.string.notification_fullway_message);

            launchSMSAlarm((int) mRoute.getDurationinMinutes() / 2, halfSMSMessage);
            launchSMSAlarm((int) mRoute.getDurationinMinutes(), fullSMSMessage);
            setTimedNotification((int) mRoute.getDurationinMinutes() / 2, halfNotifMessage);
            setTimedNotification((int) mRoute.getDurationinMinutes(), fullNotifMessage);
            mButtonFinishJourney.setVisibility(View.VISIBLE);
            mButtonStartJourney.setVisibility(View.GONE);

            if (mContacts.size() > 0) {
                for (Contact c : mContacts) {
                    try {
                        sendSMS(c.getPhoneNumber(), getString(R.string.message_notify_start_sms, mUserName, mRoute.getDurationinMinutes()));
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.toast_sms_failed, c.getPhoneNumber()), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // Open dialogue to notify start journey and contacts
            StartJourneyDialogFragment.newInstance(mRoute.getmDestinationAddress(), mRoute.getDurationinMinutes()).show(getChildFragmentManager(), "StartJourneyDialog");
            zoomToDevice(mCurrentLocation, 16);

            // add this place to recent places
            MyPlace p = new MyPlace(mPlace.getName().toString() ,mPlace.getId(), mPlace.getAddress().toString(), new Date());
            int index = mPlaces.indexOf(p);
            if (index >= 0) {
                mPlaces.remove(index);
            }
            mPlaces.add(p);

            // remove clear button, only complete journey
            mClearRoute.setVisibility(View.GONE);
        }
    };

    View.OnClickListener finishJourneyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "finishJourney, sending SMS to contacts");
            mJourneyInProgress = false;
            for (Contact c : mContacts) {
                try {
                    sendSMS(c.getPhoneNumber(), getString(R.string.message_notify_end_sms, mUserName));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.toast_sms_failed, c.getPhoneNumber()), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }

            // TODO some UI update
            FinishJourneyDialogFragment.newInstance(mRoute.getmDestinationAddress()).show(getFragmentManager(), "FinishJourneyDialog");
            resetJourney();
        }
    };
}
