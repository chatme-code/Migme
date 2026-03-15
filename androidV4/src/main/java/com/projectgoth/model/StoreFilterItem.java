package com.projectgoth.model;

public class StoreFilterItem {

    private Integer         id;
    private String          name;
    private StoreSortType   storeSortType;
    private boolean         isSelected;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StoreSortType getStoreSortType() {
        return storeSortType;
    }

    public void setStoreSortType(StoreSortType storeSortType) {
        this.storeSortType = storeSortType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }


}
