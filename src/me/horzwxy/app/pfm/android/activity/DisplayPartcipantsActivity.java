package me.horzwxy.app.pfm.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import me.horzwxy.app.pfm.android.R;
import me.horzwxy.app.pfm.model.data.UserList;

/**
 * Created by horz on 10/4/13.
 */
public class DisplayPartcipantsActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_participants);
        Intent intent = getIntent();
        UserList participants = (UserList) intent.getSerializableExtra( "participants" );
        ListView listView = (ListView) findViewById(R.id.display_participants_list);
        ArrayAdapter< String > adapter = new ArrayAdapter<String>( this,
                android.R.layout.simple_list_item_1, participants.toNicknameList() );
        listView.setAdapter( adapter );
    }
}
