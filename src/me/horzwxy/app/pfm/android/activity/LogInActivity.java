package me.horzwxy.app.pfm.android.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.AccountPicker;
import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.android.model.Person;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by horz on 9/8/13.
 */
public class LogInActivity extends UnloggedInActivity {

  private static final int REQUEST_FOR_ACCOUNT = 890;
  private static final int RESPONSE_LOGGING_IN = 891;
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
        new LoggingInTask().execute((String) accountNameTextView.getText());
      }
    });
    submitButton.setEnabled(false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_FOR_ACCOUNT && resultCode == Activity.RESULT_OK) {
      String username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
      currentUser = new Person(username);
      accountNameTextView.setText(username);
      submitButton.setEnabled(true);
    }
  }

  enum LoggingInResponse {
    SUCCESS,
    FAILED,
    SUCCESS_BUT_FIRST;
  }

  class LoggingInTask extends AsyncTask<String, Void, LoggingInResponse> {

    private String nickname = null;

    @Override
    protected LoggingInResponse doInBackground(String... strings) {
      URL url = createLogInURL(strings[0]);
      try {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        Document xmlDoc = db.parse(connection.getInputStream());

        NodeList nodeList = xmlDoc.getElementsByTagName("nickname");
        nickname = nodeList.item(0).getTextContent();
        nodeList = xmlDoc.getElementsByTagName("type");
        LoggingInResponse response = LoggingInResponse.valueOf(nodeList.item(0).getTextContent());

        return response;
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(LoggingInResponse response) {
      if (response == LoggingInResponse.SUCCESS) {
        Intent intent = new Intent(LogInActivity.this, NewDiningActivity.class);
        startActivity(intent);
      } else if (response == LoggingInResponse.SUCCESS_BUT_FIRST) {
        // todo
        AlertDialog.Builder alert = new AlertDialog.Builder(LogInActivity.this);
        alert.setTitle(R.string.set_nickname);
        alert.setMessage(R.string.set_nickname_hint);

        // Set an EditText view to get user input
        final EditText input = new EditText(LogInActivity.this);
        alert.setView(input);

        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            nickname = input.getEditableText().toString();
            Intent intent = new Intent(LogInActivity.this, NewDiningActivity.class);
            startActivity(intent);
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

  class SetNicknameTask extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... strings) {
      return null;  //TODO
    }
  }
}