package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.User;

/**
 * Created by horz on 9/8/13.
 */
public class NewDiningActivity extends LoggedInActivity {

    private final static int REQUEST_FOR_PARTICIPANTS = 987;

    private ArrayList< User > participantsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dining);

        participantsList = new ArrayList<User>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == Activity.RESULT_OK ) {
            switch ( requestCode ) {
                case REQUEST_FOR_PARTICIPANTS:
                    participantsList = ( ArrayList< User > )data.getSerializableExtra( "participants" );
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    public void chooseParticipants( View v ) {
        Intent intent = new Intent( this, ChooseParticipantsActivity.class );
        intent.putExtra( "participants", participantsList );
        startActivityForResult(intent, REQUEST_FOR_PARTICIPANTS);
    }
}
