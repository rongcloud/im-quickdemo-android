package cn.rongcloud.um.ui.fragment;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseFragment;
import cn.rongcloud.um.databinding.FragmentDomainListBinding;
import cn.rongcloud.um.ui.ApiDetailActivity;
import cn.rongcloud.um.ui.MainViewModel;

import cn.rongcloud.um.ui.api.ApiCatalog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class DoMainListFragment extends BaseFragment<MainViewModel, FragmentDomainListBinding> {

    @Override
    public int getContentViewId() {
        return R.layout.fragment_domain_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));
        Map<String, List<ApiCatalog.Api>> groups = new LinkedHashMap<>();
        for (ApiCatalog.Api api : ApiCatalog.Api.values()) {
            List<ApiCatalog.Api> items = groups.get(api.category);
            if (items == null) {
                items = new ArrayList<>();
                groups.put(api.category, items);
            }
            items.add(api);
        }
        for (Map.Entry<String, List<ApiCatalog.Api>> group : groups.entrySet()) {
            addCard(root, group.getKey(), group.getValue());
        }
        mViewData.contentListview.setVisibility(View.GONE);
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setFillViewport(true);
        scrollView.addView(root, new ScrollView.LayoutParams(-1, -2));
        ((android.view.ViewGroup) mViewData.contentListview.getParent()).addView(scrollView, 0,
                new android.widget.RelativeLayout.LayoutParams(-1, -1));
    }

    private void addCard(LinearLayout parent, String title, List<ApiCatalog.Api> children) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(round(Color.WHITE, dp(8)));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, dp(10));
        parent.addView(card, cp);

        LinearLayout header = row(title, 16);
        card.addView(header);
        LinearLayout items = new LinearLayout(requireContext());
        items.setOrientation(LinearLayout.VERTICAL);
        card.addView(items);
        items.setVisibility(View.GONE);
        for (ApiCatalog.Api child : children) {
            LinearLayout item = row(child.title + "\n" + child.method, 14);
            item.setPadding(dp(24), dp(0), dp(20), dp(0));
            item.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ApiDetailActivity.class);
                intent.putExtra(ApiDetailActivity.EXTRA_TITLE, child.title);
                intent.putExtra(ApiDetailActivity.EXTRA_CATEGORY, title);
                intent.putExtra(ApiDetailActivity.EXTRA_API_ID, child.name());
                startActivity(intent);
            });
            items.addView(item, new LinearLayout.LayoutParams(-1, dp(60)));
            View divider = new View(requireContext());
            divider.setBackgroundColor(Color.rgb(225, 225, 225));
            items.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        }
        header.setOnClickListener(v -> {
            boolean show = items.getVisibility() != View.VISIBLE;
            items.setVisibility(show ? View.VISIBLE : View.GONE);
            ((TextView) header.getChildAt(1)).setText(show ? "⌄" : "›");
        });
    }

    private LinearLayout row(String value, int size) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(13), dp(20), dp(13));
        String[] lines = value.split("\\n", 2);
        LinearLayout labels = new LinearLayout(requireContext());
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView label = text(lines[0], size, Color.rgb(35, 35, 35));
        labels.addView(label);
        if (lines.length > 1) {
            TextView api = text(lines[1], 11, Color.GRAY);
            api.setSingleLine(true);
            api.setEllipsize(android.text.TextUtils.TruncateAt.END);
            labels.addView(api);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = text("›", size + 4, Color.DKGRAY);
        arrow.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), -1));
        return row;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), Color.rgb(235, 235, 240));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }
}
