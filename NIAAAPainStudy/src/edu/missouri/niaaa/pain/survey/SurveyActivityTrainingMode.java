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
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
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
import android.widget.Toast;
import edu.missouri.niaaa.pain.MainActivity;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.activity.DialogActivity;
import edu.missouri.niaaa.pain.survey.category.Answer;
import edu.missouri.niaaa.pain.survey.category.Category;
import edu.missouri.niaaa.pain.survey.category.Question;
import edu.missouri.niaaa.pain.survey.category.RandomCategory;
import edu.missouri.niaaa.pain.survey.category.SurveyQuestion;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;
import edu.missouri.niaaa.pain.survey.parser.XMLParser;

public class SurveyActivityTrainingMode extends Activity {
    String TAG = "SurveyActivityTrainingMode.java";
    boolean logEnable = true;

    /*survey init variables*/
    List<SurveyInfo> surveylist = null;
    int surveyType = -1;
    int surveySeq = -1;
    int remindSeq = -1;
    boolean manualTrigger = false;
    String surveyDisplayName;
    String surveyName;
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
    SoundPool soundPool;
    private SparseIntArray soundMap;
    Timer soundTimer;
    TimerTask soundTask;
    int soundStreamID;
    int soundPlayAfter = 1000;
    Vibrator vibrator;

    //
    public static final int REMIND_LASTONE = 0;
    public static final int REMIND_TIMEOUT = -2;

    boolean onGoing = false;
    
    Intent dialogIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Util.Log_lifeCycle(TAG, "OnCreate~~~");
        Util.Log_debug(TAG, "~~~"+getIntent().getIntExtra(Util.SV_TYPE, -1)+" "+getIntent().getIntExtra(Util.SV_SEQ, -1)+" "+getIntent().getIntExtra(Util.SV_REMIND_SEQ, -1));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        soundMap = new SparseIntArray();
        soundTimer = new Timer();

        getSurveyList();

