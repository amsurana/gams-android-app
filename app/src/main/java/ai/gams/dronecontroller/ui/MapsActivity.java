package ai.gams.dronecontroller.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.algorithms.AlgorithmIntf;
import ai.gams.dronecontroller.algorithms.AlgorithmsFactory;
import ai.gams.dronecontroller.model.Drone;
import ai.madara.exceptions.MadaraDeadObjectException;
import ai.madara.knowledge.KnowledgeRecord;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayAdapter<Drone> robotListAdapter;
    private Drone selecedDrone;
    private Map<Drone, Marker> robotMarkerMap = new HashMap<>();

    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            EventBus.getDefault().post(KnowledgeBaseUtil.getInstance().getAgentMap());
        }
    };

    private AlgorithmIntf algorithmIntf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Spinner spinner = findViewById(R.id.agents);
        robotListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, KnowledgeBaseUtil.getInstance().getAgentMap());
        robotListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(robotListAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selecedDrone = KnowledgeBaseUtil.getInstance().getAgentMap().get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


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
    public void onMessageEvent(List<Drone> agentMap) {
        robotListAdapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.numAgents)).setText("Number of Agents: " + agentMap.size());

        for (Drone r : agentMap) {
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
            if (robotMarkerMap.containsKey(drone)) {
                robotMarkerMap.get(drone).remove();
            }

            robotMarkerMap.put(drone, marker);

            if (robotMarkerMap.size() == 1) {
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
            if (!robotMarkerMap.containsKey(selecedDrone)) {
                Toast.makeText(this, "The current device is not shown on Map", Toast.LENGTH_SHORT).show();
                return;
            }

            Marker marker = robotMarkerMap.get(selecedDrone);
            LatLng next = getNewLatLng(marker.getPosition(), distance, bearing);
            marker.remove();


            KnowledgeBaseUtil.getInstance().sendData("agent." + selecedDrone.getId() + ".location", String.format("%.3f, %.3f, 10", next.latitude, next.longitude));
        } catch (MadaraDeadObjectException e) {
            e.printStackTrace();
        }
    }


    public void showAlgorithmList(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final List<String> algoNames = new ArrayList<String>(AlgorithmsFactory.algoMap.keySet());
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
        algorithmIntf.send();
    }
}
