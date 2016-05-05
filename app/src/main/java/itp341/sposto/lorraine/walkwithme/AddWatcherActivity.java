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

/**
 * Created by LorraineSposto on 4/23/16.
 */
public class AddWatcherActivity extends FragmentActivity {
    private static final String TAG = AddWatcherActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_host);

        Intent data = getIntent();
        String origin = data.getStringExtra(Keys.KEY_ORIGIN_ADDRESS);
        String dest = data.getStringExtra(Keys.KEY_DEST_ADDRESS);
        String duration = data.getStringExtra(Keys.KEY_DURATION);
        String distance = data.getStringExtra(Keys.KEY_DISTANCE);
        ArrayList<Contact> contacts = data.getParcelableArrayListExtra(Keys.KEY_PHONE_CONTACTS);

        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.frame_container);

        if (f == null ) {
            f = AddWatcherFragment.newInstance(origin, dest, distance, duration, contacts);
        }
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, f);
        fragmentTransaction.commit();
    }
}
