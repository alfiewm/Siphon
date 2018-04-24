package com.yuantiku.siphon.mvp.viewmodel;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yuantiku.siphon.R;
import com.yuantiku.siphon.data.FileEntry;
import com.yuantiku.siphon.data.apkconfigs.ApkConfig;
import com.yuantiku.siphon.factory.EmptyObjectFactory;
import com.yuantiku.siphon.mvp.imodel.IFileModel;
import com.yuantiku.siphon.mvp.model.FileModelFactory;
import com.yuantiku.siphon.mvp.presenter.FileEntriesListPresenter.IView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwzz.taskmanager.TaskException;

/**
 * Created by wanghb on 15/9/5.
 */
public class FileEntriesViewModel extends BaseViewModel implements IView,
        OnItemClickListener, OnItemLongClickListener, TabLayout.OnTabSelectedListener {

    public interface IHandler {

        void clickFileEntry(FileEntry fileEntry);

        void longClickFileEntry(FileEntry fileEntry);

        void refresh();
    }

    enum TabTag {
        all, guess,
    }

    private IHandler handler = EmptyObjectFactory.createEmptyObject(IHandler.class);

    private SwipeRefreshLayout swipeRefreshLayout;

    private final FileEntriesAdapter adapter;

    private ListView listView;

    public FileEntriesViewModel(FileModelFactory fileModelFactory, IHandler handler) {
        adapter = new FileEntriesAdapter(fileModelFactory);
        this.handler = handler;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_file_entries, container, false);
        TabLayout tabLayout = root.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("所有").setTag(TabTag.all));
        tabLayout.addTab(tabLayout.newTab().setText("猜你想要").setTag(TabTag.guess));
        tabLayout.setOnTabSelectedListener(this);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        listView = root.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> handler.refresh());
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        return root;
    }

    @Override
    public void renderSyncStart(ApkConfig apkConfig) {

    }

    @Override
    public void renderSyncFailed(ApkConfig apkConfig, TaskException e) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void renderSyncSuccess(ApkConfig apkConfig, List<FileEntry> fileEntries) {
        adapter.update(fileEntries);
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public void renderDownloadStart(FileEntry fileEntry) {
    }

    @Override
    public void renderDownloadProgress(FileEntry fileEntry, float percent) {
        adapter.updateItemProgress(fileEntry, percent);
    }

    @Override
    public void renderDownloadSuccess(FileEntry fileEntry, IFileModel result) {
        adapter.updateItemDownloadSuccess(fileEntry);
    }

    @Override
    public void renderDownloadFailed(FileEntry fileEntry, TaskException e) {
        adapter.updateItemDownloadFailed(fileEntry);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileEntry fileEntry = (FileEntry) adapter.getItem(position);
        handler.clickFileEntry(fileEntry);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileEntry fileEntry = (FileEntry) adapter.getItem(position);
        handler.longClickFileEntry(fileEntry);
        return true;
    }

    private Map<TabTag, Parcelable> listViewStateMap = new HashMap<>();

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getTag() == TabTag.all) {
            listViewStateMap.put(TabTag.guess, listView.onSaveInstanceState());
        } else {
            listViewStateMap.put(TabTag.all, listView.onSaveInstanceState());
        }
        adapter.setTabTag((TabTag) tab.getTag());
        if (listViewStateMap.get(tab.getTag()) != null) {
            listView.onRestoreInstanceState(listViewStateMap.get(tab.getTag()));
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private static class FileEntriesAdapter extends BaseAdapter {

        private Map<String, View> entry2View = new HashMap<>();

        private Map<TabTag, List<FileEntry>> entriesMap = new HashMap<>();

        private final FileModelFactory fileModelFactory;

        private TabTag tabTag = TabTag.all;

        public FileEntriesAdapter(FileModelFactory fileModelFactory) {
            this.fileModelFactory = fileModelFactory;
        }

        public void setTabTag(TabTag tabTag) {
            this.tabTag = tabTag;
            entry2View.clear();
            notifyDataSetChanged();
        }

        public void update(List<FileEntry> fileEntries) {
            entriesMap.put(TabTag.all, fileEntries);
            List<FileEntry> entries = new ArrayList<>();
            Set<String> foundVersions = new HashSet<>();
            for (FileEntry entry : fileEntries) {
                String version = parseVersion(entry);
                if (TextUtils.isEmpty(version) || foundVersions.contains(version)) {
                    continue;
                }
                foundVersions.add(version);
                entries.add(entry);
            }
            entriesMap.put(TabTag.guess, entries);
            entry2View.clear();
            notifyDataSetChanged();
        }

        private String parseVersion(FileEntry entry) {
            if (entry == null) {
                return null;
            }
            String[] sps = entry.name.split("-");
            if (sps.length < 2) {
                return "";
            }
            return sps[1];
        }

        public void updateItemProgress(FileEntry fileEntry, float progress) {
            TextView textView = getItemView(fileEntry);
            if (textView != null) {
                textView.setText(String.format("%s\n%.2f%%", fileEntry.toString(), progress));
                textView.setTextColor(Color.parseColor("#0056cc"));
            }
        }

        public void updateItemDownloadSuccess(FileEntry fileEntry) {
            TextView textView = getItemView(fileEntry);
            if (textView == null) {
                return;
            }
            textView.setText(fileEntry.toString());
            textView.setTextColor(Color.parseColor("#239609"));
        }

        public void updateItemDownloadFailed(FileEntry fileEntry) {
            TextView textView = getItemView(fileEntry);
            if (textView == null) {
                return;
            }
            textView.setText(fileEntry.name);
            textView.setTextColor(Color.RED);
        }

        private TextView getItemView(FileEntry fileEntry) {
            return (TextView) entry2View.get(fileEntry.name);
        }

        @Override
        public int getCount() {
            if (!entriesMap.containsKey(tabTag)) {
                return 0;
            }
            return entriesMap.get(tabTag).size();
        }

        @Override
        public Object getItem(int position) {
            return entriesMap.get(tabTag).get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = new TextView(parent.getContext());
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setTextSize(18);
                textView.setPadding(dp2Px(parent.getContext(), 16), 0, 0, 0);
                textView.setMinHeight(dp2Px(parent.getContext(), 90));
            }
            FileEntry fileEntry = (FileEntry) getItem(position);
            textView.setText(fileEntry.toString());

            IFileModel fileModel = fileModelFactory.createFileModel(fileEntry);
            textView.setTextColor(fileModel.exists() ? Color.parseColor("#239609") : Color.BLACK);

            String key = (String) textView.getTag();
            if (!TextUtils.isEmpty(key)) {
                entry2View.remove(key);
            }
            textView.setTag(fileEntry.name);
            entry2View.put(fileEntry.name, textView);

            return textView;
        }

        private int dp2Px(Context context, int dp) {
            return (int) (context.getResources().getDisplayMetrics().density * dp);
        }
    }
}
