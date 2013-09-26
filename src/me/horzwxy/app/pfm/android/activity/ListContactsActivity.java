package me.horzwxy.app.pfm.android.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.AddContactRequest;
import me.horzwxy.app.pfm.model.AddContactResponse;
import me.horzwxy.app.pfm.model.ContactInfo;
import me.horzwxy.app.pfm.model.ListContactsRequest;
import me.horzwxy.app.pfm.model.ListContactsResponse;
import me.horzwxy.app.pfm.model.Response;
import me.horzwxy.app.pfm.model.SetNicknameRequest;
import me.horzwxy.app.pfm.model.User;

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
        task.execute( new ListContactsRequest( currentUser ));
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
    }

    public void addContact( View v ) {
        info = new ContactInfo( currentUser, null );

        AlertDialog.Builder alert = new AlertDialog.Builder(ListContactsActivity.this);
        alert.setTitle(R.string.list_contacts_add);
        alert.setMessage(R.string.list_contacts_hint);

        // Set an EditText view to get user input
        final EditText input = new EditText(ListContactsActivity.this);
        alert.setView(input);
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = input.getEditableText().toString();
                info.friend = new User(null, nickname);
                AddContactRequest request = new AddContactRequest(info);
                new AddContactsTask().execute(request);
                pDialog.dismiss();
                pDialog = new ProgressDialog(ListContactsActivity.this);
                pDialog.setCancelable(true);
                pDialog.setMessage(getResources().getString(R.string.list_contacts_add_connecting));
                pDialog.show();
            }
        });
        alert.show();
        pDialog.dismiss();
    }

    class AddContactsTask extends PFMHttpAsyncTask {
        @Override
        protected void onPostExecute(Response response) {
            AddContactResponse acResponse = ( AddContactResponse ) response;
            if( acResponse.getType() == AddContactResponse.AddContactResponseType.SUCCESS ) {
                String nickname = info.friend.nickname;
                adapter.add( nickname );
                adapter.notifyDataSetChanged();
                pDialog.dismiss();
            }
            else {
                AlertDialog.Builder alert = new AlertDialog.Builder(ListContactsActivity.this);
                alert.setTitle(R.string.list_contacts_add);
                alert.setMessage(R.string.list_contacts_add_no_such_user);

                // Set an EditText view to get user input
                final EditText input = new EditText(ListContactsActivity.this);
                alert.setView(input);
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String nickname = input.getEditableText().toString();
                        info.friend = new User(null, nickname);
                        AddContactRequest request = new AddContactRequest(info);
                        new AddContactsTask().execute(request);
                        pDialog.dismiss();
                        pDialog = new ProgressDialog(ListContactsActivity.this);
                        pDialog.setCancelable(true);
                        pDialog.setMessage(getResources().getString(R.string.list_contacts_add_connecting));
                        pDialog.show();
                    }
                });
                alert.show();
                pDialog.dismiss();
            }
        }
    }

    class ListContactsTask extends PFMHttpAsyncTask {

        @Override
        protected void onPostExecute(Response response) {
            ListContactsResponse lcResponse = ( ListContactsResponse ) response;
            List< User > contacts = lcResponse.getContactList();
            List< String > nicknameArray = new ArrayList<String>();
            for( User user : contacts )
            {
                nicknameArray.add( user.nickname );
            }
            adapter = new ArrayAdapter<String>( ListContactsActivity.this,
                    android.R.layout.simple_list_item_1, nicknameArray );
            listView.setAdapter( adapter );
            pDialog.dismiss();
            Toast.makeText( ListContactsActivity.this, getResources().getString( R.string.list_contacts_success ), Toast.LENGTH_SHORT ).show();
        }
    }
}
