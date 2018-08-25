package ai.gams.dronecontroller.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ai.gams.dronecontroller.R;
import ai.gams.dronecontroller.model.Drone;
import ai.gams.dronecontroller.ui.widget.BottomSheetListView;
import ai.gams.dronecontroller.ui.widget.CheckableRelativeLayout;
import ai.gams.dronecontroller.utils.KnowledgeBaseUtil;
import ai.madara.exceptions.MadaraDeadObjectException;

/**
 * Created by Amit S on 23/08/18.
 */
public class DronesListDialog extends BottomSheetDialog {

    private final MapsActivity activity;


    private String groupName;
    private List<Drone> group = new ArrayList<>();
    private DronesAdapter dronesAdapter;

    public DronesListDialog(@NonNull MapsActivity context) {
        super(context);
        this.activity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_drones);

        findViewById(R.id.algorithm_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!group.isEmpty()) {
                    activity.showAlgorithmList(new ArrayList<Drone>(group));
                    group.clear();
                } else {
                    Toast.makeText(activity, "Group size is empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
        findViewById(R.id.group_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askGroupName();
            }
        });
        BottomSheetListView listView = (BottomSheetListView) findViewById(R.id.listViewBtmSheet);
        dronesAdapter = new DronesAdapter(KnowledgeBaseUtil.getInstance().getDrones(), getContext());
        listView.setAdapter(dronesAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((CheckableRelativeLayout) view).toggle();
            }
        });

    }

    private void askGroupName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText grpName = new EditText(getContext());
        builder.setTitle("Enter group name").setView(grpName).setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                groupName = grpName.getText().toString();
                if (groupName.length() == 0) {
                    Toast.makeText(activity, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!group.isEmpty()) {
                    try {
                        KnowledgeBaseUtil.getInstance().createGroup(groupName, group);
                    } catch (MadaraDeadObjectException e) {
                        Toast.makeText(activity, "Unable to create group " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    group.clear();
                } else {
                    Toast.makeText(activity, "Group size is empty", Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton("Cancel", null).create().show();
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

            CheckBox checkBox = ((CheckBox) view.findViewById(R.id.checkbox));
            checkBox.setTag(d);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        group.add((Drone) compoundButton.getTag());
                    } else {
                        group.remove(compoundButton.getTag());
                    }
                }
            });

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
