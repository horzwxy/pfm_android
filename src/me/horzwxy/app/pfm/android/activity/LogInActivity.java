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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.LogInRequest;
import me.horzwxy.app.pfm.model.communication.LogInResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.communication.SetNicknameRequest;
import me.horzwxy.app.pfm.model.communication.SetNicknameResponse;
import me.horzwxy.app.pfm.model.data.User;

/**
 * Created by horz on 9/8/13.
 */
public class LogInActivity extends UnloggedInActivity {

    private TextView accountNameTextView;
    private TextView accountTypeTextView;
    private Button submitButton;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        accountNameTextView = (TextView) findViewById(R.id.log_in_account_name);
        accountTypeTextView = (TextView) findViewById(R.id.log_in_account_type);

        Button chooseAccountButton = (Button) findViewById(R.id.log_in_choose_account);
        submitButton = (Button) findViewById(R.id.log_in_submit);

        chooseAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountManager manager = AccountManager.get(LogInActivity.this);
                final Account[] accounts = manager.getAccounts();
                final String[] items = new String[accounts.length];
                for (int i = 0; i < accounts.length; i++) {
                    items[i] = parseAccountType(accounts[i].type) + ": " + accounts[i].name;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
                builder.setTitle(R.string.log_in_choose_account)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String name = accounts[which].name;
                                accountNameTextView.setText(name);
                                String accountType = parseAccountType(accounts[which].type);
                                currentUser = new User(name, accountType);
                                accountTypeTextView.setText(accountType);
                                submitButton.setEnabled(true);
                            }
                        });
                builder.create().show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences sp = getPreferences(MODE_PRIVATE);
        final String username = sp.getString( "username", null );
        if( username != null ) {
            AlertDialog.Builder builder = new AlertDialog.Builder( this );
            builder.setTitle(getResources().getString(R.string.log_in_use_default_account))
                    .setMessage(getResources().getString(R.string.log_in_use_default_account_hint1)
                            + username + getResources().getString(R.string.log_in_use_default_account_hint2))
                    .setCancelable(true)
                    .setPositiveButton(getResources().getString(R.string.log_in_use_default_account_true),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    currentUser = new User( username,
                                            sp.getString( "nickname", null ),
                                            sp.getString( "accountType", null ) );
                                    onSubmit();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.log_in_use_default_account_false),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // nothing
                                }
                            } );
            builder.create().show();
        }
    }

    private String parseAccountType(String accountTypeString) {
        String[] typeStrings = accountTypeString.split("\\.");
        if (typeStrings.length > 1) {
            return typeStrings[1];
        } else {
            return typeStrings[0];
        }
    }

    public void onSubmit( View v ) {
        onSubmit();
    }

    private void onSubmit() {
        pDialog = new ProgressDialog(LogInActivity.this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.logging_in));
        pDialog.show();
        LogInRequest request = new LogInRequest(currentUser);
        new LoggingInTask().execute(request);
    }

    private void onLogInSucceed( String username, String nickname, String accountType ) {
        SharedPreferences.Editor spEditor = getPreferences( MODE_PRIVATE ).edit();
        spEditor.putString( "username", username );
        spEditor.putString( "nickname", nickname );
        spEditor.putString( "accountType", accountType );
        spEditor.commit();
        currentUser = new U
    }

    class LoggingInTask extends PFMHttpAsyncTask<LogInRequest, LogInResponse> {

        @Override
        protected LogInResponse doInBackground(LogInRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse( responseString, LogInResponse.class );
        }

        @Override
        protected void onPostExecute(LogInResponse response) {
            if (response.type == LogInResponse.ResultType.SUCCEED) {
                pDialog.dismiss();
                currentUser.nickname = response.nickname;
                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                startActivity(intent);
                LogInActivity.this.finish();
            } else if (response.type == LogInResponse.ResultType.NEED_REGISTER) {
                AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);
                alert.setTitle(R.string.set_nickname);
                alert.setMessage(R.string.set_nickname_hint);

                // Set an EditText view to get user input
                final EditText input = new EditText(LogInActivity.this);
                alert.setView(input);
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String nickname = input.getEditableText().toString();
                        currentUser.nickname = nickname;
                        SetNicknameRequest request = new SetNicknameRequest(currentUser);
                        new SetNicknameTask().execute(request);
                        pDialog.dismiss();
                        pDialog = new ProgressDialog(LogInActivity.this);
                        pDialog.setCancelable(true);
                        pDialog.setMessage(getResources().getString(R.string.setting_nickname_hint));
                        pDialog.show();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(LogInActivity.this, R.string.cannot_no_nickname_hint, Toast.LENGTH_SHORT).show();
                    }
                });
                alert.show();
                pDialog.dismiss();
            } else {
                pDialog.dismiss();
                Toast.makeText(LogInActivity.this, R.string.fail_log_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    class SetNicknameTask extends PFMHttpAsyncTask<SetNicknameRequest, SetNicknameResponse> {

        @Override
        protected SetNicknameResponse doInBackground(SetNicknameRequest... requests) {
            String resultString = doConnecting( requests[0] );
            return Response.parseResponse( resultString, SetNicknameResponse.class );
        }

        @Override
        protected void onPostExecute(SetNicknameResponse response) {
            if (response.type == SetNicknameResponse.ResultType.SUCCESS) {
                pDialog.dismiss();
                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                startActivity(intent);
                LogInActivity.this.finish();
                return;
            } else if (response.type == SetNicknameResponse.ResultType.USED) {
                AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);
                alert.setTitle(R.string.set_nickname);
                alert.setMessage(R.string.setting_nickname_used);

                // Set an EditText view to get user input
                final EditText input = new EditText(LogInActivity.this);
                alert.setView(input);
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String nickname = input.getEditableText().toString();
                        currentUser.nickname = nickname;
                        SetNicknameRequest request = new SetNicknameRequest(currentUser);
                        new SetNicknameTask().execute(request);
                        pDialog.dismiss();
                        pDialog = new ProgressDialog(LogInActivity.this);
                        pDialog.setCancelable(true);
                        pDialog.setMessage(getResources().getString(R.string.setting_nickname_hint));
                        pDialog.show();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(LogInActivity.this, R.string.cannot_no_nickname_hint, Toast.LENGTH_SHORT).show();
                    }
                });
                alert.show();
                pDialog.dismiss();
            } else {
                pDialog.dismiss();
                Toast.makeText(LogInActivity.this, R.string.setting_nickname_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}