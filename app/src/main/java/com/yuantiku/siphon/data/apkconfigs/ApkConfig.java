package com.yuantiku.siphon.data.apkconfigs;

import android.text.TextUtils;

import proguard.annotation.KeepClassMemberNames;

/**
 * Created by wanghb on 15/8/23.
 */
@KeepClassMemberNames
public class ApkConfig {

    int id;
    String name;
    ApkType type;
    String icon;
    int color;
    String packageName;
    String href;

    public ApkConfig() {
    }

    public ApkConfig(ApkConfig other) {
        this.id = other.id;
        this.name = other.name;
        this.type = other.type;
        this.icon = other.icon;
        this.color = other.color;
        this.packageName = other.packageName;
        this.href = other.href;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        if (TextUtils.isEmpty(href)) {
            return name;
        } else {
            return name + href.substring(href.lastIndexOf("/") + 1);
        }
    }

    public ApkType getType() {
        return type;
    }

    public String getListPath() {
        if (TextUtils.isEmpty(href)) {
            return String.format("android/%d/%s", id, type.name());
        } else {
            return href.substring(href.indexOf('=') + 1);
        }
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ApkConfig)) {
            return false;
        }
        ApkConfig oth = (ApkConfig) o;
        return oth.id == id && oth.type == type;
    }

    @Override
    public int hashCode() {
        return id ^ type.hashCode();
    }
}
