package edu.missouri.niaaa.pain.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.Utilities;
import edu.missouri.niaaa.pain.survey.category.Answer;
import edu.missouri.niaaa.pain.survey.category.Category;
import edu.missouri.niaaa.pain.survey.category.Question;
import edu.missouri.niaaa.pain.survey.category.RandomCategory;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;
import edu.missouri.niaaa.pain.survey.parser.XMLParser;

public class SurveyAct extends Activity {
    String TAG = "SurveyActivity.java";
    boolean logEnable = true;
    
    /*survey init variables*/
    List<SurveyInfo> surveys = null;
    int surveyType = -1;
    int surveySeq = -1;
    int remindSeq = -1;
    boolean manualTrigger = false;
    String surveyDisplayName;
    String surveyFileName;
    String pinCheckDialogTitle;
    
    /*dialogs*/
    Dialog pinCheckDialog;
    Dialog retryPinDialog;
    
    /*used by survey layout*/
    //Button used to submit each question
    Button submitButton;
    Button backButton;
    //Current category
    Category currentCategory;
    //Current question
    Question currentQuestion;
    //Will be set if a question needs to skip others
    boolean hasSkip = false;
    String skipFrom = null;
    //Category position in arraylist
    int categoryNum;
    //a serializable in an intent
    LinkedHashMap<String, List<String>> answerMap;
    //List of read categories
    ArrayList<Category> cats = null;
    
    /*sound*/
    SoundPool soundpool;
    private SparseIntArray soundMap;
    Timer soundTimer;
    TimerTask soundTask;
    int soundStreamID;
    int soundPlayAfter = 1000;
    Vibrator vibrator;
    
    WakeLock wakelock;
    
    //
    SharedPreferences shp;
    

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Util.Log_lifeCycle(TAG, "OnCreate~~~");
        Util.Log_debug(TAG, "~~~"+getIntent().getIntExtra(Util.SV_TYPE, -1)+" "+getIntent().getIntExtra(Util.SV_SEQ, -1)+" "+getIntent().getIntExtra(Util.SV_REMIND_SEQ, -1));
        
        wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        soundpool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
        soundMap = new SparseIntArray();
        soundMap.put(1, soundpool.load(this, R.raw.alarm_sound, 1));
        soundTimer = new Timer();
        
        
        getSurveyList();
        
        init();
        
        setTitle(surveyDisplayName);
        setContentView(R.layout.survey_layout);
        setListeners();
        
