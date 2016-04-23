package itp341.sposto.lorraine.walkwithme;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LorraineSposto on 4/23/16.
 */
public class Route {

    private static final String TAG = Route.class.getSimpleName();

    private JSONObject mResponse;
    private ArrayList<LatLng> mCoordinates;
    private ArrayList<String> mInstructions;
    private ArrayList<Step> mSteps;
    private String mOriginAddress;
    private LatLng mOriginCoords;
    private String mDestinationAddress;
    private LatLng mDestinationCoords;
    private long mDistance; // meters
    private long mDuration; // seconds

    public Route(JSONObject obj) {
        mResponse = obj;
        extractDistanceAndDuration();
        extractSteps();
    }

    private void extractDistanceAndDuration() {
        try {
            Log.d(TAG, "extractDistanceAndDuration");
            JSONArray routes = mResponse.getJSONArray("routes");
            JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
            mDistance = leg.getJSONObject("distance").getLong("value");
            mDuration = leg.getJSONObject("duration").getLong("value");
        } catch (JSONException e) {
            Log.d(TAG, "Error in extractSteps " + e.getMessage());
        }
    }

    private void extractSteps() {
        if (mSteps == null) {
            mSteps = new ArrayList<>();
        } else {
            mSteps.clear();
        }
        try {
            Log.d(TAG, "extractSteps");
            JSONArray routes = mResponse.getJSONArray("routes");
            JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
            mOriginCoords = new LatLng(leg.getJSONObject("start_location").getDouble("lat"), leg.getJSONObject("start_location").getDouble("lng"));
            mDestinationCoords = new LatLng(leg.getJSONObject("end_location").getDouble("lat"), leg.getJSONObject("end_location").getDouble("lng"));
            mOriginAddress = leg.getString("start_address");
            mDestinationAddress = leg.getString("end_address");

            JSONArray steps = leg.getJSONArray("steps");
            JSONObject step;
            for (int i=0; i < steps.length(); ++i) {
                step = steps.getJSONObject(i);
                int duration = step.getJSONObject("duration").getInt("value");
                int distance = step.getJSONObject("distance").getInt("value");
                LatLng start = new LatLng(step.getJSONObject("start_location").getDouble("lat"),
                        step.getJSONObject("start_location").getDouble("lng"));
                LatLng end = new LatLng(step.getJSONObject("end_location").getDouble("lat"),
                        step.getJSONObject("end_location").getDouble("lng"));
                String polyline = step.getJSONObject("polyline").getString("points");
                mSteps.add(new Step(duration, distance, start, end, polyline));
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error in extractSteps " + e.getMessage());
        }
    }

    public ArrayList<LatLng> getPolylineCoordinates() {
        if (mSteps == null) {
            return null;
        }
        ArrayList<LatLng> coords = new ArrayList<>();
        for (Step s : mSteps) {
            coords.addAll(s.getDecodedPolyline());
        }
        return coords;
    }

    public JSONObject getmResponse() {
        return mResponse;
    }

    public void setmResponse(JSONObject mResponse) {
        this.mResponse = mResponse;
    }

    public ArrayList<LatLng> getmCoordinates() {
        return mCoordinates;
    }

    public void setmCoordinates(ArrayList<LatLng> mCoordinates) {
        this.mCoordinates = mCoordinates;
    }

    public ArrayList<String> getmInstructions() {
        return mInstructions;
    }

    public void setmInstructions(ArrayList<String> mInstructions) {
        this.mInstructions = mInstructions;
    }

    public ArrayList<Step> getmSteps() {
        return mSteps;
    }

    public void setmSteps(ArrayList<Step> mSteps) {
        this.mSteps = mSteps;
    }

    public LatLng getmDestinationCoords() {
        return mDestinationCoords;
    }

    public void setmDestinationCoords(LatLng mDestinationCoords) {
        this.mDestinationCoords = mDestinationCoords;
    }

    public String getmOriginAddress() {
        return mOriginAddress;
    }

    public void setmOriginAddress(String mOriginAddress) {
        this.mOriginAddress = mOriginAddress;
    }

    public LatLng getmOriginCoords() {
        return mOriginCoords;
    }

    public void setmOriginCoords(LatLng mOriginCoords) {
        this.mOriginCoords = mOriginCoords;
    }

    public String getmDestinationAddress() {
        return mDestinationAddress;
    }

    public double getDurationinMinutes() {
        Log.d(TAG, "duration is " + mDuration);
        return mDuration/60;
    }

    public double getDistanceInKm() {
        Log.d(TAG, "distance is " + mDistance);
        return mDistance/1000;
    }

    public void setmDestinationAddress(String mDestinationAddress) {
        this.mDestinationAddress = mDestinationAddress;
    }
}
