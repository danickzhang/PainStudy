package edu.missouri.niaaa.pain;


import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.missouri.niaaa.pain.survey.SurveyMenu;


public class MainActivity extends Activity {
    String TAG = "MainActivity.java";
    boolean logEnable = true;
    

    Button section_1;
    Button section_2;
    Button section_3;
    Button section_4;
    Button section_5;
    Button section_6;
    Button section_7;
    Button section_8;
    Button section_9;

    /*adapter for bluetooth switch*/
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * 
     */
    BroadcastReceiver suspensionReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Uti.Log(TAG, "on receiver break suspension");

//            section_6.setText(R.string.section_6);
////          Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false).commit();
//
//            //write to server
//            Calendar c = Calendar.getInstance();
//            SharedPreferences sp = getSharedPreferences(Uti.SP_LOGIN, Context.MODE_PRIVATE);
//            long startTimeStamp = sp.getLong(Uti.SP_KEY_SUSPENSION_TS, c.getTimeInMillis());
//            c.setTimeInMillis(startTimeStamp);
//
//            try {
//                Uti.writeEventToFile(MainActivity.this, Uti.CODE_SUSPENSION, "", "", "", "",
//                        Uti.sdf.format(c.getTime()), Uti.sdf.format(Calendar.getInstance().getTime()));
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            sp.edit().remove(Uti.SP_KEY_SUSPENSION_TS).commit();
//
//            Toast.makeText(getApplicationContext(), R.string.suspension_end, Toast.LENGTH_LONG).show();
        }
    };
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uti.Log_lifeCycle(TAG, "OnCreate~~~");
        
        /* thread policy
         * help to check if there is misuse of threads, such as read large files or network communication, that 
         * should not be in the main UI thread.
         * Should be bypass when product released */
        if(!Uti.RELEASE){
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            //.permitAll()
            .build());
        }

        
        /* initialization
         * set initial parameters, register broadcasts,
         * but not time-consuming tasks such as animation or file reading cursor reading; 
         * unregister broadcasts @onDestroy, put time-consuming tasks @onResume*/
        
        setContentView(R.layout.activity_main);

        setListeners();

        this.registerReceiver(suspensionReceiver, new IntentFilter(Uti.BD_ACTION_SUSPENSION));

        
        
        
        
        
        
        
        
//      setSharedValue();

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


    private void setListeners() {
        // TODO Auto-generated method stub
        
        /*start/stop service*/
        section_1 = (Button) findViewById(R.id.section_label1);
        section_2 = (Button) findViewById(R.id.section_label2);
        /*survey menu*/
        section_3 = (Button) findViewById(R.id.section_label3);
        /*sensor connection*/
        section_4 = (Button) findViewById(R.id.section_label4);
        /*bed time setting*/
        section_5 = (Button) findViewById(R.id.section_label5);
        /*suspension*/
        section_6 = (Button) findViewById(R.id.section_label6);
        /*support*/
        section_7 = (Button) findViewById(R.id.section_label7);
        /*test buttons for debugging*/
        section_8 = (Button) findViewById(R.id.section_label8);
        section_9 = (Button) findViewById(R.id.section_label9);

//        setServiceText();
        section_1.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                Uti.Log_debug(TAG, logEnable, "section 1 on click listener");
                
            }
        });

        section_2.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Uti.Log_debug(TAG, logEnable, "section 2 on click listener");

            }
        });

        section_3.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Uti.Log_debug(TAG, logEnable, "section 3 on click listener");

//                if(!getSuspension()){
                    startActivity(new Intent(MainActivity.this, SurveyMenu.class));
//                }else{
//                    suspensionAlert();
//                }

            }
        });

        section_4.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Uti.Log(TAG, "section 4 on click listener");

            }
        });





    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    protected void onResume() {
        super.onResume();
        Uti.Log_lifeCycle(TAG, "OnResume~~~");
        
        /*implementation here*/
        restoreCurrentStatus();
        
        
    }
    
    
    private void restoreCurrentStatus() {
        // TODO Auto-generated method stub
        
        /*check suspension status*/
        
        
        
        
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
        Uti.Log_lifeCycle(TAG, "OnCreate~~~");
        Uti.Log_lifeCycle(TAG, "~~~"+requestCode+" "+resultCode);

    }
    
    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Uti.Log_lifeCycle(TAG, "OnRestart~~~");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Uti.Log_lifeCycle(TAG, "OnStart~~~");
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Uti.Log_lifeCycle(TAG, "OnPause~~~");
        
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Uti.Log_lifeCycle(TAG, "onStop~~~");
        
    }

    @Override
    protected void onDestroy() {
        Uti.Log_lifeCycle(TAG, "onDestroy~~~");
        // implementation here
        
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Uti.Log_lifeCycle(TAG, "onBackPressed~~~");
    }


}
