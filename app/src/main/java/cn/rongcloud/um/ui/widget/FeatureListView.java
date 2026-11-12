package cn.rongcloud.um.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Shared accordion for SDK catalogs; a single scroll container includes all expanded rows. */
public final class FeatureListView extends ScrollView {
    public static final class Item {
        final String title, subtitle;
        final Runnable open;
        public Item(String title, String subtitle, Runnable open) {
            this.title = title; this.subtitle = subtitle; this.open = open;
        }
    }
    private final LinearLayout content;
    public FeatureListView(Context context) {
        super(context);
        setFillViewport(true);
        setBackgroundColor(Color.rgb(246, 247, 249));
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(12));
        addView(content, new LayoutParams(-1, -2));
    }
    public void addGroup(String title, List<Item> children) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(round(Color.WHITE, dp(8)));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, dp(10));
        content.addView(card, cp);

        LinearLayout header = row(title, 16);
        card.addView(header);
        LinearLayout items = new LinearLayout(getContext());
        items.setOrientation(LinearLayout.VERTICAL);
        card.addView(items);
        items.setVisibility(View.GONE);
        for (Item child : children) {
            LinearLayout item = row(child.title + "\n" + child.subtitle, 14);
            item.setPadding(dp(24), dp(0), dp(20), dp(0));
            item.setOnClickListener(v -> child.open.run());
            items.addView(item, new LinearLayout.LayoutParams(-1, dp(60)));
            View divider = new View(getContext());
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
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(13), dp(20), dp(13));
        String[] lines = value.split("\\n", 2);
        LinearLayout labels = new LinearLayout(getContext());
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
        TextView view = new TextView(getContext());
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
