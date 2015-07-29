package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;


public class MonitorUtilities {



    /* REcording */
    public final static String ACTION_RECORD = Util.BD_ACTION_BASE + "ACTION_RECORD";
    public final static String LINEBREAK = System.getProperty("line.separator");
    public final static String SPLIT = "---------------------------------------";
    public static final String RECORDING_CATEGORY = "Recording";
    public static final String USER_TEXT = "User: ";
    public static final String SPACE = " "; 
    public static final String COMMA = ",";
    public static String curBatt = "nan";

    /*added by nick on april 2nd 2015 for slu app */
    public static String isCharging = "unknown";
    public static String howCharging = "unknown";
    public static String batteryPercent = "unknown";
    /*public static String usbCharge = "unknown";
    public static String acCharge = "unknown";*/
    public static String activeGPS = "unknown";
    public static String gpsAccuracy = "unknown";
    public static String longLatGPS = "unknown";
    public static String gpsAccuracyGood = "unknown";
    public static String willGPSBeRecorded = "unknown";
    public static String gpsProvider = "unknown";

    public static String ID;
    public static long timeDifferenceOnStartup = -1;



    private final static String TAG = "Monitor Utilities";
    
    public final static String ACTION_ONSTART = "MONITOR_START";
    public final static String ACTION_ONDESTROY = "MONITOR_STOP"; 


    /* Recording */
    
    /**
     * this is method is for the flag variable used for the onCreate function to send hardware info 
     * that the user has started the app but only on the first time and not every time the activity's onCreate is called 
     * NOTE: if the flag is set to FALSE the hardware info WILL send to the server 
     * @param shp
     * @return
     */
    public static boolean makeSharedPreferencesMonitorOnStartToFalse(SharedPreferences shp){
    	Log.d(TAG, "makeSharedPreferencesMonitorOnStartToFalse() called");
    	Editor editor = shp.edit();
        editor.putBoolean(MonitorUtilities.ACTION_ONSTART, false);
        return editor.commit();   
    }
    
    /**
     * this is method is just the opposite as the one before
     * it is for the flag variable used for the onCreate function to send hardware info 
     * that the user has started the app but only on the first time and not every time the activity's onCreate is called
     * NOTE: if the flag is set to TRUE the hardware info WILL NOT send to the server  
     * @param shp
     * @return
     */
    public static boolean makeSharedPreferencesMonitorOnStartToTrue(SharedPreferences shp){
    	Log.d(TAG, "makeSharedPreferencesMonitorOnStartToTrue()");
    	Editor editor = shp.edit();
        editor.putBoolean(MonitorUtilities.ACTION_ONSTART, true);
        return editor.commit();   
    }
    
    
    public static boolean makeSharedPreferencesMonitorOnDestroyToTrue(SharedPreferences shp){
    	Log.d(TAG, "makeSharedPreferencesMonitorOnDestroyToTrue()");
    	Editor editor = shp.edit();
        editor.putBoolean(MonitorUtilities.ACTION_ONDESTROY, true);
        return editor.commit();   
    }
    
    public static boolean makeSharedPreferencesMonitorOnDestroyToFalse(SharedPreferences shp){
    	Log.d(TAG, "makeSharedPreferencesMonitorOnDestroyToFalse()");
    	Editor editor = shp.edit();
        editor.putBoolean(MonitorUtilities.ACTION_ONDESTROY, false);
        return editor.commit();   
    }

    
    
    public static void checkStatusOfBattery(Context context){
    	int defaultValue = -1; 
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, defaultValue);
		boolean charge = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
		
