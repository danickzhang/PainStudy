package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class RecordingReceiver extends BroadcastReceiver {
    private final String TAG = "Recording Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received");

        if(MonitorUtilities.ID == null){
            MonitorUtilities.ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
            Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
        }
        
        if(!(MonitorUtilities.ID.equals(""))){
        	String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
            // Need to be modified like the format after merging
            // String prefix =
            // RECORDING_FILENAME+"."+phoneID+"."+getFileDate
            String toWrite = prepareData(context, intent);

            try {
                Util.writeToFile(fileName + ".txt", toWrite);
                Log.d(TAG, "write to file");
            } catch (IOException e) {
                Log.d(TAG, "not write to file!!");
                e.printStackTrace();
            }

            String fileHead = MonitorUtilities.monitorGetFileHead(fileName);
            // Log.d("RecordingReceiver", fileHead);
            String toSend = fileHead + toWrite;
            String enformattedData = null;
            try {
                enformattedData = Util.encryption(context, toSend);
            } catch (Exception e) {
                Log.d(TAG, "Utilties monitorEncryption failed!!");
                e.printStackTrace();
            }

    	    MonitorUtilities.MonitorTransmitData transmitData = new MonitorUtilities.MonitorTransmitData();
    	    if (MonitorUtilities.checkNetwork(context)) {
    	       transmitData.execute(enformattedData);
    	    }
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(MonitorUtilities.ACTION_RECORD);
        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, it, PendingIntent.FLAG_CANCEL_CURRENT);

        am.setExact(AlarmManager.RTC_WAKEUP, MonitorUtilities.getNextLongTime(), piTrigger);
    }

    

    private String prepareData(Context context, Intent intent) {
        //String connectionState = MonitorUtilities.getConnectionState(context);
        
        /*added by nick on july 20th 2015 for craving study */ 
        MonitorUtilities.checkStatusOfBattery(context);

        /* Added by Nick on April 2nd 2015 for SLU app */
        String gpsMode = MonitorUtilities.checkGpsMode(context);
        String activeNetwork = MonitorUtilities.checkActiveNetwork(context);
        String airplaneMode = MonitorUtilities.checkAirplaneMode(context);
        String mobileConnectionState = MonitorUtilities.getMobileConnectionState(context);
        String wifiConnectionState = MonitorUtilities.getWifiConnectionState(context);
        String bluetoothConnectionState = MonitorUtilities.getBluetoothConnectionState(context);
        /* this is bluetooth for device connection */
        String bluetoothSupported = MonitorUtilities.checkIfBluetoothIsSupported();
        String bluetoothOn = MonitorUtilities.checkIfBluetoothIsOn();
        /*String bluetoothBonded = MonitorUtilities.checkIfBluetoothIsBonded();
        String bluetoothConnected = MonitorUtilities.checkIfBluetoothIsConnected();*/

        //String activeNetwork2 = MonitorUtilities.checkActiveNetwork2(context);

        String textForGPSAccuracy;

        if(MonitorUtilities.gpsAccuracy.equals("unknown"))
            textForGPSAccuracy = "";
        else
            textForGPSAccuracy = " meters";

        /* Added by nick for slu app on april 2nd 2015 */
        return MonitorUtilities.USER_TEXT + MonitorUtilities.ID + MonitorUtilities.COMMA + MonitorUtilities.SPACE  + MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK
                + "Is Phone Charging? -> " + MonitorUtilities.isCharging + MonitorUtilities.LINEBREAK
                + "  Charging By: " + MonitorUtilities.howCharging + MonitorUtilities.LINEBREAK
                /*+ "  Charging By USB: " + MonitorUtilities.usbCharge + MonitorUtilities.LINEBREAK
                + "  Charging By AC Outlet: " + MonitorUtilities.acCharge + MonitorUtilities.LINEBREAK*/
                /* Nick end */
                + "Battery Level: " + MonitorUtilities.batteryPercent + MonitorUtilities.LINEBREAK
                //+ "Network Connection Status: " + connectionState + MonitorUtilities.LINEBREAK
                + activeNetwork + MonitorUtilities.LINEBREAK
                /* added by nick for slu app on april 2nd 2015 */
                + "  " + mobileConnectionState + MonitorUtilities.LINEBREAK
                + "  " + wifiConnectionState + MonitorUtilities.LINEBREAK
                + "  " + bluetoothConnectionState + MonitorUtilities.LINEBREAK
                //+ "  " + activeNetwork + MonitorUtilities.LINEBREAK
                //+ "  " + activeNetwork2 + MonitorUtilities.LINEBREAK
                + gpsMode + MonitorUtilities.LINEBREAK
                + "  Is There an Active GPS Signal? -> " + MonitorUtilities.activeGPS + MonitorUtilities.LINEBREAK
                + "  Longitude and Latitude: " + MonitorUtilities.longLatGPS + MonitorUtilities.LINEBREAK
                + "  GPS Provider: " + MonitorUtilities.gpsProvider + MonitorUtilities.LINEBREAK
                + "  GPS Accuracy: " + MonitorUtilities.gpsAccuracy + textForGPSAccuracy + MonitorUtilities.LINEBREAK
                + "  Is the GPS Accuracy Good? -> " + MonitorUtilities.gpsAccuracyGood + MonitorUtilities.LINEBREAK
                + "  Will this GPS location be recorded? -> " + MonitorUtilities.willGPSBeRecorded + MonitorUtilities.LINEBREAK
                + bluetoothSupported + MonitorUtilities.LINEBREAK
                + "  " + bluetoothOn + MonitorUtilities.LINEBREAK
                /*+ "  " + bluetoothBonded + MonitorUtilities.LINEBREAK
                + "  " + bluetoothConnected + MonitorUtilities.LINEBREAK*/
                + airplaneMode + MonitorUtilities.LINEBREAK
                /* Nick end */
                + MonitorUtilities.SPLIT;
    }
}
