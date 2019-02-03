/**
 * Created by AnglerVonMur on 26.07.15.
 */
package com.offsec.nethunter;

import android.content.Context;

import androidx.appcompat.widget.AppCompatButton;

public class LauncherButton extends AppCompatButton {

    private long db_id;

    public LauncherButton(Context context) {
        super(context);
    }

    public long getDb_id() {
        return db_id;
    }

    public void setDb_id(long db_id) {
        this.db_id = db_id;
    }
}
