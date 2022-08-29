package cn.rongcloud.um.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.rongcloud.um.R;

public class InputTextLinearLayout extends LinearLayout {
    private final TextView tvContent;
    private final EditText etContent;

    public InputTextLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.input_text_layout, this);
        tvContent = findViewById(R.id.tv_content);
        etContent = findViewById(R.id.et_content);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.InputTextLinearLayout);
        String leftText = typedArray.getString(R.styleable.InputTextLinearLayout_leftText);
        if (!TextUtils.isEmpty(leftText)) {
            tvContent.setText(leftText);
        }
    }

    public EditText getEtContent() {
        return etContent;
    }

    public String getEtContentText() {
        return etContent.getText().toString().trim();
    }
}
