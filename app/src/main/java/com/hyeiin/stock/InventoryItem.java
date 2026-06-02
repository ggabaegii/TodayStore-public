package com.hyeiin.stock;

/** InventoryItem.java ???ш퀬 ?곗씠??紐⑤뜽 */
public class InventoryItem {
    private String id;
    private String name;
    private String category;
    private float  quantity;
    private String unit;
    private String imageUri; // null?대㈃ 湲곕낯 ?꾩씠肄??ъ슜

    public InventoryItem(String id, String name, String category,
                         float quantity, String unit, String imageUri) {
        this.id       = id;
        this.name     = name;
        this.category = category;
        this.quantity = quantity;
        this.unit     = unit;
        this.imageUri = imageUri;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public float  getQuantity() { return quantity; }
    public String getUnit()     { return unit; }
    public String getImageUri() { return imageUri; }

    public void setName(String name)         { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setQuantity(float quantity) { this.quantity = quantity; }
    public void setUnit(String unit)         { this.unit = unit; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
