package com.hyeiin.stock;

/** AnnouncementItem.java ???꾨떖?ы빆/?뱀씠?ы빆 ?곗씠??紐⑤뜽 */
public class AnnouncementItem {

    public enum Type { ANNOUNCEMENT, SPECIAL }

    private String id;
    private String title;
    private String content;
    private String author;
    private String authorId;
    private String dateTime;   // "2025.01.20  09:41"
    private String dateShort;  // "01.20"
    private Type   type;

    public AnnouncementItem(String id, String title, String content,
                            String author, String authorId,
                            String dateTime, String dateShort, Type type) {
        this.id        = id;
        this.title     = title;
        this.content   = content;
        this.author    = author;
        this.authorId  = authorId;
        this.dateTime  = dateTime;
        this.dateShort = dateShort;
        this.type      = type;
    }

    public String getId()        { return id; }
    public String getTitle()     { return title; }
    public String getContent()   { return content; }
    public String getAuthor()    { return author; }
    public String getAuthorId()  { return authorId; }
    public String getDateTime()  { return dateTime; }
    public String getDateShort() { return dateShort; }
    public Type   getType()      { return type; }
    public boolean isSpecial()   { return type == Type.SPECIAL; }
}
