package itp341.sposto.lorraine.walkwithme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.util.ArrayList;

import itp341.sposto.lorraine.walkwithme.models.Contact;
import itp341.sposto.lorraine.walkwithme.models.MyPlace;

/**
 * Created by LorraineSposto on 5/9/16.
 */
public class RecentPlacesActivity extends FragmentActivity {
    private static final String TAG = RecentPlacesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_host);

        Intent data = getIntent();
        ArrayList<MyPlace> places = (ArrayList<MyPlace>) data.getSerializableExtra(Keys.KEY_RECENT_PLACES);

        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.frame_container);

        if (f == null ) {
            f = RecentPlacesFragment.newInstance(places);
        }
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, f);
        fragmentTransaction.commit();
    }
}
