package ai.gams.dronecontroller.algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amit S on 31/07/18.
 */
public class AlgorithmsFactory {

    public static Map<String, AlgorithmIntf> algoMap = new HashMap<>();

    static {
        algoMap.put("URAC ", new URACAlgorithm());
        algoMap.put("UREC", new URECAlgorithm());
        algoMap.put("Move", new MoveAlgorithm());
        algoMap.put("Waypoints Coverage Area", new WaypointsCoverageAlgorithm());
        algoMap.put("PWRAC", new PWRACAlgorithm());

    }

    public static AlgorithmIntf getAlgorithmInstance(String type) {
        return algoMap.get(type);
    }
}
