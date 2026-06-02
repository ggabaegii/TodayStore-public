package com.hyeiin.stock;

/** 濡쒓렇?????ъ슜???뺣낫瑜?硫붾え由ъ뿉 蹂닿??섎뒗 ?깃???*/
public class UserSession {
    private static UserSession instance;

    private String uid, name, email, role, storeId, storeName;

    private UserSession() {}

    public static UserSession get() {
        if (instance == null) instance = new UserSession();
        return instance;
    }
    public static void clear() { instance = new UserSession(); }

    public String  getUid()       { return uid != null ? uid : ""; }
    public String  getName()      { return name != null ? name : ""; }
    public String  getEmail()     { return email != null ? email : ""; }
    public String  getRole()      { return role != null ? role : ""; }
    public String  getStoreId()   { return storeId != null ? storeId : ""; }
    public String  getStoreName() { return storeName != null ? storeName : ""; }
    public boolean isOwner()      { return "owner".equals(role); }

    public UserSession setUid(String v)       { uid = v;       return this; }
    public UserSession setName(String v)      { name = v;      return this; }
    public UserSession setEmail(String v)     { email = v;     return this; }
    public UserSession setRole(String v)      { role = v;      return this; }
    public UserSession setStoreId(String v)   { storeId = v;   return this; }
    public UserSession setStoreName(String v) { storeName = v; return this; }
}
