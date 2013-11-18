package me.horzwxy.app.pfm.android.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.LogInRequest;
import me.horzwxy.app.pfm.model.communication.LogInResponse;
import me.horzwxy.app.pfm.model.communication.Request;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.communication.SetNicknameRequest;
import me.horzwxy.app.pfm.model.communication.SetNicknameResponse;
import me.horzwxy.app.pfm.model.data.User;

/**
 * The first activity of the app. It checks records in SharedPreference for account info. \
 * If there is record, matching some account on device, it will directly jump to MainActivity, \
 * bypass the account-choosing procedure.
 *
 * When user need to choose an account on device, the server will check if the account is registered. \
 * If not, the user need set a nickname for the account.
 * There may be problems on network connection.
 */
public class LogInActivity extends UnloggedInActivity {

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get account record from SharedPreference
        // notice that we CANNOT use getPreference() here, since it will only return a activity-private SharedPreference editor
        // but we need to change its content in another activity
        final SharedPreferences sp = getSharedPreferences( "accountInfo", MODE_PRIVATE);
        final String accountName = sp.getString( "accountName", null );
        final String accountType = sp.getString( "accountType", null );
        if( accountName != null && accountType != null ) {
            User myAccount = getMatchedAccount( accountName, accountType );
            if( myAccount != null ) {
                myAccount.nickname = sp.getString( "nickname", null );
                currentUser = myAccount;
                // is this OK? -- 2013-11-17
                // Yes, it is. -- 2013-11-18
                startActivity( new Intent( this, MainActivity.class ) );
                return;
            }
        }
        // If info stored in SharedPreference matches none of the available accounts on device,
        // clear the records in SharedPreference.
        // This may happen when user logs in with one account then removes the account later.
        // Note that if info in SharedPreference is empty or incomplete, here just simply clear them.
        sp.edit().remove( "accountName" );
        sp.edit().remove( "accountType" );
        sp.edit().remove( "nickname" );
        sp.edit().commit();
    }



    /**
     * Display a nickname-setting dialog.
     * It will also dismiss the previous progress dialog.
     * @param messageStringId id of message displayed in the dialog
     */
    private void showSetNicknameDialog( int messageStringId ) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.set_nickname);
        builder.setMessage(messageStringId);

        // Set an EditText view to get user input
        final EditText input = new EditText(LogInActivity.this);
        builder.setView(input)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        currentUser.nickname = input.getEditableText().toString();
                        final SetNicknameTask task = new SetNicknameTask();
                        pDialog.dismiss();
                        showProgressDialog( task, R.string.setting_nickname_hint );
                        task.execute(new SetNicknameRequest(currentUser));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onCancleSetNickname();
                    }
                });
        builder.create().show();
    }

    /**
     * Find out whether there is a matched account on device.
     * @param accountName
     * @param accountType account type string should be the full name of Android account, for example \
     *                    "com.google"
     * @return
     */
    private User getMatchedAccount( String accountName, String accountType ) {
        AccountManager manager = AccountManager.get(this);
        final Account[] accounts = manager.getAccounts();

        for ( Account account : accounts ) {
            if( accountName.equals( account.name )
                    && accountType.equals( parseAccountType( account.type ) ) ) {
                return new User( account.name, account.type );
            }
        }
        return null;
    }

    /**
     * Usually, account type is a string like "com.google", but we only need "google".
     * Here is a parsing tool.
     * @param accountTypeString full string of account type, like "com.google"
     * @return the most valuable part of account type string, like "google" in "com.google"
     */
    private String parseAccountType(String accountTypeString) {
        String[] typeStrings = accountTypeString.split("\\.");
        if (typeStrings.length > 1) {
            return typeStrings[1];
        } else {
            return typeStrings[0];
        }
    }

    /**
     * Display an account picker dialog.
     * @param v
     */
    public void onChooseAccount( View v ) {
        AccountManager manager = AccountManager.get(this);
        final Account[] accounts = manager.getAccounts();
        final String[] items = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            // build the result string
            items[i] = parseAccountType(accounts[i].type) + ": " + accounts[i].name;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.log_in_choose_account)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String name = accounts[which].name;
                        ((TextView) findViewById(R.id.log_in_account_name)).setText(name);
                        String accountType = parseAccountType(accounts[which].type);
                        ((TextView) findViewById(R.id.log_in_account_type)).setText(accountType);
                        currentUser = new User(name, accountType);
                        // enable the submit button
                        findViewById(R.id.log_in_submit).setEnabled(true);
                    }
                });
        builder.create().show();
    }

    public void onSubmit( View v ) {
        final LoggingInTask task = new LoggingInTask();
        showProgressDialog( task, R.string.logging_in );
        task.execute(new LogInRequest(currentUser));
    }

    private void onAccountVerified( String nickname ) {
        SharedPreferences.Editor spEditor = getSharedPreferences( "accountInfo", MODE_PRIVATE ).edit();
        spEditor.putString("accountName", currentUser.accountName);
        spEditor.putString("nickname", nickname);
        spEditor.putString("accountType", currentUser.accountType);
        spEditor.commit();
        // currentUser has been instantialized before.
        // only need to set nickname
        currentUser.nickname = nickname;

        startActivity( new Intent(this, MainActivity.class) );
        this.finish();
    }

    private void onAccountRegisted() {
        onAccountVerified( currentUser.nickname );
    }

    /**
     * Display a progress dialog when working on network.
     * The dialog is bound with an asynchronous task. When click the cancel button on the dialog, \
     * the task will be canceled.
     * @param taskToBeCanceled the AsyncTask to be canceled when press on cancel button
     * @param messageStringId id of the message displayed in the dialog
     * @param <T>
     */
    private < T extends PFMHttpAsyncTask< ? extends Request, ? extends Response > > void showProgressDialog( final T taskToBeCanceled, int messageStringId ) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                taskToBeCanceled.cancel( true );
            }
        });
        pDialog.setMessage(getResources().getString( messageStringId ));
        pDialog.show();
    }

    private void onCancleSetNickname() {
        Toast.makeText(LogInActivity.this, R.string.cannot_no_nickname_hint, Toast.LENGTH_SHORT).show();
    }

    private class LoggingInTask extends PFMHttpAsyncTask<LogInRequest, LogInResponse> {

        @Override
        protected LogInResponse doInBackground(LogInRequest... requests) {
            String responseString = doConnecting(requests[0]);
            return Response.parseResponse( responseString, LogInResponse.class );
        }

        @Override
        protected void onPostExecute(LogInResponse response) {
            pDialog.dismiss();
            if (response.type == LogInResponse.ResultType.SUCCEED) {
                onAccountVerified(response.nickname);
            } else if (response.type == LogInResponse.ResultType.NEED_REGISTER) {
                showSetNicknameDialog( R.string.set_nickname_hint );
            } else {
                Toast.makeText(LogInActivity.this, R.string.fail_log_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SetNicknameTask extends PFMHttpAsyncTask<SetNicknameRequest, SetNicknameResponse> {

        @Override
        protected SetNicknameResponse doInBackground(SetNicknameRequest... requests) {
            String resultString = doConnecting( requests[0] );
            return Response.parseResponse( resultString, SetNicknameResponse.class );
        }

        @Override
        protected void onPostExecute(SetNicknameResponse response) {
            pDialog.dismiss();
            if (response.type == SetNicknameResponse.ResultType.SUCCESS) {
                onAccountRegisted();
            } else if (response.type == SetNicknameResponse.ResultType.USED) {
                showSetNicknameDialog( R.string.setting_nickname_used );
            } else {
                Toast.makeText(LogInActivity.this, R.string.setting_nickname_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}