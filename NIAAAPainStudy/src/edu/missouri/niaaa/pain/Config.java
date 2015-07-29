package edu.missouri.niaaa.pain;


public class Config {
    
    public Config(int project){
        
    }
    
    
    /*project*/
    public static final int PAIN        = 0;
    public static final int CRAVING     = 1;
    public static final int NIMH        = 2;
    public static final int EMA         = 3;
    
    public static final int PROJECT     = CRAVING;
    
    
    
    /*survey type*/
    public static final int SV_TYPE_MORNING             = 1;
    public static final int SV_TYPE_RANDOM              = 2;
    public static final int SV_TYPE_MOOD                = 3;
    public static final int SV_TYPE_PAIN_FOLLOWUP       = 4;
    public static final int SV_TYPE_DRINKING            = 5;
    public static final int SV_TYPE_DRINKING_FOLLOWUP   = 6;
    public static final int SV_TYPE_DUAL_FOLLOWUP       = 7;
    
    
    
    
    
    
    
    
    public static final String getPhoneBasePath(){
        switch(PROJECT){
        case PAIN:
            return "sdcard/TestResult_pain/";
        case CRAVING:
            return "sdcard/TestResult_craving/";
        case NIMH:
            return "sdcard/TestResult_nimh/";
        case EMA:
            return "sdcard/TestResult_ema/";
        default:
            return "sdcard/TestResult_project/";
        }
    }
    
    
    public static final int getMenu(){
        switch(PROJECT){
        case PAIN:
            return R.menu.main;
        case CRAVING:
            return R.menu.btmain;
        case NIMH:
            return R.menu.main;
        case EMA:
            return R.menu.main;
        default:
            return R.menu.btmain;
        }
    }
}
