package kr.co.photointerior.kosw.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootStartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Env.Action.KOSW_SERVICE_RESTART_ACTION.isMatch(action)){
//            BeaconRagingInRegionService.getServiceIsntance().sendServiceLog("restart beacon service:by " + action);
//            context.startService(new Intent(context, BeaconRagingInRegionService.class));
        }*/
    }
}
