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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
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
 * Interface for user to create dining info.
 * User can pick participants from a list of contacts.
 * The complete dining info will be delivered to server.
 *
 * @version v0.98 This version does not save dining into local SQLite database.
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

    private Calendar diningDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dining);

        diningDate = Calendar.getInstance();

        restaurantInput = ( EditText ) findViewById( R.id.new_dining_restaurant_input );
        costInput = ( EditText ) findViewById( R.id.new_dining_cost );
        dateButton = ( Button ) findViewById(R.id.new_dining_show_date_picker);
        timeButton = ( Button ) findViewById(R.id.new_dining_show_time_picker);
        participants = new UserList();
        specialCosts = new CostList();
        paids = new CostList();

        dateButton.setText( getDateRepresentation( diningDate ) );
        timeButton.setText( getTimeRepresentation( diningDate ) );
    }

    private static String getDateRepresentation( Calendar calendar ) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get( Calendar.MONTH ) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "年"
                + ( month < 10 ? "0" + month : month ) + "月"
                + ( day < 10 ? "0" + day : day ) + "日";
    }

    private static String getTimeRepresentation( Calendar calendar ) {
        int hour = calendar.get( Calendar.HOUR_OF_DAY );
        int minute = calendar.get( Calendar.MINUTE );
        return ( hour < 10 ? "0" + hour : hour + "" )
                + " : "
                + ( minute < 10 ? "0" + minute : minute + "" );
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
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                diningDate.set( Calendar.YEAR, year );
                diningDate.set( Calendar.MONTH, month );
                diningDate.set( Calendar.DAY_OF_MONTH, day );
                dateButton.setText( getDateRepresentation( diningDate ) );
            }
        };
        new DatePickerDialog( this,
                listener,
                diningDate.get( Calendar.YEAR ),
                diningDate.get( Calendar.MONTH ),
                diningDate.get( Calendar.DAY_OF_MONTH ) ).show();
    }

    public void showTimePicker( View v ) {
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                diningDate.set( Calendar.HOUR_OF_DAY, hour );
                diningDate.set( Calendar.MINUTE, minute );
                timeButton.setText( getTimeRepresentation( diningDate ) );
            }
        };
        new TimePickerDialog( this,
                listener,
                diningDate.get( Calendar.HOUR_OF_DAY ),
                diningDate.get( Calendar.MINUTE ),
                true ).show();
    }

    public void chooseParticipants( View v ) {
        Intent intent = new Intent( this, ChooseParticipantsActivity.class );
        intent.putExtra( "participants", participants );
        startActivityForResult(intent, REQUEST_FOR_PARTICIPANTS);
    }

    private void showCostEditor( int hintId, final CostList costList ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( hintId );

        LinearLayout dialogView = ( LinearLayout ) getLayoutInflater()
                .inflate( R.layout.dialog_add_user_cost, null );

        final Spinner spinner = (Spinner) dialogView.findViewById( R.id.add_user_cost_spinner );
        ArrayAdapter< String > adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                participants.toNicknameList() );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final EditText costInput = (EditText) dialogView.findViewById(R.id.add_user_cost_cost);

        builder.setView( dialogView );
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String nickname = participants.toNicknameList().get( spinner.getSelectedItemPosition() );
                try{
                    int cost = (int)( Double.parseDouble( costInput.getEditableText().toString() ) * 10 );
                    costList.add(new Cost(cost, nickname));
                } catch (NumberFormatException e) {
                    Toast.makeText( NewDiningActivity.this, getResources().getString( R.string.new_dining_number_invalid ), Toast.LENGTH_SHORT ).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // nothing
            }
        });
        builder.create().show();
    }

    public void addSpecialCost( View v ) {
        showCostEditor( R.string.new_dining_special_cost_add_title, specialCosts );
    }

    public void displaySpecialCosts( View v ) {
        displayCosts( specialCosts );
    }

    public void addPaid( View v ) {
        showCostEditor( R.string.new_dining_paid_add_title, paids );
    }

    public void displayPaids( View v ) {
        displayCosts( paids );
    }

    private void displayCosts( CostList costList ) {
        Intent intent = new Intent( this, ManageCostAcvtivity.class );
        intent.putExtra( "costList", costList );
        startActivity( intent );
    }

    public void submit( View v ) {
        String restaurantString = restaurantInput.getText() + "";
        String costString = costInput.getText() + "";
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
        Cost cost = new Cost( (int)( Double.parseDouble(costString) * 10 ), null );

        if( totalPaids != cost.cost ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_invalid_paids ), Toast.LENGTH_SHORT ).show();
            return;
        }
        if( totalSpecialCosts > cost.cost ) {
            Toast.makeText( this, getResources().getText( R.string.new_dining_too_much_specialCosts ), Toast.LENGTH_SHORT ).show();
            return;
        }
        Restaurant restaurant = new Restaurant( restaurantString );
        Date date = diningDate.getTime();
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
