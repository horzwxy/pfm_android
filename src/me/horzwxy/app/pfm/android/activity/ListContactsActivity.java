package me.horzwxy.app.pfm.android.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.AddContactRequest;
import me.horzwxy.app.pfm.model.communication.AddContactResponse;
import me.horzwxy.app.pfm.model.communication.ListContactsRequest;
import me.horzwxy.app.pfm.model.communication.ListContactsResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.ContactInfo;
import me.horzwxy.app.pfm.model.data.User;

/**
 * Created by horz on 9/26/13.
 */
public class ListContactsActivity extends LoggedInActivity {

    private ProgressDialog pDialog;
    private ListView listView;
    private ArrayAdapter< String > adapter;
    private ContactInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_list_contacts );

        listView = ( ListView ) findViewById( R.id.contacts_list );
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
        task.execute( new ListContactsRequest( currentUser ));

    }

    public void addContact( View v ) {
        info = new ContactInfo( currentUser, null );
        displayNicknameInput( getResources().getString( R.string.list_contacts_hint ) );
    }

    private void displayNicknameInput( String message ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ListContactsActivity.this);
        alert.setTitle(R.string.list_contacts_add);
        alert.setMessage(message);

        // Set an EditText view to get user input
        final EditText input = new EditText(ListContactsActivity.this);
        alert.setView(input);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = input.getEditableText().toString();
                info.friend = new User( nickname );
                AddContactRequest request = new AddContactRequest(info);
                new AddContactTask().execute(request);
                pDialog.dismiss();
                pDialog = new ProgressDialog(ListContactsActivity.this);
                pDialog.setCancelable(true);
                pDialog.setMessage( getResources().getString( R.string.list_contacts_add_connecting ) );
                pDialog.show();
            }
        });
        alert.show();
        pDialog.dismiss();
    }

    class AddContactTask extends PFMHttpAsyncTask< AddContactRequest, AddContactResponse> {

        @Override
        protected AddContactResponse doInBackground(AddContactRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse( responseString, AddContactResponse.class );
        }

        @Override
        protected void onPostExecute(AddContactResponse response) {
            if( response.type == AddContactResponse.ResultType.SUCCESS ) {
                String nickname = info.friend.nickname;
                adapter.add( nickname );
                adapter.notifyDataSetChanged();
                pDialog.dismiss();
            }
            else {
                displayNicknameInput( getResources().getString( R.string.list_contacts_add_no_such_user ) );
            }
        }
    }

    class ListContactsTask extends PFMHttpAsyncTask< ListContactsRequest, ListContactsResponse > {

        @Override
        protected ListContactsResponse doInBackground(ListContactsRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse( responseString, ListContactsResponse.class );
        }

        @Override
        protected void onPostExecute(ListContactsResponse response) {
            ArrayList< String > nicknameArray = response.getUserList().toNicknameList();
            adapter = new ArrayAdapter<String>( ListContactsActivity.this,
                    android.R.layout.simple_list_item_1, nicknameArray );
            listView.setAdapter( adapter );
            pDialog.dismiss();
            Toast.makeText( ListContactsActivity.this, getResources().getString( R.string.list_contacts_success ), Toast.LENGTH_SHORT ).show();
        }
    }
}