        initializeSurveyLayout();
        
    }


    private void init() {
        // TODO Auto-generated method stub
        
        initializeVariable();
        
        checkStatue();
    }


    private void initializeVariable() {
        // TODO Auto-generated method stub
        surveyType = getIntent().getIntExtra(Util.SV_TYPE, -1);// protect for -1 //onNewIntent, should be same, is there any chance that type changes??
        surveySeq = getIntent().getIntExtra(Util.SV_SEQ, -1);// protect for -1
        remindSeq = getIntent().getIntExtra(Util.SV_REMIND_SEQ, -1);// protect for -1
        manualTrigger = getIntent().getBooleanExtra(Util.SV_MANUAL, false);
        
        SurveyInfo si = surveys.get(surveyType-1);
        surveyDisplayName = (Util.RELEASE ? si.getDisplayName() : num2seq(surveySeq) + si.getDisplayName());
        surveyFileName = si.getFileName();
        
        pinCheckDialogTitle = (Util.RELEASE ? getString(R.string.pin_title) : getString(R.string.pin_title) + " for reminder "+remindSeq);
    }

    
    private void checkStatue() {
        // TODO Auto-generated method stub
        acquireWakeLock();
        
        pinCheckDialog = userPinCheckDialog(this);
        retryPinDialog = retryUserPinDialog();
        
        if(manualTrigger){
            
        }
        else{
            playSound();
            
        }
        
        if(pinCheckDialog.isShowing()) {
            pinCheckDialog.dismiss();
        }
        pinCheckDialog.show();
    }
    

    private void getSurveyList() {
        // TODO Auto-generated method stub
        
        /*prepare survey info*/
        try {
            surveys = Util.getSurverList(this);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        //print 
        for(SurveyInfo survey: surveys){
            Util.Log_debug(TAG, false, survey.getType()+" "+survey.getDisplayName()+" "+survey.getAction()+" "+SurveyInfo.TYPE_SHOWN_MAP.get(survey.getAction()));
        }
    }


    private void initializeSurveyLayout() {
        // TODO Auto-generated method stub
        
        //Initialize map that will pass questions and answers to service
        answerMap = new LinkedHashMap<String, List<String>>();
        
        //Tell the parser which survey to use
        Util.Log_debug(TAG, "survey file is "+surveyFileName);

        //Open the specified survey
        try {
            /* .parseQuestion takes an input source to the assets file,
             * a context in case there are external files, a boolean for
             * allowing external files, and a baseid that will be appended
             * to question ids.  If boolean is false, no context is needed.
             */
            cats = new XMLParser().parseQuestion(new InputSource(getAssets().open(surveyFileName)),this,true,"");
        } catch (IOException e) {
            e.printStackTrace();
        }

        printCategoryForDebug(cats, false);


        //Survey doesn't contain any categories
        if(cats == null){
            //surveyComplete();
        }
        //Survey contain categories
        else{
            //Set current category to the first category
            currentCategory = cats.get(0);
            //Setup the layout
            ViewGroup vg = setupLayout(nextQuestionLayout());
            if(vg != null) {
                setContentView(vg);
            }
        }
    }
    


    private void setListeners() {
        // TODO Auto-generated method stub

        /*
         * The same submit button is used for every question.
         * New buttons could be made for each question if
         * additional specific functionality is needed/
         */
        submitButton = new Button(this);
        backButton = new Button(this);
        submitButton.setText(R.string.btn_submit);
        backButton.setText(R.string.btn_cancel);

        submitButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(currentQuestion.validateSubmit()){
                    ViewGroup vg = setupLayout(nextQuestionLayout());
                    if(vg != null){
                        setContentView(vg);
                    }
                    backButton.setText(R.string.btn_previous);
                }

//              setTitle(surveyName);

            }
        });

        backButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                ViewGroup vg = setupLayout(lastQuestionLayout());
                if(vg != null) {
                    setContentView(vg);
                }

                if(backButton.getText().equals(SurveyAct.this.getString(R.string.btn_cancel))){
                    onBackPressed();
                }

            }
        });


    }
    
    
    private void surveyStart(){
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, "~~~Survey Start");
    }
    
    
    private void surveyComplete() {
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, "~~~Survey Complete");
    }
    
    
    
    /**
     * @return Get the next question to be displayed
     */
    protected LinearLayout nextQuestionLayout(){
//      Utilities.Log("~~~~~~~~~~~~~~~~~~~~next", "currentQ" + (currentQuestion != null ? currentQuestion.getSelectedAnswers().get(0) + currentQuestion.getSkip() : "null"));

        Question temp = null;
        boolean done = false;
        boolean allowSkip = false;

        if(currentQuestion != null && !hasSkip) {
            skipFrom = currentQuestion.getId();
        }

        do{
            if(temp != null) {
                answerMap.put(temp.getId(), null);
            }

            //Simplest case: category has the next question
            temp = currentCategory.nextQuestion();


            //Category is out of questions, try to move to next category
            if(temp == null && (++categoryNum < cats.size())){
                /* Advance the category.  Loop will get the question
                 * on next iteration.
                 */
                currentCategory = cats.get(categoryNum);
                if(currentCategory instanceof RandomCategory && currentQuestion.getSkip() != null){
                    //Check if skip is in category
                    RandomCategory tempCat = (RandomCategory) currentCategory;
                    if(tempCat.containsQuestion(currentQuestion.getSkip())){
                        allowSkip = true;
                    }

                }
            }


            //Out of categories, survey must be done
            else if(temp == null){
                //Log.d("XMLActivity","Should be done...");
                done = true;
                break;
                //surveyComplete();
            }

        }while(temp == null ||
                (currentQuestion != null && currentQuestion.getSkip() != null && !(currentQuestion.getSkip().equals(temp.getId()) || allowSkip)
                && ( !currentQuestion.getId().equals(temp.getId()) && temp.clearSelectedAnswers())
                )
              );
        /*if(currentQuestion != null){
            answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());
        }*/

        if(done){
            //surveyComplete();
            return null;
        }
        else{
            currentQuestion = temp;
//          Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", currentQuestion.getId());
            return currentQuestion.prepareLayout(this);
        }

    }

    
    /**
     * @return
     */
    protected LinearLayout lastQuestionLayout(){
//      Utilities.Log("~~~~~~~~~~~~~~~~~~~~last", "skipFrom"+ skipFrom);
        Question temp = null;

        while(temp == null){
//          Utilities.Log("~~~~~~~~~while", "0 skipfrom "+skipFrom+"skipTo "+skipTo);
            temp = currentCategory.lastQuestion();
            //Log.d(TAG,"Trying to get previous question");
            /*
             * If temp is null, this category is out of questions,
             * we need to go back to the previous category if it exists.
             */
            if(temp == null){
//              Utilities.Log("~~~~~~~~~", "1");
                //Log.d(TAG,"Temp is null, probably at begining of category");
                /* Try to go back a category, get the question on
                 * the next iteration.
                 */
                if(categoryNum - 1 >= 0){
                    //Log.d(TAG,"Moving to previous category");
                    categoryNum--;
                    currentCategory = cats.get(categoryNum);
                }
                //First question in first category, return currentQuestion
                else{
                    //Log.d(TAG,"No previous category, staying at current question");
                    backButton.setText(R.string.btn_cancel);
                    temp = currentQuestion;
                }
            }
            /* A question with no answer must have been skipped,
             * skip it again.
             */
            else if(temp != null && !temp.validateSubmit()){
                //Log.d(TAG, "No answer, skipping question");
//              Utilities.Log("~~~~~~~~~", "2 "+temp.getId()+" "+temp.validateSubmit());
                temp = null;
            }

            if(temp != null && hasSkip && !temp.getId().equals(skipFrom)){
//              Utilities.Log("~~~~~~~~~", "3 skipfrom"+skipFrom);
                temp = null;
            }
            else if(temp != null && hasSkip){
//              Utilities.Log("~~~~~~~~~", "4");
                hasSkip = false;
                skipFrom = null;
            }
            //Else: valid question, it will be returned.
        }
        currentQuestion = temp;
//      Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", currentQuestion.getId());

        return currentQuestion.prepareLayout(this);
    }
    

    /**
     * @param layout
     * @return
     */
    protected LinearLayout setupLayout(LinearLayout layout){
        /* Didn't get a layout from nextQuestion(),
         * error (shouldn't be possible) or survey complete,
         * either way finish safely.
         */
        if(layout == null){
            surveyComplete();
            return null;
        }
        else{
            //Setup LinearLayout
            LinearLayout sv = new LinearLayout(getApplicationContext());
            //Remove submit button from its parent so we can reuse it
            if(submitButton.getParent() != null){
                ((ViewGroup)submitButton.getParent()).removeView(submitButton);
            }
            if(backButton.getParent() != null){
                ((ViewGroup)backButton.getParent()).removeView(backButton);
            }
            //Add submit button to layout

            LinearLayout.LayoutParams keepFull = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

            RelativeLayout.LayoutParams keepBTTM = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            keepBTTM.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            //sv.setLayoutParams(keepFull);
            //layout.setLayoutParams(keepFull);

            LinearLayout rela = new LinearLayout(getApplicationContext());
            //rela.setLayoutParams(keepFull);

            LinearLayout buttonCTN = new LinearLayout(getApplicationContext());
            buttonCTN.setOrientation(LinearLayout.VERTICAL);
            buttonCTN.setLayoutParams(keepFull);

            buttonCTN.addView(submitButton);
            buttonCTN.addView(backButton);

            rela.addView(buttonCTN);
            layout.addView(rela);

            //layout.addView(submitButton);
            //layout.addView(backButton);
            //Add layout to scroll view in case it's too long
            sv.addView(layout);
            //Display scroll view
            setContentView(sv);
            return sv;
        }
    }
    
    
    
    private Dialog retryUserPinDialog(){

        return new AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(R.string.pin_title_wrong)
        .setMessage(R.string.pin_message_wrong)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                pinCheckDialog.show();
                dialog.cancel();

            }
        })
        .create();
    }
    
    
    private Dialog userPinCheckDialog(final Context context) {
        
        LayoutInflater inflater = LayoutInflater.from(context);
        final View DialogView = inflater.inflate(R.layout.pin_input, null);
        TextView pinText = (TextView) DialogView.findViewById(R.id.pin_text);
        pinText.setText(R.string.pin_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(pinCheckDialogTitle);
        builder.setView(DialogView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText pinEdite = (EditText) DialogView.findViewById(R.id.pin_edit);
                String pinStr = pinEdite.getText().toString();
                Utilities.Log("Pin Dialog", "pin String is "+pinStr);

                if (pinStr.equals(Utilities.getPWD(context))){
                    stopSound();

                    surveyStart();

                    dialog.cancel();
                }
                else {
                    dialog.cancel();
                    retryPinDialog.show();
                }
                dialog.cancel();

            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                stopSound();
                finish();
            }
        });

        return builder.create();
    }
    
    


    
    
    
    
    
    
    
    
    
    
    
    
    

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Util.Log_lifeCycle(TAG, "onStart~~~");
        
        
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Util.Log_lifeCycle(TAG, "onRestart~~~");
        
        
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Util.Log_lifeCycle(TAG, "onResume~~~");
        
        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        /*set new intent*/
        setIntent(intent);
        
        Util.Log_lifeCycle(TAG, "onNewIntent~~~");
        Util.Log_debug(TAG, "~~~"+getIntent().getIntExtra(Util.SV_TYPE, -1)+" "+getIntent().getIntExtra(Util.SV_SEQ, -1)+" "+getIntent().getIntExtra(Util.SV_REMIND_SEQ, -1));
        
        /*actually only auto triggered survey can be here*/
        init();
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*screen lock*/
    
    private void acquireWakeLock() {
        // TODO Auto-generated method stub
        wakelock.acquire();
    }
    
    private void releaseWakeLock() {
        // TODO Auto-generated method stub
        if(wakelock.isHeld()) {
            wakelock.release();
        }
    }
    
    
    /*sound & vibrator*/
    
    private void playSoundOnprepared(){
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

            soundStreamID = soundpool.play(soundMap.get(1), 1, 1, 1, 0, 1); // craving should be different
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
    
    
    
    /*some utilities*/
    
    private String num2seq(int num){
        String seq = "";
        switch(num){
        case 1:
            seq = "1st ";
            break;
        case 2:
            seq = "2nd ";
            break;
        case 3:
            seq = "3rd ";
            break;
        default:
            seq = ""+num+"th ";
        }
        return seq;
    }
    
    
    private void printCategoryForDebug(ArrayList<Category> cats, boolean able) {
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, able, "-------------^^^^^^^^______________");
        if(able)
        for(Category ca :cats){
            Utilities.Log(TAG, "category is "+ca.getQuestionDesc());
            Utilities.Log(TAG, "category contains questions "+ca.totalQuestions());
            for(Question q: ca.getQuestions()){
                Utilities.Log(TAG, "question id "+q.getId());
                for(Answer a: q.getAnswers()){
                    Utilities.Log(TAG, "contains trigger "+a.hasSurveyTrigger()+" is selected "+a.isSelected()+" answer skipto "+a.getSkip());
                }
            }
        }
    }
    
    /******************************************************************************************************************************************/
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Util.Log_lifeCycle(TAG, "onPause~~~");
        
        stopSound();
        releaseWakeLock();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Util.Log_lifeCycle(TAG, "onStop~~~");
        
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "onDestroy~~~");
        
        releaseSound();
        
        if(pinCheckDialog.isShowing())
            pinCheckDialog.dismiss();
        if(retryPinDialog.isShowing())
            retryPinDialog.dismiss();
        
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "onBackPressed~~~");
        
        new AlertDialog.Builder(this)
        .setTitle(R.string.survey_cancel_title)
        .setMessage(R.string.survey_cancel_msg)
        .setCancelable(false)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

//                String[] reminder = getReminderTimeStamp(context);
//                try {
//                    String seq = "";
//                    int surSeq = shp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName), -1);
//                    if (surSeq == 0) {
//                        surSeq = Utilities.MAX_TRIGGER_MAP.get(surveyName);
//                    }
//                    if (surveyName.equals(Utilities.SV_NAME_RANDOM)) {
//                        seq = "," + surSeq;
//                    }
//
//                    Utilities.writeEventToFile(context, getSurveyType(), getScheduleTimeStamp(),
//                            reminder[0], reminder[1], reminder[2],
//                            "", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

                SurveyAct.super.onBackPressed();
            }
        }).create().show();
    }
    
    
    
}
