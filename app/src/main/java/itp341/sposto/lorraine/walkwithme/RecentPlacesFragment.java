package itp341.sposto.lorraine.walkwithme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import itp341.sposto.lorraine.walkwithme.models.MyPlace;

/**
 * Created by LorraineSposto on 5/9/16.
 */
public class RecentPlacesFragment extends Fragment {

    public static final String TAG = RecentPlacesFragment.class.getSimpleName();
    private ArrayList<MyPlace> mPlaces;

    public static RecentPlacesFragment newInstance(ArrayList<MyPlace> places) {

        Bundle args = new Bundle();
        args.putSerializable(Keys.KEY_RECENT_PLACES, places);

        RecentPlacesFragment fragment = new RecentPlacesFragment();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recent_places_fragment, container, false);
        String[] addresses = new String[mPlaces.size()];
        for (int i=0; i < addresses.length; ++i) {
            addresses[i] = mPlaces.get(i).getPlaceName() + ", " + mPlaces.get(i).getAddress();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, addresses);
        ListView lv = (ListView) v.findViewById(R.id.recentPlacesList);
        lv.setAdapter(arrayAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(Keys.KEY_DEST_ADDRESS, mPlaces.get(position).getId());
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });

        return v;
    }
}
