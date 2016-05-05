package itp341.sposto.lorraine.walkwithme.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import itp341.sposto.lorraine.walkwithme.Keys;
import itp341.sposto.lorraine.walkwithme.MapPaneFragment;
import itp341.sposto.lorraine.walkwithme.R;
import itp341.sposto.lorraine.walkwithme.models.Route;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class StartJourneyDialogFragment extends DialogFragment {

    private double mDuration;
    private String mDestination;

    public static StartJourneyDialogFragment newInstance(String destination, double duration) {
        
        Bundle args = new Bundle();
        args.putString(Keys.KEY_DEST_ADDRESS, destination);
        args.putDouble(Keys.KEY_DURATION, duration);
        
        StartJourneyDialogFragment fragment = new StartJourneyDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        mDuration = b.getDouble(Keys.KEY_DURATION);
        mDestination = b.getString(Keys.KEY_DEST_ADDRESS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                }).setTitle(getString(R.string.dialog_journey_started_title))
                .setMessage(getString(R.string.dialog_journey_started_message, mDestination, mDuration/2, mDuration));

        return builder.create();
    }

}
