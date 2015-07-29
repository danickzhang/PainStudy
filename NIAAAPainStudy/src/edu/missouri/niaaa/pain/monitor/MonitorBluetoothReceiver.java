package edu.missouri.niaaa.pain.monitor;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class MonitorBluetoothReceiver extends BroadcastReceiver {
    private final String TAG = "Monitor Bluetooth Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received");

        String action = intent.getAction();
        Log.d(TAG, "Action: " + action);

        if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Bluetooth Device Connected to Device Name: " + device.getName());
            String message = "Active Bluetooth has just been CONNECTED to the Device Named '"+device.getName()+"' !!";
            writeAndSend(message, context);
        }
        if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Bluetooth Device Disconnected from Device Name: " + device.getName());
            String message = "Active Bluetooth has just been DISCONNECTED from the Device Named '"+device.getName()+"' !!";
            writeAndSend(message, context);
        }
        /*if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
            Log.d(TAG, "User has requested a disconnection from bluetooth device");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "Device Name: " + device.getName());
            String send = "Active Bluetooth has just been REQUESTED by USER for DISCONNECTION from the Device Named '"+device.getName()+"' !!";
            boolean result = writeAndSend(send, context);
            Log.d(TAG, "send to server: "+result);
        }*/
        if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
            Log.d(TAG, "Entered Action Bond State Changed");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();

            /*Action Bond State Changed gets called a lot and it would send info
             * besides the ones checked in the if statements. so in order to only send
             * the info we want, I put this here and when the if statements execute
             * then it will set this to true, cause the data to be written to file
             * and sent to server
             */
            boolean sendData = false;

            String message = "Bluetooth Pairing State";

            //the first parameter is the integer you would like to get
            //the second parameter is the default integer if there is no value
            final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            Log.d(TAG, "bond state: "+state);
            Log.d(TAG, "previous bond state: "+prevState);

            if(state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING){
                Log.d(TAG, "Bluetooth Bond State: CONNECTED; To Device Named: "+deviceName);
                sendData = true;
                message += " has just been CONNECTED to";
            }
            if(state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                Log.d(TAG, "Bluetooth Bond State: DISCONNECTED; To Device Named: "+deviceName);
                sendData = true;
                message += " has just been DISCONNECTED from";
            }
            if(state == BluetoothDevice.BOND_BONDING && prevState == BluetoothDevice.BOND_NONE){
                Log.d(TAG, "Bluetooth Bond State: CONNECTING; To Device Named"+deviceName);
                sendData = true;
                message += " is CONNECTING to";
            }

            message += " the Device Named '"+deviceName+"' !!";

            if(sendData){
                writeAndSend(message, context);
            }
        }
        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            Log.d(TAG, "entered action state changed");
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            int prevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
            Log.d(TAG, "bluetooth state: " + state);
            Log.d(TAG, "bluetooth previous state: " + prevState);

            String message = "Bluetooth";


            switch(state){
            case BluetoothAdapter.STATE_OFF:
                Log.d(TAG, "bluetooth adapter state off");
                message += "'s Current State: OFF";
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                Log.d(TAG, "bluetooth adapter state turing on");
                message += " is TURNING ON and was activated by the user !!";
                break;
            case BluetoothAdapter.STATE_ON:
                Log.d(TAG, "bluetooth adapter state on");
                message += "'s Current State: ON";
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Log.d(TAG, "bluetooth adapter state turning off");
                message += " is TURNING OFF and was activated by the user !!";
                break;
            }

            writeAndSend(message, context);
        }
    }

    private void writeAndSend(String data, Context context){
        if(MonitorUtilities.ID == null){
            MonitorUtilities.ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
            Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
        }
        
        if(!(MonitorUtilities.ID.equals(""))){
        	String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
            String toWrite = MonitorUtilities.USER_TEXT + MonitorUtilities.ID + MonitorUtilities.COMMA + MonitorUtilities.SPACE + MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + MonitorUtilities.LINEBREAK 
            		+ data + MonitorUtilities.LINEBREAK 
            		+ MonitorUtilities.SPLIT;

            try {
                Util.writeToFile(fileName + ".txt", toWrite);
                Log.d(TAG, "write to file");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "not write to file!!");
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
    }
}
