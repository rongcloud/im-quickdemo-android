package cn.rongcloud.um.ui;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityParameterSettingBinding;
import cn.rongcloud.um.utils.DataProcessorUtil;
import cn.rongcloud.um.utils.SessionManager;
import cn.rongcloud.um.base.Constants;

public class ParameterSettingActivity extends BaseActivity<MainViewModel, ActivityParameterSettingBinding> {


    @Override
    public int getContentViewId() {
        return R.layout.activity_parameter_setting;
    }

    @Override
    public void setView() {
        mViewData.topBar.setOnRightClickListener(view -> {
            int num = 0;
            if (mViewData.cbPrivate.isChecked()) {
                num++;
            }
            if (mViewData.cbGroup.isChecked()) {
                num += 2;
            }
            if (mViewData.cbSystem.isChecked()) {
                num += 4;
            }
            if (num == 0) {
                toast("请至少选中一个会话类型");
                return;
            }
            SessionManager.getInstance().put(Constants.SP_KEY_CONVERSATION_TYPE, num);
            int subNum = 0;
            if (mViewData.cbPrivateSub.isChecked()) {
                subNum++;
            }
            if (mViewData.cbGroupSub.isChecked()) {
                subNum += 2;
            }
            if (mViewData.cbSystemSub.isChecked()) {
                subNum += 4;
            }
            SessionManager.getInstance().put(Constants.SP_KEY_SUB_CONVERSATION_TYPE, subNum);
            toast("保存");
            DataProcessorUtil.conversationTypes = DataProcessorUtil.supportedTypes();
        });
    }

    @Override
    public void setData() {
        initConversationType();
        initSubConversationType();
    }

    private void initConversationType() {
        int num = SessionManager.getInstance().getInt(Constants.SP_KEY_CONVERSATION_TYPE);
        switch (num) {
            case 1:
                mViewData.cbPrivate.setChecked(true);
                break;
            case 2:
                mViewData.cbGroup.setChecked(true);
                break;
            case 3:
                mViewData.cbPrivate.setChecked(true);
                mViewData.cbGroup.setChecked(true);
                break;
            case 4:
                mViewData.cbSystem.setChecked(true);
                break;
            case 5:
                mViewData.cbPrivate.setChecked(true);
                mViewData.cbSystem.setChecked(true);
                break;
            case 6:
                mViewData.cbGroup.setChecked(true);
                mViewData.cbSystem.setChecked(true);
                break;
            case 7:
                mViewData.cbPrivate.setChecked(true);
                mViewData.cbGroup.setChecked(true);
                mViewData.cbSystem.setChecked(true);
                break;
            default:
                break;
        }
    }

    private void initSubConversationType() {
        int num = SessionManager.getInstance().getInt(Constants.SP_KEY_SUB_CONVERSATION_TYPE);
        switch (num) {
            case 1:
                mViewData.cbPrivateSub.setChecked(true);
                break;
            case 2:
                mViewData.cbGroupSub.setChecked(true);
                break;
            case 3:
                mViewData.cbPrivateSub.setChecked(true);
                mViewData.cbGroupSub.setChecked(true);
                break;
            case 4:
                mViewData.cbSystemSub.setChecked(true);
                break;
            case 5:
                mViewData.cbPrivateSub.setChecked(true);
                mViewData.cbSystemSub.setChecked(true);
                break;
            case 6:
                mViewData.cbGroupSub.setChecked(true);
                mViewData.cbSystemSub.setChecked(true);
                break;
            case 7:
                mViewData.cbPrivateSub.setChecked(true);
                mViewData.cbGroupSub.setChecked(true);
                mViewData.cbSystemSub.setChecked(true);
                break;
            default:
                break;
        }
    }
}