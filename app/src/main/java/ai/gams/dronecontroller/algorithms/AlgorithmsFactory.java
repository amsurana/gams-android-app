package ai.gams.dronecontroller.algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amit S on 31/07/18.
 */
public class AlgorithmsFactory {

    public static Map<String, Class<? extends AlgorithmIntf>> algoMap = new HashMap<>();

    static {
//This can be extended for other algorithms
        algoMap.put("URAC ", URACAlgorithm.class);
        algoMap.put("UREC", URECAlgorithm.class);
        algoMap.put("Move", MoveAlgorithm.class);
        algoMap.put("Waypoints coverage", WaypointsCoverageAlgorithm.class);
        algoMap.put("PWRAC", PWRACAlgorithm.class);
        algoMap.put("Formation", FormationAlgorithm.class);

//        algoMap.put("URAC ", new URACAlgorithm());
//        algoMap.put("UREC", new URECAlgorithm());
//        algoMap.put("Move", new MoveAlgorithm());
//        algoMap.put("Waypoints coverage", new WaypointsCoverageAlgorithm());
//        algoMap.put("PWRAC", new PWRACAlgorithm());
//        algoMap.put("Formation", new FormationAlgorithm());

    }

    public static AlgorithmIntf getAlgorithmInstance(String type) {

        try {
            return algoMap.get(type).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
