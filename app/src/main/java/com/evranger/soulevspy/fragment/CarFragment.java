package com.evranger.soulevspy.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;

import com.evranger.soulevspy.activity.MainActivity;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;
import com.evranger.soulevspy.util.KiaVinParser;
import com.evranger.soulevspy.util.Unit;

import com.evranger.soulevspy.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-07.
 */
public class CarFragment extends ListFragment implements CurrentValuesSingleton.CurrentValueListener {
    private ListViewAdapter mListViewAdapter = null;
    private List<ListViewItem> mItems = new ArrayList<>();
    private List<ListViewItem> mListItems = new ArrayList<>();
    private CurrentValuesSingleton mValues = null;
    Unit unit = new Unit();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.action_car_information);

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
            mValues.addListener(mValues.getPreferences().getContext().getResources().getString(R.string.col_system_scan_end_time_ms), this);
        }
    }

    @Override
    public void onDestroy() {
        if (mValues != null) {
            mValues.delListener(this);
        }
        super.onDestroy();
    }

    public void onValueChanged(String trig_key, Object value) {
        mItems.clear();
        Object SOH_pct = mValues.get(R.string.col_calc_battery_soh_pct);
        if (SOH_pct != null) {
            mItems.add(new ListViewItem("Battery SOH %", new DecimalFormat("0.0").format(SOH_pct)));
        } else {
            mItems.add(new ListViewItem("Battery SOH %", "unknown deterioration"));
        }

        Object DC_V = mValues.get(R.string.col_ELM327_voltage);
        if (DC_V != null) {
            mItems.add(new ListViewItem("Aux battery", new String(DC_V.toString())));
        }
        Object amb = mValues.get(R.string.col_car_ambient_C);
        if (amb != null) {
            mItems.add(new ListViewItem("Ambient Temperature", new DecimalFormat("0.0").format(unit.convertTemp((double)amb))+" "+unit.mTempUnit));
        }
        Object odo = mValues.get(R.string.col_car_odo_km);
        if (odo != null) {
            mItems.add(new ListViewItem("Odometer", new DecimalFormat("0.0").format(unit.convertDist((double)odo))+" "+unit.mDistUnit));
        }
        // ECU name(s)
        Map<String, Object> ecuNames = mValues.find("ECU.name.");
        for (String key : ecuNames.keySet()) {
            String ecuName = (String) mValues.get(key);
            if (ecuName != null && ecuName.length() > 0) {
                mItems.add(new ListViewItem(key, ecuName));
            }
        }
        // DTC codes
        Map<String, Object> dtcCodes = mValues.find("OBD.DtcCodes.");
        for (String key : dtcCodes.keySet()) {
            String codes = (String)dtcCodes.get(key);
            if (codes != null) {
                int num = (codes.length() + 1) / 6;
                mItems.add(new ListViewItem(key + " : " + num + " codes", codes));
            }
        }
        Object vin_str = mValues.get(R.string.col_VIN);
        if (vin_str != null) {
            KiaVinParser vin = new KiaVinParser(getContext(), vin_str.toString()); //"KNDJX3AEXG7123456");
            String str = vin.getVIN();
            if (str != null) {
                mItems.add(new ListViewItem("Vehicle Identification Number", str));
                if (!str.startsWith("error")) {
                    mItems.add(new ListViewItem("Brand", vin.getBrand()));
                    mItems.add(new ListViewItem("Model", vin.getModel()));
                    mItems.add(new ListViewItem("Trim", vin.getTrim()));
                    mItems.add(new ListViewItem("Engine", vin.getEngine()));
                    mItems.add(new ListViewItem("Year", vin.getYear()));
                    mItems.add(new ListViewItem("Sequential Number", vin.getSequentialNumber()));
                    mItems.add(new ListViewItem("Production Plant", vin.getProductionPlant()));
                }
            } else {
                mItems.add(new ListViewItem("Unable to process VIN response", vin_str.toString()));
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
