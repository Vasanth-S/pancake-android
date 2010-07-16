package com.imaginea.android.sugarcrm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.imaginea.android.sugarcrm.R;
import com.imaginea.android.sugarcrm.SugarCrmApp;
import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
import com.imaginea.android.sugarcrm.util.RestUtil;
import com.imaginea.android.sugarcrm.util.SugarCrmException;
import com.imaginea.android.sugarcrm.util.Util;

import org.apache.http.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * SyncAdapter implementation for syncing sugarcrm modules on the server to sugar crm provider and
 * vice versa.
 * 
 * //TODO - Stress testing for large datasets - test cases
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final AccountManager mAccountManager;

    private final Context mContext;

    private Date mLastUpdated;

    private static final String LOG_TAG = "SyncAdapter";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                                    ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync");
        String authtoken = null;
        try {
            // use the account manager to request the credentials
            authtoken = mAccountManager.blockingGetAuthToken(account, Util.AUTHTOKEN_TYPE, true /* notifyAuthFailure */);
            // Log.v(LOG_TAG, "authtoken:" + authtoken);
            // Log.v(LOG_TAG, "password:" + mAccountManager.getPassword(account));
            /*
             * if we are a password based system, the SugarCRM OAuth setup is not clear yet but
             * based on preferences, we ahould select the right one -?
             */
            String userName = account.name;
            String password = mAccountManager.getPassword(account);
            Log.v(LOG_TAG, "user name:" + userName);

            if (!Util.isNetworkOn(mContext)) {
                Log.v(LOG_TAG, "Network is not on..skipping sync:");
                syncResult.stats.numIoExceptions++;
                return;
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
            // TODO use a constant and remove this as we start from the login screen
            String url = pref.getString(Util.PREF_REST_URL, mContext.getString(R.string.defaultUrl));
            SugarCrmApp app = ((SugarCrmApp) SugarCrmApp.app);
            String sessionId = app != null ? app.getSessionId() : null;
            if (sessionId == null) {
                sessionId = RestUtil.loginToSugarCRM(url, account.name, password);
            }

            List<String> moduleList = DatabaseHelper.getModuleList();
            if (moduleList == null)
                moduleList = RestUtil.getAvailableModules(url, sessionId);
                // TODO - dynamically determine the relationships and get the values
            Collections.sort(moduleList);
            for (String moduleName : moduleList) {
                Log.i(LOG_TAG, "Syncing Module:" + moduleName);
                SugarSyncManager.syncModules(mContext, account.name, sessionId, moduleName);
            }

            // update the last synced date.
            mLastUpdated = new Date();

        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(LOG_TAG, "ParseException", e);
        } catch (OperationCanceledException e) {
            // TODO - whats the stats update here
            // syncResult.stats.++;
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (AuthenticatorException e) {
            // syncResult.stats.numAuthExceptions++;
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(LOG_TAG, e.getMessage(), e);
        } catch (SugarCrmException se) {
            Log.e(LOG_TAG, se.getMessage(), se);
        }
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        // TODO - notify is part if sync framework, with the SyncResults giving details about the
        // last sync, we perform additional steps that are specific to our app if required
    }

}
