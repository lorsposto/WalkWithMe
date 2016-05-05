package itp341.sposto.lorraine.walkwithme.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collections;

import itp341.sposto.lorraine.walkwithme.Keys;
import itp341.sposto.lorraine.walkwithme.MapPaneFragment;
import itp341.sposto.lorraine.walkwithme.R;
import itp341.sposto.lorraine.walkwithme.models.MyPlace;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class RecentPlacesDialog extends DialogFragment {

    private ArrayList<MyPlace> mPlaces;

    public static RecentPlacesDialog newInstance(ArrayList<MyPlace> places) {

        Bundle args = new Bundle();
        args.putSerializable(Keys.KEY_RECENT_PLACES, places);

        RecentPlacesDialog fragment = new RecentPlacesDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mPlaces = (ArrayList<MyPlace>)b.getSerializable(Keys.KEY_RECENT_PLACES);
        Collections.sort(mPlaces);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] addresses = new String[mPlaces.size()];
        for (int i=0; i < addresses.length; ++i) {
            addresses[i] = mPlaces.get(i).getPlaceName() + ", " + mPlaces.get(i).getAddress();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Add the buttons
        builder.setTitle(getString(R.string.dialog_recent_places_title))
                .setItems(addresses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MapPaneFragment) (getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container))).setPlaceGetDirections(mPlaces.get(which).getId());
                    }
                });

        return builder.create();
    }
}
