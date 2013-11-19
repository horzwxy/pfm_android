package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.android.dao.ContactDAO;
import me.horzwxy.app.pfm.model.communication.ListContactsRequest;
import me.horzwxy.app.pfm.model.communication.ListContactsResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.User;
import me.horzwxy.app.pfm.model.data.UserList;

/**
 * User picker in creating new dining info.
 * It gets contacts from local SQLite database and returns an array of nicknames to the caller activity.
 * It may get an array from caller, indicating which users have already been chosen.
 * No network connection is needed here. If user need add new contact, he should use ListContactsActivity from MainActivity.
 */
public class ChooseParticipantsActivity extends LoggedInActivity {

    private LinearLayout lineList;
    private UserList participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_participants );

        participants = ( UserList )getIntent().getSerializableExtra( "participants" );
        lineList = ( LinearLayout ) findViewById( R.id.participant_list );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // author himself is not in contact list
        // so we need to add him first
        LinearLayout authorLine = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                .inflate( R.layout.line_choose_participants, null );
        CheckBox authorCheckBox = ( CheckBox ) authorLine.findViewById( R.id.choose_participants_checkbox );
        authorCheckBox.setText( currentUser.nickname );
        if( participants.contains( currentUser ) ) {
            authorCheckBox.setChecked( true );
        }
        lineList.addView( authorLine );

        // add the rest contacts
        ContactDAO dao = new ContactDAO( this );
        List< String > contacts = dao.getAllContacts();
        dao.closeDAO();
        for( String nickname : contacts )
        {
            LinearLayout line = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                    .inflate( R.layout.line_choose_participants, null );
            CheckBox checkBox = ( CheckBox ) line.findViewById( R.id.choose_participants_checkbox );
            checkBox.setText( nickname );
            if( participants.contains( new User( nickname ) ) ) {
                checkBox.setChecked( true );
            }
            lineList.addView( line );
        }
    }

    public void onStateChange( View v ) {
        CheckBox checkBox = ( CheckBox )v;
        User user = new User( null, checkBox.getText() + "", null );
        if( checkBox.isChecked() ) {
            participants.add(user);
        }
        else {
            participants.remove(user);
        }
    }

    public void save( View v ) {
        Intent intent = new Intent();
        intent.putExtra("participants", participants);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
