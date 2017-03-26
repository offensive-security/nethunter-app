package com.offsec.nethunter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.offsec.nethunter.service.RunAtBootService;

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
