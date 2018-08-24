package ai.gams.dronecontroller.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.algorithms.AlgorithmIntf;
import ai.gams.dronecontroller.algorithms.AlgorithmsFactory;
import ai.gams.dronecontroller.model.Drone;
import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.madara.exceptions.MadaraDeadObjectException;
import ai.madara.knowledge.KnowledgeRecord;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Drone selecedDrone;
    private Map<Drone, Marker> droneMarkerMap = new HashMap<>();

    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            EventBus.getDefault().post(KnowledgeBaseUtil.getInstance().getDrones());
        }
    };

    private AlgorithmIntf algorithmIntf;
    private DronesListDialog dronesListDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button spinner = findViewById(R.id.numDrones);
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDronesList();
            }
        });
    }

    private void showDronesList() {
        dronesListDialog = new DronesListDialog(this);
        dronesListDialog.show();
        dronesListDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        KnowledgeBaseUtil.getInstance().cleanup();
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(List<Drone> drones) {
        ((Button) findViewById(R.id.numDrones)).setText("Drones (" + drones.size() + ")");
        for (Drone r : drones) {
            refreshMarker(r);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        handler.post(refreshRunnable);

    }

    private LatLng refreshMarker(Drone drone) {
        KnowledgeRecord rec = drone.getKnowledgeMap().get("agent." + drone.getId() + ".location");
        try {
            if (rec == null || !rec.isValid()) {
                return null;
            }

            String[] locations = rec.toString().split(",");
            // Add a marker in Maps and move the camera
            LatLng latLng = new LatLng(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]));
            drone.latLng = latLng;

            MarkerOptions options = new MarkerOptions().position(latLng).title(drone.username);
            options.icon(BitmapDescriptorFactory.defaultMarker(10));

            Marker marker = mMap.addMarker(options);

            //Remove from Map
            if (droneMarkerMap.containsKey(drone)) {
                droneMarkerMap.get(drone).remove();
            }

            droneMarkerMap.put(drone, marker);

            if (droneMarkerMap.size() == 1) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

            return latLng;
        } catch (MadaraDeadObjectException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void moveMyself(View view) {
        switch (view.getId()) {
            case R.id.left:
                moveLeft();
                break;
            case R.id.right:
                moveRight();
                break;
            case R.id.bottom:
                moveBottom();
                break;
            case R.id.top:
                moveTop();
                break;
        }
    }

    private void moveTop() {


        double distance = new Random().nextInt(20);
        double bearing = 0;

        sendMove(distance, bearing);

    }

    private LatLng getNewLatLng(LatLng latLng, double distance, double bearing) {
        double R = 6378.1;//Radius of the Earth
        double brng = Math.toRadians(bearing); //Bearing is 90 degrees converted to radians.
        double d = distance;// #Distance in km

        double lat1 = Math.toRadians(latLng.latitude);//#Current lat point converted to radians
        double lon1 = Math.toRadians(latLng.longitude);// #Current long point converted to radians

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) +
                Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));

        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
                Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        return new LatLng(lat2, lon2);
    }

    private void moveBottom() {
        double distance = new Random().nextInt(20);
        double bearing = 180;

        sendMove(distance, bearing);
    }

    private void moveRight() {
        double distance = new Random().nextInt(20);
        double bearing = 90;

        sendMove(distance, bearing);
    }

    private void moveLeft() {
        double distance = new Random().nextInt(20);
        double bearing = 270;

        sendMove(distance, bearing);

    }


    private void sendMove(double distance, double bearing) {
        try {
            if (!droneMarkerMap.containsKey(selecedDrone)) {
                Toast.makeText(this, "The current device is not shown on Map", Toast.LENGTH_SHORT).show();
                return;
            }

            Marker marker = droneMarkerMap.get(selecedDrone);
            LatLng next = getNewLatLng(marker.getPosition(), distance, bearing);
            marker.remove();


            KnowledgeBaseUtil.getInstance().sendData("agent." + selecedDrone.getId() + ".location", String.format("%.3f, %.3f, 10", next.latitude, next.longitude));
        } catch (MadaraDeadObjectException e) {
            e.printStackTrace();
        }
    }


    public void showAlgorithmList(final Drone selecedDrone) {
        dronesListDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final List<String> algoNames = new ArrayList<String>(AlgorithmsFactory.algoMap.keySet());
        builder.setTitle("Send Algorithm to " + selecedDrone.username);
        builder.setItems(algoNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                findViewById(R.id.send).setVisibility(View.VISIBLE);
                List<Drone> drones = new ArrayList<>();
                drones.add(selecedDrone);
                algorithmIntf = AlgorithmsFactory.getAlgorithmInstance(algoNames.get(i));
                algorithmIntf.start(MapsActivity.this, drones, mMap);
            }
        }).create().show();
    }


    public void sendAlgorithm(View view) {
        findViewById(R.id.send).setVisibility(View.GONE);
        Toast.makeText(this, "The algorithm is sent to selected drone", Toast.LENGTH_SHORT).show();
        algorithmIntf.send();
    }


}
