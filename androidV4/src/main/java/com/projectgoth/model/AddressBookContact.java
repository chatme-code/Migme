/**
 * Copyright (c) 2013 Project Goth
 *
 * AddressBookContact.java
 * Created Aug 27, 2013, 3:48:59 PM
 */

package com.projectgoth.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a contact that is stored in the user's device address book.
 * @author angelorohit
 */
public class AddressBookContact {
    // A unique identifier for this address book contact.
    private String  id = null;
    
    // The name of this contact.
    private String  name = null;    
    
    // A list of the numbers that have been stored on the user's device for this contact.
    private List<String> numberList = new ArrayList<String>();
    
    // Indicates whether this contact was successfully synced with the server or not.
    private boolean wasNumberSynced = false;

    private List<String> emailList = new ArrayList<String>();

    private boolean wasEmailSynced = false;

    private boolean isDataRowOnlyStoredPhoneNumbers = false;
    
    /**
     * Constructor
     * @param id
     * @param displayName
     */
    public AddressBookContact(final String id, final String displayName) {
        this.id = id;
        this.name = displayName;
        this.numberList = new ArrayList<String>();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the displayName
     */
    public String getName() {
        return name;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setName(final String displayName) {
        this.name = displayName;
    }

    /**
     * @return the numbers
     */
    public List<String> getNumbers() {
        return numberList;
    }

    /**
     * @param numbers the numbers to add
     */
    public void addNumberList(final String numbers) {
        numberList.add(numbers);
    }

    /**
     * @param numberList the number list to set
     */
    public void setNumberList(final List<String> numberList) {
        this.numberList = numberList;
    }

    /**
     * @return the emailList
     */
    public List<String> getEmailList() { return emailList; }

    /**
     * @param email the emails to add
     */
    public void addEmailList(final String email) {
        emailList.add(email);
    }

    /**
     * @param emailList to set
     */
    public void setEmailList(final List<String> emailList) {
        this.emailList = emailList;
    }

    /**
     * return Whether this contact has already been synced with the server
     */
    public boolean getWasNumberSynced() {
        return wasNumberSynced;
    }

    /**
     * @param state true / false value to set this contact as synced / unsynced.
     */
    public void setWasNumberSynced(final boolean state) {
        this.wasNumberSynced = state;
    }

    /**
     * return Whether this contact has already been synced with the server
     */
    public boolean getWasEmailSynced() {
        return wasEmailSynced;
    }
    
    /**
     * @param state true / false value to set this contact as synced / unsynced.
     */
    public void setWasEmailSynced(final boolean state) {
        this.wasEmailSynced = state;
    }

    public void setDataRowOnlyStoredPhoneNumbers(boolean isDataRowOnlyStoredPhoneNumbers) {
        this.isDataRowOnlyStoredPhoneNumbers = isDataRowOnlyStoredPhoneNumbers;
    }

    public boolean hasDataRowOnlyStoredPhoneNumbers() {
        return this.isDataRowOnlyStoredPhoneNumbers;
    }

    public int getNumberListLength() {
        return getListSize(numberList);
    }

    public int getEmailListLength() {
        return getListSize(emailList);
    }

    private int getListSize(List<String> list) {
        int length = 0;
        for (String str : list) {
            length += str.length();
        }
        return length;
    }
}
