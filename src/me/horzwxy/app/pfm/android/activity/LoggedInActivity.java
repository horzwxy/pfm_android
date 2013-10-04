package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import me.horzwxy.app.pfm.android.R;

/**
 * Created by horz on 9/8/13.
 */
public class LoggedInActivity extends PFMActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logged_in_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.menu_to_main ) {
            Intent intent = new Intent( this, MainActivity.class );
            startActivity(intent);
            createdActivities.remove( this );
            this.finish();
            return true;
        }
        else if( item.getItemId() == R.id.menu_setting ) {
            return true;
        }
        else {
            return super.onOptionsItemSelected( item );
        }
    }
}
