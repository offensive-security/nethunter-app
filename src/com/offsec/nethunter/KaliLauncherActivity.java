package com.offsec.nethunter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class KaliLauncherActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kali_launcher);
		ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
		
		addClickListener(R.id.button_start_kali, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent =
						new Intent("jackpal.androidterm.RUN_SCRIPT");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nbootkali");
				startActivity(intent);
			}
		});
		/**
		 * Launch Kali menu
		 */
		addClickListener(R.id.button_start_kalimenu, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent =
						new Intent("jackpal.androidterm.RUN_SCRIPT");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nbootkali\nkalimenu");
				startActivity(intent);
			}
		});
		/**
		 * Shutdown Kali chroot
		 */
		addClickListener(R.id.button_stop_kali, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent =
						new Intent("jackpal.androidterm.RUN_SCRIPT");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nkillkali");
				startActivity(intent);
			}
		});
		/**
		 * Launch Wifite
		 */
		addClickListener(R.id.button_launch_wifite, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent =
						new Intent("jackpal.androidterm.RUN_SCRIPT");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.putExtra("jackpal.androidterm.iInitialCommand", "su\nstart-wifite");
				startActivity(intent);
			}
		});
		/**
		 * Start Webserver
		 */
		addClickListener(R.id.button_start_web, new View.OnClickListener() {
			public void onClick(View v) {
				String[] command = {"start-web"};
				ShellExecuter exe = new ShellExecuter();
				exe.RunAsRoot(command);
			}
		});
		/**
		 * Stop Webserver
		 */
		addClickListener(R.id.button_stop_web, new View.OnClickListener() {
			public void onClick(View v) {
				String[] command = {"stop-web"};
				ShellExecuter exe = new ShellExecuter();
				exe.RunAsRoot(command);
			}
		});
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) {
        case android.R.id.home:
        	NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
    	}
    }
	
	private void addClickListener(int buttonId, View.OnClickListener onClickListener) {
		((Button) findViewById(buttonId)).setOnClickListener(onClickListener);
	}
}