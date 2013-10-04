package me.horzwxy.app.pfm.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.data.Cost;
import me.horzwxy.app.pfm.model.data.CostList;
import me.horzwxy.app.pfm.model.data.User;

/**
 * Created by horz on 10/4/13.
 */
public class DisplayCostActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_costs );

        Intent intent = getIntent();
        CostList costList = (CostList) intent.getSerializableExtra( "costList" );

        ListView listView = (ListView) findViewById( R.id.cost_list );
        ArrayAdapter< String > adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, toStringList( costList ) );
        listView.setAdapter( adapter );
    }

    private static ArrayList< String > toStringList( CostList costList ) {
        ArrayList<String> result = new ArrayList<String>();
        for( Cost cost : costList ) {
            String nickname = cost.nickname;
            float costYuan = cost.toYuan();
            String preview = nickname + "/" + costYuan;
            result.add( preview );
        }
        return result;
    }
}