        init();

    }


    private void initSurveyLayout() {
        // TODO Auto-generated method stub
        setTitle(surveyDisplayName);
        setContentView(R.layout.survey_layout);
        setListeners();

        setSurveyLayout();
    }


    private void init() {
        // TODO Auto-generated method stub

        initVariable();

        checkStatus();
    }


    private void initVariable() {
        // TODO Auto-generated method stub
        surveyType = getIntent().getIntExtra(Util.SV_TYPE, -1);// protect for -1 //onNewIntent, should be same, is there any chance that type changes??
        surveySeq = getIntent().getIntExtra(Util.SV_SEQ, -1);// protect for -1
        remindSeq = getIntent().getIntExtra(Util.SV_REMIND_SEQ, -1);// protect for -1
        manualTrigger = getIntent().getBooleanExtra(Util.SV_MANUAL, false);

        SurveyInfo si = surveylist.get(surveyType-1);
        surveyDisplayName = si.getDisplayName()+" (Training Mode)";
        surveyName = si.getName();
        surveyFileName = si.getFileName();

        pinCheckDialogTitle = (Util.RELEASE ? getString(R.string.pin_title) : getString(R.string.pin_title) + " for reminder "+remindSeq);
    }


    /*
     * if manually
     *      timeout     x
     *      remind0     x
     *      survey      without sound alarm
     * else (auto)
     *      timeout     finish + write with expire time (previous swipe quit)
     *      remind0     finish + write if needed
     *      remind123   survey with sound alarm
     *
     * */
    private void checkStatus() {
        // TODO Auto-generated method stub

        if(manualTrigger){
            //do nothing

            pinLayout();
        }
        else{
            //pin + sound
            playSoundOnPrepared();

            pinLayout();

        }
    }

    private void pinLayout(){
      //show user pin check dialog
        pinCheckDialog = userPinCheckDialog(this);
        retryPinDialog = singleOptionDialog(R.string.pin_title_wrong, R.string.pin_message_wrong, DIALOG_RETRY);
        pinCheckDialog.show();

        initSurveyLayout();
    }

    private void reInit(Intent newIntent) {
        // TODO Auto-generated method stub

        initVariable();// generate old vars
        //debug - old vars
        Util.Log_debug(TAG, "Old intent "+"survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq+" man? "+manualTrigger+" ondoing "+onGoing);

        reCheckStatus(newIntent);

    }


    /*
     * (new survey can only be auto)
     * if onGoing
     *      if new survey is same survey
     *          if old survey is man or auto
     *              timeout     write with new survey
     *              remind1230  x
     *      else (other new survey)
     *          if old survey is man or auto
     *              timeout     x
     *              remind123   noPrompt write with new survey info
     *              remind0     write if need
     * else (not input pin) - setNewIntent if needed
     *      if new survey is same survey
     *          if old survey is man
     *              x
     *          else (old auto)
     *              timeout     x
     *              remind0     finish + write ignored with old survey info
     *              remind123   sound alarm + write ignored with old survey info
     *      else (other new survey)
     *          if old survey is man
     *              timeout     x
     *              remind123   new survey with sound alarm
     *              remind0     write if need
     *          else (old auto)
     *              timeout     x
     *              remind0     write if need
     *              remind123   noPrompt write with new survey info
     *
     *
     * look up all the x to see if they need finish()
     * */
    private void reCheckStatus(Intent newIntent){

        int newSurveyType = newIntent.getIntExtra(Util.SV_TYPE, -1);
        int newSurveySeq = newIntent.getIntExtra(Util.SV_SEQ, -1);
        int newRemindSeq = newIntent.getIntExtra(Util.SV_REMIND_SEQ, -1);
        boolean sameSurvey = (surveyType == newSurveyType && surveySeq == newSurveySeq);

        if(onGoing){//keep old intent
            if(sameSurvey){
                if(newRemindSeq == REMIND_TIMEOUT){
                    //normal timeout

                    //write
                    Util.Log_debug(TAG, "### write event, timeout -> onNewIntent, survey: "+newSurveyType+" seq: "+newSurveySeq+" remind: "+newRemindSeq);

                    dialogIntent = new Intent(this, DialogActivity.class);
                    dialogIntent.putExtra(DialogActivity.DIALOG_FLAG, DialogActivity.DIALOG_TIMEOUT);
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dialogIntent);

                    finish();
                }
                else{//remind1230
                    Util.Log_debug(TAG, "############# something happen place 1");
                }
            }
            else{//diff survey
                if(newRemindSeq == REMIND_TIMEOUT){
                    Util.Log_debug(TAG, "############# something happen place 2");
                }
                else if(newRemindSeq == REMIND_LASTONE){
                    // write if need
                }
                else{//remind123

                    //normal NoPrompt_underDoing
                    Util.Log_debug(TAG, "### write event, noPrompt_underDoing -> onNewIntent, survey: "+newSurveyType+" seq: "+newSurveySeq+" remind: "+newRemindSeq);

                    //write

                    Toast.makeText(this, "An auto-triggered survey is just blocked by what you are doning right now!", Toast.LENGTH_LONG).show();
                }
            }
        }

        else{//not input pin yet
            if(sameSurvey){
                if(newRemindSeq == REMIND_TIMEOUT){
                    Util.Log_debug(TAG, "############# something happen place 3");
                }
                else{//remind1230

                    //normal re-alarm
                    Util.Log_debug(TAG, "### write event, ignored -> onNewIntent, old survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);

                    //write ignored for old reminder

                    //if remind0
                    if(newRemindSeq == REMIND_LASTONE){
                        
                        Intent launchIntent = new Intent(this, MainActivity.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(launchIntent);

                        finish();
                    }

                    setIntent(newIntent);
                    initVariable();// generate new vars

                    //sound + pin
                    pinSound();
                }
            }
            else{//diff survey
                if(newRemindSeq == REMIND_TIMEOUT){
                    Util.Log_debug(TAG, "############# something happen place 4");
                }
                else if(newRemindSeq == REMIND_TIMEOUT){
                    Util.Log_debug(TAG, "############# something happen place 5");
                }
                else{//remind123
                    if(manualTrigger){
                        //new survey
                        setIntent(newIntent);
                        initVariable();// generate new vars

                        //sound + pin
                        pinSound();

                        //load survey with new intent
                        initSurveyLayout();
                    }
                    else{//old auto
                        //normal NoPrompt_underDoing
                        Util.Log_debug(TAG, "### write event, noPrompt_underDoing -> onNewIntent, survey: "+newSurveyType+" seq: "+newSurveySeq+" remind: "+newRemindSeq);

                        //write

                        Toast.makeText(this, "An auto-triggered survey is just blocked by what you are doning right now!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }


    private void pinSound(){
        playSoundOnPrepared();

        if(pinCheckDialog.isShowing()) {
            pinCheckDialog.dismiss();
        }
        pinCheckDialog = userPinCheckDialog(this);
        pinCheckDialog.show();
    }

    private void getSurveyList() {
        // TODO Auto-generated method stub

        /*prepare survey info*/
        try {
            surveylist = null;
            surveylist = Util.getSurveyList(this);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //print
        for(SurveyInfo survey: surveylist){
            Util.Log_debug(TAG, false, survey.getType()+" "+survey.getDisplayName()+" "+survey.getAction()+" "+SurveyInfo.TYPE_SHOWN_MAP.get(survey.getAction()));
        }
    }


    private void setSurveyLayout() {
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

                if(backButton.getText().equals(SurveyActivityTrainingMode.this.getString(R.string.btn_cancel))){
                    onBackPressed();
                }
            }
        });


    }


    private void surveyStart(){
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, "~~~Survey Start");

        //

        onGoing = true;

    }


    private void surveyComplete() {
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, "~~~Survey Complete");

        workWithAnswers();

        dialogIntent = new Intent(this, DialogActivity.class);
        dialogIntent.putExtra(DialogActivity.DIALOG_FLAG, DialogActivity.DIALOG_FINISH);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        
        startActivity(dialogIntent);
        finish();
    }


    private void workWithAnswers() {
        // TODO Auto-generated method stub
        
        //Fill answer map for when it is passed to service
        for(Category cat: cats){
//          Util.Log_debug(TAG, "category is "+cat.getQuestionDesc());
//          Util.Log_debug(TAG, "category contains questions "+cat.totalQuestions());
            for(Question question: cat.getQuestions()){
//              Util.Log_debug(TAG, "question id "+question.getId());
                answerMap.put(question.getId(), question.getSelectedAnswers());
                //Here to target the first question of Drinking Follow-up
                for(Answer answer: question.getAnswers()){
//                    Util.Log_debug("_________________________________","answer "+answer.getAnswerText()+" "+answer.getId()+" "+answer.hasSurveyTrigger());
//                    Util.Log_debug(TAG, "contains trigger "+answer.hasSurveyTrigger()+" is selected "+answer.isSelected());
                }

                for(String answer: question.getSelectedAnswers()){
                    Util.Log_debug("+++++++++++++++++++++++++++++","answer string "+answer);
                }
            }
        }

        Toast.makeText(this, R.string.survey_completed, Toast.LENGTH_LONG).show();
    }
    /**
     * @return Get the next question to be displayed
     */
    protected LinearLayout nextQuestionLayout(){
//      Util.Log_debug("~~~~~~~~~~~~~~~~~~~~next", "currentQ" + (currentQuestion != null ? currentQuestion.getSelectedAnswers().get(0) + currentQuestion.getSkip() : "null"));

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
//          Util.Log_debug("~~~~~~~~~~~~~~~~~~~~n", currentQuestion.getId());
            return currentQuestion.prepareLayout(this);
        }

    }


    /**
     * @return
     */
    protected LinearLayout lastQuestionLayout(){
//      Util.Log_debug("~~~~~~~~~~~~~~~~~~~~last", "skipFrom"+ skipFrom);
        Question temp = null;

        while(temp == null){
//          Util.Log_debug("~~~~~~~~~while", "0 skipfrom "+skipFrom+"skipTo "+skipTo);
            temp = currentCategory.lastQuestion();
            //Log.d(TAG,"Trying to get previous question");
            /*
             * If temp is null, this category is out of questions,
             * we need to go back to the previous category if it exists.
             */
            if(temp == null){
//              Util.Log_debug("~~~~~~~~~", "1");
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
//              Util.Log_debug("~~~~~~~~~", "2 "+temp.getId()+" "+temp.validateSubmit());
                temp = null;
            }

            if(temp != null && hasSkip && !temp.getId().equals(skipFrom)){
//              Util.Log_debug("~~~~~~~~~", "3 skipfrom"+skipFrom);
                temp = null;
            }
            else if(temp != null && hasSkip){
//              Util.Log_debug("~~~~~~~~~", "4");
                hasSkip = false;
                skipFrom = null;
            }
            //Else: valid question, it will be returned.
        }
        currentQuestion = temp;
//      Util.Log_debug("~~~~~~~~~~~~~~~~~~~~l", currentQuestion.getId());

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


    private final int DIALOG_RETRY = 1;

    private Dialog singleOptionDialog(int title, int message, final int flag){

        return new AlertDialog.Builder(this)
        .setCancelable(false)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                switch(flag){
                case DIALOG_RETRY:

                    pinCheckDialog.show();
                    dialog.cancel();
                    break;
                default:

                    break;
                }
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
                Util.Log_debug("Pin Dialog", "pin String is "+pinStr);

                if (pinStr.equals(Util.getPWD(context))){

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


                //write refused
                Util.Log_debug(TAG, "### write event, refused -> survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);

                //write

                //stop sound and quit
                stopSound();

                SurveyActivityTrainingMode.super.onBackPressed();
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

        Util.Log_lifeCycle(TAG, "onNewIntent~~~");
        Util.Log_debug(TAG, "~~~"+intent.getIntExtra(Util.SV_TYPE, -1)+" "+intent.getIntExtra(Util.SV_SEQ, -1)+" "+intent.getIntExtra(Util.SV_REMIND_SEQ, -1));

        reInit(intent);
    }




















    /*sound & vibrator*/

    private void playSoundOnPrepared(){
        Util.Log_debug(TAG, "play sound on prepared---");

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


        vibrator.vibrate(Util.VIBRATE_FOR_SECONDS * 1000);
    }

    private class StartTask extends TimerTask {
        @Override
        public void run(){

            soundStreamID = soundPool.play(soundMap.get(1), 1, 1, 1, 0, 1); // craving should be different
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



    /*some utilities*/

    private void printCategoryForDebug(ArrayList<Category> cats, boolean able) {
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, able, "-------------^^^^^^^^______________");
        if(able)
        for(Category ca :cats){
            Util.Log_debug(TAG, "category is "+ca.getQuestionDesc());
            Util.Log_debug(TAG, "category contains questions "+ca.totalQuestions());
            for(Question q: ca.getQuestions()){
                Util.Log_debug(TAG, "question id "+q.getId());
                for(Answer a: q.getAnswers()){
                    Util.Log_debug(TAG, "contains trigger "+a.hasSurveyTrigger()+" is selected "+a.isSelected()+" answer skipto "+a.getSkip());
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
        if(SurveyQuestion.softTriggers != null){
            SurveyQuestion.softTriggers.clear();
        }

        releaseSound();

        if(pinCheckDialog != null && pinCheckDialog.isShowing())
            pinCheckDialog.dismiss();
        if(retryPinDialog !=null &&retryPinDialog.isShowing())
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
                Util.Log_lifeCycle(TAG, "~~~onBackPressed YES");

                //write quit
                Util.Log_debug(TAG, "### write event, quit -> survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);

                //write
                
                //end Cycle

                SurveyActivityTrainingMode.super.onBackPressed();
            }
        }).create().show();
    }


}
