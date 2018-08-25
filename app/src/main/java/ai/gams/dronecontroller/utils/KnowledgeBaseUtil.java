package ai.gams.dronecontroller.utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.gams.controllers.BaseController;
import ai.gams.dronecontroller.model.Drone;
import ai.gams.exceptions.GamsDeadObjectException;
import ai.gams.platforms.BasePlatform;
import ai.gams.utility.Axes;
import ai.gams.utility.Position;
import ai.madara.exceptions.MadaraDeadObjectException;
import ai.madara.knowledge.EvalSettings;
import ai.madara.knowledge.KnowledgeBase;
import ai.madara.knowledge.KnowledgeRecord;
import ai.madara.knowledge.Variables;
import ai.madara.transport.QoSTransportSettings;
import ai.madara.transport.TransportContext;
import ai.madara.transport.TransportType;
import ai.madara.transport.filters.AggregateFilter;
import ai.madara.transport.filters.Packet;

/**
 * Created by Amit S on 20/07/18.
 */
public class KnowledgeBaseUtil implements AggregateFilter {

    static {
        /**
         * libc++_shared.so
         libcapnp-0.6.1.so
         libcapnp-json-0.6.1.so
         libcapnp-rpc-0.6.1.so
         libcapnpc-0.6.1.so
         libGAMS.so
         libkj-0.6.1.so
         libkj-async-0.6.1.so
         libkj-http-0.6.1.so
         libMADARA.so
         libzmq.so
         */
        System.loadLibrary("MADARA");
        System.loadLibrary("GAMS");
    }

    private static final String TAG = KnowledgeBaseUtil.class.getSimpleName();
    private static KnowledgeBaseUtil instance;
    private QoSTransportSettings qoSTransportSettings;


    public static KnowledgeBaseUtil getInstance() {
        if (instance == null) {
            instance = new KnowledgeBaseUtil();
        }
        return instance;
    }

    private KnowledgeBase myKnowledgeBase;
    private Drone self;


    private BaseController controller;

    private List<Drone> drones = new ArrayList<>();

    Pattern pattern = Pattern.compile("agent.(\\d+).*");

    private Timer timer = new Timer();
    private TimerTask pingSender = new TimerTask() {
        @Override
        public void run() {
            try {
                sendData(self.prefix + ".name", self.username);
            } catch (MadaraDeadObjectException e) {
                e.printStackTrace();
            }
        }
    };

    private KnowledgeBaseUtil() {
    }

    public void cleanup() {
        try {
            sendData(self.prefix + ".online", "" + false);
        } catch (MadaraDeadObjectException e) {
            e.printStackTrace();
        }
        qoSTransportSettings.free();
        myKnowledgeBase.free();

        timer.cancel();
    }

    public void initKb(Drone self) {

        try {

            qoSTransportSettings = new QoSTransportSettings();
            qoSTransportSettings.setHosts(new String[]{"239.255.0.1:4150"});
            qoSTransportSettings.setType(TransportType.MULTICAST_TRANSPORT);
            qoSTransportSettings.addReceiveFilter(this);
            qoSTransportSettings.addSendFilter(this);

            myKnowledgeBase = new KnowledgeBase("", qoSTransportSettings);

            EvalSettings evalSettings = new EvalSettings();
            evalSettings.setDelaySendingModifieds(false);

            int myId = new Random().nextInt(100);
            String myPrefix = "agent." + myId;
            myKnowledgeBase.set(".id", myId, evalSettings);
            myKnowledgeBase.set(myPrefix + ".name", self.username, evalSettings);
            myKnowledgeBase.set(myPrefix + ".location", "12.12, 77.50, 12", evalSettings);

            this.self = self;
            this.self.id = "" + myId;
            this.self.prefix = myPrefix;
            this.self.knowledgeMap = (myKnowledgeBase.toKnowledgeMap("agent." + myId));

            //drones.add(this.self);

            //Let's join the swarm
            myKnowledgeBase.sendModifieds();


            controller = new BaseController(myKnowledgeBase);
            controller.initPlatform(new BasePlatform() {
                @Override
                public int analyze() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public double getAccuracy() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public double getPositionAccuracy() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public Position getPosition() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return null;
                }

                @Override
                public int home() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public int land() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public int move(Position target, double proximity) throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public int rotate(Axes axes) throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public double getMinSensorRange() throws GamsDeadObjectException, MadaraDeadObjectException {
                    return 0;
                }

                @Override
                public double getMoveSpeed() throws GamsDeadObjectException, MadaraDeadObjectException {
                    return 0;
                }

                @Override
                public String getId() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public int sense() throws GamsDeadObjectException, MadaraDeadObjectException {
                    return 0;
                }

                @Override
                public void setMoveSpeed(double speed) throws MadaraDeadObjectException {

                }

                @Override
                public int takeoff() throws MadaraDeadObjectException, GamsDeadObjectException {
                    return 0;
                }

                @Override
                public void stopMove() throws MadaraDeadObjectException, GamsDeadObjectException {

                }
            });


            timer.scheduleAtFixedRate(pingSender, 5000, 30000);

        } catch (MadaraDeadObjectException | GamsDeadObjectException e) {
            e.printStackTrace();
        }

    }

    public void addReceiveFilter(AggregateFilter filter) throws MadaraDeadObjectException {
        qoSTransportSettings.addReceiveFilter(filter);
    }


    /**
     * 1.
     *
     * @param packet
     * @param context
     * @param variables
     * @throws MadaraDeadObjectException
     */
    @Override
    public void filter(Packet packet, TransportContext context, Variables variables) throws MadaraDeadObjectException {

        myKnowledgeBase.print();


        String[] keys = packet.getKeys();
        if (keys != null && keys.length > 0) {
            Matcher matcher = pattern.matcher(keys[0]);
            if (matcher.find()) {
                String index = matcher.group(1);
                Drone drone = null;
                if (!drones.contains(new Drone(index))) {
                    drone = new Drone(index);
                    drones.add(drone);
                } else {
                    drone = drones.get(drones.indexOf(new Drone(index)));
                }

                drone.setKnowledgeMap(myKnowledgeBase.toKnowledgeMap("agent." + index));
                KnowledgeRecord rec = myKnowledgeBase.get("agent." + index + ".name");
                drone.username = rec.isValid() ? rec.toString() : "-NA-";
                drone.lastUpdatedTime = System.currentTimeMillis();
                drone.prefix = "agent." + index;
            }

            EventBus.getDefault().post(drones);
        }

    }

    public List<Drone> getDrones() {
        return drones;
    }

    public void sendData(String key, String value) throws MadaraDeadObjectException {
        EvalSettings evalSettings = new EvalSettings();
        evalSettings.setDelaySendingModifieds(false);
        myKnowledgeBase.set(key, value, evalSettings);
        myKnowledgeBase.sendModifieds(evalSettings);

    }

    public void sendData(Map<String, String> params) throws MadaraDeadObjectException {
        EvalSettings evalSettings = new EvalSettings();
        evalSettings.setDelaySendingModifieds(false);
        for (String key : params.keySet()) {
            myKnowledgeBase.set(key, params.get(key), evalSettings);
        }
        myKnowledgeBase.sendModifieds(evalSettings);
    }


    public void createGroup(String groupName, List<Drone> group) throws MadaraDeadObjectException {
        //Convert to what KB requires
        groupName = "group." + groupName;
        Map<String, String> params = new HashMap<>();
        params.put(groupName + ".members.size", Integer.toString(group.size()));
        int index = 0;
        for (Drone drone : group) {
            params.put(groupName + ".members." + (index++), drone.prefix);
        }

        sendData(params);
    }
}
