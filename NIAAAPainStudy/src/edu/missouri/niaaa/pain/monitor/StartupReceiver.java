package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "Startup Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received");

        if(MonitorUtilities.ID == null){
            MonitorUtilities.ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
            Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
        }

        if(!(MonitorUtilities.ID.equals(""))){
        	// Recording
            String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
            Log.d(TAG, "filename: "+fileName);
            String toWrite = MonitorUtilities.USER_TEXT + MonitorUtilities.ID + MonitorUtilities.COMMA + MonitorUtilities.SPACE + MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK 
            		+ "Device was TURNED ON by user! And just finished starting up." + MonitorUtilities.LINEBREAK 
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
                Log.d(TAG, "utilities monitorEncryption failed!!");
                e.printStackTrace();
            }

            MonitorUtilities.MonitorTransmitData transmitData = new MonitorUtilities.MonitorTransmitData();
            if (MonitorUtilities.checkNetwork(context)) {
                transmitData.execute(enformattedData);
            }
        }
        
        
        //get the current time difference for the TimeChangedReciever to determine the previous and current times
      	MonitorUtilities.timeDifferenceOnStartup = MonitorUtilities.getCurrentTimeDifference();
      	
//      	SharedPreferences shp = context.getSharedPreferences(Util.SP_LOGIN, Context.MODE_PRIVATE);
//      	if(shp != null){
//      		MonitorUtilities.makeSharedPreferencesMonitorOnDestroyToTrue(shp);
//      	}
//      	
      	
    }
}
