package ai.gams.dronecontroller.algorithms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.gams.dronecontroller.model.Drone;
import ai.madara.exceptions.MadaraDeadObjectException;

/**
 * Created by Amit S on 27/07/18.
 */
public class MoveAlgorithm implements AlgorithmIntf {

    private List<LatLng> locations = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Drone> drones;
    private Context context;
    private GoogleMap map;

    public MoveAlgorithm() {

    }

    @Override
    public void start(Context context, final List<Drone> droneList, final GoogleMap map) {
        this.drones = droneList;
        this.context = context;
        this.map = map;

        locations.clear();
        markers.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Long press on map to create location arguments for move algorithm. Press 'Send' button to execute the algorithm then").setTitle("Move Algorithm").setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initAlgorithm(map);
            }
        });
        builder.create().show();
    }

    private void initAlgorithm(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                locations.add(latLng);
                Marker marker = map.addMarker(new MarkerOptions().position(latLng));
                markers.add(marker);
            }
        });

    }


    @Override
    public void send() {
        String rep = "1";

        if (locations.size() < 2) {
            Toast.makeText(context, "Please enter at least two locations", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Drone drone : drones) {
            Map<String, String> params = new HashMap<>();
            String prefix = "agent." + drone.getId();
            params.put(prefix + ".algorithm", "waypoints");
            params.put(prefix + ".algorithm.args.repeat", rep);
            params.put(prefix + ".algorithm.args.locations.size", "" + locations.size());
            int i = 0;
            for (LatLng latLng : locations) {
                params.put(prefix + ".algorithm.args.locations." + (i++), String.format("[%f,%f,0]", latLng.latitude, latLng.longitude));
            }

            for (Marker m : markers) {
                m.remove();
            }

            map.setOnMapLongClickListener(null);


            try {
                KnowledgeBaseUtil.getInstance().sendData(params);
            } catch (MadaraDeadObjectException e) {
                e.printStackTrace();
            }
        }
    }
}
