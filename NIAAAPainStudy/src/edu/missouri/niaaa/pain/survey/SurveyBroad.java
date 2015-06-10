package edu.missouri.niaaa.pain.survey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.Utilities;

public class SurveyBroad extends BroadcastReceiver {
    String TAG = "SurveyBroad.java";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "OnReceive~~~ "+intent.getAction()+" "+intent.getIntExtra(Utilities.SV_NAME, -1));
        Util.Log_lifeCycle(TAG, "~~~seq is "+intent.getIntExtra(Utilities.SV_NAME, -1));
    }
    
}
