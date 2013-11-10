package me.horzwxy.app.pfm.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.AddDiningRequest;
import me.horzwxy.app.pfm.model.communication.AddDiningResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.Cost;
import me.horzwxy.app.pfm.model.data.CostList;
import me.horzwxy.app.pfm.model.data.Dining;
import me.horzwxy.app.pfm.model.data.Restaurant;
import me.horzwxy.app.pfm.model.data.User;
import me.horzwxy.app.pfm.model.data.UserList;

/**
 * Created by horz on 9/8/13.
 */
public class NewDiningActivity extends LoggedInActivity {

    private final static int REQUEST_FOR_PARTICIPANTS = 987;

    private EditText restaurantInput;
    private EditText costInput;
    private Button dateButton;
    private Button timeButton;
    private ProgressDialog pDialog;
    private UserList participants;
    private CostList specialCosts;
    private CostList paids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dining);

        restaurantInput = ( EditText ) findViewById( R.id.new_dining_restaurant_input );
        costInput = ( EditText ) findViewById( R.id.new_dining_cost );
        dateButton = ( Button ) findViewById(R.id.new_dining_show_date_picker);
        timeButton = ( Button ) findViewById(R.id.new_dining_show_time_picker);
        participants = new UserList();
        specialCosts = new CostList();
        paids = new CostList();

        Calendar calendar = Calendar.getInstance();
        dateButton.setText( calendar.get( Calendar.YEAR ) + "/"
                + ( calendar.get( Calendar.MONTH ) + 1 ) + "/"
                + calendar.get( Calendar.DAY_OF_MONTH ) );
        int hour = calendar.get( Calendar.HOUR_OF_DAY );
        int minute = calendar.get( Calendar.MINUTE );
        String hourString = hour + "";
        if( hour < 10 ) {
            hourString = "0" + hour;
        }
        String minuteString = minute + "";
        if( minute < 10 ) {
            minuteString = "0" + minute;
        }
        timeButton.setText( hourString + ":"
                + minuteString );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == Activity.RESULT_OK ) {
            switch ( requestCode ) {
                case REQUEST_FOR_PARTICIPANTS:
                    participants = (UserList)data.getSerializableExtra( "participants" );
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    public void showDatePicker( View v ) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateButton.setText( year + "/" + ( month + 1 ) + "/" + day );
            }
        };
        new DatePickerDialog( this,
                listener,
                calendar.get( Calendar.YEAR ),
                calendar.get( Calendar.MONTH ),
                calendar.get( Calendar.DAY_OF_MONTH ) ).show();
    }

    public void showTimePicker( View v ) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String hourString = hour + "";
                if( hour < 10 ) {
                    hourString = "0" + hour;
                }
                String minuteString = minute + "";
                if( minute < 10 ) {
                    minuteString = "0" + minute;
                }
                timeButton.setText( hourString + " : " + minuteString );
            }
        };
        new TimePickerDialog( this,
                listener,
                calendar.get( Calendar.HOUR_OF_DAY ),
                calendar.get( Calendar.MINUTE ),
                true ).show();
    }

    public void chooseParticipants( View v ) {
        Intent intent = new Intent( this, ChooseParticipantsActivity.class );
        intent.putExtra( "participants", participants );
        startActivityForResult(intent, REQUEST_FOR_PARTICIPANTS);
    }

    public void addSpecialCost( View v ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.new_dining_special_cost_add_title);

        LinearLayout dialogView = ( LinearLayout ) this.getLayoutInflater()
                .inflate( R.layout.dialog_add_user_cost, null );

        final EditText nicknameInput = (EditText) dialogView.findViewById(R.id.add_user_cost_nickname);
        final EditText costInput = (EditText) dialogView.findViewById(R.id.add_user_cost_cost);
        alert.setView( dialogView );
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = nicknameInput.getEditableText().toString();
                User user = new User( nickname );
                if( !participants.contains( user ) ) {
                    Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_not_participant ), Toast.LENGTH_SHORT ).show();
                    return;
                }
                String costString = costInput.getEditableText().toString();
                try{
                    int cost = Integer.parseInt( costString );
                    specialCosts.add( new Cost( cost, nickname ) );
                } catch (NumberFormatException e) {
                    Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_number_invalid ), Toast.LENGTH_SHORT ).show();
                }
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // nothing
            }
        });
        alert.show();
    }

    public void displaySpecialCosts( View v ) {
        Intent intent = new Intent( this, ManageCostAcvtivity.class );
        intent.putExtra("costList", specialCosts);
        startActivity( intent );
    }

    public void addPaid( View v ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.new_dining_paid_add_title);

        LinearLayout dialogView = ( LinearLayout ) this.getLayoutInflater()
                .inflate( R.layout.dialog_add_user_cost, null );

        final EditText nicknameInput = (EditText) dialogView.findViewById(R.id.add_user_cost_nickname);
        final EditText costInput = (EditText) dialogView.findViewById(R.id.add_user_cost_cost);
        alert.setView( dialogView );
        alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = nicknameInput.getEditableText().toString();
                User user = new User( nickname );
                if( !participants.contains( user ) ) {
                    Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_not_participant ), Toast.LENGTH_SHORT ).show();
                    return;
                }
                String costString = costInput.getEditableText().toString();
                try{
                    int cost = Integer.parseInt( costString );
                    paids.add( new Cost(cost, nickname));
                } catch (NumberFormatException e) {
                    Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_number_invalid ), Toast.LENGTH_SHORT ).show();
                }
            }
        });
        alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // nothing
            }
        });
        alert.show();
    }

    public void displayPaids( View v ) {
        Intent intent = new Intent( this, ManageCostAcvtivity.class );
        intent.putExtra( "costList", paids );
        startActivity( intent );
    }

    public void submit( View v ) {
        String restaurantString = restaurantInput.getText() + "";
        String costString = costInput.getText() + "";
        String dateString = dateButton.getText().toString();
        String timeString = timeButton.getText().toString();
        if( restaurantString.equals( "" ) ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_restaurant ), Toast.LENGTH_SHORT ).show();
            return;
        }
        if( costString.equals( "" ) ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_cost ), Toast.LENGTH_SHORT ).show();
            return;
        }
        if( participants.size() == 0 ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_failed_no_participants ), Toast.LENGTH_SHORT ).show();
            return;
        }
        int totalPaids = 0;
        int totalSpecialCosts = 0;
        for( Cost cost : specialCosts ) {
            totalSpecialCosts += cost.cost;
        }
        for( Cost cost : paids ) {
            totalPaids += cost.cost;
        }
        Cost cost = new Cost( Integer.parseInt(costString), null );

        if( totalPaids != cost.cost ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_invalid_paids ), Toast.LENGTH_SHORT ).show();
            return;
        }
        if( totalSpecialCosts > cost.cost ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_too_much_specialCosts ), Toast.LENGTH_SHORT ).show();
            return;
        }
        Restaurant restaurant = new Restaurant( restaurantString );
        DateFormat format = new SimpleDateFormat( "yyyy/MM/dd/HH : mm" );
        Date date = null;
        try {
            date = format.parse( dateString + "/" + timeString );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        User author = currentUser;
        Dining dining = new Dining( restaurant, date, cost, participants, specialCosts, paids, author );
        final AddDiningInfoTask task = new AddDiningInfoTask();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel( true );
            }
        });
        pDialog.setMessage(getResources().getString(R.string.new_dining_submitting));
        pDialog.show();
        task.execute( new AddDiningRequest( dining ) );
    }

    class AddDiningInfoTask extends PFMHttpAsyncTask< AddDiningRequest, AddDiningResponse > {

        @Override
        protected AddDiningResponse doInBackground(AddDiningRequest... requests) {
            String resultString = doConnecting( requests[0] );
            return Response.parseResponse( resultString, AddDiningResponse.class );
        }

        @Override
        protected void onPostExecute(AddDiningResponse response) {
            if( response.type == AddDiningResponse.ResultType.SUCCEED ) {
                pDialog.dismiss();
                NewDiningActivity.this.finish();
            }
            else {
                pDialog.dismiss();
                Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_failed_submit ), Toast.LENGTH_SHORT ).show();
            }
        }
    }
}
