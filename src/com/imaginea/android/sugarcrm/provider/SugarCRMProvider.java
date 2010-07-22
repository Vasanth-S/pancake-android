package com.imaginea.android.sugarcrm.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.imaginea.android.sugarcrm.ModuleFields;
import com.imaginea.android.sugarcrm.RestUtilConstants;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Accounts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsContactsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsOpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsOpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Leads;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LeadsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Opportunities;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.OpportunitiesColumns;

import java.util.HashMap;
import java.util.Map;

/**
 * SugarCRMProvider Provides access to a database of sugar modules, their data and relationships.
 */
public class SugarCRMProvider extends ContentProvider {

    public static final String AUTHORITY = "com.imaginea.sugarcrm.provider";

    private static final int ACCOUNT = 0;

    private static final int ACCOUNT_ID = 1;

    private static final int CONTACT = 2;

    private static final int CONTACT_ID = 3;

    private static final int LEAD = 4;

    private static final int LEAD_ID = 5;

    private static final int OPPORTUNITY = 6;

    private static final int OPPORTUNITY_ID = 7;

    private static final int MEETING = 8;

    private static final int MEETING_ID = 9;

    private static final int CASE = 10;

    private static final int CASE_ID = 11;

    private static final int CALL = 12;

    private static final int CALL_ID = 13;

    private static final int ACCOUNT_CONTACT = 14;

    private static final int ACCOUNT_LEAD = 15;

    private static final int ACCOUNT_OPPORTUNITY = 16;

    private static final int CONTACT_LEAD = 17;

    // TODO - is this required
    private static final int CONTACT_OPPORTUNITY = 18;

    private static final int LEAD_OPPORTUNITY = 19;

    private static final int OPPORTUNITY_CONTACT = 20;

    private static final UriMatcher sUriMatcher;

