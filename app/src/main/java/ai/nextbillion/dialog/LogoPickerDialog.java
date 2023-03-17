package ai.nextbillion.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import ai.nextbillion.MainActivity;
import ai.nextbillion.R;


public class LogoPickerDialog extends FrameLayout {

    LogoOnClickListener logoOnClickListener;
    int selectedId = 0;
    Switch switchBtn;
    RecyclerView recyclerView;
    TextView confirmButton;
    private LogoInfo selectedLogo;

    private final LogoInfo[] logoInfoSet = {
            new LogoInfo(false,R.mipmap.map_log_1),
            new LogoInfo(false,R.mipmap.ic_launcher),
    };

    public LogoPickerDialog(Context context) {
        super(context);
    }

    public interface LogoOnClickListener {
        void onSelect(int resource, boolean checked);

        void onCancel();
    }

    public LogoPickerDialog(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LogoPickerDialog(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LogoPickerDialog(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    public void setupListener(LogoOnClickListener listener) {
        logoOnClickListener = listener;
    }

    public void setupSwitchButton(boolean enable) {
        switchBtn.setChecked(enable);
    }

    private void initView(Context context) {
        inflate(context, R.layout.pick_logo_bottom_sheet, this);
        bind(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void bind(Context context) {
        recyclerView = findViewById(R.id.logo_rv);
        LogoAdapter adapter = new LogoAdapter(logoInfoSet);
        adapter.setOnclickListener(info -> {
            if (selectedLogo != null){
                selectedLogo.selected = false;
            }
            selectedLogo = info;
            selectedLogo.selected = true;
            confirmButton.setEnabled(true);
        });
        LinearLayoutManager recyclerViewManager = new LinearLayoutManager(context);
        recyclerViewManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(recyclerViewManager);
        recyclerView.setAdapter(adapter);

        confirmButton = findViewById(R.id.confirm_button);
        TextView cancelButton = findViewById(R.id.cancel_button);
        assert confirmButton != null;
        confirmButton.setEnabled(false);
        switchBtn = findViewById(R.id.logo_switch);

        confirmButton.setOnClickListener(v -> logoOnClickListener.onSelect(selectedLogo.resourceId, switchBtn.isChecked()));
        assert cancelButton != null;
        cancelButton.setOnClickListener(v -> logoOnClickListener.onCancel());
    }


}
