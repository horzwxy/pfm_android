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
    public boolean onContextItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.menu_quit ) {
            for( Activity activity : createdActivities ) {
                if( activity != null && activity != this ) {
                    activity.finish();
                }
            }
            this.finish();
        }
        return true;
    }
}
