package me.horzwxy.app.pfm.android.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;

import me.horzwxy.app.android.R;
import me.horzwxy.app.pfm.android.model.Person;

/**
 * Created by horz on 9/8/13.
 */
public class LogInActivity extends UnloggedInActivity {

    private static final int REQUEST_FOR_ACCOUNT = 890;

    TextView accountNameTextView;
    Button chooseAccountButton;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        createdActivities.add(this);

        accountNameTextView = (TextView) findViewById(R.id.log_in_account_name);
        chooseAccountButton = (Button) findViewById(R.id.log_in_choose_account);
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
                //todo
            }
        });
        submitButton.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FOR_ACCOUNT && resultCode == Activity.RESULT_OK) {
            String username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            currentUser = new Person( username );
            accountNameTextView.setText( username );
            submitButton.setEnabled(true);
        }
    }
}
