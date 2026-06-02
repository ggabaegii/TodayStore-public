package com.hyeiin.stock;

import java.util.ArrayList;
import java.util.List;

/**
 * 루틴 모델.
 *
 * dayHint: 요일/반복 주기 표시용 문자열
 * items  : 루틴에 포함된 체크리스트 항목
 */
public class Routine {

    private String       id;
    private String       name;       // 루틴 이름
    private String       ownerId;    // 루틴 작성자
    private String       dayHint;    // 요일/반복 표시
    private List<String> items;      // 체크리스트 항목 목록
    private boolean      expanded;   // UI 펼침 상태

    public Routine(String id, String name, String ownerId, String dayHint) {
        this.id      = id;
        this.name    = name;
        this.ownerId = ownerId;
        this.dayHint = dayHint;
        this.items   = new ArrayList<>();
    }

    public void addItem(String item)    { items.add(item); }
    public void removeItem(int index)   { if (index < items.size()) items.remove(index); }

    public String       getId()       { return id; }
    public String       getName()     { return name; }
    public String       getOwnerId()  { return ownerId; }
    public String       getDayHint()  { return dayHint; }
    public List<String> getItems()    { return items; }
    public int          getCount()    { return items.size(); }
    public boolean      isExpanded()  { return expanded; }
    public void         setExpanded(boolean e) { expanded = e; }
    public void         setName(String n)      { name = n; }
    public void         setDayHint(String d)   { dayHint = d; }
}
