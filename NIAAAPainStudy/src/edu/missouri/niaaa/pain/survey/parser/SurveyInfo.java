package edu.missouri.niaaa.pain.survey.parser;

import java.util.HashMap;

/**
 * @author Chen
 * data: 06/13/2015
 * change methods of launching to action
 * make type for id of surveys
 *
 * @author Chen
 * date: 06/07/2015
 * start using survey type for methods of launching
 *
 * Author: Paul Baskett
 * Last Update: 9/25/2012
 * Comments Added
 *
 * This class is used to wrap information about surveys that
 * will be displayed when selected from the menu.  Data is
 * read from an xml configuration file (config.xml currently)
 * and wrapped in one of these objects.  Accessor methods are
 * provided to get all of the stored data, and a setter is
 * provided to set the display name because of the XML format
 * and the way the XML parser reads data from tags.
 */
public class SurveyInfo {

    //Instance variables
    protected String surveyType;
    protected String surveyAction;
    protected String surveyName;
    protected String surveyFile;
    protected String surveyDisplayName;

    /*
     * Takes the survey location (in assets folder),
     * survey type (methods of launch),
     * and survey name (used for storage, not displayed for user)
     */
    public SurveyInfo(String surveyType, String surveyAction, String surveyFile, String surveyName){
        this.surveyType = surveyType;
        this.surveyAction = surveyAction;
        this.surveyFile = surveyFile;
        this.surveyName = surveyName;
    }

    /**
     * This method is provided to set the displayed name for
     * the survey (for example, "Morning Report").  The XML
     * parser reads the text in the tag after this object
     * has been created, so the name is set later.
     */
    public void setDisplayName(String name){
        this.surveyDisplayName = name;
    }

    /**
     * Returns the name that should be displayed to the user.
     */
    public String getDisplayName(){
        return this.surveyDisplayName;
    }

    /**
     * @return the id of the survey, which is used in app to specify survey type.
     */
    public String getType(){
        return this.surveyType;
    }

    /**
     * The internal name for the survey, the user doesn't see this.
     */
    public String getName(){
        return this.surveyName;
    }

    /**
     * The name of the file for the survey.  All surveys
     * are stored in assets so no path is necessary currently.
     */
    public String getFileName(){
        return this.surveyFile;
    }

    /**
     * @see #TYPE_SHOWN_MAP
     */
    public String getAction(){
        return this.surveyAction;
    }

    /**
     *     A1. auto triggered - do not shown on menu</br>
     *     M2. manually launched </br>
     *     MC3. manually launched with confirmation </br>
     *     MA4. manually launched with restriction OR auto triggered.
     */
    public final static HashMap<String, Boolean> TYPE_SHOWN_MAP = new HashMap<String, Boolean>() {
        {
            put("1", false);
            put("2", true);
            put("3", true);
            put("4", true);
        }
    };
}
