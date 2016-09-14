package io.straas.android.media.demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.straas.android.sdk.mediacore.demo.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class StraasDemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setContentView(recyclerView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        PackageInfo packageInfo = null;
        PackageManager pm = getPackageManager();
        try {
            packageInfo = pm.getPackageInfo(
                    getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            finish();
        }
        List<DemoItem> demoItems = new ArrayList<>();
        for (ActivityInfo activityInfo : packageInfo != null ? packageInfo.activities : new ActivityInfo[0]) {
            String name = activityInfo.name;
            if (TextUtils.equals(name, getComponentName().getClassName())) {
                continue;
            }
            String title = activityInfo.loadLabel(pm).toString();
            if (TextUtils.isEmpty(title) ||
                    TextUtils.equals(title, getApplicationInfo().loadLabel(pm).toString())) {
                continue;
            }
            demoItems.add(new DemoItem(title, name));
        }
        recyclerView.setAdapter(new Adapter(demoItems));
    }

    private static class DemoItem {
        public String mTitle;
        public String mClassName;

        public DemoItem(String title, String className) {
            mTitle = title;
            mClassName = className;
        }
    }


    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private final List<DemoItem> mDemoItemList;

        static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }

        public Adapter(List<DemoItem> demoItemList) {
            mDemoItemList = demoItemList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.demo_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final DemoItem item = mDemoItemList.get(position);
            holder.mTextView.setText(item.mTitle);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(context.getPackageName(), item.mClassName));
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDemoItemList.size();
        }
    }
}
