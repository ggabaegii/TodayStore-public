package com.hyeiin.stock;

/**
 * 매장 목록에 표시할 매장 데이터 모델.
 */
public class StoreItem {

    private String id;
    private String name;
    private String address;

    public StoreItem(String id, String name, String address) {
        this.id      = id;
        this.name    = name;
        this.address = address;
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getAddress() { return address; }

    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setAddress(String address) { this.address = address; }
}
