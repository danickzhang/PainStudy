package edu.missouri.niaaa.pain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RebootReceiver extends BroadcastReceiver {

    public static final String REBOOT = "Intent_Reboot";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Intent s = new Intent(context, MainActivity.class);
        s.putExtra(REBOOT, true);
        s.setAction(Intent.ACTION_MAIN);
        s.addCategory(Intent.CATEGORY_LAUNCHER);
        s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(s);
    }
}
