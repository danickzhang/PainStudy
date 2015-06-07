package edu.missouri.niaaa.pain;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {
    static String TAG = "MainActivity~~~";

    Button section_1;
    Button section_2;
    Button section_3;
    Button section_4;
    Button section_5;
    Button section_6;
    Button section_7;
    Button section_8;
    Button section_9;

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uti.LogSys_lifeC(TAG, "OnCreate~~~");
        

        //threadpolicy, maybe changed later
        StrictMode.ThreadPolicy policy =new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);



//      setListeners();

//      setSharedValue();

        IntentFilter suspensionIntent = new IntentFilter(Uti.BD_ACTION_SUSPENSION);
//      this.registerReceiver(suspensionReceiver, suspensionIntent);


        ////startSService();
        //
        //check if device is assigned with an ID
//        shp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
//        ID = shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
        //for recording info
        //end recording info
//        PWD = shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "");
//        editor = shp.edit();

//        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

//        Log.d(TAG,"id is "+ID);

//        if(ID.equals("")){
//          management();
//
//
//            imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);
//
//        }else if(PWD.equals("")){
            //set password

//          UserPWDSetDialog(this, ID).show();
//
//        }else{
//          Log.d(TAG,"pwd is "+shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "get fail?"));
////            startSService();

            //set fun to 0
//          sendBroadcast(new Intent(Utilities.BD_ACTION_DAEMON));

            //restart gps
//          if(Utilities.completedMorningToday(this) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
//              sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
//          }


            // RECORDING

            Log.d(TAG, "onCreate is scheduling Monitor Recording");

        }

















    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        //ENABLE BLUETOOTH
        if (item.getItemId() == R.id.Enable){
            if(btAdapter.isEnabled())           {
                Toast.makeText(getApplicationContext(), R.string.bluetooth_enabled ,Toast.LENGTH_LONG).show();
            }
            else{
                turnOnBt();
            }

            return true;
        }


        //DISABLE BLUETOOTH
        else if (item.getItemId() == R.id.Disable){
            btAdapter.disable();
            Toast.makeText(getApplicationContext(), R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
            return true;
        }

        //MANAGEMENT
        else if(item.getItemId() == R.id.manage){
//          management();
        }

        // ABOUT
        else if(item.getItemId() == R.id.about){

            //initial versionCode
            int versionCode = 100;
            String versionName = "2.2";
            PackageInfo pinfo;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                versionCode = pinfo.versionCode;
                versionName = pinfo.versionName;
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // show current version, which is defined in Android Manifest
            Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
            .setCancelable(false)
            .setTitle(getString(R.string.menu_about)+"  ver."+versionName+"."+versionCode)
//          .setMessage("User ID: "+ID+"\n"+Utilities.getScheduleForToady(MainActivity.this))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            })
            .create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    private void turnOnBt(){
        // TODO Auto-generated method stub
        Intent Enable_Bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//      startActivityForResult(Enable_Bluetooth, INTENT_REQUEST_BLUETOOTH);
    }




    private PublicKey getPublicKey() throws Exception {
        // TODO Auto-generated method stub
        InputStream is = getResources().openRawResource(R.raw.publickey);
        ObjectInputStream ois = new ObjectInputStream(is);

        BigInteger m = (BigInteger)ois.readObject();
        BigInteger e = (BigInteger)ois.readObject();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);


        KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
        PublicKey pubKey = fact.generatePublic(keySpec);

        return pubKey;
    }

//================================================================================================================================
//================================================================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Uti.LogSys_lifeC(TAG, "OnCreate~~~");
        Uti.LogSys_lifeC(TAG, "~~~"+requestCode+" "+resultCode);

    }
    
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Uti.LogSys_lifeC(TAG, "OnRestart~~~");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Uti.LogSys_lifeC(TAG, "OnStart~~~");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uti.LogSys_lifeC(TAG, "OnResume~~~");
        
        // implementation here
        
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Uti.LogSys_lifeC(TAG, "OnPause~~~");
        
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Uti.LogSys_lifeC(TAG, "onStop~~~");
        
    }

    @Override
    protected void onDestroy() {
        Uti.LogSys_lifeC(TAG, "onDestroy~~~");
        // implementation here
        
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Uti.LogSys_lifeC(TAG, "onBackPressed~~~");
    }


}
