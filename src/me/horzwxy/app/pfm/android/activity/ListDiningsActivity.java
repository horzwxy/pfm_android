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
import me.horzwxy.app.pfm.model.communication.ListDiningsRequest;
import me.horzwxy.app.pfm.model.communication.ListDiningsResponse;
import me.horzwxy.app.pfm.model.communication.Response;
import me.horzwxy.app.pfm.model.data.Dining;

/**
 * Created by horz on 10/3/13.
 */
public class ListDiningsActivity extends LoggedInActivity {

    private ProgressDialog pDialog;
    private ListView listView;
    private ArrayAdapter< String > adapter;
    private ArrayList< String > previews;
    private ArrayList< Dining > dinings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dinings );

        previews = new ArrayList<String>();
        listView = ( ListView ) findViewById( R.id.dinings_list );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Dining dining = dinings.get( i );
                Intent intent = new Intent(ListDiningsActivity.this, ShowDiningActivity.class);
                intent.putExtra( "dining", dining );
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
        final ListDiningsTask task = new ListDiningsTask();
        task.execute( new ListDiningsRequest( currentUser ));
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener( new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                task.cancel( true );
            }
        });
        pDialog.setMessage(getResources().getString(R.string.list_dinings_connecting));
        pDialog.show();
    }

    class ListDiningsTask extends PFMHttpAsyncTask<ListDiningsRequest, ListDiningsResponse> {

        @Override
        protected ListDiningsResponse doInBackground(ListDiningsRequest... requests) {
            String responseString = doConnecting( requests[0] );
            return Response.parseResponse( responseString, ListDiningsResponse.class );
        }

        @Override
        protected void onPostExecute(ListDiningsResponse response) {
            dinings = response.dinings;
            for( Dining dining : dinings ) {
                previews.add( getDiningPreview( dining ) );
            }
            adapter.notifyDataSetChanged();
            pDialog.dismiss();
            Toast.makeText( ListDiningsActivity.this, getResources().getString( R.string.list_dinings_succeed ), Toast.LENGTH_SHORT ).show();
        }
    }

    private String getDiningPreview( Dining dining ) {
        return dining.date + "/" + dining.cost.toYuan() + "元";
    }
}
