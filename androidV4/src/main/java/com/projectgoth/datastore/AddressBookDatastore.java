/**
 * Copyright (c) 2013 Project Goth
 *
 * AddressBookController.java
 * Created Aug 27, 2013, 3:47:12 PM
 */

package com.projectgoth.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.enums.CollectedDataType;
import com.projectgoth.blackhole.fusion.packet.FusionPktUploadAddressBookContacts;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.dao.AddressBookContactsDAO;
import com.projectgoth.model.AddressBookContact;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetUploadDataTicketListener;
import com.projectgoth.nemesis.listeners.UploadAddressBookContactsListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.util.AndroidLogger;

/**
 * The purpose of this class is to:
 * - Retrieve a user's addressbook contacts from device.
 * - Organize dispatching of batches of contacts to the server. 
 * 
 * @author angelorohit
 */
public class AddressBookDatastore extends BaseDatastore {
    // All possible states of a batch of contacts that were sent to the server.
    public enum LastSyncState {
        Completed, Failed, InProgress;
    }       
    
    private static final String                 LOG_TAG                             = AndroidLogger.makeLogTag(AddressBookDatastore.class);
    
    // A lock that is obtained when working with any of the caches.
    private static final Object                 CACHE_LOCK                          = new Object();
    
    // The content resolver that will be provided by the activity / fragment that uses this
    // controller.
    private ContentResolver                     mContentResolver                    = null;
    
    // The maximum number of bytes that can be sent to the server per batch.
    private final int                           MaxSizePerBatch                     = 1000;
    
    // A map of unique contacts that were retrieved from the user's address book.
    // The key of the map is the contact id.
    // These contacts may or may not have been synced with the server.
    private Map<String, AddressBookContact>     mRetrievedContactsMap;
    
    // A list of contacts that were last sent to the caller for syncing.
    private List<AddressBookContact>            mAllSyncBatchList;
    
    // The state of the last sync that was sent to the server.
    private LastSyncState                       mLastSyncState;
    
    // Indicates that there are unsynced contacts in the retrieved contacts map.
    private boolean                             mCanSyncContacts;
    
    // Indicates that contacts were fetched from db.
    private boolean                             mDidFetchContactsFromDB;
    
    // The current ticket that can be used for uploading address book data to
    // the server.
    // Data can only be sent with a valid ticket that is first requested from
    // the server.
    private String                              mUploadTicket;
    
    // Indicates whether a response has been received from the server since the
    // last ticket request.
    // This will prevent spurious ticket requests from being sent.
    private boolean                             mCanSendUploadTicketRequest;
    
    // The DAO for saving and fetching address book contacts from DB.
    private AddressBookContactsDAO              mDAO;
    
    // Preferences for saving address book data
    private static final String                 SHARED_PREFS_FILE_NAME_PREFIX       = "AddressBookSharedPrefs";

    // Preference keys
    private static final String                 SHARED_PREFS_ADDRESSBOOK_SYNC_TIME  = "LastAddressBookSyncTime";

    //request server interval
    private static final long                   EXECUTION_DELAY_MS                  = 15000;

    //each upload max limit
    private static final int                    MAX_CHUNK_SIZE                      = 1000;

    // Only sync address book to server again excess this interval
    private static final long                   NEED_SYNC_INTERVAL_MS               = 3 * 24 * 60 * 60 * 1000;

    private static final FusionPktUploadAddressBookContacts.DataType TYPE_EMAIL_ADDRESS
            = FusionPktUploadAddressBookContacts.DataType.EMAIL_ADDRESS;
    private static final FusionPktUploadAddressBookContacts.DataType TYPE_PHONE_NUMBER
            = FusionPktUploadAddressBookContacts.DataType.PHONE_NUMBER;

    private LinkedList<SyncBatchPackage> mSyncPackageQueue;
    //private LinkedList<SyncBatchPackage> mSendingEmailQueue;
    private SyncBatchPackage mCurrentSyncBatchPackage;
    private boolean mIsProcessing;

    private int mRetryTimesCount = 0;
    private final int mRetryTimesLimit = 10;
    
