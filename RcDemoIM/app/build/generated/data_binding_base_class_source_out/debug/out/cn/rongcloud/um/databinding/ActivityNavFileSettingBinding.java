// Generated by data binding compiler. Do not edit!
package cn.rongcloud.um.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import cn.rongcloud.um.R;
import cn.rongcloud.um.widget.InputTextLinearLayout;
import cn.rongcloud.um.widget.TopBar;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivityNavFileSettingBinding extends ViewDataBinding {
  @NonNull
  public final InputTextLinearLayout inputTextFile;

  @NonNull
  public final InputTextLinearLayout inputTextNav;

  @NonNull
  public final TopBar topBar;

  protected ActivityNavFileSettingBinding(Object _bindingComponent, View _root,
      int _localFieldCount, InputTextLinearLayout inputTextFile, InputTextLinearLayout inputTextNav,
      TopBar topBar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.inputTextFile = inputTextFile;
    this.inputTextNav = inputTextNav;
    this.topBar = topBar;
  }

  @NonNull
  public static ActivityNavFileSettingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_nav_file_setting, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityNavFileSettingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityNavFileSettingBinding>inflateInternal(inflater, R.layout.activity_nav_file_setting, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityNavFileSettingBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_nav_file_setting, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityNavFileSettingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityNavFileSettingBinding>inflateInternal(inflater, R.layout.activity_nav_file_setting, null, false, component);
  }

  public static ActivityNavFileSettingBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static ActivityNavFileSettingBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityNavFileSettingBinding)bind(component, view, R.layout.activity_nav_file_setting);
  }
}
