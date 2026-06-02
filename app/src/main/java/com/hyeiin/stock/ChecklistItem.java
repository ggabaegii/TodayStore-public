package com.hyeiin.stock;

/**
 * 체크리스트 항목 모델.
 *
 * type:
 *   GLOBAL   매장 공용 체크리스트
 *   PERSONAL 개인 체크리스트
 *
 * canUncheck:
 *   사장은 모든 항목의 완료 취소 가능
 *   직원은 본인이 완료한 항목만 취소 가능
 */
public class ChecklistItem {

    public enum Type { GLOBAL, PERSONAL }

    private String  id;
    private String  task;
    private Type    type;
    private boolean done;
    private String  doneBy;
    private String  doneByUserId;
    private String  doneAt;
    private String  date;      // "yyyy-MM-dd"
    private String  ownerId;   // PERSONAL 소유자
    private String  routineId; // 루틴에서 생성되면 루틴 ID, 직접 생성이면 null

    private ChecklistItem() {}

    public static ChecklistItem createGlobal(String id, String task, String date) {
        ChecklistItem i = new ChecklistItem();
        i.id = id; i.task = task; i.type = Type.GLOBAL; i.date = date;
        return i;
    }

    public static ChecklistItem createPersonal(String id, String task,
                                               String date, String ownerId,
                                               String routineId) {
        ChecklistItem i = new ChecklistItem();
        i.id = id; i.task = task; i.type = Type.PERSONAL;
        i.date = date; i.ownerId = ownerId; i.routineId = routineId;
        return i;
    }

    public void markDone(String byName, String byUserId, String atTime) {
        done = true; doneBy = byName; doneByUserId = byUserId; doneAt = atTime;
    }

    public void markUndone() {
        done = false; doneBy = null; doneByUserId = null; doneAt = null;
    }

    /** 완료 취소 가능 여부 */
    public boolean canUncheck(String currentUserId, boolean isOwner) {
        if (!done) return false;
        if (isOwner) return true;
        return currentUserId.equals(doneByUserId);
    }

    public String  getId()           { return id; }
    public String  getTask()         { return task; }
    public Type    getType()         { return type; }
    public boolean isDone()          { return done; }
    public String  getDoneBy()       { return doneBy; }
    public String  getDoneByUserId() { return doneByUserId; }
    public String  getDoneAt()       { return doneAt; }
    public String  getDate()         { return date; }
    public String  getOwnerId()      { return ownerId; }
    public String  getRoutineId()    { return routineId; }
    public void    setTask(String t) { task = t; }
}
