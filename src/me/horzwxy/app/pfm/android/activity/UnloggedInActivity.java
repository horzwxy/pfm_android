package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import me.horzwxy.app.pfm.android.R;

/**
 * Created by horz on 9/8/13.
 */
public class UnloggedInActivity extends PFMActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.unlogged_in_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected( item );
    }
}
