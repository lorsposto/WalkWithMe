package itp341.sposto.lorraine.walkwithme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

/**
 * Created by LorraineSposto on 4/25/16.
 */
public class AddWatcherFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AddWatcherFragment.class.getSimpleName();

    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Data.DISPLAY_NAME
    };
    private final static int[] TO_IDS = {
            android.R.id.text1
    };
    private ListView mContactsList;
    private long mContactId;
    private String mContactKey;
    private Uri mContactUri;
    private SimpleCursorAdapter mCursorAdapter;

    private ArrayList<String> mPhoneNumbers;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION =
            {
                    ContactsContract.Data._ID,
                    ContactsContract.Data.LOOKUP_KEY,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    Build.VERSION.SDK_INT
                            >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data._ID

            };

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;

    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY + " LIKE ? " :
                    ContactsContract.Data.DISPLAY_NAME + " LIKE ? ")
                    + " AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
    private String mSearchString = "";
    private String[] mSelectionArgs = { mSearchString };

    private Button mButtonNotify;

    public AddWatcherFragment() {
        mPhoneNumbers = new ArrayList<>();
    }

    public static AddWatcherFragment newInstance(String origin, String dest, String distance, String duration) {

        Bundle args = new Bundle();
        args.putString(Keys.KEY_ORIGIN_ADDRESS, origin);
        args.putString(Keys.KEY_DEST_ADDRESS, dest);
        args.putString(Keys.KEY_DISTANCE, distance);
        args.putString(Keys.KEY_DURATION, duration);

        AddWatcherFragment fragment = new AddWatcherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_watcher_fragment,
                container, false);
        mButtonNotify = (Button) v.findViewById(R.id.notifyButton);
        mButtonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Keys.KEY_PHONE_NUMBERS, mPhoneNumbers);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContactsList =
                (ListView) getActivity().findViewById(R.id.contactsList);
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.select_contact_list_item,
                null,
                FROM_COLUMNS,
                TO_IDS,
                0);
        mContactsList.setAdapter(mCursorAdapter);
        mContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                checkBox.setChecked(!checkBox.isChecked());
                boolean checked = checkBox.isChecked();

                Cursor cursor = ((CursorAdapter)parent.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                mContactId = cursor.getLong(CONTACT_ID_INDEX);
                mContactKey = cursor.getString(LOOKUP_KEY_INDEX);
                mContactUri = ContactsContract.Contacts.getLookupUri(mContactId, mContactKey);
                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                int index = mPhoneNumbers.indexOf(phoneNumber);
                if (checked && index < 0) {
                    mPhoneNumbers.add(phoneNumber);
                    Log.d(TAG, "Added " + phoneNumber);
                } else if (index >= 0) {
                    mPhoneNumbers.remove(phoneNumber);
                    Log.d(TAG, "Removed " + phoneNumber);
                }

//                Log.d(TAG, "Clicked " + mContactKey + "; " + mContactId + "; " + mContactUri.toString() + ";" + phoneNumber + " -- " + checked);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                getActivity(),
                ContactsContract.Data.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                null
        );
    }

}
