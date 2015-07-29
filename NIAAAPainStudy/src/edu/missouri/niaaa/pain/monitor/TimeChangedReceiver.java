package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class TimeChangedReceiver extends BroadcastReceiver {

	private String TAG = "TimeChangedReceiver";
	private String toWrite = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive" );
		
		if(MonitorUtilities.ID == null){
			MonitorUtilities.ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
			Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
		}
		
		
		//get the first time difference which was calculated at startup in StartupReceiver.java
		long lastDifference = MonitorUtilities.timeDifferenceOnStartup;
		
		if(lastDifference != -1){
		
			long currentDifference = MonitorUtilities.getCurrentTimeDifference();
			
			//Calculate the difference of the differences lol
			long userChangeInMillis = lastDifference - currentDifference;
			
			//calculate the previous time before they change the clock 
			long previousTime = System.currentTimeMillis() + userChangeInMillis;
			
			//reset the difference for the next time user change the clock
			MonitorUtilities.timeDifferenceOnStartup = currentDifference;
			
			
			String previous = new Date(previousTime).toString();
			String current = new Date().toString();
			
			Log.d(TAG, "Previous time: "+ previous);
			Log.d(TAG, "Current time: "+ current);
			
			toWrite = MonitorUtilities.USER_TEXT + MonitorUtilities.ID + MonitorUtilities.COMMA + MonitorUtilities.SPACE + MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK 
						+ "The System Clock was JUST CHANGED by user!" + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK 
						+ "The previous time was: " + previous + MonitorUtilities.LINEBREAK 
						+ "The new time is: " + current + MonitorUtilities.LINEBREAK 
						+ MonitorUtilities.SPLIT;
		}
		else{
			//reset the difference for the next time user change the clock
			MonitorUtilities.timeDifferenceOnStartup = MonitorUtilities.getCurrentTimeDifference();
			
			toWrite = MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK 
					    + MonitorUtilities.LINEBREAK + "The System Clock was JUST CHANGED by user!"
						+ MonitorUtilities.LINEBREAK + MonitorUtilities.SPLIT;
		}
		
		
		if(!(MonitorUtilities.ID.equals(""))){
			// Recording
			String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
			Log.d(TAG, "filename: "+fileName);
			
			
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

			MonitorUtilities.MonitorTransmitData monitorTransmitData = new MonitorUtilities.MonitorTransmitData();
			if (MonitorUtilities.checkNetwork(context)) {
				monitorTransmitData.execute(enformattedData);
			}	
		}		
	}
	
}
