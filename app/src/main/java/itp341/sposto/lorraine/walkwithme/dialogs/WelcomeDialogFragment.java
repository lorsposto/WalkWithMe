package itp341.sposto.lorraine.walkwithme.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import itp341.sposto.lorraine.walkwithme.Keys;
import itp341.sposto.lorraine.walkwithme.MapPaneFragment;
import itp341.sposto.lorraine.walkwithme.R;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class WelcomeDialogFragment extends DialogFragment {

    private String name;

    public static WelcomeDialogFragment newInstance(String name) {

        Bundle args = new Bundle();
        args.putString(Keys.SharedPref.KEY_USERNAME, name);

        WelcomeDialogFragment fragment = new WelcomeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        name = getArguments().getString(Keys.SharedPref.KEY_USERNAME);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("WELCOME DIALOG", "Name: " + name);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.welcome_dialog_fragment, null);
        ((TextView) v.findViewById(R.id.welcometext)).setText(getString(R.string.dialog_welcome_message));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CheckBox box = (CheckBox) v.findViewById(R.id.showWelcomeCheckbox);
                        ((MapPaneFragment) (getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container))).safePreferenceNoWelcome(!box.isChecked());
                        dismiss();
                    }
                })
        .setView(v)
        .setTitle((name == null) ? getString(R.string.dialog_welcome_title) : getString(R.string.dialog_welcome_title_named, name));

        return builder.create();
    }
}
