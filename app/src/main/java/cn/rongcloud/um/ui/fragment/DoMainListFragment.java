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
import java.util.Map;
import cn.rongcloud.um.ui.ApiDetailActivity;
import cn.rongcloud.um.ui.api.ApiCatalog;
import cn.rongcloud.um.ui.widget.FeatureListView;

public class DoMainListFragment extends Fragment {
    @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        FeatureListView list = new FeatureListView(requireContext());
        for (Map.Entry<ApiCatalog.Group, List<ApiCatalog.Api>> group : ApiCatalog.groups().entrySet()) {
            List<FeatureListView.Item> items = new ArrayList<>();
            for (ApiCatalog.Api api : group.getValue()) {
                items.add(new FeatureListView.Item(api.title, api.method, () -> {
                    Intent intent = new Intent(requireContext(), ApiDetailActivity.class);
                    intent.putExtra(ApiDetailActivity.EXTRA_API_ID, api.name());
                    startActivity(intent);
                }));
            }
            list.addGroup(group.getKey().title, items);
        }
        return list;
    }
}
