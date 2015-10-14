package org.hexpresso.soulevspy.obd;

import org.hexpresso.obd.ObdMessageData;
import org.hexpresso.obd.ObdMessageFilter;

import java.util.ArrayList;

/**
 * Created by pemessier on 2015-10-13.
 */
public class BatteryChargingMessageFilter extends ObdMessageFilter {

    public enum ConnectedChargerType {
        NONE,
        TYPE1,
        J1772,
        CHADEMO
    }

    private boolean mIsCharging = false;
    private ConnectedChargerType mChargerType = ConnectedChargerType.NONE;
    private double mChargingPowerKW = 0.0;

    public BatteryChargingMessageFilter() {
        super("581");
    }

    @Override
    protected boolean doProcessMessage(ObdMessageData messageData) {
        ArrayList<String> data = messageData.getData();
        if ( data.size() != 8 )
        {
            return false;
        }

        // Charging state
        mIsCharging = messageData.getDataByte(3) != 0;

        // Charger Type
        mChargerType = ConnectedChargerType.NONE;
        switch ( messageData.getDataByte(5) )
        {
            case 0x0D:
                mChargerType = ConnectedChargerType.TYPE1;
                break;
            case 0x0E:
                mChargerType = ConnectedChargerType.J1772;
                break;
        }

        // Charging power
        mChargingPowerKW = ( ( messageData.getDataByte(7) << 8 ) +
                             ( messageData.getDataByte(6) ) ) / 256.0;

        return true;
    }

    public boolean getIsCharging() {
        return mIsCharging;
    }

    public ConnectedChargerType getConnectedChargerType() {
        return mChargerType;
    }

    public double getChargingPowerKW() {
        return mChargingPowerKW;
    }
}
