package edu.missouri.niaaa.pain.survey.question;

import java.util.ArrayList;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.survey.category.Answer;
import edu.missouri.niaaa.pain.survey.category.QuestionType;
import edu.missouri.niaaa.pain.survey.category.SurveyQuestion;

public class RadioQuestion extends SurveyQuestion {

/*  field*/
    boolean answered;
    String skipTo;

/*  constructor*/
    public RadioQuestion(String id){
        this.questionId = id;
        this.questionType = QuestionType.RADIO;
    }


/*  function*/
    @Override
    public LinearLayout prepareLayout(final Context c) {
        SharedPreferences shp = Util.getSP(c, Util.SP_LOGIN);
        
        String primary_ori = "Primary";
        String secondary_ori = "Secondary";
        String none = "None";
        String primary = shp.getString(Util.SP_LOGIN_PRIMARY_MED, none);
        String secondary = shp.getString(Util.SP_LOGIN_SECONDARY_MED, none);
        
        String other1_ori = "Non-Opioid PRESCRIPTION pain medication";
        String other2_ori = "OVER-THE-COUNTER pain medication";
        String other1 = shp.getString(Util.SP_LOGIN_OTHER_MED1, other1_ori);
        String other2 = shp.getString(Util.SP_LOGIN_OTHER_MED2, other2_ori);

        //Linearlayout
        LinearLayout layout = new LinearLayout(c);
        layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        //question layout
        LinearLayout.LayoutParams QTextLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        QTextLayout.setMargins(10, 0, 0, 0);

        TextView questionText = new TextView(c);
        questionText.setText(getQuestion().replace("|", "\n"));
        //questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        questionText.setLines(4);
        questionText.setLayoutParams(QTextLayout);


        //answer layout
        RadioGroup radioGroup = new RadioGroup(c);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        for(Answer ans: this.answers){
            LinearLayout.LayoutParams radioLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            radioLayout.setMargins(10, 0, 10, 0);

            RadioButton radio = new RadioButton(c);
            radio.setText(ans.getAnswerText()
                    .replace(primary_ori, primary)
                    .replace(secondary_ori, secondary)
                    .replace(other1_ori, other1)
                    .replace(other2_ori, other2)
                    );
            
            int size = (this.answers.size()>8 ? 17: (ans.getAnswerText().length()<35? 25 : 22)) ;
            radio.setTextSize(TypedValue.COMPLEX_UNIT_DIP,size);
            radio.setLayoutParams(radioLayout);
            radio.setPadding(0, 10, 20, 0);
            if(this.answers.size()>8){
                radio.setHeight(52);//??##need to be change later
            }

            radioGroup.addView(radio);

            answerViews.put(radio, ans);
            radio.setOnCheckedChangeListener(new OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Answer a = answerViews.get(buttonView);


                    if(isChecked){
//                      Log.d("final ", "answer text is "+a.getAnswerText()+" "+"answer getskip is "+a.getSkip());
                        skipTo = a.getSkip();
                        a.setSelected(true);
                        for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
                            if(!entry.getValue().equals(a)){
                                entry.getValue().setSelected(false);
                            }
                        }
                    }
                    else{
                        a.setSelected(false);
                    }

                    answered = true;

                    //dialog
                    if(isChecked && a.hasOption()){
                        new AlertDialog.Builder(c)
//                      .setTitle(R.string.bedtime_title)
                        .setMessage(a.getOption())
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create().show();
                    }
                    
                    //soft trigger
                    if(a.hasSoftTrigger()){
                        if(isChecked){
                            softTriggers.put(a.getSoftTrigger(), true);
                        }
                        else{
                            softTriggers.put(a.getSoftTrigger(), false);
                        }
                        
                        Util.Log_debug("soft triggerttttttttttt", ""+softTriggers.get(a.getSoftTrigger()));
                    }
                    
                    //soft skip
                    if(hasSoftSkip()){
                        String sTrigger = getSoftSkip().split(":")[0];
                        String sSkip = getSoftSkip().split(":")[1];
                        boolean jump = false;
                        for(String s : sTrigger.split("_")){
                            jump |= (softTriggers.get(s) == null ? false : softTriggers.get(s));
                        }
                        
                        if(!jump){
                            skipTo = sSkip;
                        }
                        Util.Log_debug("soooooooooooooooft skip", softTriggers.get("A")+" "+softTriggers.get("B")+" "+softTriggers.get("C")+" "+jump+" "+sTrigger+" "+sSkip);
                    }
                }
            });

            //check the one that had been checked before
            if(ans.isSelected()){
                radio.setChecked(true);
            }
        }


        LinearLayout.LayoutParams RGroupLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        RGroupLayout.setMargins(0, 20, 0, 0);
        radioGroup.setLayoutParams(RGroupLayout);

        LinearLayout A_layout = new LinearLayout(c);
        A_layout.setOrientation(LinearLayout.VERTICAL);
        A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        A_layout.addView(radioGroup);

        layout.addView(questionText);
        layout.addView(A_layout);
        return layout;
    }


    @Override
    public boolean validateSubmit() {
        return answered;
    }

    @Override
    public String getSkip(){
        return skipTo;
    }

    @Override
    public ArrayList<String> getSelectedAnswers(){
        ArrayList<String> temp = new ArrayList<String>();
        for(Answer answer: answers){
            if(answer.isSelected()) {
                temp.add(answer.getId());
            }
        }
        return temp;
    }

    @Override
    public boolean clearSelectedAnswers(){
//      Log.d("final 3", "clear");
//      answers. = null;
        for(Answer answer: answers){
            answer.setSelected(false);
        }
        answered = false;
        return true;
    }
}
