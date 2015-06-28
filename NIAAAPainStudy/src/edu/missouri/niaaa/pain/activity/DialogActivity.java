package edu.missouri.niaaa.pain.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseIntArray;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;

public class DialogActivity extends Activity {

    /*sound*/
    SoundPool soundpool;
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
        
        init();
        int flag = getIntent().getIntExtra(DIALOG_FLAG, -1);
        
        switch(flag){
        case DIALOG_CHARGE_REMIND:
            playSoundOnPrepared();
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
        soundpool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        soundMap = new SparseIntArray();
        if(Util.RELEASE){
            soundMap.put(1, soundpool.load(this, R.raw.alarm_sound, 1));
        }else{
            soundMap.put(1, soundpool.load(this, R.raw.alarm_sound_nodelay, 1));
        }
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
                finish();
            }
        }).create();
        
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
        soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

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

            soundStreamID = soundpool.play(soundMap.get(1), 1, 1, 1, 5, 1);
        }
    }

    private void stopSound(){
//        soundTimer.cancel();
        if(soundTask != null)
        soundTask.cancel();

        soundpool.stop(soundStreamID);

        vibrator.cancel();
    }

    private void releaseSound(){
        stopSound();
        soundpool.release();
        soundTimer.cancel();
        soundTimer.purge();
        soundTimer = null;
    }

}
