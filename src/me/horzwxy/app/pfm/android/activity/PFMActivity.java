package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.Request;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.User;

/**
 * Created by horz on 9/8/13.
 */
public class PFMActivity extends Activity {

    protected static ArrayList<Activity> createdActivities;
    protected static User currentUser;

    static {
        createdActivities = new ArrayList<Activity>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createdActivities.add( this );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == R.id.menu_quit ) {
            for( Activity activity : createdActivities ) {
                if( activity != null && activity != this ) {
                    activity.finish();
                }
            }
            this.finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        createdActivities.remove( this );
        super.onDestroy();
    }

    protected abstract class PFMHttpAsyncTask<S extends Request, T extends Response> extends AsyncTask<S, Void, T> {
        protected static final String HOST_NAME = "http://pfm.horzwxy.me";

        @Override
        protected abstract T doInBackground(S... requests);

        @Override
        protected abstract void onPostExecute(T response);

        protected String doConnecting(S request) {
            String responseString = null;
            try {
                URL postUrl = new URL(HOST_NAME + request.getServlePattern());
                HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.connect();
                PrintWriter writer = new PrintWriter( new OutputStreamWriter( connection.getOutputStream(), "utf-8" ) );
                writer.println(URLEncoder.encode( request.toPostContent(), "utf-8" ));
                writer.flush();
                writer.close();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                responseString = URLDecoder.decode( reader.readLine(), "utf-8" );
                reader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }
    }
}
