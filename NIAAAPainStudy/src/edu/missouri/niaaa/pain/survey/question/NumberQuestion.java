package edu.missouri.niaaa.pain.survey.question;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.survey.category.QuestionType;
import edu.missouri.niaaa.pain.survey.category.SurveyQuestion;

public class NumberQuestion extends SurveyQuestion {

    TextView counterText;
    boolean answered = false;
    int result = -1;
    int digit = 0;
    String item = "number(s)";
    int min = 0;
    int max = 1;
    String skipTo = null;
    String cmpValue = null;
    final String[] display = {".0",".5"};
    boolean digitQ = false;
    
    NumberPickerMe np;
    NumberPickerMe np2;

    public NumberQuestion(String id){
        this.questionId = id;
        this.questionType = QuestionType.NUMBER;
    }


    @Override
    public LinearLayout prepareLayout(Context c) {
        SharedPreferences shp = Util.getSP(c, Util.SP_LOGIN);
        String primary_ori = "PRIMARY";
        String secondary_ori = "SECONDARY";
        String none = "None";
        String primary = shp.getString(Util.SP_LOGIN_PRIMARY_MED, none);
        String secondary = shp.getString(Util.SP_LOGIN_SECONDARY_MED, none);
        
        np = new NumberPickerMe(c);
        np2 = new NumberPickerMe(c);
        
        this.item = this.answers.get(0).getAnswerText();
        this.min = Integer.parseInt(this.answers.get(1).getAnswerText());
        this.max = Integer.parseInt(this.answers.get(2).getAnswerText());
        this.digitQ = this.answers.get(3).getAnswerText().equals("Y") ? true : false;
        if(this.answers.size()>4){
            this.skipTo = this.answers.get(4).getAnswerText();
        }
        if(this.answers.size()>5){
            this.cmpValue = this.answers.get(5).getAnswerText();
        }
        
        if(result == -1) {
            result = digitQ ? 1 : this.min;
        }

        //set anwsered true
        answered = true;
        
        LinearLayout layout = new LinearLayout(c);
        layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        //text
        TextView questionText = new TextView(c);
        questionText.setText(getQuestion().replace("|", "\n").replace(primary_ori, primary).replace(secondary_ori, secondary));
        questionText.setTextAppearance(c, android.R.attr.textAppearanceLarge);
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
        questionText.setLines(4);

        counterText = new TextView(c);
        counterText.setText(result+ (digitQ ? display[digit] : "") + " " + item);
        counterText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);

        LinearLayout.LayoutParams layoutt = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutt.setMargins(10,15,10,15);

        questionText.setLayoutParams(layoutt);
        counterText.setLayoutParams(layoutt);


        //number picker
        LinearLayout A_layout = new LinearLayout(c);
        A_layout.setOrientation(LinearLayout.HORIZONTAL);
        A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
        
        LinearLayout.LayoutParams layoutNP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        layoutNP.setMargins(20,15,20,15);
        
        np.setLayoutParams(layoutNP);
        np.setMaxValue(max);
        np.setMinValue(min);

        np.setValue(result);
        
        //compare result and cmpValue, if true, set isSelect to true
        if(cmpValue != null){
            int cmp = Integer.valueOf(cmpValue);
            if(result > cmp){
                this.answers.get(5).setSelected(true);
                if(this.answers.get(0).hasSoftTrigger())
                    softTriggers.put(this.answers.get(0).getSoftTrigger(), true);
            }else{
                this.answers.get(5).setSelected(false);
                if(this.answers.get(0).hasSoftTrigger())
                    softTriggers.put(this.answers.get(0).getSoftTrigger(), false);
            }
        }

        np.setOnValueChangedListener(new OnValueChangeListener(){

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                Log.d("_________", "number picker");
                result = newVal;
                np.setValue(result);
                counterText.setText(result+ (digitQ? display[digit] : "") + " " + item);
                
                //compare result and cmpValue, if true, set isSelect to true
                if(cmpValue != null){
                    int cmp = Integer.valueOf(cmpValue);
                    if(result > cmp){
                        answers.get(5).setSelected(true);
                        if(answers.get(0).hasSoftTrigger())
                            softTriggers.put(answers.get(0).getSoftTrigger(), true);
                    }else{
                        answers.get(5).setSelected(false);
                        if(answers.get(0).hasSoftTrigger())
                            softTriggers.put(answers.get(0).getSoftTrigger(), false);
                    }
                }
                
                //avoid 0.0
                if(digitQ && newVal == 0){
                    np2.setValue(1);
                    digit = 1;
                }
            }
        });

//      SeekBar sb = new SeekBar(c);
//      sb.setMax(15);
//      sb.setProgress(result);
//      sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//          public void onProgressChanged(SeekBar seekBar, int progress,
//                  boolean fromUser) {
//              if(fromUser){
//                  result = progress;
//                  counterText.setText(progress + " drinks");
//                  answered = true;
//              }
//          }
//          public void onStartTrackingTouch(SeekBar seekBar) {     }
//          public void onStopTrackingTouch(SeekBar seekBar)  {     }
//      });

        
        np2.setLayoutParams(layoutNP);
        np2.setMaxValue(1);
        np2.setMinValue(0);
        np2.setDisplayedValues(display);
        
        np2.setOnValueChangedListener(new OnValueChangeListener(){

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                Log.d("_________", "number picker digit");
//                result = newVal;
//                np.setValue(result);
                
                digit = newVal;
                np2.setValue(newVal);
                counterText.setText(result+ display[newVal] + " " + item);
                
                //compare result and cmpValue, if true, set isSelect to true
//                if(cmpValue != null){
//                    int cmp = Integer.valueOf(cmpValue);
//                    if(result > cmp){
//                        answers.get(5).setSelected(true);
//                    }else{
//                        answers.get(5).setSelected(false);
//                    }
//                }
                
                //avoid 0.0
                if(digitQ && newVal == 0 && np.getValue()==0){
                    np.setValue(1);
                    result = 1;
                }
            }
        });
        
        layout.addView(questionText);
        layout.addView(counterText);
        
        A_layout.addView(np);
        if(digitQ)
        A_layout.addView(np2);
        layout.addView(A_layout);

//      layout.addView(counterText);
//      layout.addView(sb);
//      layout.addView(np);

        return layout;
    }


    @Override
    public boolean validateSubmit() {
        if(answered && result >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getSkip(){
        return this.skipTo;
    }


    @Override
    public ArrayList<String> getSelectedAnswers(){
        if(!validateSubmit()){
            ArrayList<String> temp = new ArrayList<String>();
            temp.add(Integer.valueOf(-1).toString());
            return temp;
        }
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(Integer.valueOf(result).toString() + (digitQ ? display[digit] : ""));
        return temp;
    }

    @Override
    public boolean clearSelectedAnswers(){
//      Log.d("final 3", "clear");
//      answers = null;
        result = -1;
        answered = false;
        return true;
    }
}
