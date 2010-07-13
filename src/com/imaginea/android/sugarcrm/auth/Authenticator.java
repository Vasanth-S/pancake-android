package com.imaginea.android.sugarcrm.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;

import com.imaginea.android.sugarcrm.R;
import com.imaginea.android.sugarcrm.SugarCrmSettings;
import com.imaginea.android.sugarcrm.WizardAuthActivity;
import com.imaginea.android.sugarcrm.util.RestUtil;
import com.imaginea.android.sugarcrm.util.SugarCrmException;
import com.imaginea.android.sugarcrm.util.Util;

/**
 * Based on the implementation of AbstractAccountAuthenticator for authenticating accounts in the
 * com.example.android.samplesync domain.
 */
class Authenticator extends AbstractAccountAuthenticator {
    // Authentication Service context
    private final Context mContext;

    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                                    String authTokenType, String[] requiredFeatures, Bundle options) {
        final Intent intent = new Intent(mContext, WizardAuthActivity.class);
        intent.putExtra(WizardAuthActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                    Bundle options) {
        if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            final boolean verified = onlineConfirmPassword(account.name, password);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        // Launch AuthenticatorActivity to confirm credentials
        final Intent intent = new Intent(mContext, WizardAuthActivity.class);
        intent.putExtra(Util.PREF_USERNAME, account.name);
        intent.putExtra(WizardAuthActivity.PARAM_CONFIRMCREDENTIALS, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle loginOptions) {
        if (!authTokenType.equals(Util.AUTHTOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        if (password != null) {
            final boolean verified = onlineConfirmPassword(account.name, password);
            if (verified) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                result.putString(AccountManager.KEY_AUTHTOKEN, password);
                return result;
            }
        }
        // the password was missing or incorrect, return an Intent to an
        // Activity that will prompt the user for the password.
        final Intent intent = new Intent(mContext, WizardAuthActivity.class);
        intent.putExtra(Util.PREF_USERNAME, account.name);
        intent.putExtra(WizardAuthActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(Util.AUTHTOKEN_TYPE)) {
            return mContext.getString(R.string.appName);
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                                    String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    /**
     * Validates user's password on the server
     */
    private boolean onlineConfirmPassword(String username, String password) {
        String url = SugarCrmSettings.getSugarRestUrl(mContext);
        String sessionId = null;
        try {
            sessionId = RestUtil.loginToSugarCRM(url, username, password);

        } catch (SugarCrmException e) {

        }
        return sessionId != null ? true : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle loginOptions) {
        final Intent intent = new Intent(mContext, WizardAuthActivity.class);
        intent.putExtra(Util.PREF_USERNAME, account.name);
        intent.putExtra(WizardAuthActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(WizardAuthActivity.PARAM_CONFIRMCREDENTIALS, false);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

}
