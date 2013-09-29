package me.horzwxy.app.pfm.android.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.LogInRequest;
import me.horzwxy.app.pfm.model.LogInResponse;
import me.horzwxy.app.pfm.model.Response;
import me.horzwxy.app.pfm.model.SetNicknameRequest;
import me.horzwxy.app.pfm.model.SetNicknameResponse;
import me.horzwxy.app.pfm.model.User;

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
    createdActivities.add(this);

    accountNameTextView = (TextView) findViewById(R.id.log_in_account_name);
    accountTypeTextView = (TextView) findViewById(R.id.log_in_account_type);

    Button chooseAccountButton = (Button) findViewById(R.id.log_in_choose_account);
    submitButton = (Button) findViewById(R.id.log_in_submit);

    chooseAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AccountManager manager = AccountManager.get( LogInActivity.this );
        final Account[] accounts = manager.getAccounts();
        final String[] items = new String[ accounts.length ];
        for ( int i = 0; i < accounts.length; i++ ) {
          items[ i ] = parseAccountType( accounts[ i ].type ) + ": " + accounts[ i ].name;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder( LogInActivity.this );
        builder.setTitle( R.string.log_in_choose_account )
            .setItems( items, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int which) {
                String name = accounts[ which ].name;
                accountNameTextView.setText( name );
                String accountType = parseAccountType( accounts[ which ].type );
                currentUser = new User( name, null, accountType );
                accountTypeTextView.setText( accountType );
                submitButton.setEnabled(true);
              }
            } );
        builder.create().show();
      }
    });
    submitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pDialog = new ProgressDialog(LogInActivity.this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.logging_in));
        pDialog.show();
        LogInRequest request = new LogInRequest( currentUser );
        new LoggingInTask().execute( request );
      }
    });
    submitButton.setEnabled(false);
  }

  private String parseAccountType( String accountTypeString ) {
    String[] typeStrings = accountTypeString.split( "\\." );
    if( typeStrings.length > 1 ) {
      return typeStrings[1];
    }
    else {
      return typeStrings[0];
    }
  }

  class LoggingInTask extends PFMHttpAsyncTask {
    @Override
    protected void onPostExecute(Response response) {
        LogInResponse logInResponse = (LogInResponse) response;
        if (logInResponse.getType() == LogInResponse.LogInResponseType.SUCCESS) {
            pDialog.dismiss();
            currentUser.nickname = logInResponse.getNickname();
            Intent intent = new Intent(LogInActivity.this, NewDiningActivity.class);
            startActivity(intent);
            createdActivities.remove( LogInActivity.this );
            LogInActivity.this.finish();
        } else if (logInResponse.getType() == LogInResponse.LogInResponseType.SUCCESS_BUT_FIRST) {
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
                    SetNicknameRequest request = new SetNicknameRequest( currentUser );
                    new SetNicknameTask().execute( request );
                    pDialog.dismiss();
                    pDialog = new ProgressDialog(LogInActivity.this);
                    pDialog.setCancelable(true);
                    pDialog.setMessage(getResources().getString(R.string.setting_nickname_hint));
                    pDialog.show();
                }
            });
            alert.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
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

  class SetNicknameTask extends PFMHttpAsyncTask {

      @Override
      protected void onPostExecute(Response response) {
          SetNicknameResponse setNicknameResponse = (SetNicknameResponse)response;
          SetNicknameResponse.SetNicknameResponseType type = setNicknameResponse.getType();
          if( type == SetNicknameResponse.SetNicknameResponseType.SUCCESS ) {
              createdActivities.remove( LogInActivity.this );
              pDialog.dismiss();
              Intent intent = new Intent(LogInActivity.this, NewDiningActivity.class);
              startActivity(intent);
              LogInActivity.this.finish();
              return;
          }
          else if( type == SetNicknameResponse.SetNicknameResponseType.USED ) {
              AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);
              alert.setTitle(R.string.set_nickname);
              alert.setMessage(R.string.setting_nickname_used);

              // Set an EditText view to get user input
              final EditText input = new EditText(LogInActivity.this);
              alert.setView(input);
              alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                      String nickname = input.getEditableText().toString();
                      SetNicknameRequest request = new SetNicknameRequest( currentUser );
                      new SetNicknameTask().execute( request );
                      pDialog.dismiss();
                      pDialog = new ProgressDialog(LogInActivity.this);
                      pDialog.setCancelable(true);
                      pDialog.setMessage(getResources().getString(R.string.setting_nickname_hint));
                      pDialog.show();
                  }
              });
              alert.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                      Toast.makeText(LogInActivity.this, R.string.cannot_no_nickname_hint, Toast.LENGTH_SHORT).show();
                  }
              });
              alert.show();
              pDialog.dismiss();
          }
          else {
              pDialog.dismiss();
              Toast.makeText(LogInActivity.this, R.string.setting_nickname_failed, Toast.LENGTH_SHORT).show();
          }
      }
  }
}