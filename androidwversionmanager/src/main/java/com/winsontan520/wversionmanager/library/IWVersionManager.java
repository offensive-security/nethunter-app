package com.winsontan520.wversionmanager.library;

import android.graphics.drawable.Drawable;

interface IWVersionManager {

    /**
     * @return Label of update now button
     */
    String getUpdateNowLabel();


    /**
     * @param updateNowLabel Set the label for update now button
     */
    void setUpdateNowLabel(String updateNowLabel);

    /**
     * @return label of remind me later button
     */
    String getRemindMeLaterLabel();

    /**
     * @param remindMeLaterLabel Set label of remind me later button
     */
    void setRemindMeLaterLabel(String remindMeLaterLabel);

    /**
     * @return label of ignore this version button
     */
    String getIgnoreThisVersionLabel();

    /**
     * @param ignoreThisVersionLabel Set label of ignore this version button
     */
    void setIgnoreThisVersionLabel(String ignoreThisVersionLabel);

    /**
     * @param icon Set drawable of icon in dialog
     */
    void setIcon(Drawable icon);

    /**
     * @param title Set title of dialog
     */
    void setTitle(String title);

    /**
     * @param message Set message of dialog
     */
    void setMessage(String message);

    /**
     * @return message of dialog
     */
    String getMessage();

    /**
     * @return title of dialog
     */
    String getTitle();

    /**
     * @return drawable of icon
     */
    Drawable getIcon();

    /**
     * @return url to execute when update now button clicked. Default value is the link in google play based on app package name.
     */
    String getUpdateUrl();

    /**
     * @param updateUrl Set url to execute when update now button clicked
     */
    void setUpdateUrl(String updateUrl);

    /**
     * @return url which should return update content in json format
     */
    String getVersionContentUrl();

    /**
     * @param versionContentUrl Set the update content url
     */
    void setVersionContentUrl(String versionContentUrl);

    /**
     * @param minutes Set reminder time in minutes when remind me later button clicked
     */
    void setReminderTimer(int minutes);

    /**
     * @return reminder timer in minutes
     */
    int getReminderTimer();

    /**
     * @return current version code
     */
    int getCurrentVersionCode();

    /**
     * @return version code which will be ignored
     */
    int getIgnoreVersionCode();

    /**
     * @return CustomTagHandler object
     */
    CustomTagHandler getCustomTagHandler();

    /**
     * @param customTagHandler Set your own custom tag handler
     */
    void setCustomTagHandler(CustomTagHandler customTagHandler);

    /**
     * @param OnReceiveListener Set your own callback listener when receiving response from server
     */
    void setOnReceiveListener(OnReceiveListener listener);

}