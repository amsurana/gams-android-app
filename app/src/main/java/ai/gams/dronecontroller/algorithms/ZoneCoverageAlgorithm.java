package ai.gams.dronecontroller.algorithms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.gams.dronecontroller.model.Drone;
import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.madara.exceptions.MadaraDeadObjectException;

/**
 * Created by Amit S on 27/07/18.
 */
public class ZoneCoverageAlgorithm implements AlgorithmIntf {

    private List<LatLng> locations = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private List<Drone> drones;
    private Context context;
    private GoogleMap map;
    private Polygon polygon;
    private Drone headDrone;

    public ZoneCoverageAlgorithm() {

    }

    @Override
    public void start(Context context, final List<Drone> droneList, final GoogleMap map) {
        this.drones = droneList;
        this.context = context;
        this.map = map;

        polygon = null;
        locations.clear();
        markers.clear();

        selectHeadList(droneList);

    }

    private void selectHeadList(List<Drone> droneList) {
        List<String> prefixes = new ArrayList<>();
        for (Drone d : droneList) {
            prefixes.add(d.prefix);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setSingleChoiceItems(prefixes.toArray(new String[0]), -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                headDrone = drones.get(i);
                startDrawPolygonStep(map);
                dialogInterface.dismiss();
            }
        }).create().show();

    }

    private void startDrawPolygonStep(final GoogleMap map) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Long press on map to create a polygon for Area algorithm. Press 'Send' button to execute the algorithm then").setTitle("Instructions").setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        locations.add(latLng);
                        Marker marker = map.addMarker(new MarkerOptions().position(latLng));
                        if (polygon == null) {
                            polygon = map.addPolygon(new PolygonOptions()
                                    .add(locations.toArray(new LatLng[0]))
                                    .strokeColor(Color.GREEN)
                                    .fillColor(Color.argb(100, 0xd5, 0xd5, 0xd5)));
                        } else {
                            polygon.setPoints(locations);
                        }
                        markers.add(marker);
                    }
                });

            }
        });

        builder.create().show();


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
            String agentPrefix = drone.prefix;


            /**
             * region.0.object_type=1;
             region.0.type=0;
             region.0.size=4;
             region.0.0=[40.443237, -79.940570];
             region.0.1=[40.443387, -79.940270];
             region.0.2=[40.443187, -79.940098];
             region.0.3=[40.443077, -79.940398
             */


            String regionPrefix = "region." + drone.getId();

            params.put(regionPrefix + ".type", "0");
            params.put(regionPrefix + ".size", "" + locations.size());

            int i = 0;
            for (LatLng latLng : locations) {
                params.put(regionPrefix + "." + (i++), String.format("[%f,%f]", latLng.latitude, latLng.longitude));
            }

            params.put(agentPrefix + ".algorithm", "formation coverage");

            params.put(agentPrefix + ".algorithm.args.head", headDrone.prefix);
            params.put(agentPrefix + ".algorithm.args.offset", "[0,0,0]");
            params.put(agentPrefix + ".algorithm.args.group", "group.formation");
            params.put(agentPrefix + ".algorithm.args.modifier", "default");

            params.put(agentPrefix + ".algorithm.args.coverage", "urec");
            params.put(agentPrefix + ".algorithm.args.coverage.args.area", regionPrefix);

            for (Marker m : markers) {
                m.remove();
            }

            if (polygon != null) {
                polygon.remove();
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
