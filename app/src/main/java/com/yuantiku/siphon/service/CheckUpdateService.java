package com.yuantiku.siphon.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;

import com.google.gson.*;
import com.yuantiku.siphon.constant.*;
import com.yuantiku.siphon.fragment.*;
import com.yuantiku.siphon.helper.CheckUpdateHelper;

import bwzz.activityCallback.*;
import bwzz.activityReuse.*;
import im.fir.sdk.version.AppVersion;

/**
 * Created by wanghb on 15/8/22.
 */
public class CheckUpdateService extends Service implements CheckUpdateHelper.CheckUpdateCallback, ILauncher {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CheckUpdateHelper.checkUpdate(this);
        return START_NOT_STICKY;
    }

    @Override
    public void onNewVersion(AppVersion appVersion) {
        Bundle bundle = new Bundle();
        bundle.putString(Key.AppVersion, new Gson().toJson(appVersion));
        FragmentPackage fragmentPackage = new FragmentPackage();
        fragmentPackage.setContainer(android.R.id.content)
                .setArgument(bundle)
                .setFragmentClassName(CheckUpdateFragment.class.getName());

        LaunchArgument argument = ReuseIntentBuilder.build()
                .activity(ContainerActivity.class)
                .fragmentPackage(fragmentPackage)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .getLaunchArgumentBuilder(this)
                .get();

        LaunchHelper launchHelper = new LaunchHelper(this);
        launchHelper.launch(argument);
    }

    @Override
    public void onNoNewVersion(AppVersion appVersion) {

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivity(intent);
    }
}