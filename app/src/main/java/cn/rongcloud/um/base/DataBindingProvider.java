package cn.rongcloud.um.base;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
public interface DataBindingProvider {

     @LayoutRes
    int getContentViewId();

     void initView(@Nullable Bundle savedInstanceState);

}
