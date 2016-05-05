package itp341.sposto.lorraine.walkwithme.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import itp341.sposto.lorraine.walkwithme.Keys;
import itp341.sposto.lorraine.walkwithme.R;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class FinishJourneyDialogFragment extends DialogFragment {

    private String mDestination;

    public static FinishJourneyDialogFragment newInstance(String destination) {

        Bundle args = new Bundle();
        args.putString(Keys.KEY_DEST_ADDRESS, destination);

        FinishJourneyDialogFragment fragment = new FinishJourneyDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
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
        }).setTitle(getString(R.string.dialog_journey_finished_title))
                .setMessage(getString(R.string.dialog_journey_finished_message, mDestination));

        return builder.create();
    }

}
