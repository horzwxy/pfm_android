package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

import me.horzwxy.app.pfm.android.R;

/**
 * Created by horz on 9/8/13.
 */
public class LoggedInActivity extends PFMActivity {

    @Override
    protected void onDestroy() {
        createdActivities.remove( this );
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logged_in_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.menu_log_out:
                currentUser = null;
                //final SharedPreferences.Editor spEditor = getPreferences(MODE_PRIVATE).edit();
                SharedPreferences sp = getPreferences( MODE_PRIVATE );
                System.out.println( sp );
                System.out.println( sp.getString( "accountName", null ) + "" );
                System.out.println(  );
                sp.edit().remove("accountName").commit();
                System.out.println( sp.getString( "accountName", null ) + "" );
                sp.edit().remove("accountType").commit();
                sp.edit().remove("nickname").commit();
                startActivity(new Intent(this, LogInActivity.class));
                this.finish();
                break;
            case R.id.menu_to_main:
                startActivity(new Intent( this, MainActivity.class ));
                this.finish();
                break;
            case R.id.menu_setting:
                break;
            default:
                return super.onOptionsItemSelected( item );
        }
        return true;
    }
}
