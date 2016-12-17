package com.fabiolee.architecture.mvp.ezkl.model.bean;

import io.realm.RealmModel;

/**
 * @author fabiolee
 */
public class UserBean implements RealmModel {
    public String login;
    public int id;
    public String avatarUrl;
}
