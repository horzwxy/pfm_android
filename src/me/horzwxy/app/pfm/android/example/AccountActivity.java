package me.horzwxy.app.pfm.android.example;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import 	com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

import me.horzwxy.app.pfm.android.R;

/**
 * Created by horz on 9/8/13.
 */
public class AccountActivity extends Activity {

    AccountManager mAccountManager;
    private static final int SOME_REQUEST_CODE = 999;
    private String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.choose_user);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, SOME_REQUEST_CODE);
            }
        });
        Button auth = (Button)findViewById(R.id.authenticate);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String token = null;

                        try {
                            token = GoogleAuthUtil.getToken(
                                    AccountActivity.this,
                                    accountName,
                                    Scopes.PLUS_LOGIN);
                        } catch (IOException transientEx) {
                            transientEx.printStackTrace();
                        } catch (UserRecoverableAuthException e) {
                            // Recover (with e.getIntent())
                            e.printStackTrace();
                        } catch (GoogleAuthException authEx) {
                            // The call is not ever expected to succeed
                            // assuming you have already verified that
                            // Google Play services is installed.
                            authEx.printStackTrace();
                        }
                        return token;
                    }

                    @Override
                    protected void onPostExecute(String token) {
                        System.out.println("Access token retrieved:" + token);
                    }

                };
                task.execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == SOME_REQUEST_CODE && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            System.out.println( accountName );
        }
    }
}
