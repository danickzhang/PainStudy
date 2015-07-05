package edu.missouri.niaaa.pain.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseIntArray;
import edu.missouri.niaaa.pain.MainActivity;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;

public class DialogActivity extends Activity {

    /*sound*/
    SoundPool soundPool;
    private SparseIntArray soundMap;
    Timer soundTimer;
    TimerTask soundTask;
    int soundStreamID;
    int soundPlayAfter = 1000;
    Vibrator vibrator;

    Dialog dialog;

    public static final String DIALOG_FLAG = "DIALOG_FLAG";

    public static final int DIALOG_CHARGE_REMIND = 1;
    public static final int DIALOG_TIMEOUT = 2;
    public static final int DIALOG_MORNING = 3;
    public static final int DIALOG_FINISH = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("test", "onCreate chargerActivity");
        super.onCreate(savedInstanceState);

        int flag = getIntent().getIntExtra(DIALOG_FLAG, -1);

        init();

        switch(flag){
        case DIALOG_CHARGE_REMIND:

            if(!Util.isSuspensionFlag(this)){
                playSoundOnPrepared();
            }
            makeDialog(R.string.charge_reminder_alert_title, R.string.charge_reminder_alert_message, flag).show();
            break;
        case DIALOG_TIMEOUT:
            makeDialog(R.string.morning_report_title4, R.string.survey_timeout, flag).show();
            break;
        case DIALOG_MORNING:
            makeDialog(R.string.morning_report_title4, R.string.morning_report_msg4, flag).show();
            break;
        case DIALOG_FINISH:
            makeDialog(R.string.survey_completed, flag).show();
            break;
        default:
            break;
        }


//        setContentView(R.layout.activity_charge_reminder);

    }


    private void init() {
        // TODO Auto-generated method stub

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        soundMap = new SparseIntArray();
        soundTimer = new Timer();

    }

    private AlertDialog makeDialog(int title, final int flag){
        return makeDialog(title, R.string.no_message, flag);
    }

    private AlertDialog makeDialog(int title, int message, final int flag){

        return new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopSound();

                dialog.cancel();

                Intent launchIntent = new Intent(DialogActivity.this, MainActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(launchIntent);

                finish();
            }
        }).create();

    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        stopSound();

        super.onPause();
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        releaseSound();

        super.onDestroy();
    }


    /*sound & vibrator*/

    private void playSoundOnPrepared(){

        soundMap.clear();
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        if(Util.RELEASE){
            soundMap.put(1, soundPool.load(this, R.raw.alarm_sound, 1));
        }else{
            soundMap.put(1, soundPool.load(this, R.raw.alarm_sound_nodelay, 1));
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub

                playSound();
            }
        });
    }

    private void playSound(){
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Util.VOLUME, AudioManager.FLAG_PLAY_SOUND);

        soundTask = new StartTask();
        soundTimer.schedule(soundTask,soundPlayAfter);


        vibrator.vibrate(5000);
    }

    private class StartTask extends TimerTask {
        @Override
        public void run(){

            soundStreamID = soundPool.play(soundMap.get(1), 1, 1, 1, 5, 1);
        }
    }

    private void stopSound(){
//        soundTimer.cancel();
        if(soundTask != null)
        soundTask.cancel();

        if(soundPool != null)
        soundPool.stop(soundStreamID);

        vibrator.cancel();
    }

    private void releaseSound(){
        stopSound();
        if(soundPool != null)
        soundPool.release();

        soundTimer.cancel();
        soundTimer.purge();
        soundTimer = null;
    }

}
