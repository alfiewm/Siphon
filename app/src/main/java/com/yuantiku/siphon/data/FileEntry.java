package com.yuantiku.siphon.data;

import android.support.annotation.NonNull;

import com.yuantiku.siphon.data.apkconfigs.ApkConfig;
import com.yuantiku.siphon.data.apkconfigs.ApkType;
import com.yuantiku.siphon.factory.SingletonFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import proguard.annotation.KeepClassMemberNames;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.PrimaryKey;
import se.emilsjolander.sprinkles.annotations.Table;

/**
 * @author wanghb
 * @date 15/8/15.
 */
@KeepClassMemberNames
@Table(FileEntry.FileEntries)
public class FileEntry extends Model {

    public final static String FileEntries = "FileEntries";
    public final static String ApkConfigId = "apkConfigId";
    public final static String ApkConfigType = "apkConfigType";

    private ApkConfig apkConfig;

    @Column(ApkConfigId)
    private int apkConfigId;

    @Column(ApkConfigType)
    private String apkConfigType;

    @PrimaryKey
    @Column("href")
    public String href;

    @Column("name")
    public String name;

    @Column("date")
    public String date;

    public ApkConfig getApkConfig() {
        if (apkConfig == null) {
            ApkType type = ApkType.valueOf(apkConfigType);
            apkConfig = SingletonFactory.get().getApkConfigModel()
                    .getByIdAndType(apkConfigId, type);
        }
        return apkConfig;
    }

    public void setApkConfig(@NonNull ApkConfig apkConfig) {
        this.apkConfig = apkConfig;
        apkConfigId = apkConfig.getId();
        apkConfigType = apkConfig.getType().name();
    }

    @Override
    protected void beforeSave() {
        super.beforeSave();
        apkConfigId = apkConfig.getId();
        apkConfigType = apkConfig.getType().name();
    }

    @Override
    public String toString() {
        return String.format("%s\n%s", name, formatDate(date));
    }

    private static String formatDate(String dateStr) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");
            return simpleDateFormat1.format(simpleDateFormat.parse(dateStr));
        } catch (ParseException ignored) {
        }
        return dateStr;
    }
}
