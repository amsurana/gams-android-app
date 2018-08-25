package ai.gams.dronecontroller.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import ai.gams.dronecontroller.R;

/**
 * Created by Amit S on 25/08/18.
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private CheckBox checkBox;

    public CheckableRelativeLayout(Context context) {
        super(context);

    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        checkBox = findViewById(R.id.checkbox);

    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean b) {
        checkBox.setChecked(true);
    }

    @Override
    public boolean isChecked() {
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        if (checkBox == null) {
            checkBox = findViewById(R.id.checkbox);
        }
        checkBox.setChecked(!checkBox.isChecked());
    }
}
