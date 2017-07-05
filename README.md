# Nethunter Application for Android

This is the next-gen Nethunter app, which acts as an installer, updater,
and interface for the Kali Linux chroot.

## Setting up Android Studio

You may want to build a version of the Nethunter application for testing or own peace of mind.  Here's a quick guide on getting started.

[Download Android Studio](http://developer.android.com/sdk/index.html)

Launch to welcome screen

Configure > SDK Manager > Launch Standalone SDK Manager

Select:
* SDK Tools
* SDK Platform-tools
* SDK Build-tools
* Android 6.0 (API 23) or latest

Install then accept the license.  Once you have the SDK you may need to update it in the future but are good to go for now.  Now we need to go back to welcome screen and download the Nethunter application source files:

Checkout the project from version control > Git >
```bash
Github Repository Url: https://github.com/offensive-security/nethunter-app.git
Parent Directory: [where you want to store files]
Directory Name:[nethunter-app]
```

This should load your new project.

## Building in Android Studio

When you first launch the program you may need to sync Gradle with your project.  I usually use the icon with the green circle and down arrow in icon bar but you may also use File > Sync.  Assuming you have no errors, you can easily build a test application.

## Running your application

If your device is connected to your computer then you can click Run > Run 'nethunter app' then click on your device.  The emulator is not going to give you accurate results so its best to test only on the device.  If you want an APK go to Build > Build apk.


# Structure of Application

A brief rundown of the files and folders:
```bash
androidwversionmanager <-- The app updater
nethunter-app
  manifests
    AndroidManifest.xml <-- Manages permissions among other things
  java
    com.offsec.nethunter <-- Contains the source for application
      GPS <-- Handles NMEA data for chroot GPS
      updateReceiver <-- For "run at boot service"
      service  <-- For "run at boot service"
      utils    <-- Misc system variables and shell executer. Controls Nethunter paths also
    res
      drawable <-- Icons and pictures
      layout  <-- The "gui" for each section of the app
      menu    <-- Small menus
      values  <-- Contains strings or "text" used by application
    assets
      etc.init.d <-- Startup scripts
      files
      nh_files   <-- Copied to sdcard
        deauth <-- Used by the Deauthenticator.
        configs  <-- Config files used by apps
        duckscripts <-- Some default ducky scripts
        modules     <-- Mainly used by duckhunter.  Contains keyseed.py which does all the HID work.
      scripts <-- This is what launches chroot (bootkali) and checks services
```
