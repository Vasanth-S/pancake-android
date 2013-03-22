package com.imaginea.android.sugarcrm;

import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.imaginea.android.sugarcrm.util.RestUtil;
import com.imaginea.android.sugarcrm.util.SugarCrmException;
import com.imaginea.android.sugarcrm.util.Util;

/**
 * <p>
 * SugarCrmApp class.
 * </p>
 * 
 */
public class SugarCrmApp extends Application {

    // easy ref to App instance for classes which do not have access to Activity/Service context
    public static Application app = null;

    /*
     * sessionId is obtained after successful login into the Sugar CRM instance Now, sessionId will
     * be available to the entire application Access the sessionId from any part of the application
     * as follows : SugarCrmApp app = ((SugarCrmApp) getApplication()); app.getSessionId();
     */
    private String mSessionId;

    private long mLastLoginTime = 0;

    private Map<String, Map<String, String>> moduleSortOrder = new HashMap<String, Map<String, String>>();

    private static final String LOG_TAG = SugarCrmApp.class.getSimpleName();

    /**
     * <p>
     * getSessionId
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getSessionId() {
        long currentTime = SystemClock.currentThreadTimeMillis();

        if (Util.isNetworkOn(this) && (mSessionId == null || currentTime - mLastLoginTime > 30000)) {
            final String userName = SugarCrmSettings.getUsername(this);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String url = pref.getString(Util.PREF_REST_URL, this.getString(R.string.defaultUrl));
            Account account = getAccount(userName);
            if (account != null) {
                String password = AccountManager.get(this).getPassword(account);
                mLastLoginTime = currentTime;
                try {
                    mSessionId = RestUtil.loginToSugarCRM(url, account.name, password);
                } catch (SugarCrmException se) {
                    Log.e(LOG_TAG, se.getMessage(), se);
                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage(), ex);
                }
            }
        }
        return mSessionId;
    }

    /**
     * returns the Account associated with the current user name
     * 
     * @param userName
     *            a {@link java.lang.String} object.
     * @return a {@link android.accounts.Account} object.
     */
    public Account getAccount(String userName) {

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(Util.ACCOUNT_TYPE);
        Account userAccount = null;
        for (Account account : accounts) {
            // never print the password
            // Log.i(LOG_TAG, "user name is " + account.name);
            if (account.name.equals(userName)) {
                userAccount = account;
                break;
            }
        }
        return userAccount;
    }

    /**
     * <p>
     * setSessionId
     * </p>
     * 
     * @param mSessionId
     *            a {@link java.lang.String} object.
     */
    public void setSessionId(String mSessionId) {
        this.mSessionId = mSessionId;
    }

    /**
     * <p>
     * Setter for the field <code>moduleSortOrder</code>.
     * </p>
     * 
     * @param moduleName
     *            a {@link java.lang.String} object.
     * @param fieldName
     *            a {@link java.lang.String} object.
     * @param sortBy
     *            a {@link java.lang.String} object.
     */
    public void setModuleSortOrder(String moduleName, String fieldName, String sortBy) {
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put(fieldName, sortBy);
        moduleSortOrder.put(moduleName, fieldMap);
    }

    /**
     * <p>
     * Getter for the field <code>moduleSortOrder</code>.
     * </p>
     * 
     * @param moduleName
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getModuleSortOrder(String moduleName) {
        return moduleSortOrder.get(moduleName);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        setDefaultModuleSortOrders();
    }

    // TODO - hardcoded here for now, we have the infrastructure, but we have commented the code as
    // we do not support dynamic modules yet
    private void setDefaultModuleSortOrders() {
        Map<String, String> fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.ACCOUNTS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.FIRST_NAME, Util.ASC);
        moduleSortOrder.put(Util.CONTACTS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.FIRST_NAME, Util.ASC);
        moduleSortOrder.put(Util.LEADS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.OPPORTUNITIES, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.CASES, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.CALLS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.MEETINGS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME, Util.ASC);
        moduleSortOrder.put(Util.CAMPAIGNS, fieldMap);

        fieldMap = new HashMap<String, String>();
        fieldMap.put(ModuleFields.NAME_1, Util.ASC);
        moduleSortOrder.put(Util.RECENT, fieldMap);

    }

}
