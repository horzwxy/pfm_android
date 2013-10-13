package me.horzwxy.app.pfm.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.ApproveBillRequest;
import me.horzwxy.app.pfm.model.communication.ApproveBillResponse;
import me.horzwxy.app.pfm.model.communication.ApproveDiningRequest;
import me.horzwxy.app.pfm.model.communication.ClearBillRequest;
import me.horzwxy.app.pfm.model.communication.ClearBillResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.Bill;
import me.horzwxy.app.pfm.model.data.BillApproval;

/**
 * Created by horz on 10/5/13.
 */
public class ShowBillActivity extends LoggedInActivity {

    private Button approveButton;
    private Button rejectButton;
    private Button clearButton;
    private ProgressDialog pDialog;
    private Bill bill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_bill);

        TextView lenderView = (TextView) findViewById(R.id.display_bill_lender);
        TextView borrowerView = (TextView) findViewById(R.id.display_bill_borrower);
        TextView stateView = (TextView) findViewById(R.id.display_bill_state);
        TextView costView = (TextView) findViewById(R.id.display_bill_cost);

        approveButton = (Button) findViewById(R.id.display_bill_approve);
        rejectButton = (Button) findViewById(R.id.display_bill_reject);
        clearButton = (Button) findViewById(R.id.display_bill_clear);

        Intent intent = getIntent();
        bill = (Bill) intent.getSerializableExtra("bill");
        lenderView.setText(bill.lender.nickname);
        borrowerView.setText(bill.borrower.nickname);
        String stateString;
        if (bill.state == Bill.BillState.APPROVED) {
            stateString = getResources().getString(R.string.dining_info_state_approved);
            clearButton.setEnabled(true);
        } else if (bill.state == Bill.BillState.NOT_APPROVED_YET) {
            stateString = getResources().getString(R.string.dining_info_state_not_approved);
            approveButton.setEnabled(true);
            rejectButton.setEnabled(true);
        } else if (bill.state == Bill.BillState.REJECTED) {
            stateString = getResources().getString(R.string.dining_info_state_rejected);
            clearButton.setEnabled(true);
        } else {
            stateString = getResources().getString(R.string.display_bill_state_cleared);
        }
        stateView.setText(stateString);
        costView.setText(bill.cost.toYuan() + "元");
    }

    private void deliverBa(BillApproval ba) {
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        ApproveBillTask task = new ApproveBillTask();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.display_bill_approving));
        pDialog.show();
        task.execute(new ApproveBillRequest(ba));
    }

    public void onApprove(View v) {
        deliverBa(new BillApproval(currentUser, bill.billId, Bill.BillState.APPROVED));
    }

    public void onReject(View v) {
        deliverBa(new BillApproval(currentUser, bill.billId, Bill.BillState.REJECTED));
    }

    public void onClear(View v) {
        clearButton.setEnabled(false);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage(getResources().getString(R.string.display_bill_approving));
        pDialog.show();
        ClearBillTask task = new ClearBillTask();
        task.execute( new ClearBillRequest( currentUser, bill.billId ) );
    }

    public void displayDining(View v) {

    }

    class ApproveBillTask extends PFMHttpAsyncTask<ApproveBillRequest, ApproveBillResponse> {

        private Bill.BillState state;

        @Override
        protected ApproveBillResponse doInBackground(ApproveBillRequest... requests) {
            ApproveBillRequest request = requests[0];
            state = request.state;
            String responseString = doConnecting(request);
            return Response.parseResponse(responseString, ApproveBillResponse.class);
        }

        @Override
        protected void onPostExecute(ApproveBillResponse response) {
            if (state == Bill.BillState.APPROVED) {
                if (response.type == ApproveBillResponse.ResultType.SUCCEED) {
                    Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_approve_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_approve_failed), Toast.LENGTH_SHORT).show();
                }
            } else if (state == Bill.BillState.REJECTED) {
                if (response.type == ApproveBillResponse.ResultType.SUCCEED) {
                    Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_reject_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_reject_failed), Toast.LENGTH_SHORT).show();
                }
            }
            pDialog.dismiss();
        }
    }

    class ClearBillTask extends PFMHttpAsyncTask<ClearBillRequest, ClearBillResponse> {

        @Override
        protected ClearBillResponse doInBackground(ClearBillRequest... requests) {
            String responseString = doConnecting(requests[0]);
            return Response.parseResponse(responseString, ClearBillResponse.class);
        }

        @Override
        protected void onPostExecute(ClearBillResponse response) {
            if (response.type == ClearBillResponse.ResultType.SUCCESS) {
                Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_approve_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ShowBillActivity.this, getResources().getString(R.string.display_bill_approve_failed), Toast.LENGTH_SHORT).show();
            }
            pDialog.dismiss();
        }
    }
}
