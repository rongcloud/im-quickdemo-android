package cn.rongcloud.um.ui;

import android.view.View;

import cn.rongcloud.um.R;
import cn.rongcloud.um.auth.LoginController;
import cn.rongcloud.um.auth.LoginSession;
import cn.rongcloud.um.auth.RongLoginClient;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.base.MyApplication;
import cn.rongcloud.um.config.DemoConfig;
import cn.rongcloud.um.databinding.ActivityLoginBinding;

/**
 * 登录页
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    private boolean connecting;
    private LoginController controller;

    @Override public int getContentViewId() { return R.layout.activity_login; }

    @Override public void setView() {
        LoginSession session = MyApplication.loginSession();
        android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
        controller = new LoginController(new RongLoginClient(getApplication()), session,
                command -> main.post(command), new LoginController.View() {
            @Override public void connecting(String userId) {
                setConnecting(true);
                showStatus(getString(R.string.login_connecting_user, userId), false);
            }
            @Override public void failed(String message) {
                setConnecting(false);
                showStatus(message, true);
            }
            @Override public void signedIn() {
                connecting = false;
                goActivity(MainActivity.class);
                finish();
            }
        });
        android.widget.ArrayAdapter<DemoConfig.User> adapter = new android.widget.ArrayAdapter<>(
                this, R.layout.item_login_user, DemoConfig.USERS);
        adapter.setDropDownViewResource(R.layout.item_login_user_dropdown);
        mViewData.userSpinner.setAdapter(adapter);
        String previous = session.lastUserId();
        for (int i = 0; i < DemoConfig.USERS.size(); i++) {
            if (DemoConfig.USERS.get(i) != null && DemoConfig.USERS.get(i).userId.equals(previous)) mViewData.userSpinner.setSelection(i);
        }
        mViewData.settingsButton.setOnClickListener(v -> goActivity(SettingActivity.class));
        mViewData.loginButton.setOnClickListener(v -> login());
        mViewData.userSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                showConfigurationState();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { showConfigurationState(); }
        });
        showConfigurationState();
    }


    private DemoConfig.User selectedUser() {
        return (DemoConfig.User) mViewData.userSpinner.getSelectedItem();
    }

    @Override protected int systemBarSurfaceColor() {
        return androidx.core.content.ContextCompat.getColor(this, R.color.login_background);
    }

    private void showConfigurationState() {
        if (connecting) return;
        String error = DemoConfig.validationError(DemoConfig.APP_KEY, DemoConfig.USERS, selectedUser());
        mViewData.loginButton.setEnabled(error == null);
        showStatus(error == null ? getString(R.string.login_ready) : error, error != null);
    }

    private void showStatus(String text, boolean error) {
        mViewData.loginStatus.setText(text);
        mViewData.loginStatus.setTextColor(android.graphics.Color.parseColor(error ? "#B42318" : "#667085"));
    }

    private void setConnecting(boolean value) {
        connecting = value;
        mViewData.userSpinner.setEnabled(!value);
        mViewData.settingsButton.setEnabled(!value);
        mViewData.loginButton.setEnabled(!value);
        mViewData.loginButton.setText(value ? R.string.login_connecting : R.string.login_connect);
        mViewData.loginProgress.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    private void login() {
        controller.login(DemoConfig.APP_KEY, DemoConfig.USERS, selectedUser());
    }

    @Override protected void onDestroy() {
        if (controller != null) controller.close();
        super.onDestroy();
    }
}
