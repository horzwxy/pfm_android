package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import me.horzwxy.app.pfm.model.LogInResponse;
import me.horzwxy.app.pfm.model.RequestInfo;
import me.horzwxy.app.pfm.model.ResponseInfo;
import me.horzwxy.app.pfm.model.User;
import me.horzwxy.app.pfm.model.tool.LogInMessage;

/**
 * Created by horz on 9/8/13.
 */
public class PFMActivity extends Activity {

    protected ArrayList<Activity> createdActivities = new ArrayList<Activity>();
    protected User currentUser;

    protected URL createLogInURL( String accountname ) {
        URL result = null;
        try {
            result = new URL( HOST_NAME + "/" + LOG_IN_SERVLET + "?"
                    + LOG_IN_ATTR_ACCOUNTNAME + "=" + accountname );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected class PFMHttpAsyncTask extends AsyncTask< RequestInfo, Object, ResponseInfo> {
        protected static final String HOST_NAME = "http://pfm.horzwxy.me";

        @Override
        protected ResponseInfo doInBackground(RequestInfo... infos) {
            ResponseInfo response = null;
            RequestInfo request = infos[0];
            try {
                URL postUrl = new URL( HOST_NAME + request.getRequestType().getServletPattern() );
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
                String content = request.getAttachment().toKVPair();
                writer.println(content);
                writer.flush();
                writer.close();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader( connection.getInputStream() ) );
                response = LogInMessage.parseLogInResponse(reader.readLine());
                reader.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}
