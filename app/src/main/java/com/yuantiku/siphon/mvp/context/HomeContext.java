package com.yuantiku.siphon.mvp.context;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import bwzz.activityCallback.LaunchArgument;

import com.yuantiku.siphon.R;
import com.yuantiku.siphon.constant.Key;
import com.yuantiku.siphon.data.apkconfigs.ApkConfig;
import com.yuantiku.siphon.fragment.AboutFragment;
import com.yuantiku.siphon.helper.ApkHelper;
import com.yuantiku.siphon.helper.JsonHelper;
import com.yuantiku.siphon.helper.LaunchHelper;
import com.yuantiku.siphon.mvp.imodel.IFileModel;
import com.yuantiku.siphon.mvp.presenter.HomePresenter;
import com.yuantiku.siphon.mvp.presenter.IPresenterManager;
import com.yuantiku.siphon.mvp.presenter.PresenterFactory;
import com.yuantiku.siphon.mvp.viewmodel.HomeViewModel;

/**
 * Created by wanghb on 15/9/3.
 */
public class HomeContext extends BaseContext implements HomeViewModel.IHandler,
        HomePresenter.IHandler {

    private HomePresenter homePresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void createPresenters(@NonNull IPresenterManager presenterManager) {
        super.createPresenters(presenterManager);
        homePresenter = PresenterFactory.createHomePresenter(presenterManager);
        homePresenter.setHandler(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_check_update:
                checkUpdate();
                break;
            case R.id.action_select_apk:
                selectApplication();
                break;
            case R.id.action_test_tutor:
                testTutor();
                break;
            case R.id.action_about:
                about();
                break;
            case R.id.action_open_tutor_settings:
                openTutorSetting();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openTutorSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:com.yuantiku.tutor"));
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        HomeViewModel homeViewModel = new HomeViewModel(view, this);
        homePresenter.attachView(homeViewModel);
        return view;
    }

    @Override
    public void onOneStep() {
        homePresenter.oneStep();
    }

    @Override
    public void onSync() {
        homePresenter.sync();
    }

    @Override
    public void onDownloadAndInstall() {
        homePresenter.downloadInstall();
    }

    @Override
    public void onIcon(ApkConfig apkConfig) {
        Bundle bundle = new Bundle();
        bundle.putString(Key.ApkConfig, JsonHelper.json(apkConfig));
        LaunchArgument argument = LaunchHelper.createArgument(FileEntriesContext.class,
                getActivity(), bundle);
        launch(argument);
    }

    @Override
    public void installApkFile(IFileModel apkFile) {
        try {
            ApkHelper.installApk(getActivity(), apkFile);
        } catch (Exception e) {
            L.e("安转失败了，是人品问题。");
        }
    }

    private void selectApplication() {
        LaunchArgument argument = LaunchHelper.createArgument(AppListContext.class, getActivity(),
                (resultCode, data) -> {
                    if (resultCode == Activity.RESULT_OK) {
                        String acs = data.getStringExtra(Key.ApkConfig);
                        ApkConfig apkConfig = JsonHelper.json(acs, ApkConfig.class);
                        homePresenter.updateApkConfig(apkConfig);
                        // TODO : view
                getActivity().setTitle(String.format("%s %s", apkConfig.getName(),
                        apkConfig.getType()));
            }
            return true;
        });
        launch(argument);
    }

    private void checkUpdate() {
        LaunchArgument argument = LaunchHelper.createArgument(CheckUpdateContext.class,
                getActivity());
        launch(argument);
    }

    private void testTutor() {
        LaunchArgument argument = LaunchHelper.createArgument(TutorEntryTestContext.class,
                getActivity());
        launch(argument);
    }

    private void about() {
        LaunchArgument argument = LaunchHelper.createArgument(AboutFragment.class,
                getActivity());
        launch(argument);
    }

}
