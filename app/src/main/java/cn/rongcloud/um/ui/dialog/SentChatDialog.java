package cn.rongcloud.um.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import cn.rongcloud.um.R;

public class SentChatDialog extends Dialog implements View.OnClickListener {
    private final Context context;
    private final int themeResId;
    private final int[] listenedItem;
    private EditText etTargetId;

    private final String[] typeString = {"单聊", "群聊"};
    private String result = "1";


    public SentChatDialog(@NonNull Context context, int themeResId, int[] listenedItem) {
        super(context, themeResId);
        this.context = context;
        this.themeResId = themeResId;
        this.listenedItem = listenedItem;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        setContentView(themeResId);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = size.x * 4 / 5;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        setCanceledOnTouchOutside(true);
        //    private EditText etConversationType;
        Spinner spConversationType = findViewById(R.id.spinner_type);
        etTargetId = findViewById(R.id.et_targetId);
        for (int id : listenedItem) {
            findViewById(id).setOnClickListener(this);
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typeString);
        //设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到spinner中去
        spConversationType.setAdapter(adapter);
        spConversationType.setVisibility(View.VISIBLE);//设置默认显示
        spConversationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){
                    result = "1";
                }else {
                    result = "3";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        dismiss();
        listener.OnItemClick(this, view);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void OnItemClick(SentChatDialog dialog, View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public String getEtConversationType() {
        return result;
    }

    public EditText getEtTargetId() {
        return etTargetId;
    }
}
