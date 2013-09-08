package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import me.horzwxy.app.pfm.android.model.Person;

/**
 * Created by horz on 9/8/13.
 */
public class PFMActivity extends Activity {

    protected static final String HOST_NAME = "http://pfm.horzwxy.me";
    protected static final String LOG_IN_SERVLET = "login";
    protected static final String LOG_IN_ATTR_ACCOUNTNAME = "aname";

    protected ArrayList<Activity> createdActivities = new ArrayList<Activity>();
    protected Person currentUser;

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
}
