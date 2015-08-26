package edu.missouri.niaaa.pain.survey.category;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;


public abstract class SurveyQuestion implements Question {

    protected ArrayList<Answer> answers = new ArrayList<Answer>();
    protected HashMap<View, Answer> answerViews = new HashMap<View, Answer>();
    protected String questionText;
    protected String questionId;
    protected QuestionType questionType;
    
    protected String softSkip;
    protected boolean hasSoftSkip = false;
    public static HashMap<String, Boolean> softTriggers = new HashMap<String, Boolean>();


    @Override
    public String getQuestion() {
        return questionText;
    }


    @Override
    public void setQuestion(String questionText) {
        this.questionText = questionText;

    }


    @Override
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }


    @Override
    public void addAnswers(ArrayList<Answer> answers) {
        this.answers.addAll(answers);
    }


    @Override
    public void addAnswers(Answer[] answers) {
        for(Answer a: answers){
            this.answers.add(a);
        }
    }


    @Override
    public ArrayList<Answer> getAnswers() {
        return answers;
    }


    @Override
    public void setQuestionType(QuestionType type) {
        this.questionType = type;
    }


    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }


    @Override
    public abstract LinearLayout prepareLayout(Context c);


    @Override
    public abstract boolean validateSubmit();


    @Override
    public abstract String getSkip();


    @Override
    public String getId(){
        return questionId;
    }


    @Override
    public void setSoftSkip(String skip) {
        // TODO Auto-generated method stub
        this.softSkip = skip;
        this.hasSoftSkip = true;
    }


    @Override
    public String getSoftSkip() {
        // TODO Auto-generated method stub
        return this.softSkip;
    }


    @Override
    public boolean hasSoftSkip() {
        // TODO Auto-generated method stub
        return this.hasSoftSkip;
    }
    
    

}
