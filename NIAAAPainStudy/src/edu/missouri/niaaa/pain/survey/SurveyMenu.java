package edu.missouri.niaaa.pain.survey;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;

public class SurveyMenu extends Activity {

    String TAG = "SurveyMenu.java";
    boolean logEnable = true;

    List<SurveyInfo> surveylist;
    HashMap<View, SurveyInfo> buttonMap;
    
    boolean trainMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        /*for training mode*/
        trainMode = getIntent().getBooleanExtra(Util.SV_TRAINING_MODE, false);

//      ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        //linearLayout.addView(new Button(this));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
//      scrollView.addView(linearLayout);

        //surveys = new ArrayList<SurveyInfo>();
        buttonMap = new HashMap<View, SurveyInfo>();

        //Try to read surveys from give file
        try {
            surveylist = null;
            surveylist = Util.getSurveyList(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(surveylist == null){
            Toast.makeText(this, "Invalid configuration file", Toast.LENGTH_LONG).show();
            Util.Log_debug(TAG, logEnable, "No surveys in config.xml");
            finish();
        }
        else{
            setTitle(trainMode? R.string.survey_menu_title_training : R.string.survey_menu_title);
            TextView tv = new TextView(this);
            tv.setText(trainMode? R.string.survey_menu_select_training : R.string.survey_menu_select);
            linearLayout.addView(tv);
            for(SurveyInfo survey: surveylist){
                Util.Log_debug(TAG, logEnable, survey.getType()+" "+survey.getDisplayName()+" "+survey.getAction()+" "+SurveyInfo.TYPE_SHOWN_MAP.get(survey.getAction()));
                Button b = new Button(this);
                b.setText(survey.getDisplayName());
                b.setPadding(0, 30, 0, 30);

                /* only show surveys with type contains "manually"
                 * bypass if debug mood */
                if(SurveyInfo.TYPE_SHOWN_MAP.get(survey.getAction()) || !Util.RELEASE || trainMode){
                    linearLayout.addView(b);
                }

                b.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        final SurveyInfo temp = buttonMap.get(v);
                        Util.Log_debug(TAG, logEnable, temp.getDisplayName());
                        Util.Log_debug(TAG, logEnable, temp.getDisplayName()+" "+temp.getFileName()+" "+temp.getName());

                        /*Morning
                         *      once per day
                         *      after 3am before noon
                         *
                         *confirm initial drinking
                         *      check if morning finished
                         *      check if suspension
                         * */

                        if(trainMode){
                            launchSurvey(temp.getType());
                        }
                        else if(temp.getAction().equals("4")){
                            //
                            Calendar mT = Calendar.getInstance();
                            Calendar noonT = Calendar.getInstance();
                            noonT.set(Calendar.HOUR_OF_DAY, 12);
                            noonT.set(Calendar.MINUTE, 20);
                            noonT.set(Calendar.SECOND, 0);

                            Calendar threeT = Calendar.getInstance();
                            threeT.set(Calendar.HOUR_OF_DAY, 3);
                            threeT.set(Calendar.MINUTE, 0);
                            threeT.set(Calendar.SECOND, 0);

                            if(Util.isTodayActivated(SurveyMenu.this)){
                                Alert(R.string.morning_report_title,R.string.morning_report_msg);
                            }
                            else if(mT.after(noonT)){
                                Alert(R.string.morning_report_title2,R.string.morning_report_msg2);
                            }
                            else if(mT.before(threeT)){
                                Alert(R.string.morning_report_title3, R.string.morning_report_msg3);
                            }
                            else {
                                launchSurvey(temp.getType(), true);
                            }
                        }
                        else if(temp.getAction().equals("3")){//based on activateToday, initial is still able from deactivate to 3am
                            /*manually confirmation*/
                            if(!Util.isTodayActivated(SurveyMenu.this)){//??##
                                Toast.makeText(SurveyMenu.this, R.string.morning_report_unfinished, Toast.LENGTH_LONG).show();
                            }
                            else if(Util.isInCycle(SurveyMenu.this)){
                                Toast.makeText(SurveyMenu.this, R.string.morning_report_msg5, Toast.LENGTH_LONG).show();
                            }
                            else{
                                Alert(R.string.first_drink_title, R.string.first_drink_msg, temp);
                            }
                        }
                        else if(temp.getAction().equals("2")){
                            /*maunally*/
                            launchSurvey(temp.getType(), true);
                        }
                        else{
                            /*for debug mode only*/
                            if(!Util.RELEASE){
                                launchSurvey(temp.getType(), false);
                            }
                        }

                    }
                });

                buttonMap.put(b, survey);
            }
        }

        setContentView(linearLayout);
    }

    protected void Alert(int title, int msg){
        Alert(title, msg, false, false, null);
    }

    protected void Alert(int title, int msg, SurveyInfo si){
        Alert(title, msg, true, true, si);
    }

    protected void Alert(int title, int msg, boolean neg, final boolean trigger, final SurveyInfo survey) {
        // TODO Auto-generated method stub
        Builder alertgBuilder = new AlertDialog.Builder(SurveyMenu.this);
        if(neg){
            alertgBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                        }
            })
            .setMessage(getResources().getString(msg)+" "+survey.getDisplayName()+"?");
        }
        else{
            alertgBuilder.setMessage(msg);
        }
        Dialog alertDialog = alertgBuilder
        .setCancelable(true)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(trigger){
                    launchSurvey(survey.getType(), true);
                }
            }
        })
        .create();
        alertDialog.show();
    }

    
    private void launchSurvey(String type){
        Intent launchIntent = new Intent(getApplicationContext(), SurveyActivityTrainingMode.class);
        launchIntent.putExtra(Util.SV_TYPE, (int)Integer.valueOf(type));
        
        startActivityForResult(launchIntent, 0);
    }

    private void launchSurvey(String type, boolean man){
        Intent launchIntent = new Intent(getApplicationContext(), SurveyActivity.class);
        launchIntent.putExtra(Util.SV_TYPE, (int)Integer.valueOf(type));
        if(man){
            launchIntent.putExtra(Util.SV_MANUAL, true);
        }

//      if (surveyName.equalsIgnoreCase("RANDOM_ASSESSMENT"))
//          launchIntent.putExtra("random_sequence", randomSeq);

        //add timeout alarm here
        startActivityForResult(launchIntent, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Util.Log_debug(TAG, "OnActivityResult~~~ "+requestCode+" "+resultCode);

        switch(requestCode){
        case 0:
            if(resultCode == 1){
                Toast.makeText(this, R.string.survey_timeout, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == 2){
                Toast.makeText(this, R.string.morning_report_unfinished, Toast.LENGTH_LONG).show();
            }
            else if(resultCode == 3){
//              Toast.makeText(this, "morning complete", Toast.LENGTH_LONG).show();
//              new AlertDialog.Builder(this)
//              .setTitle(R.string.morning_report_title4)
//              .setMessage(R.string.morning_report_msg4)
//              .setCancelable(false)
//              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                  @Override
//                  public void onClick(DialogInterface dialog, int which) {
//                      dialog.cancel();
//                  }
//              })
//              .create().show();
            }else{

            }

            break;
        default:

            break;
        }

    }

}