    private AddressBookDatastore() {
        super();
        
        // Initialize the DAO.
        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mDAO = new AddressBookContactsDAO(appCtx);
        }                
    }
    
    @Override
    public void clearData() {
        super.clearData();
        
        if (mDAO != null) {
            mDAO.clearTables();
        }
    }
    
    private static class AddressBookDatastoreHolder {
        static final AddressBookDatastore sINSTANCE = new AddressBookDatastore();
    }

    public static AddressBookDatastore getInstance() {
        return AddressBookDatastoreHolder.sINSTANCE;
    }
    
    @Override
    protected void initData() {
        synchronized (CACHE_LOCK) {
            mRetrievedContactsMap = new HashMap<String, AddressBookContact>();
            mAllSyncBatchList    = new LinkedList<AddressBookContact>();
            mSyncPackageQueue = new LinkedList<SyncBatchPackage>();
            mLastSyncState = LastSyncState.Completed;
            mCanSyncContacts = true;
            mDidFetchContactsFromDB = false;
            mCanSendUploadTicketRequest = true;
            mRetryTimesCount = 0;
            mIsProcessing = false;
            resetUploadDataTicket();
        }
    }           
    
    /**
     * Set the content resolver. The content resolver is typically set by the activity that uses this controller.
     * @param contentResolver   The contentResolver to set
     */
    public void setContentResolver(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }
    
    /**
     * This must be called whenever the last batch of contacts sent to the server was successfully synced.
     * Typically, this is done on receiving a "success" response. 
     * For eg; OK packet for UploadAddressBookContacts packet.
     */
    public void setLastSyncState(final LastSyncState state) {
        synchronized (CACHE_LOCK) {
            mLastSyncState = state;
        }        
    }
    
    /**
     * @return the upload data ticket that may have been received from the server.
     */
    public String getUploadDataTicket() {
        synchronized (CACHE_LOCK) {
            return mUploadTicket;
        }
    }
    
    /**
     * Sets the upload data ticket.
     * @param uploadDataTicket The ticket to set
     */
    public void setUploadDataTicket(final String uploadDataTicket) {
        synchronized (CACHE_LOCK) {
            mUploadTicket = uploadDataTicket;
        }
    }
    
    /**
     * Resets the upload data ticket to a value that indicates that 
     * the upload ticket is not available.
     */
    public void resetUploadDataTicket() {
        setUploadDataTicket(Constants.BLANKSTR);
    }
    
    /**
     * Returns the address book preferences file name
     * as a concatenation of the prefix and logged in user name
     * @return the shared preferences file name
     */
    private final String getSharedPrefsFileName() {
        final String userName = Session.getInstance().getUsername();
        if (userName != null) {
            return SHARED_PREFS_FILE_NAME_PREFIX + userName;
        }
        
        return SHARED_PREFS_FILE_NAME_PREFIX;
    }
    
    /**
     * Indicates whether the contacts have previously been fetched from the device for this particular user.
     * @return true / false
     */
    public boolean isNeedToSyncAddressbook() {
        boolean result = false;
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                getSharedPrefsFileName(), Context.MODE_PRIVATE);
                
        if (sharedPref != null) {
            try {
                long lastSyncTime = sharedPref.getLong(SHARED_PREFS_ADDRESSBOOK_SYNC_TIME, 0);
                long diffTimes = System.currentTimeMillis() - lastSyncTime;
                if (diffTimes > NEED_SYNC_INTERVAL_MS) {
                    result = true;
                }
            }
            catch (ClassCastException e) {
                Logger.error.log(LOG_TAG, e);
            }
        }
        return result;
    }

    private void updateAddressbookSyncTime(){
        // Save information that the client has successfully fetched the user's addressbook.
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                getSharedPrefsFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(SHARED_PREFS_ADDRESSBOOK_SYNC_TIME, System.currentTimeMillis());
        Logger.debug.log(LOG_TAG, "updateAddressbookSyncTime");
        editor.commit();
    }

    
    /**
     * Indicates that there are pending address book contacts waiting to be synced.
     * This depends on 2 things:
     * - Whether there are any more contacts left to be synced.
     * - Whether the last batch of contacts sent to the server was successfully synced.
     */
    public boolean canSyncContacts() {
        synchronized (CACHE_LOCK) {
            return (mCanSyncContacts &&
                    (mLastSyncState == LastSyncState.Completed || mLastSyncState == LastSyncState.Failed) &&
                    !mRetrievedContactsMap.isEmpty());
        }        
    }   
    
    /**
     * Indicates whether the address book data can be uploaded to the server.
     * This is dependent on whether a valid ticket has been received from the server.
     * @return true / false 
     */
    public boolean hasUploadTicket() {
        synchronized (CACHE_LOCK) {
            return (!TextUtils.isEmpty(mUploadTicket));
        }
    }
    
    /**
     * Indicates that a response was received since the last upload ticket request and
     * that a new one can be sent.
     * @return true / false
     */
    public boolean canSendUploadTicketRequest() {
        synchronized (CACHE_LOCK) {
            return mCanSendUploadTicketRequest;
        }
    }
    
    /**
     * state will be set to false as soon as a request is sent to the server.
     * state will be set to true when an OK / Error response is received from the server.
     * @param state The value to set.
     */
    public void setCanSendUploadTicketRequest(final boolean state) {
        synchronized (CACHE_LOCK) {
            mCanSendUploadTicketRequest = state;
        }
    }
    
    /**
     * Fetches all contacts in a user's address book.
     * @return A list of Address book contacts
     * @see com.projectgoth.model.AddressBookContact
     */
    private List<AddressBookContact> getContacts() {
        ArrayList<AddressBookContact> contacts = null;
                
        if (mContentResolver != null) {
            Cursor cur = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
            if (cur != null && cur.getCount() > 0) {
                contacts = new ArrayList<AddressBookContact>();
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (!TextUtils.isEmpty(id)) {
                        AddressBookContact temp = new AddressBookContact(id, name);
                        List<String> emailList = getEmails(id);
                        if (Integer.parseInt(cur.getString(cur
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            List<String> phoneNumberList = getPhoneNumbers(id);
                            if (phoneNumberList != null && !phoneNumberList.isEmpty()) {
                                temp.setNumberList(getPhoneNumbers(id));
                                if (emailList != null && emailList.isEmpty()) {
                                    temp.setDataRowOnlyStoredPhoneNumbers(true);
                                }
                            }
                        }
                        if (emailList != null && !emailList.isEmpty()) {
                            temp.setEmailList(emailList);
                        }
                        contacts.add(temp);
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
        return contacts;
    }
    
    /**
     * Retrieves a list of phone numbers associated with an address book contact
     * @param id    A contact id
     * @return      a list of email strings.
     */
    private List<String> getPhoneNumbers(String id) {
        List<String> phoneNumbers = null;
        if (mContentResolver != null && !TextUtils.isEmpty(id)) {
            Cursor pCur = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[] { id }, null);
            if (pCur != null && pCur.getCount() > 0) {
                int numberColumnIndex = pCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = null;
                HashMap<String, String> temp = new HashMap<String, String>();
                while (pCur.moveToNext()) {
                    number = pCur.getString(numberColumnIndex);
                    if (!TextUtils.isEmpty(number)) {       
                        // Sanitize phone number by removing 
                        // leading + or 0 and any thing other than a digit.
                        number = number.replaceAll("^(0|\\+)+(?!$)|\\D", "");
                        temp.put(number, number);
                        number = null;                      
                    }
                }
                phoneNumbers = new ArrayList<String>(temp.values());
            }
            pCur.close();
        }
        return phoneNumbers;
    }
    
    /**
     * Retrieves a List of emails associated with an address book contact
     * @param id    A contact id
     * @return      a List of email strings.
     */
    private List<String> getEmails(String id) {
        ArrayList<String> emailList = new ArrayList<String>();
        if (mContentResolver != null && !TextUtils.isEmpty(id)) {
            Cursor emailCur = mContentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[] { id }, null);
            if (emailCur != null && emailCur.getCount() > 0) {
                int emailIndex = emailCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                while (emailCur.moveToNext()) {
                    String email = emailCur.getString(emailIndex);
                    if (!TextUtils.isEmpty(email)) {
                        emailList.add(email);
                    }
                }
            }
            emailCur.close();
        }
        return emailList;
    }
    
    /**
     * Begins the process of retrieving contacts from the user's address book.
     * NOTE: This is a long running task and must be executed in a separated thread.
     */
    private void retrieveContacts() {
        Logger.debug.log(LOG_TAG,"start to Retrieve contacts from content provider");
        synchronized (CACHE_LOCK) {
            // If the address book contacts were not retrieved from db before, then do it now.
            if (mDidFetchContactsFromDB == false) {
                fetchAllContactsFromDB();
            }
            
            final List<AddressBookContact> contactList = getContacts();     
            
            if (contactList != null) {          
                for (AddressBookContact retrievedContact : contactList) {               
                    final List<String> phoneNumberList = retrievedContact.getNumbers();
                    final List<String> emailList = retrievedContact.getEmailList();
    
                    // Filter out only those contacts with phone numbers.               
                    if (retrievedContact.getId() != null &&
                            (phoneNumberList != null && phoneNumberList.size() > 0)
                            || (emailList !=null && emailList.size() > 0)) {
                        // See if this contact is already in the list. 
                        // If so, then check whether one or more of the contact's numbers has since been changed.                   
                        AddressBookContact contactInMap = mRetrievedContactsMap.get(retrievedContact.getId());
                        if (contactInMap != null) {
                            for (String number : phoneNumberList) {
                                if (!contactInMap.getNumbers().contains(number)) {
                                    contactInMap.addNumberList(number);
                                    contactInMap.setWasNumberSynced(false);
                                    break;
                                }
                            }
                            for (String email: emailList) {
                                if (!contactInMap.getEmailList().contains(email)) {
                                    contactInMap.addEmailList(email);
                                    contactInMap.setWasEmailSynced(false);
                                    break;
                                }
                            }
                        }
                        else {
                            mRetrievedContactsMap.put(retrievedContact.getId(), retrievedContact);
                        }                   
                    }                           
                }           
            }
            
            if (mRetrievedContactsMap.size() > 0) {
                for (AddressBookContact contact : mRetrievedContactsMap.values()) {
                    if (!contact.getWasNumberSynced() || !contact.getWasEmailSynced()) {
                        saveContactToDB(contact);
                    }
                }
                mCanSyncContacts = true;
            } else {
                mCanSyncContacts = false;
            }
            Logger.debug.log(LOG_TAG, "Successfully retrieved ", mRetrievedContactsMap.size(), " contacts.");
        }
        

    }
    
    /**
     * Compiles and returns the next batch of contacts to be synced. 
     * @return A list of address book contacts to be synced. This value can be either null or an empty list.
     */ 
    private List<AddressBookContact> getAllSyncBatch() {
        synchronized (CACHE_LOCK) {
            // Another batch can only be retrieved when the last sync was completed successfully.

            if (canSyncContacts()) {    
                // If the last sync had failed, then we just send the last sync batch again.
                if (mLastSyncState != LastSyncState.Failed) {
                    mLastSyncState = LastSyncState.InProgress;
                    
                    // If the address book contacts were not retrieved from db before, then do it now.
                    if (mDidFetchContactsFromDB == false) {
                        fetchAllContactsFromDB();
                    }
                    mAllSyncBatchList.clear();
                    for (AddressBookContact contact : mRetrievedContactsMap.values()) {             
                        if (contact.getWasNumberSynced() == false || contact.getWasEmailSynced() == false) {
                            mAllSyncBatchList.add(contact);
                        }               
                    }
                }           
            }
                    
            if (mAllSyncBatchList.size() == 0) {
                Logger.debug.log(LOG_TAG, "No more addressbook contacts to sync");
                mCanSyncContacts = false;
            }
            return mAllSyncBatchList;
        }
    }
    
    /**
     * Fetches all the contacts from DB.
     * This function will overwrite the contents of the retrieved contacts map.
     */
    private void fetchAllContactsFromDB() {
        if (mDAO != null) {
            mRetrievedContactsMap = mDAO.loadAllAddressBookContacts();
            mDidFetchContactsFromDB = true;
        }
    }
    
    /**
     * Saves or updates a contact to DB.
     * @param contact   The contact to be inserted or updated.
     */
    private boolean saveContactToDB(final AddressBookContact contact) {
        if (mDAO != null) {
            return mDAO.saveAddressBookContactToDatabase(contact);
        }
        return false;
    }

    public void startRetrieveContactsAndSyncToServer() {
        if (mIsProcessing) {
            Logger.debug.log(LOG_TAG, "Previous process is still running.. ignore this request!");
            return;
        }

        if(!isNeedToSyncAddressbook()) {
            Logger.debug.log(LOG_TAG, "it does not need to sync within this interval");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                mIsProcessing = true;
                retrieveContacts();
                if (getAddressBookContacts()) {
                    sendAddressBookContactsToServer();
                } else {
                    Logger.debug.log(LOG_TAG, "All contacts have been synced!");
                    mIsProcessing = false;
                }
            }
        }).start();
    }

     /**
     * Get the next batch of unsynced address book contacts and dispatch them to the server
     * via a fusion packet. 
     */
    private boolean getAddressBookContacts() {
        getAllSyncBatch();
        prepareSyncQueue(TYPE_PHONE_NUMBER);
        prepareSyncQueue(TYPE_EMAIL_ADDRESS);
        Logger.debug.log(LOG_TAG, "Wait for sync, queue size : ", mSyncPackageQueue.size());
        return mSyncPackageQueue.size() > 0;
    }

    private void prepareSyncQueue(FusionPktUploadAddressBookContacts.DataType dataType) {
        ArrayList<AddressBookContact> syncBatchContactList = new ArrayList<AddressBookContact>();
        ArrayList<String> syncBatchStringList = new ArrayList<String>();
        int dataStringListLength = 0;
        boolean hasDataRowOnlyStoredPhoneNumber = false;

        if (mAllSyncBatchList != null && !mAllSyncBatchList.isEmpty()) {

            for (AddressBookContact contact : mAllSyncBatchList) {

                List<String> syncSingleContactList;
                boolean isSynced;
                int syncListLength;
                if (dataType == TYPE_PHONE_NUMBER) {
                    syncSingleContactList = contact.getNumbers();
                    isSynced = contact.getWasNumberSynced();
                    syncListLength = contact.getNumberListLength();
                    if (contact.hasDataRowOnlyStoredPhoneNumbers()) {
                        hasDataRowOnlyStoredPhoneNumber = true;
                    }
                } else {
                    syncSingleContactList = contact.getEmailList();
                    isSynced = contact.getWasEmailSynced();
                    syncListLength = contact.getEmailListLength();
                }

                if (syncSingleContactList != null && !syncSingleContactList.isEmpty() && !isSynced) {
                    Logger.debug.log(LOG_TAG, "collect contact data : ", syncSingleContactList);

                    dataStringListLength += syncListLength;
                    if (dataStringListLength > MAX_CHUNK_SIZE) {
                        createSyncPackageToQueue(syncBatchContactList, syncBatchStringList, dataType, hasDataRowOnlyStoredPhoneNumber);
                        syncBatchStringList.clear();
                        syncBatchContactList.clear();
                        hasDataRowOnlyStoredPhoneNumber = false;
                        dataStringListLength = syncListLength;
                    }
                    syncBatchStringList.addAll(syncSingleContactList);
                    syncBatchContactList.add(contact);
                }
            }
            if (syncBatchStringList.size() > 0) {
                createSyncPackageToQueue(syncBatchContactList, syncBatchStringList, dataType, hasDataRowOnlyStoredPhoneNumber);
            }
        }
    }

    private void createSyncPackageToQueue(ArrayList<AddressBookContact> syncBatchContactList, ArrayList<String> syncBatchStringList,
                                          FusionPktUploadAddressBookContacts.DataType dataType, boolean hasDataRowOnlyStoredPhoneNumber ) {
        SyncBatchPackage syncBatchPackage = new SyncBatchPackage(syncBatchStringList, syncBatchContactList, dataType);
        if (dataType == TYPE_PHONE_NUMBER && !hasDataRowOnlyStoredPhoneNumber) {
            syncBatchPackage.setSkipGenerateRecommendation(true);
        } else {
            syncBatchPackage.setSkipGenerateRecommendation(false);
        }
        mSyncPackageQueue.add(syncBatchPackage);
    }

    private synchronized void sendAddressBookContactsToServer(){
        if (mSyncPackageQueue.size() > 0) {
            SyncBatchPackage batchPackage = mSyncPackageQueue.get(0);
            Logger.debug.log(LOG_TAG, "sendAddressBookContactsToServer : ", batchPackage.dataType);

            if (hasUploadTicket()) {
                mCurrentSyncBatchPackage = batchPackage;
                String[] syncBatchList = (String[]) batchPackage.getSyncBatchList().toArray(
                        new String[batchPackage.getSyncBatchList().size()]);
                requestUploadAddressBookData(batchPackage.dataType, syncBatchList, getUploadDataTicket(),
                        batchPackage.getSkipGenerateRecommendation());
            } else if (canSendUploadTicketRequest()) {
                // Need to get another upload ticket.
                setCanSendUploadTicketRequest(false);
                requestGetUploadTicket(CollectedDataType.ADDRESS_BOOK_CONTACT);
            }
        }
    }
    
    private void requestGetUploadTicket(final CollectedDataType uploadTicketType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetUploadDataTicket(new GetUploadDataTicketListener() {

                    @Override
                    public void onUploadDataTicketReceived(final CollectedDataType uploadTicketType, final String uploadDataTicket) {
                        Logger.debug.log(LOG_TAG, "Received upload data ticket: ", uploadDataTicket);
                        mRetryTimesCount = 0;
                        setUploadDataTicket(uploadDataTicket);
                        setCanSendUploadTicketRequest(true);
                        sendAddressBookContactsToServer();
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        Logger.error.log(LOG_TAG, "Failed to receive upload data ticket! -> ", error.getErrorMsg());
                        resetUploadDataTicket();
                        setCanSendUploadTicketRequest(true);
                        retryGetAndSendAddressBookContacts();
                    }
                }, uploadTicketType);
            }
        }
    }
    
    private void requestUploadAddressBookData(final FusionPktUploadAddressBookContacts.DataType contactsType, final String[] data, final String uploadDataTicket, final boolean isSkipped) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {

                requestManager.sendUploadAddressBookData(new UploadAddressBookContactsListener() {

                    @Override
                    public void onUploadAddressBookContactsComplete() {
                        Logger.debug.log(LOG_TAG, "onUploadAddressBookContactsComplete! , skipGenerateRecommendation : " + isSkipped);
                        mRetryTimesCount = 0;
                        setLastSyncState(LastSyncState.Completed);

                        //record these phone number has been synced to server, we dont need to sync again
                        for (AddressBookContact contact : mCurrentSyncBatchPackage.getSyncAddressBookContactList()) {
                            AddressBookContact retrievedContact = mRetrievedContactsMap.get(contact.getId());
                            if (retrievedContact != null) {
                                if (mCurrentSyncBatchPackage.dataType == TYPE_PHONE_NUMBER) {
                                    retrievedContact.setWasNumberSynced(true);
                                } else {
                                    retrievedContact.setWasEmailSynced(true);
                                }
                                saveContactToDB(retrievedContact);
                            }
                        }

                        mSyncPackageQueue.remove(0);
                        if (mSyncPackageQueue.size() > 0) {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    resetUploadDataTicket();
                                    sendAddressBookContactsToServer();
                                }
                            }, EXECUTION_DELAY_MS);
                        } else {
                            mIsProcessing = false;
                            updateAddressbookSyncTime();
                            Logger.debug.log(LOG_TAG, "All queues have already sync!");
                        }
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        Logger.error.log(LOG_TAG, "Failed to sync addressbook contacts with server. -> ", error.getErrorMsg());
                        setLastSyncState(LastSyncState.Failed);
                        resetUploadDataTicket();
                        retryGetAndSendAddressBookContacts();
                    }
                }, contactsType, data, uploadDataTicket, isSkipped);
            }
        }
    }

    private void retryGetAndSendAddressBookContacts() {
        if (mRetryTimesCount < mRetryTimesLimit) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mRetryTimesCount++;
                    sendAddressBookContactsToServer();
                }
            }, EXECUTION_DELAY_MS);
        } else {
            Logger.error.log(LOG_TAG, "server has no response for a long time, stop retry!");
        }

    }

    private class SyncBatchPackage {
        private FusionPktUploadAddressBookContacts.DataType dataType;
        private ArrayList<String> syncBatchList;
        private ArrayList<AddressBookContact> syncContactList;
        private boolean isSkipGenerateRecommendation;

        public SyncBatchPackage(ArrayList<String> syncBatchList, ArrayList<AddressBookContact> syncContact, FusionPktUploadAddressBookContacts.DataType dataType) {
            this.dataType = dataType;
            this.syncBatchList = new ArrayList<String>(syncBatchList);
            this.syncContactList = new ArrayList<AddressBookContact>(syncContact);
            this.isSkipGenerateRecommendation = false;
        }

        public List<String> getSyncBatchList() {
            return syncBatchList;
        }

        public ArrayList<AddressBookContact> getSyncAddressBookContactList() {
            return syncContactList;
        }

        public void setSkipGenerateRecommendation(boolean isSkipGenerateRecommendation) {
            this.isSkipGenerateRecommendation = isSkipGenerateRecommendation;
        }

        public boolean getSkipGenerateRecommendation() {
            return this.isSkipGenerateRecommendation;
        }


    }
}
