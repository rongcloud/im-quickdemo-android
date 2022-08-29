package cn.rongcloud.um.ui.fragment;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.annotation.Nullable;
import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseFragment;
import cn.rongcloud.um.databinding.FragmentDomainListBinding;
import cn.rongcloud.um.ui.DoMainChatRoomActivity;
import cn.rongcloud.um.ui.DoMainGroupActivity;
import cn.rongcloud.um.ui.DoMainMessageActivity;
import cn.rongcloud.um.ui.DoMainUltraGroupActivity;
import cn.rongcloud.um.ui.MainViewModel;

public class DoMainListFragment extends BaseFragment<MainViewModel, FragmentDomainListBinding> {
    @Override
    public int getContentViewId() {
        return R.layout.fragment_domain_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mViewData.contentListview.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.picklist_text,
                new String[]{"消息", "群组", "聊天室", "超级群"}));
        initListener();
    }

    private void initListener() {
        mViewData.contentListview.setOnItemClickListener((adapterView, view, i, l) -> {
            switch (i) {
                case 0:
                    goActivity(DoMainMessageActivity.class);
                    break;
                case 1:
                    goActivity(DoMainGroupActivity.class);
                    break;
                case 2:
                    goActivity(DoMainChatRoomActivity.class);
                    break;
                case 3:
                    goActivity(DoMainUltraGroupActivity.class);
                    break;
            }
        });
    }
}
