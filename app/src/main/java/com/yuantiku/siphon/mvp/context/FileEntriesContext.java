package com.yuantiku.siphon.mvp.context;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yuantiku.siphon.app.ApplicationComponentProvider;
import com.yuantiku.siphon.constant.Key;
import com.yuantiku.siphon.data.FileEntry;
import com.yuantiku.siphon.data.apkconfigs.ApkConfig;
import com.yuantiku.siphon.helper.ApkHelper;
import com.yuantiku.siphon.helper.JsonHelper;
import com.yuantiku.siphon.helper.LaunchHelper;
import com.yuantiku.siphon.mvp.imodel.IFileModel;
import com.yuantiku.siphon.mvp.model.FileModelFactory;
import com.yuantiku.siphon.mvp.presenter.FileEntriesListPresenter;
import com.yuantiku.siphon.mvp.presenter.IPresenterManager;
import com.yuantiku.siphon.mvp.presenter.PresenterFactory;
import com.yuantiku.siphon.mvp.viewmodel.FileEntriesViewModel;

import javax.inject.Inject;

import bwzz.activityCallback.LaunchArgument;

/**
 * Created by wanghb on 15/9/5.
 */
public class FileEntriesContext extends BaseContext implements FileEntriesViewModel.IHandler {

    private FileEntriesListPresenter fileEntriesListPresenter;
    @Inject
    FileModelFactory fileModelFactory;

    private ApkConfig apkConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationComponentProvider.getApplicationComponent().inject(this);
    }

    @Override
    protected void createPresenters(@NonNull IPresenterManager presenterManager) {
        super.createPresenters(presenterManager);
        apkConfig = JsonHelper.json(getArguments().getString(Key.ApkConfig),
                ApkConfig.class);
        fileEntriesListPresenter = PresenterFactory.createFileEntriesListPresenter(
                presenterManager, apkConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        FileEntriesViewModel fileEntriesViewModel = new FileEntriesViewModel(fileModelFactory, this);
        View view = fileEntriesViewModel.onCreateView(inflater, container, savedInstanceState);
        fileEntriesListPresenter.attachView(fileEntriesViewModel);
        getActivity().setTitle(String.format("%s %s", apkConfig.getName(), apkConfig.getType()));
        return view;
    }

    @Override
    public void clickFileEntry(FileEntry fileEntry) {
        IFileModel fileModel = fileModelFactory.createFileModel(fileEntry);
        if (fileModel.exists()) {
            ApkHelper.installApk(getActivity(), fileModel);
        } else if (fileEntry.href.endsWith(".apk")) {
            fileEntriesListPresenter.download(null, fileEntry);
        } else {
            Bundle bundle = new Bundle();
            ApkConfig dirConfig = new ApkConfig(apkConfig);
            dirConfig.setHref(fileEntry.href);
            bundle.putString(Key.ApkConfig, JsonHelper.json(dirConfig));
            LaunchArgument argument = LaunchHelper.createArgument(FileEntriesContext.class, getActivity(), bundle);
            launch(argument);
        }
    }

    @Override
    public void longClickFileEntry(FileEntry fileEntry) {
        // TODO: 16/8/4 [by wanghb.]
    }

    @Override
    public void refresh() {
        fileEntriesListPresenter.sync(null);
    }
}
