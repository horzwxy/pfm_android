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
import me.horzwxy.app.pfm.model.communication.ListContactsRequest;
import me.horzwxy.app.pfm.model.communication.ListContactsResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.User;
import me.horzwxy.app.pfm.model.data.UserList;

/**
 * Created by horz on 9/27/13.
 */
public class ChooseParticipantsActivity extends LoggedInActivity {

    private ProgressDialog pDialog;
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

        final ListContactsTask task = new ListContactsTask();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel( true );
            }
        });
        pDialog.setMessage(getResources().getString(R.string.list_contacts_connecting));
        pDialog.show();
        task.execute( new ListContactsRequest( currentUser ) );
    }

    public void onStateChange( View v ) {
        CheckBox checkBox = ( CheckBox )v;
        User user = new User( null, checkBox.getHint() + "", null );
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

    class ListContactsTask extends PFMHttpAsyncTask< ListContactsRequest, ListContactsResponse > {

        @Override
        protected ListContactsResponse doInBackground(ListContactsRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse( responseString, ListContactsResponse.class );
        }

        @Override
        protected void onPostExecute(ListContactsResponse response) {

            LinearLayout authorLine = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                    .inflate( R.layout.line_choose_participants, null );
            CheckBox authorCheckBox = ( CheckBox ) authorLine.findViewById( R.id.choose_participants_checkbox );
            authorCheckBox.setHint( currentUser.nickname );
            if( participants.contains( currentUser ) ) {
                authorCheckBox.setChecked( true );
            }
            lineList.addView( authorLine );

            UserList contacts = response.getUserList();
            for( User user : contacts )
            {
                LinearLayout line = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                        .inflate( R.layout.line_choose_participants, null );
                CheckBox checkBox = ( CheckBox ) line.findViewById( R.id.choose_participants_checkbox );
                checkBox.setHint( user.nickname );
                if( participants.contains( user ) ) {
                    checkBox.setChecked( true );
                }

                lineList.addView( line );
            }

            pDialog.dismiss();
        }
    }
}
