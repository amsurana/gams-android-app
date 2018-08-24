package ai.gams.dronecontroller.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.model.Drone;
import ai.gams.dronecontroller.ui.widget.BottomSheetListView;
import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;

/**
 * Created by Amit S on 23/08/18.
 */
public class DronesListDialog extends BottomSheetDialog {

    private final MapsActivity activity;


    private ImageView groupIcon;

    public DronesListDialog(@NonNull MapsActivity context) {
        super(context);
        this.activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_drones);

        groupIcon = findViewById(R.id.icon_group);
        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCheckBoxes();
            }
        });
        BottomSheetListView listView = (BottomSheetListView) findViewById(R.id.listViewBtmSheet);
        DronesAdapter dronesAdapter = new DronesAdapter(KnowledgeBaseUtil.getInstance().getDrones(), getContext());
        listView.setAdapter(dronesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                activity.showAlgorithmList(KnowledgeBaseUtil.getInstance().getDrones().get(i));
            }
        });

    }

    private void showCheckBoxes() {
        findViewById(R.id.create_group).setVisibility(View.VISIBLE);
        findViewById(R.id.create_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private class DronesAdapter extends BaseAdapter {

        List<Drone> drones;
        Context context;

        DronesAdapter(List<Drone> drones, Context context) {
            this.drones = drones;
            this.context = context;
        }

        @Override
        public int getCount() {
            return drones.size();
        }

        @Override
        public Object getItem(int i) {
            return drones.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(context, R.layout.item_drone, null);
            }

            Drone d = (Drone) getItem(i);

            ((TextView) view.findViewById(R.id.agent_name)).setText(d.username);
            ((TextView) view.findViewById(R.id.agent_lastSeen)).setText("" + new Date(d.lastUpdatedTime));
            if (System.currentTimeMillis() - d.lastUpdatedTime > 2 * 60 * 1000) {
                ((ImageView) view.findViewById(R.id.status_icon)).setImageResource(R.drawable.red_circle);
            } else {
                ((ImageView) view.findViewById(R.id.status_icon)).setImageResource(R.drawable.green_circle);
            }
            ((TextView) view.findViewById(R.id.agent_prefix)).setText(d.prefix);
            return view;

        }
    }


}