    private static final String TAG = "SugarCRMProvider";

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                                    String sortOrder) {
        Cursor c = null;
        int size = uri.getPathSegments().size();
        String maxResultsLimit = null;
        String offset = null;
        String module;

        if (size == 3) {
            offset = uri.getPathSegments().get(1);
            maxResultsLimit = uri.getPathSegments().get(2);
        }
        // SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
        case ACCOUNT:
            module = RestUtilConstants.ACCOUNTS_MODULE;
            c = db.query(DatabaseHelper.ACCOUNTS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            break;

        case ACCOUNT_ID:
            module = RestUtilConstants.ACCOUNTS_MODULE;
            // db.setProjectionMap(sNotesProjectionMap);
            selection = SugarCRMContent.RECORD_ID + " = ?";
            c = db.query(DatabaseHelper.ACCOUNTS_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            // qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
            break;

        case ACCOUNT_CONTACT:
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(DatabaseHelper.ACCOUNTS_TABLE_NAME + ","
                                            + DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME + ","
                                            + DatabaseHelper.CONTACTS_TABLE_NAME);
            module = RestUtilConstants.CONTACTS_MODULE;
            selection = DatabaseHelper.ACCOUNTS_TABLE_NAME + "." + Accounts.ID + " = ?" + " AND "
                                            + DatabaseHelper.ACCOUNTS_TABLE_NAME + "."
                                            + Accounts.ID + "="
                                            + DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME + "."
                                            + AccountsContactsColumns.ACCOUNT_ID + " AND "
                                            + DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME + "."
                                            + AccountsContactsColumns.CONTACT_ID + "="
                                            + DatabaseHelper.CONTACTS_TABLE_NAME + "."
                                            + Contacts.ID;
            Map<String, String> contactsProjectionMap = getProjectionMap(DatabaseHelper.CONTACTS_TABLE_NAME, projection);
            qb.setProjectionMap(contactsProjectionMap);
            c = qb.query(db, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, sortOrder, "");
            // c = db.query(DatabaseHelper.CONTACTS_TABLE_NAME, projection, selection, new String[]
            // { uri.getPathSegments().get(1) }, null, null, null);
            break;

        case ACCOUNT_LEAD:
            // TODO - whats the set relationship for this
            module = RestUtilConstants.LEADS_MODULE;
            selection = LeadsColumns.ACCOUNT_ID + " = ?";
            c = db.query(DatabaseHelper.LEADS_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            break;

        case ACCOUNT_OPPORTUNITY:
            module = RestUtilConstants.OPPORTUNITIES_MODULE;

            // c = db.query(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection, selection, new
            // String[] { uri.getPathSegments().get(1) }, null, null, null);

            qb = new SQLiteQueryBuilder();
            qb.setTables(DatabaseHelper.ACCOUNTS_TABLE_NAME + ","
                                            + DatabaseHelper.ACCOUNTS_OPPORTUNITIES_TABLE_NAME
                                            + "," + DatabaseHelper.OPPORTUNITIES_TABLE_NAME);

            selection = DatabaseHelper.ACCOUNTS_TABLE_NAME + "." + Accounts.ID + " = ?" + " AND "
                                            + DatabaseHelper.ACCOUNTS_TABLE_NAME + "."
                                            + Accounts.ID + "="
                                            + DatabaseHelper.ACCOUNTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + AccountsOpportunitiesColumns.ACCOUNT_ID
                                            + " AND "
                                            + DatabaseHelper.ACCOUNTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + AccountsOpportunitiesColumns.OPPORTUNITY_ID
                                            + "=" + DatabaseHelper.OPPORTUNITIES_TABLE_NAME + "."
                                            + Opportunities.ID;
            Map<String, String> oppurtunityProjectionMap = getProjectionMap(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection);
            qb.setProjectionMap(oppurtunityProjectionMap);
            c = qb.query(db, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, sortOrder, "");

            break;

        case CONTACT:
            module = RestUtilConstants.CONTACTS_MODULE;
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Querying Contacts");
                Log.d(TAG, "Uri:->" + uri.toString());

                Log.d(TAG, "Offset" + offset);
                Log.d(TAG, "maxResultsLimit" + maxResultsLimit);
            }

            if (maxResultsLimit != null) {
                // maxResultsLimit = maxResultsLimit + "  OFFSET " + offset;
                if (selection == null) {
                    selection = SugarCRMContent.RECORD_ID + " > ?";
                    selectionArgs = new String[] { offset };
                }
            }
            c = db.query(DatabaseHelper.CONTACTS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, maxResultsLimit);

            break;

        case CONTACT_ID:
            module = RestUtilConstants.CONTACTS_MODULE;
            selection = SugarCRMContent.RECORD_ID + " = ?";
            c = db.query(DatabaseHelper.CONTACTS_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            break;

        case CONTACT_LEAD:
            module = RestUtilConstants.LEADS_MODULE;
            selection = LeadsColumns.ACCOUNT_ID + " = ?";
            c = db.query(DatabaseHelper.LEADS_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            break;

            // TODO - this case is dubious - remove it later
        case CONTACT_OPPORTUNITY:

            module = RestUtilConstants.OPPORTUNITIES_MODULE;
            qb = new SQLiteQueryBuilder();
            qb.setTables(DatabaseHelper.CONTACTS_TABLE_NAME + ","
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "," + DatabaseHelper.OPPORTUNITIES_TABLE_NAME);

            selection = DatabaseHelper.CONTACTS_TABLE_NAME + "." + Contacts.ID + " = ?" + " AND "
                                            + DatabaseHelper.CONTACTS_TABLE_NAME + "."
                                            + Contacts.ID + "="
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + ContactsOpportunitiesColumns.CONTACT_ID
                                            + " AND "
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + ContactsOpportunitiesColumns.OPPORTUNITY_ID
                                            + "=" + DatabaseHelper.OPPORTUNITIES_TABLE_NAME + "."
                                            + Opportunities.ID;
            oppurtunityProjectionMap = getProjectionMap(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection);
            qb.setProjectionMap(oppurtunityProjectionMap);
            c = qb.query(db, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, sortOrder, "");

            break;

        case LEAD:
            module = RestUtilConstants.LEADS_MODULE;
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Querying Leads");
                Log.d(TAG, "Uri:->" + uri.toString());
                Log.d(TAG, "Offset" + offset);
                Log.d(TAG, "maxResultsLimit" + maxResultsLimit);
            }
            if (maxResultsLimit != null) {
                // maxResultsLimit = maxResultsLimit + "  OFFSET " + offset;
                if (selection == null) {
                    selection = SugarCRMContent.RECORD_ID + " > ?";
                    selectionArgs = new String[] { offset };
                }
            }
            c = db.query(DatabaseHelper.LEADS_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, maxResultsLimit);

            break;

        case LEAD_ID:
            module = RestUtilConstants.LEADS_MODULE;
            // db.setProjectionMap(sNotesProjectionMap);
            selection = SugarCRMContent.RECORD_ID + " = ?";
            c = db.query(DatabaseHelper.LEADS_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            // qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
            break;

        case LEAD_OPPORTUNITY:
            module = RestUtilConstants.OPPORTUNITIES_MODULE;
            selection = OpportunitiesColumns.ACCOUNT_ID + " = ?";
            c = db.query(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            break;

        case OPPORTUNITY:
            module = RestUtilConstants.OPPORTUNITIES_MODULE;
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Querying OPPORTUNITIES");
                Log.d(TAG, "Uri:->" + uri.toString());

                // qb.setTables(DatabaseHelper.CONTACTS_TABLE_NAME);

                Log.d(TAG, "Offset" + offset);
                Log.d(TAG, "maxResultsLimit" + maxResultsLimit);
            }
            if (maxResultsLimit != null) {
                // maxResultsLimit = maxResultsLimit + "  OFFSET " + offset;
                if (selection == null) {
                    selection = SugarCRMContent.RECORD_ID + " > ?";
                    selectionArgs = new String[] { offset };
                }
            }
            c = db.query(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, maxResultsLimit);

            break;

        case OPPORTUNITY_ID:
            module = RestUtilConstants.OPPORTUNITIES_MODULE;
            // db.setProjectionMap(sNotesProjectionMap);
            selection = SugarCRMContent.RECORD_ID + " = ?";
            c = db.query(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, null);
            // qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
            break;

        case OPPORTUNITY_CONTACT:

            module = RestUtilConstants.OPPORTUNITIES_MODULE;
            qb = new SQLiteQueryBuilder();
            qb.setTables(DatabaseHelper.CONTACTS_TABLE_NAME + ","
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "," + DatabaseHelper.OPPORTUNITIES_TABLE_NAME);

            selection = DatabaseHelper.OPPORTUNITIES_TABLE_NAME + "." + Opportunities.ID + " = ?"
                                            + " AND " + DatabaseHelper.CONTACTS_TABLE_NAME + "."
                                            + Contacts.ID + "="
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + ContactsOpportunitiesColumns.CONTACT_ID
                                            + " AND "
                                            + DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME
                                            + "." + ContactsOpportunitiesColumns.OPPORTUNITY_ID
                                            + "=" + DatabaseHelper.OPPORTUNITIES_TABLE_NAME + "."
                                            + Opportunities.ID;
            oppurtunityProjectionMap = getProjectionMap(DatabaseHelper.CONTACTS_TABLE_NAME, projection);
            qb.setProjectionMap(oppurtunityProjectionMap);
            c = qb.query(db, projection, selection, new String[] { uri.getPathSegments().get(1) }, null, null, sortOrder, "");

            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (Log.isLoggable(TAG, Log.DEBUG))
            Log.d(TAG, "Count:" + c.getCount());
        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);

        // TODO - moce this code to sync and cache manager - database cache miss, start a rest api
        // call , package the params appropriately
        // if (c.getCount() == 0)
        // ServiceHelper.startService(getContext(), uri, module, projection, sortOrder);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        // TODO - add the remaining types
        case ACCOUNT:
            return "vnd.android.cursor.dir/accounts";
        case CONTACT:
            return "vnd.android.cursor.dir/contacts";

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());
        // Make sure that the fields are all set

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // Validate the requested uri
        // if (sUriMatcher.match(uri) != CONTACT) {
        // throw new IllegalArgumentException("Unknown URI " + uri);
        // }
        switch (sUriMatcher.match(uri)) {
        case ACCOUNT:
            long rowId = db.insert(DatabaseHelper.ACCOUNTS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri accountUri = ContentUris.withAppendedId(Accounts.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(accountUri, null);
                return accountUri;
            }
            break;

        case ACCOUNT_CONTACT:
            String parentPath = uri.getPathSegments().get(0);
            String accountId = uri.getPathSegments().get(1);
            String selection = AccountsColumns.ID + "=" + accountId;

            String parentModuleName = mOpenHelper.getRelationshipForPath(parentPath);
            Uri parentUri = mOpenHelper.getModuleUri(parentModuleName);
            Cursor cursor = query(parentUri, Accounts.DETAILS_PROJECTION, selection, null, null);
            boolean rowsPresent = cursor.moveToFirst();
            if (Log.isLoggable(TAG, Log.VERBOSE))
                Log.v(TAG, "Uri to insert:" + uri.toString() + " rows present:" + rowsPresent);
            if (!rowsPresent) {
                cursor.close();
                return uri;
            }
            String accountName = cursor.getString(cursor.getColumnIndex(AccountsColumns.NAME));
            // values.put(Contacts.ACCOUNT_ID, accountId);
            values.put(Contacts.ACCOUNT_NAME, accountName);
            cursor.close();
            rowId = db.insert(DatabaseHelper.CONTACTS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(contactUri, null);

                ContentValues val2 = new ContentValues();
                val2.put(AccountsContactsColumns.ACCOUNT_ID, accountId);
                val2.put(AccountsContactsColumns.CONTACT_ID, rowId);
                // TODO - delete flag and date_modified
                db.insert(DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME, "", val2);

                return contactUri;
            }
            break;

        case ACCOUNT_LEAD:
            accountId = uri.getPathSegments().get(1);
            selection = AccountsColumns.ID + "=" + accountId;
            cursor = query(mOpenHelper.getModuleUri("Accounts"), Accounts.DETAILS_PROJECTION, selection, null, null);
            cursor.moveToFirst();
            accountName = cursor.getString(cursor.getColumnIndex(AccountsColumns.NAME));
            values.put(ModuleFields.ACCOUNT_ID, accountId);
            values.put(ModuleFields.ACCOUNT_NAME, accountName);

            rowId = db.insert(DatabaseHelper.LEADS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri leadUri = ContentUris.withAppendedId(Leads.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(leadUri, null);
                cursor.close();
                return leadUri;
            }
            break;

        case ACCOUNT_OPPORTUNITY:
            parentPath = uri.getPathSegments().get(0);
            accountId = uri.getPathSegments().get(1);
            selection = AccountsColumns.ID + "=" + accountId;

            parentModuleName = mOpenHelper.getRelationshipForPath(parentPath);
            cursor = query(mOpenHelper.getModuleUri(parentModuleName), Accounts.DETAILS_PROJECTION, selection, null, null);
            cursor.moveToFirst();
            accountName = cursor.getString(cursor.getColumnIndex(AccountsColumns.NAME));
            // values.put(Contacts.ACCOUNT_ID, accountId);
            values.put(Contacts.ACCOUNT_NAME, accountName);
            cursor.close();
            rowId = db.insert(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri opportunityUri = ContentUris.withAppendedId(Opportunities.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(opportunityUri, null);

                ContentValues val2 = new ContentValues();
                val2.put(AccountsOpportunitiesColumns.ACCOUNT_ID, accountId);
                val2.put(AccountsOpportunitiesColumns.OPPORTUNITY_ID, rowId);
                // TODO - delete flag and date_modified
                db.insert(DatabaseHelper.ACCOUNTS_OPPORTUNITIES_TABLE_NAME, "", val2);

                return opportunityUri;
            }

            break;

        case CONTACT:
            rowId = db.insert(DatabaseHelper.CONTACTS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(contactUri, null);
                return contactUri;
            }
            break;

        case CONTACT_LEAD:
            rowId = db.insert(DatabaseHelper.LEADS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri leadUri = ContentUris.withAppendedId(Leads.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(leadUri, null);
                return leadUri;
            }
            break;

        case CONTACT_OPPORTUNITY:

            parentPath = uri.getPathSegments().get(0);
            String contactId = uri.getPathSegments().get(1);
            selection = Contacts.ID + "=" + contactId;

            parentModuleName = mOpenHelper.getRelationshipForPath(parentPath);
            // cursor = query(mOpenHelper.getModuleUri(parentModuleName),
            // Contacts.DETAILS_PROJECTION, selection, null, null);
            // cursor.moveToFirst();

            // values.put(Contacts.ACCOUNT_ID, accountId);
            // values.put(Contacts.ACCOUNT_NAME, accountName);
            // cursor.close();
            rowId = db.insert(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri opportunityUri = ContentUris.withAppendedId(Opportunities.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(opportunityUri, null);

                ContentValues val2 = new ContentValues();
                val2.put(ContactsOpportunitiesColumns.CONTACT_ID, contactId);
                val2.put(ContactsOpportunitiesColumns.OPPORTUNITY_ID, rowId);
                // TODO - delete flag and date_modified
                db.insert(DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME, "", val2);

                return opportunityUri;
            }

            break;

        case LEAD:
            rowId = db.insert(DatabaseHelper.LEADS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri leadUri = ContentUris.withAppendedId(Leads.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(leadUri, null);
                return leadUri;
            }
            break;

        case LEAD_OPPORTUNITY:
            rowId = db.insert(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri opportunityUri = ContentUris.withAppendedId(Leads.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(opportunityUri, null);
                return opportunityUri;
            }
            break;

        case OPPORTUNITY:
            rowId = db.insert(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri oppUri = ContentUris.withAppendedId(Opportunities.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(oppUri, null);
                return oppUri;
            }
            break;
            
        case OPPORTUNITY_CONTACT:

            parentPath = uri.getPathSegments().get(0);
            String opportunityId = uri.getPathSegments().get(1);
            selection = Opportunities.ID + "=" + opportunityId;

            parentModuleName = mOpenHelper.getRelationshipForPath(parentPath);
            // cursor = query(mOpenHelper.getModuleUri(parentModuleName),
            // Contacts.DETAILS_PROJECTION, selection, null, null);
            // cursor.moveToFirst();

            // values.put(Contacts.ACCOUNT_ID, accountId);
            // values.put(Contacts.ACCOUNT_NAME, accountName);
            // cursor.close();
            rowId = db.insert(DatabaseHelper.CONTACTS_TABLE_NAME, "", values);
            if (rowId > 0) {
                Uri contactsUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(contactsUri, null);

                ContentValues val2 = new ContentValues();
                val2.put(ContactsOpportunitiesColumns.CONTACT_ID, rowId);
                val2.put(ContactsOpportunitiesColumns.OPPORTUNITY_ID, opportunityId);
                // TODO - delete flag and date_modified
                db.insert(DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME, "", val2);

                return contactsUri;
            }

            break;
        default:
            // return uri;
            throw new IllegalArgumentException("Unknown URI " + uri);

        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
        case ACCOUNT:
            count = db.delete(DatabaseHelper.ACCOUNTS_TABLE_NAME, where, whereArgs);
            break;

        case ACCOUNT_ID:
            String accountId = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.ACCOUNTS_TABLE_NAME, Accounts.ID
                                            + "="
                                            + accountId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case ACCOUNT_CONTACT:
            accountId = uri.getPathSegments().get(1);
            String contactId = uri.getPathSegments().get(3);
            count = db.delete(DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME, AccountsContactsColumns.ACCOUNT_ID
                                            + "="
                                            + accountId
                                            + " AND "
                                            + AccountsContactsColumns.CONTACT_ID
                                            + "="
                                            + contactId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case ACCOUNT_OPPORTUNITY:
            accountId = uri.getPathSegments().get(1);
            String opportunityId = uri.getPathSegments().get(3);
            count = db.delete(DatabaseHelper.ACCOUNTS_CONTACTS_TABLE_NAME, AccountsOpportunitiesColumns.ACCOUNT_ID
                                            + "="
                                            + accountId
                                            + " AND "
                                            + AccountsOpportunitiesColumns.OPPORTUNITY_ID
                                            + "="
                                            + opportunityId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case CONTACT:
            count = db.delete(DatabaseHelper.CONTACTS_TABLE_NAME, where, whereArgs);
            break;

        case CONTACT_ID:
            contactId = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.CONTACTS_TABLE_NAME, Contacts.ID + "=" + contactId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'

                                            : ""), whereArgs);
            break;

        case CONTACT_OPPORTUNITY:
            contactId = uri.getPathSegments().get(1);
            opportunityId = uri.getPathSegments().get(3);
            count = db.delete(DatabaseHelper.CONTACTS_OPPORTUNITIES_TABLE_NAME, ContactsOpportunitiesColumns.CONTACT_ID
                                            + "="
                                            + contactId
                                            + " AND "
                                            + ContactsOpportunitiesColumns.OPPORTUNITY_ID
                                            + "="
                                            + opportunityId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case LEAD:
            count = db.delete(DatabaseHelper.LEADS_TABLE_NAME, where, whereArgs);
            break;

        case LEAD_ID:
            String leadId = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.LEADS_TABLE_NAME, Leads.ID
                                            + "="
                                            + leadId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;
        case OPPORTUNITY:
            count = db.delete(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, where, whereArgs);
            break;

        case OPPORTUNITY_ID:
            String oppId = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, Opportunities.ID
                                            + "="
                                            + oppId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ACCOUNT:
            count = db.update(DatabaseHelper.ACCOUNTS_TABLE_NAME, values, where, whereArgs);
            break;

        case ACCOUNT_ID:
            String accountId = uri.getPathSegments().get(1);
            count = db.update(DatabaseHelper.ACCOUNTS_TABLE_NAME, values, Accounts.ID
                                            + "="
                                            + accountId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case CONTACT:
            count = db.update(DatabaseHelper.CONTACTS_TABLE_NAME, values, where, whereArgs);
            break;

        case CONTACT_ID:
            String contactId = uri.getPathSegments().get(1);
            count = db.update(DatabaseHelper.CONTACTS_TABLE_NAME, values, Contacts.ID
                                            + "="
                                            + contactId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case LEAD:
            count = db.update(DatabaseHelper.LEADS_TABLE_NAME, values, where, whereArgs);
            break;

        case LEAD_ID:
            String leadId = uri.getPathSegments().get(1);
            count = db.update(DatabaseHelper.LEADS_TABLE_NAME, values, Leads.ID
                                            + "="
                                            + leadId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;

        case OPPORTUNITY:
            count = db.update(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, values, where, whereArgs);
            break;

        case OPPORTUNITY_ID:
            String oppId = uri.getPathSegments().get(1);
            count = db.update(DatabaseHelper.OPPORTUNITIES_TABLE_NAME, values, Opportunities.ID
                                            + "="
                                            + oppId
                                            + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
                                                                            : ""), whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static Map<String, String> getProjectionMap(String tableName, String[] projections) {
        Map<String, String> projectionMap = mProjectionMaps.get(tableName);
        if (projectionMap != null)
            return projectionMap;
        projectionMap = new HashMap<String, String>();
        for (String column : projections) {
            projectionMap.put(column, tableName + "." + column);
        }
        mProjectionMaps.put(tableName, projectionMap);
        return projectionMap;
    }

    static Map<String, Map> mProjectionMaps = new HashMap<String, Map>();

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account", ACCOUNT);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/#/#", ACCOUNT);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/#", ACCOUNT_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/#/contact", ACCOUNT_CONTACT);
        // sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/*/contact", ACCOUNT_BEAN_CONTACT);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/#/lead", ACCOUNT_LEAD);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "account/#/opportunity", ACCOUNT_OPPORTUNITY);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "contact", CONTACT);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "contact/#", CONTACT_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "contact/#/#", CONTACT);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "contact/#/opportunity", CONTACT_OPPORTUNITY);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "lead", LEAD);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "lead/#", LEAD_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "lead/#/#", LEAD);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "lead/#/opportunity", LEAD_OPPORTUNITY);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "opportunity", OPPORTUNITY);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "opportunity/#", OPPORTUNITY_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "opportunity/#/#", OPPORTUNITY);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "opportunity/#/contact", OPPORTUNITY_CONTACT);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "meeting", MEETING);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "meeting/#", MEETING_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "meeting/#/#", MEETING);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "call", CALL);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "call/#", CALL_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "call/#/#", CALL);

        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "case", CASE);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "case/#", CASE_ID);
        sUriMatcher.addURI(SugarCRMContent.AUTHORITY, "case/#/#", CASE);

        // sUriMatcher.addURI(SugarBeans.AUTHORITY, "sugarbeans/#", SUGAR_BEAN_ID);

    }
}
