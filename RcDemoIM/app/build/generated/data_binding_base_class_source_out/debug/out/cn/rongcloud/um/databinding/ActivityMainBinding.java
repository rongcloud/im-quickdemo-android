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
import cn.rongcloud.um.widget.BottomTabLayout;
import cn.rongcloud.um.widget.BottomTabView;
import cn.rongcloud.um.widget.NoScrollViewPager;
import cn.rongcloud.um.widget.TopBar;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivityMainBinding extends ViewDataBinding {
  @NonNull
  public final BottomTabView btvConsultation;

  @NonNull
  public final BottomTabView btvCourse;

  @NonNull
  public final BottomTabView btvMessage;

  @NonNull
  public final NoScrollViewPager container;

  @NonNull
  public final BottomTabLayout mainBottomBarr;

  @NonNull
  public final TopBar topBar;

  protected ActivityMainBinding(Object _bindingComponent, View _root, int _localFieldCount,
      BottomTabView btvConsultation, BottomTabView btvCourse, BottomTabView btvMessage,
      NoScrollViewPager container, BottomTabLayout mainBottomBarr, TopBar topBar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.btvConsultation = btvConsultation;
    this.btvCourse = btvCourse;
    this.btvMessage = btvMessage;
    this.container = container;
    this.mainBottomBarr = mainBottomBarr;
    this.topBar = topBar;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_main, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityMainBinding>inflateInternal(inflater, R.layout.activity_main, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_main, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityMainBinding>inflateInternal(inflater, R.layout.activity_main, null, false, component);
  }

  public static ActivityMainBinding bind(@NonNull View view) {
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
  public static ActivityMainBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityMainBinding)bind(component, view, R.layout.activity_main);
  }
}