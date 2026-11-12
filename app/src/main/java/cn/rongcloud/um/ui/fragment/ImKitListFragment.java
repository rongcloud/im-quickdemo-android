package cn.rongcloud.um.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import cn.rongcloud.um.ui.ImKitDetailActivity;
import cn.rongcloud.um.ui.kit.ImKitCatalog;
import cn.rongcloud.um.ui.widget.FeatureListView;

public final class ImKitListFragment extends Fragment {
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        FeatureListView list = new FeatureListView(requireContext());
        for (ImKitCatalog.Group group : ImKitCatalog.Group.values()) {
            List<FeatureListView.Item> items = new ArrayList<>();
            for (ImKitCatalog.Scene scene : ImKitCatalog.Scene.values()) {
                if (scene.group != group) continue;
                items.add(new FeatureListView.Item(scene.title, scene.subtitle, () -> {
                    Intent intent = new Intent(requireContext(), ImKitDetailActivity.class);
                    intent.putExtra(ImKitDetailActivity.EXTRA_SCENE, scene.name());
                    startActivity(intent);
                }));
            }
            list.addGroup(group.title, items);
        }
        return list;
    }
}
