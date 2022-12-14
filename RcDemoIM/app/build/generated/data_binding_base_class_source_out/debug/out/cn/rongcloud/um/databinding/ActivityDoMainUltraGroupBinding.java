// Generated by data binding compiler. Do not edit!
package cn.rongcloud.um.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import cn.rongcloud.um.R;
import cn.rongcloud.um.widget.InputTextLinearLayout;
import cn.rongcloud.um.widget.TopBar;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivityDoMainUltraGroupBinding extends ViewDataBinding {
  @NonNull
  public final ListView contentListview;

  @NonNull
  public final InputTextLinearLayout inputTextChannelId;

  @NonNull
  public final InputTextLinearLayout inputTextUltraGroupId;

  @NonNull
  public final TopBar topBar;

  protected ActivityDoMainUltraGroupBinding(Object _bindingComponent, View _root,
      int _localFieldCount, ListView contentListview, InputTextLinearLayout inputTextChannelId,
      InputTextLinearLayout inputTextUltraGroupId, TopBar topBar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.contentListview = contentListview;
    this.inputTextChannelId = inputTextChannelId;
    this.inputTextUltraGroupId = inputTextUltraGroupId;
    this.topBar = topBar;
  }

  @NonNull
  public static ActivityDoMainUltraGroupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_do_main_ultra_group, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityDoMainUltraGroupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityDoMainUltraGroupBinding>inflateInternal(inflater, R.layout.activity_do_main_ultra_group, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityDoMainUltraGroupBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_do_main_ultra_group, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityDoMainUltraGroupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityDoMainUltraGroupBinding>inflateInternal(inflater, R.layout.activity_do_main_ultra_group, null, false, component);
  }

  public static ActivityDoMainUltraGroupBinding bind(@NonNull View view) {
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
  public static ActivityDoMainUltraGroupBinding bind(@NonNull View view,
      @Nullable Object component) {
    return (ActivityDoMainUltraGroupBinding)bind(component, view, R.layout.activity_do_main_ultra_group);
  }
}
