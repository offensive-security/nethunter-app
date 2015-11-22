package com.winsontan520.wversionmanager.library;

import android.graphics.drawable.Drawable;

public interface IWVersionManager {

	/**
	 * @return Label of update now button
	 */
	public String getUpdateNowLabel();


	/**
	 * @param updateNowLabel Set the label for update now button
	 */
	public void setUpdateNowLabel(String updateNowLabel);

	/**
	 * @return label of remind me later button
	 */
	public String getRemindMeLaterLabel();

	/**
	 * @param remindMeLaterLabel Set label of remind me later button
	 */
	public void setRemindMeLaterLabel(String remindMeLaterLabel);

	/**
	 * @return label of ignore this version button
	 */
	public String getIgnoreThisVersionLabel();

	/**
	 * @param ignoreThisVersionLabel Set label of ignore this version button
	 */
	public void setIgnoreThisVersionLabel(String ignoreThisVersionLabel);

	/**
	 * @param icon Set drawable of icon in dialog
	 */
	public void setIcon(Drawable icon);

	/**
	 * @param title Set title of dialog
	 */
	public void setTitle(String title);

	/**
	 * @param message Set message of dialog
	 */
	public void setMessage(String message);

	/**
	 * @return message of dialog
	 */
	public String getMessage();

	/**
	 * @return title of dialog
	 */
	public String getTitle();

	/**
	 * @return drawable of icon
	 */
	public Drawable getIcon();

	/**
	 * @return url to execute when update now button clicked. Default value is the link in google play based on app package name.
	 */
	public String getUpdateUrl();

	/**
	 * @param updateUrl Set url to execute when update now button clicked
	 */
	public void setUpdateUrl(String updateUrl);

	/**
	 * @return url which should return update content in json format
	 */
	public String getVersionContentUrl();

	/**
	 * @param versionContentUrl Set the update content url
	 */
	public void setVersionContentUrl(String versionContentUrl);

	/**
	 * @param minutes Set reminder time in minutes when remind me later button clicked
	 */
	public void setReminderTimer(int minutes);

	/**
	 * @return reminder timer in minutes
	 */
	public int getReminderTimer();
	
	/**
	 * @return current version code
	 */
	public int getCurrentVersionCode();

	/**
	 * @return version code which will be ignored
	 */
	public int getIgnoreVersionCode();

	/**
	 * @return CustomTagHandler object
	 */
	public CustomTagHandler getCustomTagHandler();

	/**
	 * @param customTagHandler Set your own custom tag handler
	 */
	public void setCustomTagHandler(CustomTagHandler customTagHandler);
	
	/**
	 * @param OnReceiveListener Set your own callback listener when receiving response from server
	 */
	public void setOnReceiveListener(OnReceiveListener listener);

}