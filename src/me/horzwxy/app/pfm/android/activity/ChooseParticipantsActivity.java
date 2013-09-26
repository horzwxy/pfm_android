package me.horzwxy.app.pfm.android.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.ListContactsRequest;
import me.horzwxy.app.pfm.model.ListContactsResponse;
import me.horzwxy.app.pfm.model.Response;
import me.horzwxy.app.pfm.model.User;

/**
 * Created by horz on 9/27/13.
 */
public class ChooseParticipantsActivity extends LoggedInActivity {

    private ProgressDialog pDialog;
    private LinearLayout lineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_participants );

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

    class ListContactsTask extends PFMHttpAsyncTask {

        @Override
        protected void onPostExecute(Response response) {
            ListContactsResponse lcResponse = ( ListContactsResponse ) response;

            LinearLayout authorLine = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                    .inflate( R.layout.line_choose_participants, null );
            CheckBox authorCheckBox = ( CheckBox ) authorLine.findViewById( R.id.choose_participants_checkbox );
            authorCheckBox.setChecked( true );
            TextView authorTextView = ( TextView ) authorLine.findViewById( R.id.choose_participants_nickname );
            authorTextView.setText( currentUser.nickname );
            lineList.addView( authorLine );

            List< User > contacts = lcResponse.getContactList();
            for( User user : contacts )
            {
                LinearLayout line = ( LinearLayout ) ChooseParticipantsActivity.this.getLayoutInflater()
                        .inflate( R.layout.line_choose_participants, null );
                CheckBox checkBox = ( CheckBox ) line.findViewById( R.id.choose_participants_checkbox );
                TextView textView = ( TextView ) line.findViewById( R.id.choose_participants_nickname );
                textView.setText( user.nickname );

                lineList.addView( line );
            }

            pDialog.dismiss();
        }
    }
}
