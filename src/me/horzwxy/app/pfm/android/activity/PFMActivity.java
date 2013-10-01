package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import me.horzwxy.app.pfm.model.communication.RequestPackage;
import me.horzwxy.app.pfm.model.communication.ResponsePackage;
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

    protected abstract class PFMHttpAsyncTask< S extends RequestPackage, T extends ResponsePackage > extends AsyncTask< S, Void, T > {
        protected static final String HOST_NAME = "http://pfm.horzwxy.me";

        @Override
        protected T doInBackground(S... packages) {
            T responsePackage = null;
            S requestPackage = packages[0];
            Gson gson = new Gson();
            try {
                URL postUrl = new URL( HOST_NAME + requestPackage.type.servletPattern );
                HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                connection.connect();
                PrintWriter writer = new PrintWriter( connection.getOutputStream() );
                String requestJson = gson.toJson( requestPackage.attachment );
                writer.print(requestJson);
                writer.flush();
                writer.close();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader( connection.getInputStream() ) );
                String responseJson  = reader.readLine();
                ResponsePackage rawPackage = gson.fromJson( responseJson, ResponsePackage.class );
                responsePackage = (T)gson.fromJson( responseJson, rawPackage.type.responseContentClass );
                reader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responsePackage;
        }

        @Override
        protected abstract void onPostExecute(T response);
    }
}
