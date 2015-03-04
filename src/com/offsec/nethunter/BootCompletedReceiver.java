package com.offsec.nethunter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by fattire on 2/19/15.
 */
public class BootCompletedReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent startServiceIntent = new Intent(context, RunAtBootService.class);
            context.startService(startServiceIntent);
        }
}
