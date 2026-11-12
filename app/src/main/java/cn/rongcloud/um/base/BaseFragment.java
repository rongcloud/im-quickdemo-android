package cn.rongcloud.um.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;


import io.rong.imkit.picture.tools.ToastUtils;

public abstract class BaseFragment<Vdb extends ViewDataBinding> extends Fragment {
    public Vdb mViewData;

    public abstract int getContentViewId();
    public abstract void initView(@Nullable Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewData = DataBindingUtil.inflate(inflater, getContentViewId(), container, false);
        return mViewData.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewData.setLifecycleOwner(getViewLifecycleOwner());
        initView(savedInstanceState);
    }

    /** Dispatch only while the exact view binding that started the request is still alive. */
    protected void withView(Vdb expected, Runnable action) {
        if (expected == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (isAdded() && mViewData == expected) action.run();
        });
    }

    protected void goActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(getActivity(), clazz));
    }

    protected void toast(String msg) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (isAdded() && !activity.isFinishing() && !activity.isDestroyed()) ToastUtils.s(activity, msg);
        });
    }

    @Override
    public void onDestroyView() {
        if (mViewData != null) {
            mViewData.unbind();
            mViewData = null;
        }
        super.onDestroyView();
    }
}
