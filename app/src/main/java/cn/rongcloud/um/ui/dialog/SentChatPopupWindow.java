package cn.rongcloud.um.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.databinding.DataBindingUtil;

import cn.rongcloud.um.R;
import cn.rongcloud.um.databinding.PopupwindowSentChatBinding;


public class SentChatPopupWindow extends PopupWindow implements View.OnClickListener {

    private PopupwindowSentChatBinding mbinding;
    private Context context;
    private SentChatOnClickInterface mSentChatOnClickInterface;

    public SentChatPopupWindow(Context context) {
        super(context);
        this.context = context;
        initView(context, true);
    }

    private void initView(Context context, boolean isShowGoHome) {
        View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_sent_chat, null);
        setContentView(view);
        mbinding = DataBindingUtil.bind(view);
        mbinding.setListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_sent_chat:
                if (null != mSentChatOnClickInterface) {
                    mSentChatOnClickInterface.itemOnClick(0);
                }
                dismiss();
                break;
        }
    }

    public interface SentChatOnClickInterface {
        void itemOnClick(int index);
    }

    public void setItemOnClick(SentChatOnClickInterface sentChatOnClickInterface) {
        mSentChatOnClickInterface = sentChatOnClickInterface;
    }
}