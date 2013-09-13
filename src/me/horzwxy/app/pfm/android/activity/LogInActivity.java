package me.horzwxy.app.pfm.android.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.AccountPicker;
import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.LogInResponse;
import me.horzwxy.app.pfm.model.LogInResponseType;
import me.horzwxy.app.pfm.model.User;
import me.horzwxy.app.pfm.model.tool.LogInMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by horz on 9/8/13.
 */
public class LogInActivity extends UnloggedInActivity {

  private static final int REQUEST_FOR_ACCOUNT = 890;
  private static final int RESPONSE_LOG_IN = 891;
  private TextView accountNameTextView;
  private Button submitButton;
  private ProgressDialog pDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_log_in);
    createdActivities.add(this);

    accountNameTextView = (TextView) findViewById(R.id.log_in_account_name);
    Button chooseAccountButton = (Button) findViewById(R.id.log_in_choose_account);
    submitButton = (Button) findViewById(R.id.log_in_submit);

    chooseAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
            false, null, null, null, null);
        startActivityForResult(intent, REQUEST_FOR_ACCOUNT);
      }
    });
    submitButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pDialog = new ProgressDialog(LogInActivity.this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.logging_in));
        pDialog.show();
        new LoggingInTask().execute();
      }
    });
    submitButton.setEnabled(false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_FOR_ACCOUNT && resultCode == Activity.RESULT_OK) {
      String username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      currentUser = new User(username, null);
      accountNameTextView.setText(username);
      submitButton.setEnabled(true);
    }
  }

  class LoggingInTask extends AsyncTask<User, Void, LogInResponse> {

    private String nickname = null;

    @Override
    protected LogInResponse doInBackground(User... users) {

    }

    @Override
    protected void onPostExecute(LogInResponse response) {
        if (response.type == LogInResponseType.SUCCESS) {
            Intent intent = new Intent(LogInActivity.this, NewDiningActivity.class);
            startActivity(intent);
        } else if (response.type == LogInResponseType.SUCCESS_BUT_FIRST) {
            AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);
            alert.setTitle(R.string.set_nickname);
            alert.setMessage(R.string.set_nickname_hint);

            // Set an EditText view to get user input
            final EditText input = new EditText(LogInActivity.this);
            alert.setView(input);
            alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String nickname = input.getEditableText().toString();
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

  class SetNicknameTask extends AsyncTask<User, Void, Boolean> {
    @Override
    protected Boolean doInBackground(User... users) {


      return false;
    }
  }
}