package itp341.sposto.lorraine.walkwithme.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import itp341.sposto.lorraine.walkwithme.MapPaneFragment;
import itp341.sposto.lorraine.walkwithme.R;

/**
 * Created by LorraineSposto on 5/5/16.
 */
public class GetNameDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.get_name_dialog_fragment, null);
        builder.setView(v).
                setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                EditText editText = (EditText) v.findViewById(R.id.usernameEditText);
                String username = editText.getText().toString();
                ((MapPaneFragment) (getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_container))).setUsernameDialogResult(username);
                dismiss();
            }
        });

        return builder.create();
    }
}
