package me.horzwxy.app.pfm.android.activity;

import android.os.Bundle;

import me.horzwxy.app.pfm.android.R;

/**
 * Created by horz on 10/4/13.
 */
public class ShowDiningActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_dining_info );
    }
}
