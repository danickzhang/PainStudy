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

//        Handler h = new Handler();
//        h.postDelayed(new Runnable(){
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                Utilities.scheduleAll(t);
////              Intent startScheduler = new Intent(Utilities.BD_ACTION_SCHEDULE_ALL);
////              startScheduler.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);//useless
////              t.sendBroadcast(startScheduler);
//
//                Utilities.scheduleDaemon(t);
////              Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
////              i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 0);
////              t.sendBroadcast(i);
//            }
//
//        }, 30*1000);
    }

}
