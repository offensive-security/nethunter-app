package com.offsec.nethunter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ManaActivity extends FragmentActivity implements ActionBar.TabListener {

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;
    
    
	private Integer selectedScriptIndex = 0;
	final CharSequence[] scripts={"mana-nat-full","mana-nat-simple","start-noupstream","start-noupstream-eap"};
	private static Context context;
	private String configFilePath = "/data/local/kali-armhf/etc/mana-toolkit/hostapd-karma.conf";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getApplicationContext();
        setContentView(R.layout.mana);

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                invalidateOptionsMenu();
            }
        });

        actionBar.addTab(
        		actionBar.newTab()
        			.setText("hostapd-karma.conf")
        			.setTabListener(this));
        actionBar.addTab(
        		actionBar.newTab()
        			.setText("dhcpd.conf")
        			.setTabListener(this));
        
        actionBar.addTab(
                actionBar.newTab()
                	.setText("dnsspoof.conf")
                	.setTabListener(this));
    
        actionBar.addTab(
                actionBar.newTab()
                	.setText("nat-mana-full")
                	.setTabListener(this));
        
        actionBar.addTab(
                actionBar.newTab()
                	.setText("nat-mana-simple")
                	.setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                	.setText("start-nouopstream")
                	.setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                	.setText("start-noupstream-eap")
                	.setTabListener(this));
        
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
        
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mana, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    
    public boolean onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        if(pageNum == 0){      
        	menu.findItem(R.id.source_button).setVisible(true);
        }else{            
        	menu.findItem(R.id.source_button).setVisible(false);
        }
        invalidateOptionsMenu();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
            	NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.start_service:
            	startMana();
                return true;
            case R.id.stop_service:
            	stopMana();
                return true;
            case R.id.source_button:
            	Intent i = new Intent(this, EditSourceActivity.class);
            	i.putExtra("path", configFilePath);
            	i.putExtra("shell", true);
            	startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void startMana()
    {
    	AlertDialog.Builder builder=new AlertDialog.Builder(this);
    	builder.setTitle("Pick script:");
    	builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String command = "";
				switch (which) {
                	case 0:
                		command = "start-mana-full &> /sdcard/htdocs/mana.log &";
                		break;
                	case 1:
                		command = "start-mana-simple &> /sdcard/htdocs/mana.log &";
                		break;
                	case 2:
                		command = "start-mana-noup &> /sdcard/htdocs/mana.log &";
                		break;
                	case 3:
                		command = "start-mana-noupeap &> /sdcard/htdocs/mana.log &";
                		break;
                	default:
                		showMessage("Invalid script!");
                		return;
				}
				String[] commands = {"sh", "-c", command};
				ShellExecuter exe = new ShellExecuter();
				exe.RunAsRoot(commands);
				showMessage("Attack executed!");
			}
		});
    	builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
     	builder.setSingleChoiceItems(scripts,selectedScriptIndex, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selectedScriptIndex = which;
			}
		});
    	builder.show();
    	
    }
    
    private void stopMana()
    {
    	ShellExecuter exe = new ShellExecuter();
    	String[] command = {"stop-mana"};
		exe.RunAsRoot(command);
    	showMessage("Mana Stopped");
    }

    public void onPageSelected(int pageNum) {
    	int  currentposition = pageNum;
    	invalidateOptionsMenu();
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    
    public static void showMessage(String message)
    {
    	int duration = Toast.LENGTH_SHORT;
    	Toast toast = Toast.makeText(context, message, duration);
    	toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
    	toast.show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                	return new HostapdFragment();
                case 1:
                	return new DhcpdFragment();
                case 2:
                	return new DnsspoofFragment();
                case 3:
                	return new ManaNatFullFragment();
                case 4:
                	return new ManaNatSimpleFragment();
                case 5:
                	return new ManaStartNoUpstreamFragment();
                default:
                	return new ManaStartNoUpstreamEapFragment();
            }
        }
        @Override
        public int getCount() {
            return 7;
        }
    }

    
    public static class HostapdFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/etc/mana-toolkit/hostapd-karma.conf";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.mana_hostapd, container, false);
            
            
            //Update button
            Button button = (Button) rootView.findViewById(R.id.updateButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	
                	ShellExecuter exe = new ShellExecuter();
            		String source = exe.Executer("cat "+ configFilePath);
            		
                    EditText ifc = (EditText) getView().findViewById(R.id.ifc);
                    EditText bssid = (EditText) getView().findViewById(R.id.bssid);
                    EditText ssid = (EditText) getView().findViewById(R.id.ssid);
                    EditText channel = (EditText) getView().findViewById(R.id.channel);
                    EditText enableKarma = (EditText) getView().findViewById(R.id.enable_karma);
                    EditText karmaLoud = (EditText) getView().findViewById(R.id.karma_loud);
                                 
                    source = source.replaceAll("(?m)^interface=(.*)$", "interface="+ifc.getText().toString());
                	source = source.replaceAll("(?m)^bssid=(.*)$", "bssid="+bssid.getText().toString());
                	source = source.replaceAll("(?m)^ssid=(.*)$", "ssid="+ssid.getText().toString());
                	source = source.replaceAll("(?m)^channel=(.*)$", "channel="+channel.getText().toString());
                	source = source.replaceAll("(?m)^enable_karma=(.*)$", "enable_karma="+enableKarma.getText().toString());
                	source = source.replaceAll("(?m)^karma_loud=(.*)$", "karma_loud="+karmaLoud.getText().toString());
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + source + "\nEOF"};
                    exe.RunAsRoot(command);
                	showMessage("Options updated!");
                }
            });
            return rootView;
        }
        
        public void onResume()
        {
        	super.onResume();
        	ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
            /*
             * Interface
             */
            EditText ifc = (EditText) getView().findViewById(R.id.ifc);
            String regExpatInterface = "^interface=(.*)$";
            Pattern patternIfc = Pattern.compile(regExpatInterface, Pattern.MULTILINE);
            Matcher matcherIfc = patternIfc.matcher(text);
            if (matcherIfc.find()) {
            	String ifcValue = matcherIfc.group(1);
            	ifc.setText(ifcValue);
            }
            
            /*
             * bssid
             */
            EditText bssid = (EditText) getView().findViewById(R.id.bssid);
            String regExpatbssid = "^bssid=(.*)$";
            Pattern patternBssid = Pattern.compile(regExpatbssid, Pattern.MULTILINE);
            Matcher matcherBssid = patternBssid.matcher(text);
            if (matcherBssid.find()) {
            	String bssidVal = matcherBssid.group(1);
            	bssid.setText(bssidVal);
            }
            /*
             * ssid
             */
            EditText ssid = (EditText) getView().findViewById(R.id.ssid);
            String regExpatssid = "^ssid=(.*)$";
            Pattern patternSsid = Pattern.compile(regExpatssid, Pattern.MULTILINE);
            Matcher matcherSsid = patternSsid.matcher(text);
            if (matcherSsid.find()) {
            	String ssidVal = matcherSsid.group(1);
            	ssid.setText(ssidVal);
            }
            /*
             * channel
             */
            EditText channel = (EditText) getView().findViewById(R.id.channel);
            String regExpatChannel = "^channel=(.*)$";
            Pattern patternChannel = Pattern.compile(regExpatChannel, Pattern.MULTILINE);
            Matcher matcherChannel = patternChannel.matcher(text);
            if (matcherChannel.find()) {
            	String channelVal = matcherChannel.group(1);
            	channel.setText(channelVal);
            }
            /*
             * enable_karma
             */
            EditText enableKarma = (EditText) getView().findViewById(R.id.enable_karma);
            String regExpatEnableKarma = "^enable_karma=(.*)$";
            Pattern patternEnableKarma = Pattern.compile(regExpatEnableKarma, Pattern.MULTILINE);
            Matcher matcherEnableKarma = patternEnableKarma.matcher(text);
            if (matcherEnableKarma.find()) {
            	String enableKarmaVal = matcherEnableKarma.group(1);
            	enableKarma.setText(enableKarmaVal);
            }
            
            /*
             * karma_loud
             */
            EditText karmaLoud = (EditText) getView().findViewById(R.id.karma_loud);
            String regExpatKarmaLoud = "^karma_loud=(.*)$";
            Pattern patternKarmaLoud = Pattern.compile(regExpatKarmaLoud, Pattern.MULTILINE);
            Matcher matcherKarmaLoud = patternKarmaLoud.matcher(text);
            if (matcherKarmaLoud.find()) {
            	String karmaLoudVal = matcherKarmaLoud.group(1);
            	karmaLoud.setText(karmaLoudVal);
            }
        }
    }
    
    

    public static class DhcpdFragment extends Fragment {

    	private String configFilePath = "files/dhcpd.conf";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.source_short, container, false);
            
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, configFilePath);
            StringBuilder text = new StringBuilder();
            try {
            	BufferedReader br = new BufferedReader(new FileReader(file));
            	String line;
            	while ((line = br.readLine()) != null) {
            		text.append(line);
            		text.append('\n');
            	}
            	br.close();
            }
            catch (IOException e) {
            	Log.e("Nethunter", "exception", e);
            }
            EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	try {
                		File sdcard = Environment.getExternalStorageDirectory();
                        File myFile = new File(sdcard,configFilePath);		
                        myFile.createNewFile();       
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        EditText source = (EditText) rootView.findViewById(R.id.source);
                        myOutWriter.append(source.getText());
                        myOutWriter.close();
                        fOut.close();
                        showMessage("Source updated");                	
                	} catch (Exception e) {
                		showMessage(e.getMessage());
                	}
                }
            });
            return rootView;
        }
    }
    
    public static class DnsspoofFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/etc/mana-toolkit/dnsspoof.conf";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            
            ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
    		EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();             
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
            		showMessage("Source updated");
                }
            });
            return rootView;
        }
    }
    
    public static class ManaNatFullFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/usr/share/mana-toolkit/run-mana/start-nat-full-mod.sh";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
    		EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();             
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
            		showMessage("Source updated");
                }
            });
            return rootView;
        }
    }
    
    public static class ManaNatSimpleFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/usr/share/mana-toolkit/run-mana/start-nat-simple.sh";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
    		EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();             
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
            		showMessage("Source updated");
                }
            });
            return rootView;
        }
    }
    
    public static class ManaStartNoUpstreamFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/usr/share/mana-toolkit/run-mana/start-noupstream.sh";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
    		EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();             
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
            		showMessage("Source updated");
                }
            });
            return rootView;
        }
    }
    
    public static class ManaStartNoUpstreamEapFragment extends Fragment {

    	private String configFilePath = "/data/local/kali-armhf/usr/share/mana-toolkit/run-mana/start-noupstream-eap.sh";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.source_short, container, false);
            ShellExecuter exe = new ShellExecuter();
    		String text = exe.Executer("cat "+ configFilePath);
    		EditText source = (EditText) rootView.findViewById(R.id.source);
            source.setText(text);
            
            Button button = (Button) rootView.findViewById(R.id.update);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	EditText source = (EditText) getView().findViewById(R.id.source);
                    String newSource = source.getText().toString();

                    ShellExecuter exe = new ShellExecuter();             
                    String[] command = {"sh", "-c", "cat <<'EOF' > " + configFilePath + "\n" + newSource + "\nEOF"};
                    exe.RunAsRoot(command);
            		showMessage("Source updated");
                }
            });
            return rootView;
        }
    }
}