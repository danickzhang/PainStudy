package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class ShutdownReceiver extends BroadcastReceiver {
    private final String TAG = "Shutdown Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received");

        //stop the battery recording service
//        Intent i = new Intent(context, RecordingService.class);
//        if(context.stopService(i))
//            Log.d(TAG, "stopping recording service");
//        else
//            Log.d(TAG, "unable to stop recording service");


        if(MonitorUtilities.ID == null){
            MonitorUtilities.ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
            Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
        }
        
        if(!(MonitorUtilities.ID.equals(""))){
        	String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
            Log.d(TAG, "filename: "+fileName);
            String toWrite = MonitorUtilities.USER_TEXT + MonitorUtilities.ID + MonitorUtilities.COMMA + MonitorUtilities.SPACE + MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK 
            		+ "Device is TURNING OFF! And was activated by user!" + MonitorUtilities.LINEBREAK 
            		+ MonitorUtilities.SPLIT;

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
                e.printStackTrace();
                Log.d(TAG, "Utilities monitorEncryption failed!!");
            }

            MonitorUtilities.MonitorTransmitData transmitData = new MonitorUtilities.MonitorTransmitData();
            if (MonitorUtilities.checkNetwork(context)) {
                transmitData.execute(enformattedData);
            }
        }

        
    }
}
