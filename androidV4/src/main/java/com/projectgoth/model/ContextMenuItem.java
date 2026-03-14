
package com.projectgoth.model;

public class ContextMenuItem {

    private int    id;
    private int    icon;
    private String title;
    private Object data;

    public ContextMenuItem(String title, int id, Object data) {
        super();
        this.title = title;
        this.id = id;
        this.data = data;
    }

    public ContextMenuItem(int id, int icon, String title, Object data) {
        super();
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getData() {
        return this.data;
    }
}
