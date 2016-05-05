package itp341.sposto.lorraine.walkwithme.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LorraineSposto on 4/23/16.
 */
public class Step {
    private int mDuration; // seconds
    private int mDistance; // meters
    private LatLng mEndLocation;
    private LatLng mStartLocation;
    private String mPolylineStr;

    public Step(int duration, int distance, LatLng startLocation, LatLng endLocation, String polyline) {
        mDuration = duration;
        mDistance = distance;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mPolylineStr = polyline;
    }

    public List<LatLng> getDecodedPolyline() {
        return PolyUtil.decode(mPolylineStr);
    }

    public int getmDuration() {
        return mDuration;
    }

    public void setmDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public int getmDistance() {
        return mDistance;
    }

    public void setmDistance(int mDistance) {
        this.mDistance = mDistance;
    }

    public LatLng getmEndLocation() {
        return mEndLocation;
    }

    public void setmEndLocation(LatLng mEndLocation) {
        this.mEndLocation = mEndLocation;
    }

    public LatLng getmStartLocation() {
        return mStartLocation;
    }

    public void setmStartLocation(LatLng mStartLocation) {
        this.mStartLocation = mStartLocation;
    }

    public String getmPolylineStr() {
        return mPolylineStr;
    }

    public void setmPolylineStr(String mPolylineStr) {
        this.mPolylineStr = mPolylineStr;
    }

    @Override
    public String toString() {
        return "Step{" +
                "mDuration=" + mDuration +
                ", mDistance=" + mDistance +
                ", mEndLocation=" + mEndLocation +
                ", mStartLocation=" + mStartLocation +
                ", mPolylineStr='" + mPolylineStr + '\'' +
                '}';
    }
}
