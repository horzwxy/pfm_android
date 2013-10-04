package me.horzwxy.app.pfm.android.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.ApproveDiningRequest;
import me.horzwxy.app.pfm.model.communication.ApproveDiningResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.Dining;
import me.horzwxy.app.pfm.model.data.DiningApproval;
import me.horzwxy.app.pfm.model.data.UserList;

/**
 * Created by horz on 10/4/13.
 */
public class ShowDiningActivity extends LoggedInActivity {

    private Dining dining;
    private Button approveButton;
    private Button rejectButton;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_dining_info );

        Intent intent = getIntent();
        dining = (Dining)intent.getSerializableExtra( "dining" );
        TextView restaurantView = (TextView) findViewById( R.id.dining_info_restuarant );
        restaurantView.setText( dining.restaurant.toString() );
        TextView costView = (TextView) findViewById(R.id.dining_info_cost);
        costView.setText(dining.cost.toYuan()+"");
        TextView dateView = (TextView) findViewById(R.id.dining_info_date);
        dateView.setText(dining.date.toString());
        TextView stateView = (TextView) findViewById( R.id.dining_info_state );
        String stateString = null;
        if( dining.state == Dining.DiningState.APPROVED ) {
            stateString = getResources().getString( R.string.dining_info_state_approved );
        }
        else if( dining.state == Dining.DiningState.NOT_APPROVED_YET ) {
            stateString = getResources().getString( R.string.dining_info_state_not_approved );
            approveButton = (Button) findViewById( R.id.dining_info_approve );
            rejectButton = ( Button ) findViewById( R.id.dining_info_reject );
            approveButton.setEnabled( true );
            rejectButton.setEnabled( true );
        }
        else {
            stateString = getResources().getString( R.string.dining_info_state_rejected );

        }
        stateView.setText(stateString);
    }

    public void displayParticipants( View v ) {
        UserList partcipants = dining.participants;
        Intent intent = new Intent( this, DisplayPartcipantsActivity.class );
        intent.putExtra( "participants", partcipants );
        startActivity( intent );
    }

    public void displayPaids( View v ) {
        Intent intent = new Intent( this, DisplayCostActivity.class );
        intent.putExtra( "costList", dining.paids );
        startActivity( intent );
    }

    public void displaySpecialCosts( View v ) {
        Intent intent = new Intent( this, DisplayCostActivity.class );
        intent.putExtra( "costList", dining.specialCosts );
        startActivity( intent );
    }

    public void onApprove( View v ) {
        deliverDa( new DiningApproval( dining.id, currentUser, Dining.DiningState.APPROVED ) );
    }

    public void onReject( View v ) {
        deliverDa( new DiningApproval( dining.id, currentUser, Dining.DiningState.REJECTED ) );
    }

    private void deliverDa( DiningApproval da ) {
        approveButton.setEnabled( false );
        rejectButton.setEnabled( false );
        final ApproveDiningTask task = new ApproveDiningTask();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.dining_info_approve_connecting));
        pDialog.show();
        task.execute( new ApproveDiningRequest( da ) );
    }

    class ApproveDiningTask extends PFMHttpAsyncTask<ApproveDiningRequest, ApproveDiningResponse> {

        ApproveDiningRequest request;

        @Override
        protected ApproveDiningResponse doInBackground(ApproveDiningRequest... requests) {
            request = requests[0];
            String responseString = doConnecting( request );
            return Response.parseResponse( responseString, ApproveDiningResponse.class );
        }

        @Override
        protected void onPostExecute(ApproveDiningResponse response) {
            pDialog.dismiss();
            if( request.newState == Dining.DiningState.APPROVED ) {
                if( response.type == ApproveDiningResponse.ResultType.SUCCESS ) {
                    Toast.makeText( ShowDiningActivity.this, getResources().getString( R.string.dining_info_approve_success ), Toast.LENGTH_SHORT ).show();
                }
                else {
                    Toast.makeText( ShowDiningActivity.this, getResources().getString( R.string.dining_info_approve_failed ), Toast.LENGTH_SHORT ).show();
                }
            }
            else {
                if( response.type == ApproveDiningResponse.ResultType.SUCCESS ) {
                    Toast.makeText( ShowDiningActivity.this, getResources().getString( R.string.dining_info_reject_success ), Toast.LENGTH_SHORT ).show();
                }
                else {
                    Toast.makeText( ShowDiningActivity.this, getResources().getString( R.string.dining_info_reject_failed ), Toast.LENGTH_SHORT ).show();
                }
            }
        }
    }
}
