package ai.gams.dronecontroller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.model.Drone;

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

//    private class InitKbAsyncTask extends AsyncTask<Void, Void, Integer> {
//        @Override
//        protected void onPostExecute(Integer integer) {
//            super.onPostExecute(integer);
//            if (integer == 1) {
//                startActivity(new Intent(MainActivity.this, MapsActivity.class));
//                finish();
//            } else {
//                Toast.makeText(MainActivity.this, "Could not initialize Controller. Try again", Toast.LENGTH_SHORT).show();
//                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
//            }
//        }
//
//        @Override
//        protected Integer doInBackground(Void... voids) {
//            try {
//                int tries = 0;
//                while (!KnowledgeBaseUtil.getInstance().isInitialized() && ++tries < 10) {
//                    Thread.sleep(10000);
//                    if (KnowledgeBaseUtil.getInstance().isInitialized()) {
//                        return 1;
//                    }
//                    KnowledgeBaseUtil.getInstance().reInit();
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return 0;
//        }
//    }

}
