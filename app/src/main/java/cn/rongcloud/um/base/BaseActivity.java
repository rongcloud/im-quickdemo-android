package cn.rongcloud.um.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


import io.rong.imkit.picture.tools.ToastUtils;

/**
 * Activity基类
 *
 * @param <Vdb> Ui控制
 */
public abstract class BaseActivity<Vdb extends ViewDataBinding> extends AppCompatActivity {
    public abstract int getContentViewId();

    public abstract void setView();//设置UI相关

    public void setData() { }

    public Vdb mViewData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if((getIntent().getFlags()&Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)!=0){
                finish();
                return;
        }
        mViewData = DataBindingUtil.setContentView(this, getContentViewId());
        mViewData.setLifecycleOwner(this);

        this.setView();
        this.setData();
        cn.rongcloud.um.ui.widget.SystemBars.apply(this, systemBarSurfaceColor());
    }

    protected int systemBarSurfaceColor() { return android.graphics.Color.WHITE; }

    protected void goActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(this, clazz));
    }

    protected void toast(String msg) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) ToastUtils.s(this, msg);
        });
    }

    @Override
    protected void onDestroy() {
        if (mViewData != null) {
            mViewData.unbind();
        }
        super.onDestroy();
    }
}