		isCharging = String.valueOf(charge);
		
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, defaultValue);
		
		if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB){
			howCharging = "USB";
		}
		else if(chargePlug == BatteryManager.BATTERY_PLUGGED_AC){
			howCharging = "AC Outlet";
		}
		else if(chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS){
			howCharging = "Wireless Power Source";
		}
		else {
			howCharging = "unknown";
		}
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, defaultValue);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, defaultValue);
		
		batteryPercent = String.valueOf(level / (float)scale); 
    }
    
    

    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to check the settings for the gps (gps mode)
     */
    public static String checkGpsMode(Context context){
        int locationMode = -1;
        String locationString = "GPS Mode in Phone Settings: ";

        try {
            locationMode = Secure.getInt(context.getApplicationContext().getContentResolver(), Secure.LOCATION_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        /*This is the old way of how to get the location mode but it is not depreciated
         *
         * if(locationMode == -1){


            locationString = Secure.getString(context.getContentResolver(), Secure.LOCATION_PROVIDERS_ALLOWED);

            //Utilities.writeToFile(provs);
        }*/
        if(locationMode == -1){
            locationString += "LOCATION_MODE_UNKNOWN";
        }
        else {
            switch (locationMode){
                case 0:
                    locationString += "LOCATION_MODE_OFF";
                    break;
                case 1:
                    locationString += "LOCATION_MODE_SENSORS_ONLY";
                    break;
                case 2:
                    locationString += "LOCATION_MODE_BATTERY_SAVING";
                    break;
                case 3:
                    locationString += "LOCATION_MODE_HIGH_ACCURACY";
                    break;
                default:
                    locationString += "LOCATION_MODE_UNKNOWN";
                    break;
            }
        }

        return locationString;
    }

    public static boolean checkNetwork(Context context){
        boolean activeNetwork = false;

        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if(manager != null){
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                activeNetwork = true;
                Log.d(TAG, "checkNetwork() result: "+networkInfo.isConnected());
            }
        }

        return activeNetwork;

    }

    public static String checkActiveNetwork2(Context context){
        boolean activeNetwork = false;
        String result = "Is Phone Connected To Active Network2? -> ";

        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if(manager != null){
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if(networkInfo!= null && networkInfo.isConnected()){
                activeNetwork = true;
                Log.d(TAG, "active network 2 result: "+networkInfo.isConnected());
            }
        }

        return result + String.valueOf(activeNetwork);
    }

    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to check if there is an active network
     * sometimes the phone can be connected to the wifi router
     * but the router might not have an internet connection
     * therefore, the method "getConnectionState" will say the
     * phone is connected when really it is not connected
     */
    public static String checkActiveNetwork(Context context){
        boolean activeInternet = false;
        String result = "Is Phone Connected To Active Network? -> ";

        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();

            CheckActiveNetwork activeNetwork = new CheckActiveNetwork();
            if (networkInfo!= null && networkInfo.isConnected()) {
                try {
                    boolean treadResult = activeNetwork.execute().get();
                    Log.d(TAG, "active internet tread result: "+treadResult);
                    activeInternet = treadResult;
                } catch (InterruptedException e1) {
                    Log.d(TAG, "active internet tread interrupted!");
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    Log.d(TAG, "active internet thread aborted (thread threw exception)");
                    e1.printStackTrace();
                }
            }
        }

        return result + String.valueOf(activeInternet);

    }

    private static class CheckActiveNetwork extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();

                Log.d(TAG, "Active internet response code: "+ urlc.getResponseCode());

                if(urlc.getResponseCode() == 200){
                    //activeInternet = true;
                    Log.d(TAG, "active internet: true");
                }

                return true;
            } catch (IOException e) {
                Log.e("Connecting To Internet", "Error checking internet connection", e);
                return false;
            }
        }
    }



    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to determine if the user has airplane mode on
     */
    public static String checkAirplaneMode(Context context) {
        boolean airplaneIsEnabled = Settings.Global.getInt(context.getApplicationContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        String result = "Airplane Mode Is On: ";


        return result + String.valueOf(airplaneIsEnabled);
    }

    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to determine if the phone supports bluetooth for
     * connecting to a device to transfer data
     */
    public static String checkIfBluetoothIsSupported(){
        String result = "Is BLUETOOTH for Device Supported? -> ";

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null){
            //device does not support bluetooth
            result += "false";
        }
        else{
            result += "true";
        }

        return result;
    }

    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to determine if the settings for bluetooth are enabled
     */
    public static String checkIfBluetoothIsOn(){
        String result = "Is BLUETOOTH for Device On? -> ";

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(adapter != null){
            if(adapter.isEnabled()){
                result += "true";
            }
            else {
                result += "false";
            }
        }
        else {
            result += "unknown";
        }

        return result;
    }

    /* this method was added by Nick on April 11 2015
     * it is used to see if any bluetooth devices are bonded
     * or paired
     */

    public static String checkIfBluetoothIsBonded(){
        String result = "Is BLUETOOTH Paired with any Devices? -> ";

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            result += "true";
        }
        else {
            result += "false";
        }

        return result;
    }

    /*this method was added by Nick on April 11 2015
     * it is used to see if any of the profiles are connected
     * to a device. when I added this method there were only 3
     * different profiles.
     */

    public static String checkIfBluetoothIsConnected(){
        String result = "Active Bluetooth for Device Connection Status: ";

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        int state1 = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int state2 = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int state3 = adapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        Log.d(TAG, "bluetooth a2dp state: "+state1);
        Log.d(TAG, "bluetooth headset state: "+state2);
        Log.d(TAG, "bluetooth health state: "+state3);

        if(state1 == BluetoothProfile.STATE_CONNECTED){
            result += "CONNECTED";
            Log.d(TAG, "bluetooth connected to A2DP profile");
        }
        else if(state2 == BluetoothProfile.STATE_CONNECTED){
            result += "CONNECTED";
            Log.d(TAG, "bluetooth connected to HEADSET profile");
        }
        else if(state3 == BluetoothProfile.STATE_CONNECTED){
            result += "CONNECTED";
            Log.d(TAG, "bluetooth connected to HEALTH profile");
        }
        else {
            result += "DISCONNECTED";
        }

        return result;
    }



    /* this method was added by nick on april 2nd 2015 for slu app
     * it is used to determine the status of the mobile or cellular connection
     *  (connected or disconnected)
     */
    public static String getMobileConnectionState(Context context){
        String result = "MOBILE: ";
        ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (info != null) {
                result += info.getState().toString();
            }
        }

        return result;
    }

    /* this method was added by nick on april 2nd 2015
     * it is used to determine the status of the wifi connection
     * (connected or disconnected )
     */
    public static String getWifiConnectionState(Context context){
        String result = "WIFI: ";
        ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (info != null) {
                result += info.getState().toString();
            }
        }

        return result;
    }

    /* this method was created by nick on april 2nd 2015 for slu app
     * it is used to determine the status of the bluetooth connection
     * for network communication (hotspot or bluetooth teathering)
     * (connected or disconnected)
     */
    public static String getBluetoothConnectionState(Context context){
        String result = "BLUETOOTH for Network: ";
        ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);

            if (info != null) {
                result += info.getState().toString();
            }
        }

        return result;
    }

    public static String getConnectionState(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return "Hardware Problem";
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return "Not Connected";
        }
        if (networkInfo.isAvailable()) {
            return "Connected";
        }
        return "Not Connected";

    }

    public static void scheduleRecording(final Context context) {

        Handler h = new Handler();
        h.postDelayed(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent i = new Intent(MonitorUtilities.ACTION_RECORD);
                context.sendBroadcast(i);
            }

        }, 5000);

    }

    public static String getCurrentTimeStamp()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return String.valueOf(cal.getTime());
    }

    public static String getFileDate()
    {
        Calendar c = Calendar.getInstance();
        String dateObj = Util.mdF.format(c.getTime());
        return dateObj;
    }
    public static long getCurrentTimeDifference() {
		long elapsedTime = SystemClock.elapsedRealtime();
		long currentTime = System.currentTimeMillis();
		
		return currentTime - elapsedTime;
	}
    
    /** this was created to set the interval for the hardware info
     * right now it is set for every 5 mintues 
     * @author Nick Wergeles created on July 20th 2015 for craving study 
     * @return
     */
    public static long getNextLongTime() {
        Calendar s = Calendar.getInstance();
        s.add(Calendar.MINUTE, 5);
        // s.add(Calendar.SECOND, 30);
        return s.getTimeInMillis();
    }
    
    
    
    
    
    
    
  //keep this separate 
  	public static String monitorGetFileHead(String fileName) {
  		StringBuilder prefix_sb = new StringBuilder(Util.PREFIX_LEN);
  		prefix_sb.append(fileName);
  	
  		for (int i = fileName.length(); i <= Util.PREFIX_LEN; i++) {
  			prefix_sb.append(" ");
  		}
  		return prefix_sb.toString();
  	}
  	
  	public static class MonitorTransmitData extends AsyncTask<String, Void, Boolean> {
  		@Override
  		protected Boolean doInBackground(String... strings) {
  			boolean result = false;
  			String data = strings[0];
  			// String fileName = strings[0];
  			// String dataToSend = strings[1];
  	
  			HttpPost request = new HttpPost(Util.UPLOAD_ADDRESS);
  			List<NameValuePair> params = new ArrayList<NameValuePair>();
  			params.add(new BasicNameValuePair("data", data));
  			// // file_name
  			// params.add(new BasicNameValuePair("file_name", fileName));
  			// // data
  			// params.add(new BasicNameValuePair("data", dataToSend));
  			try {
  				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
  				HttpResponse response = new DefaultHttpClient().execute(request);
  				Log.d("Sensor Data Point Info", String.valueOf(response.getStatusLine().getStatusCode()));
  				if(response.getStatusLine().getStatusCode() == 200){
  					result = true; 
  					Log.d(TAG, "send to server: true");	
  				}	
  			} catch (Exception e){
  				Log.d(TAG, "not send to server!!");
  				e.printStackTrace();
  			}
  			
  			return result; 
  		}
  	}


}

