package me.horzwxy.app.pfm.android.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.communication.ListBillsRequest;
import me.horzwxy.app.pfm.model.communication.ListBillsResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.Bill;

/**
 * Created by horz on 10/3/13.
 */
public class ListBillsActivity extends LoggedInActivity {

    private ProgressDialog pDialog;
    private ListView listView;
    private ArrayAdapter< String > adapter;
    private ArrayList< String > previews;
    private ArrayList< Bill > bills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bills );

        previews = new ArrayList<String>();
        listView = ( ListView ) findViewById( R.id.bills_list );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bill bill = bills.get( i );
                Intent intent = new Intent(ListBillsActivity.this, ShowBillActivity.class);
                intent.putExtra( "bill", bill );
                startActivity(intent);
            }
        });
        adapter = new ArrayAdapter<String>( this,
                android.R.layout.simple_list_item_1, previews );
        listView.setAdapter( adapter );
    }

    @Override
    protected void onResume() {
        super.onResume();

        previews.clear();
        final ListBillsTask task = new ListBillsTask();
        task.execute( new ListBillsRequest( currentUser ));
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel( true );
            }
        });
        pDialog.setMessage(getResources().getString(R.string.list_bills_connecting));
        pDialog.show();
    }

    class ListBillsTask extends PFMHttpAsyncTask<ListBillsRequest, ListBillsResponse> {

        @Override
        protected ListBillsResponse doInBackground(ListBillsRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse(responseString, ListBillsResponse.class);
        }

        @Override
        protected void onPostExecute(ListBillsResponse response) {
            bills = response.bills;
            for( Bill bill : bills ) {
                previews.add( getBillPreview( bill ) );
            }
            adapter.notifyDataSetChanged();
            pDialog.dismiss();
            Toast.makeText(ListBillsActivity.this, getResources().getString(R.string.list_bills_succeed), Toast.LENGTH_SHORT).show();
        }
    }

    private String getBillPreview( Bill bill ) {
        return bill.borrower.nickname + "->" + bill.lender.nickname + "  " + bill.cost.toYuan() + "元";
    }
}
