package com.evranger.soulevspy.fragment;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;

import com.evranger.soulevspy.activity.MainActivity;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;

import com.evranger.soulevspy.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by henrik on 09/06/2017.
 */

public class GpsFragment extends ListFragment implements CurrentValuesSingleton.CurrentValueListener {
    private ListViewAdapter mListViewAdapter = null;
    private List<ListViewItem> mItems = new ArrayList<>();
    private List<ListViewItem> mListItems = new ArrayList<>();
    private CurrentValuesSingleton mValues = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.action_gps);
        mValues = CurrentValuesSingleton.getInstance();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            // initialize the list adapter
            mListViewAdapter = new ListViewAdapter(getActivity(), mListItems);
            ((MainActivity) mValues.getPreferences().getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setListAdapter(mListViewAdapter);
                }
            });
            onValueChanged(null, null);
            mValues.addListener(mValues.getPreferences().getContext().getResources().getString(R.string.col_route_time_s), this);
        }
    }

    @Override
    public void onDestroy() {
        mValues.delListener(this);
        super.onDestroy();
    }

    public void onValueChanged(String key, Object value) {
        Resources res = mValues.getPreferences().getContext().getResources();
        Object lat = mValues.get(res.getString(R.string.col_route_lat_deg));
        Object lng = mValues.get(res.getString(R.string.col_route_lng_deg));
        Object alt = mValues.get(res.getString(R.string.col_route_elevation_m));
        Object spd = mValues.get(res.getString(R.string.col_route_speed_mps));
        Object tim = mValues.get(res.getString(R.string.col_route_time_s));
        mItems.clear();
        if (lat != null && lng != null && alt != null && tim != null && spd != null) {
            mItems.add(new ListViewItem(mValues.getString(R.string.lattitude), lat.toString()));
            mItems.add(new ListViewItem(mValues.getString(R.string.longtitude), lng.toString()));
            mItems.add(new ListViewItem(mValues.getString(R.string.altitude) + " (m)", alt.toString()));
            mItems.add(new ListViewItem(mValues.getString(R.string.speed) + " (m/s)", spd.toString()));
            Long utim = (Long) tim;
            if (utim != null) {
                DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date(utim);
                String formatted = format.format(date);
                mItems.add(new ListViewItem(mValues.getString(R.string.time), formatted));
            }
        }

        // update the list adapter display
        ((MainActivity) mValues.getPreferences().getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListItems.clear();
                mListItems.addAll(mItems);
                mListViewAdapter.notifyDataSetChanged();
            }
        });
    }
}
