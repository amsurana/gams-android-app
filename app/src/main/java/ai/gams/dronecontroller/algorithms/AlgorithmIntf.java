package ai.gams.dronecontroller.algorithms;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import ai.gams.dronecontroller.model.Drone;

/**
 * Created by Amit S on 31/07/18.
 */
public interface AlgorithmIntf {

    void start(Context context, final List<Drone> droneList, final GoogleMap map);

    void send();
}
