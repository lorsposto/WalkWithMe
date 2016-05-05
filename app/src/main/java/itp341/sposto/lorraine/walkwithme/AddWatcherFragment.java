package itp341.sposto.lorraine.walkwithme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import itp341.sposto.lorraine.walkwithme.models.Contact;

/**
 * Created by LorraineSposto on 4/25/16.
 */
public class AddWatcherFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AddWatcherFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 101;

    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private final static int[] TO_IDS = {
            android.R.id.text1,
            android.R.id.text2
    };
    private ListView mContactsList;
    private SimpleCursorAdapter mCursorAdapter;

//    private ArrayList<String> mPhoneNumbers;
    private ArrayList<Contact> mContacts;

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
//        mPhoneNumbers = new ArrayList<>();
        mContacts = new ArrayList<>();
    }

    public static AddWatcherFragment newInstance(String origin, String dest, String distance, String duration, ArrayList<Contact> contacts) {

        Bundle args = new Bundle();
        args.putString(Keys.KEY_ORIGIN_ADDRESS, origin);
        args.putString(Keys.KEY_DEST_ADDRESS, dest);
        args.putString(Keys.KEY_DISTANCE, distance);
        args.putString(Keys.KEY_DURATION, duration);
        args.putParcelableArrayList(Keys.KEY_PHONE_CONTACTS, contacts);

        AddWatcherFragment fragment = new AddWatcherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        mContacts = args.getParcelableArrayList(Keys.KEY_PHONE_CONTACTS);
        View v = inflater.inflate(R.layout.add_watcher_fragment,
                container, false);
        mButtonNotify = (Button) v.findViewById(R.id.notifyButton);
        mButtonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("TAG", AddWatcherActivity.class.getSimpleName());
                data.putExtra(Keys.KEY_PHONE_CONTACTS, mContacts);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    Log.d(TAG, "Permissions granted contacts");
                    loadContacts();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Until you grant the permission, we cannot utilize contacts", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    Log.d(TAG, "Permissions granted sms");
                    loadContacts();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Until you grant the permission, we cannot utilize contacts", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void loadContacts() {
        Log.d(TAG, "Checking permissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissiosn requesting contacts");
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissiosn requesting sms");
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else {
            Log.d(TAG, "Permissiosn granted");
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
                    Log.d(TAG, "onItemClick " + position);
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                    Log.d(TAG, "Checked 1" + checkBox.isChecked());
                    checkBox.setChecked(!checkBox.isChecked());
                    boolean checked = checkBox.isChecked();
                    Log.d(TAG, "Checked 2" + checkBox.isChecked());

                    Cursor cursor = ((CursorAdapter) parent.getAdapter()).getCursor();
                    cursor.moveToPosition(position);

                    long contactId = cursor.getLong(CONTACT_ID_INDEX);
                    String contactKey = cursor.getString(LOOKUP_KEY_INDEX);
                    Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    Contact c = new Contact(contactId, phoneNumber, contactUri);

                    int index = mContacts.indexOf(c);
                    if (checked && index < 0) {
                        mContacts.add(c);
                        Log.d(TAG, "Added " + phoneNumber);
                    } else if (index >= 0 && !checked) {
                        mContacts.remove(c);
                        Log.d(TAG, "Removed " + phoneNumber);
                    }

//                Log.d(TAG, "Clicked " + mContactKey + "; " + mContactId + "; " + mContactUri.toString() + ";" + phoneNumber + " -- " + checked);
                }
            });
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadContacts();

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        mCursorAdapter.swapCursor(data);
        for (Contact c : mContacts) Log.d(TAG, "mContact " + c.toString());
        // check already added ppl
        if (data != null) {
            Log.d(TAG, "Checking peole already selected " + data.getCount());
            while (data.moveToNext()) {
                long id = data.getLong(CONTACT_ID_INDEX);
                String contactKey = data.getString(LOOKUP_KEY_INDEX);
                Uri contactUri = ContactsContract.Contacts.getLookupUri(id, contactKey);
                String phoneNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Contact c = new Contact(id, phoneNumber, contactUri);
                if (mContacts.indexOf(c) >= 0) {
                    Log.d(TAG, "Clicking item " + id + ", " + data.getPosition());
                   CheckBox box = (CheckBox) mContactsList.getAdapter().getView(data.getPosition(), null, null).findViewById(R.id.checkBox);
                    Log.d(TAG, "Check box" + box.isChecked());
                    box.setChecked(!box.isChecked());
                    Log.d(TAG, "Check box" + box.isChecked());
//                    mContactsList.performItemClick(mContactsList.getAdapter().getView(data.getPosition(), null, null),
//                            data.getPosition(),
//                            mContactsList.getAdapter().getItemId(data.getPosition()));
                }
            }
        } else {
            Log.d(TAG, "Cursor is null");
        }
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
