package me.horzwxy.app.pfm.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.horzwxy.app.pfm.android.R;

/**
 * Main activity of the app. It's a navigation activity.
 */
public class MainActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main );

        TextView textView = ( TextView ) findViewById( R.id.main_nickname );
        textView.setText( currentUser.nickname );
    }

    public void toAddDining( View v ) {
        toNewActivity( NewDiningActivity.class );
    }

    public void toListDinings( View v ) {
        toNewActivity( ListDiningsActivity.class );
    }

    public void toListContacts( View v ) {
        toNewActivity( ListContactsActivity.class );
    }

    public void toListBills( View v ) {
        toNewActivity( ListBillsActivity.class );
    }

    private < T extends PFMActivity > void toNewActivity( Class< T > activityClass ) {
        Intent intent = new Intent( this, activityClass );
        startActivity( intent );
    }
}
