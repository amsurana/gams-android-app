package ai.gams.dronecontroller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.model.Drone;
import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    public void goNext(View view) {
        EditText username = findViewById(R.id.username);
        String name = username.getText().toString();
        if (name.length() == 0) {
            Toast.makeText(this, "Please enter a unique ID", Toast.LENGTH_SHORT).show();
            return;
        }


        Drone drone = new Drone();
        drone.username = name;

        KnowledgeBaseUtil.getInstance().initKb(drone);

        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        // new InitKbAsyncTask().execute();
        startActivity(new Intent(MainActivity.this, MapsActivity.class));
        finish();

    }

}
