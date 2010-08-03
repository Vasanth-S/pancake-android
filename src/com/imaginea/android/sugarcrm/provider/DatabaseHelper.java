package com.imaginea.android.sugarcrm.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.imaginea.android.sugarcrm.ModuleFields;
import com.imaginea.android.sugarcrm.R;
import com.imaginea.android.sugarcrm.RestUtilConstants;
import com.imaginea.android.sugarcrm.SugarCrmSettings;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLActionColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLActions;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLRoleColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ACLRoles;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Accounts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsCasesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsContactsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsOpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Calls;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Cases;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsCasesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsOpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Leads;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LeadsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LinkFieldColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Meetings;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleFieldColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleFieldGroupColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleFieldSortOrder;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleFieldSortOrderColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Modules;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Opportunities;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.OpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Sync;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.SyncColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Users;
import com.imaginea.android.sugarcrm.sync.SyncRecord;
import com.imaginea.android.sugarcrm.util.ACLConstants;
import com.imaginea.android.sugarcrm.util.LinkField;
import com.imaginea.android.sugarcrm.util.Module;
import com.imaginea.android.sugarcrm.util.ModuleField;
import com.imaginea.android.sugarcrm.util.ModuleFieldBean;
import com.imaginea.android.sugarcrm.util.SugarBean;
import com.imaginea.android.sugarcrm.util.SugarCrmException;
import com.imaginea.android.sugarcrm.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sugar_crm.db";

    // TODO: RESET the database version to 1
    private static final int DATABASE_VERSION = 30;

    public static final String ACCOUNTS_TABLE_NAME = "accounts";

    public static final String CONTACTS_TABLE_NAME = "contacts";

    public static final String ACCOUNTS_CONTACTS_TABLE_NAME = "accounts_contacts";

    public static final String ACCOUNTS_OPPORTUNITIES_TABLE_NAME = "accounts_opportunities";

    public static final String ACCOUNTS_CASES_TABLE_NAME = "accounts_cases";

    public static final String CONTACTS_OPPORTUNITIES_TABLE_NAME = "contacts_opportunities";

    public static final String CONTACTS_CASES_TABLE_NAME = "contacts_cases";

    public static final String LEADS_TABLE_NAME = "leads";

    public static final String OPPORTUNITIES_TABLE_NAME = "opportunities";

    public static final String MEETINGS_TABLE_NAME = "meetings";

    public static final String CALLS_TABLE_NAME = "calls";

    public static final String CASES_TABLE_NAME = "cases";

    public static final String MODULES_TABLE_NAME = "modules";

    public static final String MODULE_FIELDS_TABLE_NAME = "module_fields";

    public static final String LINK_FIELDS_TABLE_NAME = "link_fields";

    public static final String SYNC_TABLE_NAME = "sync_table";

    public static final String USERS_TABLE_NAME = "users";

    public static final String ACL_ROLES_TABLE_NAME = "acl_roles";

    public static final String ACL_ACTIONS_TABLE_NAME = "acl_actions";

    public static final String MODULE_FIELDS_SORT_ORDER_TABLE_NAME = "module_fields_sort_order";

    public static final String MODULE_FIELDS_GROUP_TABLE_NAME = "module_fields_group";

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private String[] defaultSupportedModules = { Util.ACCOUNTS, Util.CONTACTS, Util.LEADS,
            Util.OPPORTUNITIES, Util.CASES, Util.CALLS, Util.MEETINGS };

    private static HashMap<String, Integer> moduleIcons = new HashMap<String, Integer>();

    private static List<String> moduleList;

    private static final HashMap<String, String[]> moduleProjections = new HashMap<String, String[]>();
    
    private static final HashMap<String, String[]> moduleListProjections = new HashMap<String, String[]>();

    private static final HashMap<String, String[]> moduleListSelections = new HashMap<String, String[]>();

    private static final HashMap<String, String> moduleSortOrder = new HashMap<String, String>();

    private static final HashMap<String, Uri> moduleUris = new HashMap<String, Uri>();

    private static final HashMap<String, String> moduleSelections = new HashMap<String, String>();

    private static HashMap<String, HashMap<String, ModuleField>> moduleFields;

    private static HashMap<String, HashMap<String, LinkField>> linkFields;

    private static final HashMap<String, String[]> moduleRelationshipItems = new HashMap<String, String[]>();

    private static final HashMap<String, String> linkfieldNames = new HashMap<String, String>();

    private static List<String> billingAddressGroup = new ArrayList<String>();

    private static List<String> shippingAddressGroup = new ArrayList<String>();

    private static List<String> durationGroup = new ArrayList<String>();

    private Map<String, Map<String, Integer>> accessMap = new HashMap<String, Map<String, Integer>>();

    private static Map<String, String> fieldsExcludedForEdit = new HashMap<String, String>();

    private static Map<String, String> fieldsExcludedForDetails = new HashMap<String, String>();

    private Context mContext;

    private static String mSelection = SugarCRMContent.RECORD_ID + "=?";
    
    static {

        // Icons projection
        moduleIcons.put(Util.ACCOUNTS, R.drawable.account);
        moduleIcons.put(Util.CONTACTS, R.drawable.contacts);
        moduleIcons.put(Util.LEADS, R.drawable.leads);
        moduleIcons.put(Util.OPPORTUNITIES, R.drawable.opportunity);
        moduleIcons.put(Util.CASES, R.drawable.cases);
        moduleIcons.put(Util.CALLS, R.drawable.calls);
        moduleIcons.put(Util.MEETINGS, R.drawable.meeting);
        moduleIcons.put("Settings", R.drawable.settings);

        // Module Projections
        moduleProjections.put(Util.ACCOUNTS, Accounts.DETAILS_PROJECTION);
        moduleProjections.put(Util.CONTACTS, Contacts.DETAILS_PROJECTION);
        moduleProjections.put(Util.LEADS, Leads.DETAILS_PROJECTION);
        moduleProjections.put(Util.OPPORTUNITIES, Opportunities.DETAILS_PROJECTION);
        moduleProjections.put(Util.CASES, Cases.DETAILS_PROJECTION);
        moduleProjections.put(Util.CALLS, Calls.DETAILS_PROJECTION);
        moduleProjections.put(Util.MEETINGS, Meetings.DETAILS_PROJECTION);

        // module list projections
        moduleListProjections.put(Util.ACCOUNTS, Accounts.LIST_PROJECTION);
        moduleListProjections.put(Util.CONTACTS, Contacts.LIST_PROJECTION);
        moduleListProjections.put(Util.LEADS, Leads.LIST_PROJECTION);
        moduleListProjections.put(Util.OPPORTUNITIES, Opportunities.LIST_PROJECTION);
        moduleListProjections.put(Util.CASES, Cases.LIST_PROJECTION);
        moduleListProjections.put(Util.CALLS, Calls.LIST_PROJECTION);
        moduleListProjections.put(Util.MEETINGS, Meetings.LIST_PROJECTION);
        
        // Module List Selections
        moduleListSelections.put(Util.ACCOUNTS, Accounts.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.CONTACTS, Contacts.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.LEADS, Leads.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.OPPORTUNITIES, Opportunities.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.CASES, Cases.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.CALLS, Calls.LIST_VIEW_PROJECTION);
        moduleListSelections.put(Util.MEETINGS, Meetings.LIST_VIEW_PROJECTION);

        // Default sort orders
        moduleSortOrder.put(Util.ACCOUNTS, Accounts.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.CONTACTS, Contacts.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.LEADS, Leads.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.OPPORTUNITIES, Opportunities.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.CASES, Cases.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.CALLS, Calls.DEFAULT_SORT_ORDER);
        moduleSortOrder.put(Util.MEETINGS, Meetings.DEFAULT_SORT_ORDER);

        // Content Uris
        moduleUris.put(Util.ACCOUNTS, Accounts.CONTENT_URI);
        moduleUris.put(Util.CONTACTS, Contacts.CONTENT_URI);
        moduleUris.put(Util.LEADS, Leads.CONTENT_URI);
        moduleUris.put(Util.OPPORTUNITIES, Opportunities.CONTENT_URI);
        moduleUris.put(Util.CASES, Cases.CONTENT_URI);
        moduleUris.put(Util.CALLS, Calls.CONTENT_URI);
        moduleUris.put(Util.MEETINGS, Meetings.CONTENT_URI);
        moduleUris.put(Util.USERS, Users.CONTENT_URI);

        // TODO - complete this list
        moduleRelationshipItems.put(Util.ACCOUNTS, new String[] { Util.CONTACTS,
                Util.OPPORTUNITIES, Util.CASES });
        // TODO - leads removed from CONTACTS relationship and vice versa
        moduleRelationshipItems.put(Util.CONTACTS, new String[] { Util.OPPORTUNITIES });
        // TODO -
        // moduleRelationshipItems.put(Util.LEADS, new String[] { Util.OPPORTUNITIES});
        moduleRelationshipItems.put(Util.OPPORTUNITIES, new String[] { Util.CONTACTS });
        // TODO -
        // moduleRelationshipItems.put(Util.CASES, new String[] { Util.CONTACTS });
        // moduleRelationshipItems.put(Util.CALLS, new String[] { Util.CONTACTS });
        // moduleRelationshipItems.put(Util.MEETINGS, new String[] { Util.CONTACTS });

        linkfieldNames.put(Util.CONTACTS, "contacts");
        linkfieldNames.put(Util.LEADS, "leads");
        linkfieldNames.put(Util.OPPORTUNITIES, "opportunities");
        linkfieldNames.put(Util.CASES, "cases");
        linkfieldNames.put(Util.CALLS, "calls");
        linkfieldNames.put(Util.MEETINGS, "meetings");
        linkfieldNames.put(Util.ACLROLES, "aclroles");
        linkfieldNames.put(Util.ACLACTIONS, "actions");

        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_STREET);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_STREET_2);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_STREET_3);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_STREET_4);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_CITY);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_STATE);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_POSTALCODE);
        billingAddressGroup.add(ModuleFields.BILLING_ADDRESS_COUNTRY);

        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_STREET);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_STREET_2);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_STREET_3);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_STREET_4);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_CITY);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_STATE);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_POSTALCODE);
        shippingAddressGroup.add(ModuleFields.SHIPPING_ADDRESS_COUNTRY);

        durationGroup.add(ModuleFields.DURATION_HOURS);
        durationGroup.add(ModuleFields.DURATION_MINUTES);

        // add a field name to the map if a module field in detail projection is to be excluded
        fieldsExcludedForEdit.put(SugarCRMContent.RECORD_ID, SugarCRMContent.RECORD_ID);
        fieldsExcludedForEdit.put(SugarCRMContent.SUGAR_BEAN_ID, SugarCRMContent.SUGAR_BEAN_ID);
        fieldsExcludedForEdit.put(ModuleFields.DELETED, ModuleFields.DELETED);
        fieldsExcludedForEdit.put(ModuleFields.ACCOUNT_ID, ModuleFields.ACCOUNT_ID);
        fieldsExcludedForEdit.put(ModuleFields.DATE_ENTERED, ModuleFields.DATE_ENTERED);
        fieldsExcludedForEdit.put(ModuleFields.DATE_MODIFIED, ModuleFields.DATE_MODIFIED);
        fieldsExcludedForEdit.put(ModuleFields.CREATED_BY, ModuleFields.CREATED_BY);
        fieldsExcludedForEdit.put(ModuleFields.CREATED_BY_NAME, ModuleFields.CREATED_BY_NAME);
        fieldsExcludedForEdit.put(ModuleFields.MODIFIED_USER_ID, ModuleFields.MODIFIED_USER_ID);
        fieldsExcludedForEdit.put(ModuleFields.MODIFIED_BY_NAME, ModuleFields.MODIFIED_BY_NAME);

        // add a field name to the map if a module field in detail projection is to be excluded in
        // details screen
        fieldsExcludedForDetails.put(SugarCRMContent.RECORD_ID, SugarCRMContent.RECORD_ID);
        fieldsExcludedForDetails.put(SugarCRMContent.SUGAR_BEAN_ID, SugarCRMContent.SUGAR_BEAN_ID);
        fieldsExcludedForDetails.put(ModuleFields.DELETED, ModuleFields.DELETED);
        fieldsExcludedForDetails.put(ModuleFields.ACCOUNT_ID, ModuleFields.ACCOUNT_ID);
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountsTable(db);
        createContactsTable(db);
        createLeadsTable(db);
        createOpportunitiesTable(db);
        createCasesTable(db);
        createCallsTable(db);
        createMeetingsTable(db);

        // create meta-data tables
        createModulesTable(db);
        createModuleFieldsTable(db);
        createLinkFieldsTable(db);

        createUsersTable(db);
        createAclRolesTable(db);
        createAclActionsTable(db);

        /*
         * createModuleFieldsSortOrderTable(db); createModuleFieldsGroupTable(db);
         */

        // create join tables

        createAccountsContactsTable(db);
        createAccountsOpportunitiesTable(db);
        createAccountsCasesTable(db);
        createContactsOpportunitiesTable(db);
        createContactsCases(db);

        // create sync tables
        createSyncTable(db);

    }

    void dropAccountsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME);
    }

    void dropContactsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
    }

    void dropLeadsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + LEADS_TABLE_NAME);
    }

    void dropOpportunitiesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + OPPORTUNITIES_TABLE_NAME);
    }

    void dropCasesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CASES_TABLE_NAME);
    }

    void dropCallsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CALLS_TABLE_NAME);
    }

    void dropMeetingsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MEETINGS_TABLE_NAME);
    }

    void dropModulesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MODULES_TABLE_NAME);
    }

    void dropModuleFieldsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MODULE_FIELDS_TABLE_NAME);
    }

    void dropLinkFieldsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + LINK_FIELDS_TABLE_NAME);
    }

    void dropAccountsContactsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_CONTACTS_TABLE_NAME);
    }

    void dropAccountsCasesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_CASES_TABLE_NAME);
    }

    void dropAccountsOpportunitiesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_OPPORTUNITIES_TABLE_NAME);
    }

    void dropContactsOpportunitiesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_OPPORTUNITIES_TABLE_NAME);
    }

    void dropContactsCasesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_CASES_TABLE_NAME);
    }

    void dropSyncTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + SYNC_TABLE_NAME);
    }

    void dropUsersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
    }

    void dropAclRolesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACL_ROLES_TABLE_NAME);
    }

    void dropAclActionsTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACL_ACTIONS_TABLE_NAME);
    }

    void dropModuleFieldsSortOrderTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MODULE_FIELDS_SORT_ORDER_TABLE_NAME);
    }

    void dropModuleFieldsGroupTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MODULE_FIELDS_GROUP_TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                                        + ", which will destroy all old data");
        // TODO - do not drop - only for development right now
        dropAllDataTables(db);
        onCreate(db);
    }

    private void dropAllDataTables(SQLiteDatabase db) {
        dropAccountsTable(db);
        dropContactsTable(db);
        dropLeadsTable(db);
        dropOpportunitiesTable(db);
        dropCasesTable(db);
        dropCallsTable(db);
        dropMeetingsTable(db);

        dropModulesTable(db);
        dropModuleFieldsTable(db);
        dropLinkFieldsTable(db);

        dropUsersTable(db);
        dropAclRolesTable(db);
        dropAclActionsTable(db);

        /*
         * dropModuleFieldsGroupTable(db); dropModuleFieldsSortOrderTable(db);
         */

        // drop join tables
        dropAccountsContactsTable(db);
        dropAccountsOpportunitiesTable(db);
        dropAccountsCasesTable(db);
        dropContactsOpportunitiesTable(db);
        dropContactsCasesTable(db);

        dropSyncTable(db);
    }

    private static void createAccountsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE_NAME + " (" + AccountsColumns.ID
                                        + " INTEGER PRIMARY KEY," + AccountsColumns.BEAN_ID
                                        + " TEXT," + AccountsColumns.NAME + " TEXT,"
                                        + AccountsColumns.EMAIL1 + " TEXT,"
                                        + AccountsColumns.PARENT_NAME + " TEXT,"
                                        + AccountsColumns.PHONE_OFFICE + " TEXT,"
                                        + AccountsColumns.PHONE_FAX + " TEXT,"
                                        + AccountsColumns.WEBSITE + " TEXT,"
                                        + AccountsColumns.EMPLOYEES + " TEXT,"
                                        + AccountsColumns.TICKER_SYMBOL + " TEXT,"
                                        + AccountsColumns.ANNUAL_REVENUE + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_STREET + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_STREET_2 + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_STREET_3 + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_STREET_4 + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_CITY + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_STATE + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_POSTALCODE + " TEXT,"
                                        + AccountsColumns.BILLING_ADDRESS_COUNTRY + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_STREET + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_STREET_2 + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_STREET_3 + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_STREET_4 + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_CITY + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_STATE + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_POSTALCODE + " TEXT,"
                                        + AccountsColumns.SHIPPING_ADDRESS_COUNTRY + " TEXT,"
                                        + AccountsColumns.ASSIGNED_USER_NAME + " TEXT,"
                                        + AccountsColumns.CREATED_BY_NAME + " TEXT,"
                                        + AccountsColumns.DATE_ENTERED + " TEXT,"
                                        + AccountsColumns.DATE_MODIFIED + " TEXT,"
                                        + AccountsColumns.DELETED + " INTEGER," + " UNIQUE("
                                        + AccountsColumns.BEAN_ID + ")" + ");");
    }

    private static void createContactsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + CONTACTS_TABLE_NAME + " (" + ContactsColumns.ID
                                        + " INTEGER PRIMARY KEY," + ContactsColumns.BEAN_ID
                                        + " TEXT," + ContactsColumns.FIRST_NAME + " TEXT,"
                                        + ContactsColumns.LAST_NAME + " TEXT,"
                                        + ContactsColumns.ACCOUNT_NAME + " TEXT,"
                                        + ContactsColumns.PHONE_MOBILE + " TEXT,"
                                        + ContactsColumns.PHONE_WORK + " TEXT,"
                                        + ContactsColumns.EMAIL1 + " TEXT,"
                                        + ContactsColumns.CREATED_BY + " TEXT,"
                                        + ContactsColumns.MODIFIED_BY_NAME + " TEXT,"
                                        + ContactsColumns.CREATED_BY_NAME + " TEXT,"
                                        + ContactsColumns.DATE_ENTERED + " TEXT,"
                                        + ContactsColumns.DATE_MODIFIED + " TEXT,"
                                        + ContactsColumns.DELETED + " INTEGER,"
                                        + ContactsColumns.ACCOUNT_ID + " INTEGER," + " UNIQUE("
                                        + ContactsColumns.BEAN_ID + ")" + ");");
    }

    private static void createLeadsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + LEADS_TABLE_NAME + " (" + LeadsColumns.ID
                                        + " INTEGER PRIMARY KEY," + LeadsColumns.BEAN_ID + " TEXT,"
                                        + LeadsColumns.FIRST_NAME + " TEXT,"
                                        + LeadsColumns.LAST_NAME + " TEXT,"
                                        + LeadsColumns.LEAD_SOURCE + " TEXT," + LeadsColumns.EMAIL1
                                        + " TEXT," + LeadsColumns.PHONE_WORK + " TEXT,"
                                        + LeadsColumns.PHONE_FAX + " TEXT,"
                                        + LeadsColumns.ACCOUNT_NAME + " TEXT," + LeadsColumns.TITLE
                                        + " TEXT," + LeadsColumns.ASSIGNED_USER_NAME + " TEXT,"
                                        + LeadsColumns.CREATED_BY_NAME + " TEXT,"
                                        + LeadsColumns.DATE_ENTERED + " TEXT,"
                                        + LeadsColumns.DATE_MODIFIED + " TEXT,"
                                        + LeadsColumns.DELETED + " INTEGER,"
                                        + LeadsColumns.ACCOUNT_ID + " INTEGER," + " UNIQUE("
                                        + LeadsColumns.BEAN_ID + ")" + ");");
    }

    private static void createOpportunitiesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + OPPORTUNITIES_TABLE_NAME + " (" + OpportunitiesColumns.ID
                                        + " INTEGER PRIMARY KEY," + OpportunitiesColumns.BEAN_ID
                                        + " TEXT," + OpportunitiesColumns.NAME + " TEXT,"
                                        + OpportunitiesColumns.ACCOUNT_NAME + " TEXT,"
                                        + OpportunitiesColumns.AMOUNT + " TEXT,"
                                        + OpportunitiesColumns.AMOUNT_USDOLLAR + " TEXT,"
                                        + OpportunitiesColumns.ASSIGNED_USER_ID + " TEXT,"
                                        + OpportunitiesColumns.ASSIGNED_USER_NAME + " TEXT,"
                                        + OpportunitiesColumns.CAMPAIGN_NAME + " TEXT,"
                                        + OpportunitiesColumns.CREATED_BY + " TEXT,"
                                        + OpportunitiesColumns.CREATED_BY_NAME + " TEXT,"
                                        + OpportunitiesColumns.CURRENCY_ID + " TEXT,"
                                        + OpportunitiesColumns.CURRENCY_NAME + " TEXT,"
                                        + OpportunitiesColumns.CURRENCY_SYMBOL + " TEXT,"
                                        + OpportunitiesColumns.DATE_CLOSED + " TEXT,"
                                        + OpportunitiesColumns.DATE_ENTERED + " TEXT,"
                                        + OpportunitiesColumns.DATE_MODIFIED + " TEXT,"
                                        + OpportunitiesColumns.DESCRIPTION + " TEXT,"
                                        + OpportunitiesColumns.LEAD_SOURCE + " TEXT,"
                                        + OpportunitiesColumns.MODIFIED_BY_NAME + " TEXT,"
                                        + OpportunitiesColumns.MODIFIED_USER_ID + " TEXT,"
                                        + OpportunitiesColumns.NEXT_STEP + " TEXT,"
                                        + OpportunitiesColumns.OPPORTUNITY_TYPE + " TEXT,"
                                        + OpportunitiesColumns.PROBABILITY + " TEXT,"
                                        + OpportunitiesColumns.SALES_STAGE + " TEXT,"
                                        + OpportunitiesColumns.DELETED + " INTEGER,"
                                        + OpportunitiesColumns.ACCOUNT_ID + " INTEGER,"
                                        + " UNIQUE(" + OpportunitiesColumns.BEAN_ID + ")" + ");");
    }

    private static void createCasesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + CASES_TABLE_NAME + " (" + Cases.ID + " INTEGER PRIMARY KEY,"
                                        + Cases.BEAN_ID + " TEXT," + Cases.NAME + " TEXT,"
                                        + Cases.CASE_NUMBER + " TEXT," + Cases.PRIORITY + " TEXT,"
                                        + Cases.ASSIGNED_USER_NAME + " TEXT," + Cases.STATUS
                                        + " TEXT," + Cases.DESCRIPTION + " TEXT,"
                                        + Cases.RESOLUTION + " TEXT," + Cases.CREATED_BY_NAME
                                        + " TEXT," + Cases.DATE_ENTERED + " TEXT,"
                                        + Cases.DATE_MODIFIED + " TEXT," + Cases.DELETED
                                        + " INTEGER," + " UNIQUE(" + Cases.BEAN_ID + ")" + ");");
    }

    private static void createCallsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + CALLS_TABLE_NAME + " (" + Calls.ID + " INTEGER PRIMARY KEY,"
                                        + Calls.BEAN_ID + " TEXT," + Calls.NAME + " TEXT,"
                                        + Calls.STATUS + " TEXT," + Calls.START_DATE + " TEXT,"
                                        + Calls.DURATION_HOURS + " TEXT," + Calls.DURATION_MINUTES
                                        + " TEXT," + Calls.ASSIGNED_USER_NAME + " TEXT,"
                                        + Calls.DESCRIPTION + " TEXT," + Calls.CREATED_BY_NAME
                                        + " TEXT," + Calls.DATE_ENTERED + " TEXT,"
                                        + Calls.DATE_MODIFIED + " TEXT," + Calls.DELETED
                                        + " INTEGER," + " UNIQUE(" + Calls.BEAN_ID + ")" + ");");
    }

    private static void createMeetingsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + MEETINGS_TABLE_NAME + " (" + Meetings.ID
                                        + " INTEGER PRIMARY KEY," + Meetings.BEAN_ID + " TEXT,"
                                        + Meetings.NAME + " TEXT," + Meetings.STATUS + " TEXT,"
                                        + Meetings.LOCATION + " TEXT," + Meetings.START_DATE
                                        + " TEXT," + Meetings.DURATION_HOURS + " TEXT,"
                                        + Meetings.DURATION_MINUTES + " TEXT,"
                                        + Meetings.ASSIGNED_USER_NAME + " TEXT,"
                                        + Meetings.DESCRIPTION + " TEXT,"
                                        + Meetings.CREATED_BY_NAME + " TEXT,"
                                        + Meetings.DATE_ENTERED + " TEXT," + Meetings.DATE_MODIFIED
                                        + " TEXT," + Meetings.DELETED + " INTEGER," + " UNIQUE("
                                        + Meetings.BEAN_ID + ")" + ");");
    }

    private static void createModulesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + MODULES_TABLE_NAME + " (" + ModuleColumns.ID
                                        + " INTEGER PRIMARY KEY," + ModuleColumns.MODULE_NAME
                                        + " TEXT," + " UNIQUE(" + ModuleColumns.MODULE_NAME + ")"
                                        + ");");
    }

    private static void createModuleFieldsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + MODULE_FIELDS_TABLE_NAME + " (" + ModuleFieldColumns.ID
                                        + " INTEGER PRIMARY KEY," + ModuleFieldColumns.NAME
                                        + " TEXT," + ModuleFieldColumns.LABEL + " TEXT,"
                                        + ModuleFieldColumns.TYPE + " TEXT,"
                                        + ModuleFieldColumns.IS_REQUIRED + " INTEGER,"
                                        + ModuleFieldColumns.MODULE_ID + " INTEGER" + ");");
    }

    private static void createLinkFieldsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + LINK_FIELDS_TABLE_NAME + " (" + LinkFieldColumns.ID
                                        + " INTEGER PRIMARY KEY," + LinkFieldColumns.NAME
                                        + " TEXT," + LinkFieldColumns.TYPE + " TEXT,"
                                        + LinkFieldColumns.RELATIONSHIP + " TEXT,"
                                        + LinkFieldColumns.MODULE + " TEXT,"
                                        + LinkFieldColumns.BEAN_NAME + " TEXT,"
                                        + LinkFieldColumns.MODULE_ID + " INTEGER" + ");");
    }

    private static void createAccountsContactsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACCOUNTS_CONTACTS_TABLE_NAME + " ("
                                        + AccountsContactsColumns.ACCOUNT_ID + " INTEGER ,"
                                        + AccountsContactsColumns.CONTACT_ID + " INTEGER ,"
                                        + AccountsContactsColumns.DATE_MODIFIED + " TEXT,"
                                        + AccountsContactsColumns.DELETED + " INTEGER,"
                                        + " UNIQUE(" + AccountsContactsColumns.ACCOUNT_ID + ","
                                        + AccountsContactsColumns.CONTACT_ID + ")" + ");");
    }

    private static void createAccountsOpportunitiesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACCOUNTS_OPPORTUNITIES_TABLE_NAME + " ("
                                        + AccountsOpportunitiesColumns.ACCOUNT_ID + " INTEGER ,"
                                        + AccountsOpportunitiesColumns.OPPORTUNITY_ID
                                        + " INTEGER ," + AccountsOpportunitiesColumns.DATE_MODIFIED
                                        + " TEXT," + AccountsOpportunitiesColumns.DELETED
                                        + " INTEGER," + " UNIQUE("
                                        + AccountsOpportunitiesColumns.ACCOUNT_ID + ","
                                        + AccountsOpportunitiesColumns.OPPORTUNITY_ID + ")" + ");");
    }

    private static void createAccountsCasesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACCOUNTS_CASES_TABLE_NAME + " ("
                                        + AccountsCasesColumns.ACCOUNT_ID + " INTEGER ,"
                                        + AccountsCasesColumns.CASE_ID + " INTEGER ,"
                                        + AccountsCasesColumns.DATE_MODIFIED + " TEXT,"
                                        + AccountsCasesColumns.DELETED + " INTEGER," + " UNIQUE("
                                        + AccountsCasesColumns.ACCOUNT_ID + ","
                                        + AccountsCasesColumns.CASE_ID + ")" + ");");
    }

    private static void createContactsOpportunitiesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + CONTACTS_OPPORTUNITIES_TABLE_NAME + " ("
                                        + ContactsOpportunitiesColumns.CONTACT_ID + " INTEGER ,"
                                        + ContactsOpportunitiesColumns.OPPORTUNITY_ID
                                        + " INTEGER ," + ContactsOpportunitiesColumns.DATE_MODIFIED
                                        + " TEXT," + ContactsOpportunitiesColumns.DELETED
                                        + " INTEGER," + " UNIQUE("
                                        + ContactsOpportunitiesColumns.CONTACT_ID + ","
                                        + ContactsOpportunitiesColumns.OPPORTUNITY_ID + ")" + ");");
    }

    private static void createContactsCases(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + CONTACTS_CASES_TABLE_NAME + " ("
                                        + ContactsCasesColumns.CONTACT_ID + " INTEGER ,"
                                        + ContactsCasesColumns.CASE_ID + " INTEGER ,"
                                        + ContactsCasesColumns.DATE_MODIFIED + " TEXT,"
                                        + ContactsCasesColumns.DELETED + " INTEGER," + " UNIQUE("
                                        + ContactsCasesColumns.CONTACT_ID + ","
                                        + ContactsCasesColumns.CASE_ID + ")" + ");");
    }

    private static void createSyncTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + SYNC_TABLE_NAME + " (" + Sync.ID + " INTEGER PRIMARY KEY,"
                                        + Sync.SYNC_ID + " INTEGER ," + Sync.SYNC_RELATED_ID
                                        + " INTEGER ," + Sync.SYNC_COMMAND + " INTEGER,"
                                        + Sync.MODULE + " TEXT," + Sync.RELATED_MODULE + " TEXT,"
                                        + Sync.DATE_MODIFIED + " TEXT," + Sync.SYNC_STATUS
                                        + " INTEGER," + " UNIQUE(" + Sync.SYNC_ID + ","
                                        + Sync.MODULE + "," + Sync.RELATED_MODULE + ")" + ");");
    }

    private static void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + USERS_TABLE_NAME + " (" + Users.ID + " INTEGER PRIMARY KEY,"
                                        + Users.USER_ID + " INTEGER," + Users.USER_NAME + " TEXT,"
                                        + Users.FIRST_NAME + " TEXT," + Users.LAST_NAME + " TEXT,"
                                        + " UNIQUE(" + Users.USER_NAME + ")" + ");");
    }

    private static void createAclRolesTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACL_ROLES_TABLE_NAME + " (" + ACLRoleColumns.ID
                                        + " INTEGER PRIMARY KEY," + ACLRoleColumns.ROLE_ID
                                        + " INTEGER," + ACLRoleColumns.NAME + " TEXT,"
                                        + ACLRoleColumns.TYPE + " TEXT,"
                                        + ACLRoleColumns.DESCRIPTION + " TEXT," + " UNIQUE("
                                        + ACLRoleColumns.ROLE_ID + ")" + ");");
    }

    private static void createAclActionsTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + ACL_ACTIONS_TABLE_NAME + " (" + ACLActionColumns.ID
                                        + " INTEGER PRIMARY KEY," + ACLActionColumns.ACTION_ID
                                        + " INTEGER," + ACLActionColumns.NAME + " INTEGER,"
                                        + ACLActionColumns.CATEGORY + " TEXT,"
                                        + ACLActionColumns.ACLACCESS + " TEXT,"
                                        + ACLActionColumns.ACLTYPE + " TEXT,"
                                        + ACLActionColumns.ROLE_ID + " INTEGER," + " UNIQUE("
                                        + ACLActionColumns.ACTION_ID + ")" + ");");
    }

    private static void createModuleFieldsSortOrderTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + MODULE_FIELDS_SORT_ORDER_TABLE_NAME + " ("
                                        + ModuleFieldSortOrderColumns.ID + " INTEGER PRIMARY KEY,"
                                        + ModuleFieldSortOrderColumns.FIELD_SORT_ID + " INTEGER,"
                                        + ModuleFieldSortOrderColumns.GROUP_ID + " INTEGER,"
                                        + ModuleFieldSortOrderColumns.MODULE_FIELD_ID + " INTEGER,"
                                        + ModuleFieldSortOrderColumns.MODULE_ID + " INTEGER" + ");");
    }

    private static void createModuleFieldsGroupTable(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + MODULE_FIELDS_GROUP_TABLE_NAME + " ("
                                        + ModuleFieldGroupColumns.ID + " INTEGER PRIMARY KEY,"
                                        + ModuleFieldGroupColumns.TITLE + " TEXT,"
                                        + ModuleFieldGroupColumns.GROUP_ID + " INTEGER" + ");");
    }

    private void setAclAccessMap() {
        // get the module list
        List<String> moduleNames = getModuleList();

        SQLiteDatabase db = getReadableDatabase();
        for (String moduleName : moduleNames) {
            String selection = "(" + ACLActionColumns.CATEGORY + "= '" + moduleName + "')";
            Cursor cursor = db.query(DatabaseHelper.ACL_ACTIONS_TABLE_NAME, ACLActions.DETAILS_PROJECTION, selection, null, null, null, null);
            cursor.moveToFirst();

            // access map for each module
            Map<String, Integer> moduleAccessMap = new HashMap<String, Integer>();
            for (int i = 0; i < cursor.getCount(); i++) {
                String name = cursor.getString(2);
                String category = cursor.getString(3);
                int aclAccess = cursor.getInt(4);
                String aclType = cursor.getString(5);

                moduleAccessMap.put(name, aclAccess);
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, name + " " + category + " " + aclAccess + " " + aclType);

                cursor.moveToNext();
            }
            if (moduleAccessMap.size() > 0)
                accessMap.put(moduleName, moduleAccessMap);
            cursor.close();
        }
    }

    private Map<String, Map<String, Integer>> getAclAccessMap() {
        if (accessMap != null && accessMap.size() != 0) {
            return accessMap;
        } else {
            setAclAccessMap();
            return accessMap;
        }
    }

    public boolean isAclEnabled(String moduleName, String name) {
        return isAclEnabled(moduleName, name, null);
    }

    public boolean isAclEnabled(String moduleName, String name, String ownerName) {
        Map<String, Map<String, Integer>> aclAccessMap = getAclAccessMap();
        // TODO - checkk if syncd ACLRoles and Actions succesfully- if no roles are given to a user,
        // then we give access to the entire application
        if (aclAccessMap.size() == 0)
            return true;
        Map<String, Integer> moduleAccessMap = aclAccessMap.get(moduleName);
        // if (moduleAccessMap == null || moduleAccessMap.size() == 0)
        // return true;
        int aclAccess = moduleAccessMap.get(name);
        switch (aclAccess) {
        case ACLConstants.ACL_ALLOW_ADMIN:
            break;
        case ACLConstants.ACL_ALLOW_ADMIN_DEV:
            break;
        case ACLConstants.ACL_ALLOW_ALL:
            return true;
        case ACLConstants.ACL_ALLOW_DEFAULT:
            break;
        case ACLConstants.ACL_ALLOW_DEV:
            break;
        case ACLConstants.ACL_ALLOW_DISABLED:
            return false;
        case ACLConstants.ACL_ALLOW_ENABLED:
            return true;
        case ACLConstants.ACL_ALLOW_NONE:
            return false;
        case ACLConstants.ACL_ALLOW_NORMAL:
            break;
        case ACLConstants.ACL_ALLOW_OWNER:
            if (ownerName != null) {
                // TODO: get the user name from Account Manager
                String userName = SugarCrmSettings.getUsername(mContext);
                return userName.equals(ownerName) ? true : false;
            } else {
                return false;
            }
        }
        return true;
    }

    public Map<String, String> getFieldsExcludedForEdit() {
        return fieldsExcludedForEdit;
    }

    public Map<String, String> getFieldsExcludedForDetails() {
        return fieldsExcludedForDetails;
    }

    public String[] getModuleProjections(String moduleName) {
        return moduleProjections.get(moduleName);
    }
    
    public String[] getModuleListProjections(String moduleName) {
        return moduleListProjections.get(moduleName);
    }

    public String[] getModuleListSelections(String moduleName) {
        return moduleListSelections.get(moduleName);
    }

    public String getModuleSortOrder(String moduleName) {
        return moduleSortOrder.get(moduleName);
    }

    public Uri getModuleUri(String moduleName) {
        return moduleUris.get(moduleName);
    }

    public String getModuleSelection(String moduleName, String searchString) {
        // TODO: modify this if the selection criteria has to be applied on a different module field
        // for a module
        if (moduleName.equals(Util.CONTACTS) || moduleName.equals(Util.LEADS)) {
            return "(" + LeadsColumns.FIRST_NAME + " LIKE '%" + searchString + "%' OR "
                                            + LeadsColumns.LAST_NAME + " LIKE '%" + searchString
                                            + "%'" + ")";
        } else {
            // for Accounts, Opportunities, Cases, Calls and Mettings
            return ModuleFields.NAME + " LIKE '%" + searchString + "%'";
        }
    }

    // TODO - get from DB
    public String[] getModuleRelationshipItems(String moduleName) {
        return moduleRelationshipItems.get(moduleName);
    }

    public List<String> getModuleList() {
        List<String> userModules = getUserModules();
        List<String> supportedModules = Arrays.asList(getSupportedModulesList());
        List<String> modules = new ArrayList<String>();
        for (String module : userModules) {
            if (supportedModules.contains(module)) {
                modules.add(module);
            }
        }
        return modules;
        // TODO: return the module List after the exclusion of modules from the user moduleList
        // return moduleList;
    }

    /**
     * while fetching relationship module items, we need to determine if current user has access to
     * that module, this module should be present in the modules available to the user
     * 
     * @param moduleName
     * @return
     */
    public boolean isModuleAccessAvailable(String moduleName) {
        // userModules list is already sorted when querying from DB, hey we can as well go against
        // the db or use a map
        List<String> userModules = getUserModules();
        int index = Collections.binarySearch(userModules, moduleName);
        return index < 0 ? false : true;
    }

    public String getLinkfieldName(String moduleName) {
        return linkfieldNames.get(moduleName);
    }

    public String[] getSupportedModulesList() {
        return defaultSupportedModules;
    }

    public int getModuleIcon(String moduleName) {
        Integer iconResource = moduleIcons.get(moduleName);
        if (iconResource == null)
            return android.R.drawable.alert_dark_frame;
        return iconResource;
    }

    public List<String> getBillingAddressGroup() {
        return billingAddressGroup;
    }

    public List<String> getShippingAddressGroup() {
        return shippingAddressGroup;
    }

    public List<String> getDurationGroup() {
        return durationGroup;
    }

    public ModuleField getModuleField(String moduleName, String fieldName) {
        SQLiteDatabase db = getReadableDatabase();
        ModuleField moduleField = null;

        String selection = ModuleColumns.MODULE_NAME + "='" + moduleName + "'";
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, selection, null, null, null, null);
        int num = cursor.getCount();
        if (num > 0) {
            cursor.moveToFirst();
            String moduleId = cursor.getString(0);
            cursor.close();

            selection = "(" + ModuleFieldColumns.MODULE_ID + "=" + moduleId + " AND "
                                            + ModuleFieldColumns.NAME + "='" + fieldName + "')";
            cursor = db.query(MODULE_FIELDS_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleField.DETAILS_PROJECTION, selection, null, null, null, null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
                moduleField = new ModuleField(cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.NAME)), cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.TYPE)), cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.LABEL)), cursor.getInt(cursor.getColumnIndex(ModuleFieldColumns.IS_REQUIRED)) == 1 ? true

                                                : false);
        }
        cursor.close();

        db.close();

        return moduleField;
    }

    public Map<String, ModuleField> getModuleFields(String moduleName) {
        if (moduleFields != null) {
            HashMap<String, ModuleField> map = moduleFields.get(moduleName);
            if (map != null && map.size() > 0)
                return map;
        } else
            moduleFields = new HashMap<String, HashMap<String, ModuleField>>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = ModuleColumns.MODULE_NAME + "='" + moduleName + "'";
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        String moduleId = cursor.getString(0);
        cursor.close();

        // name of the module field is the key and ModuleField is the value
        HashMap<String, ModuleField> fieldNameVsModuleField = new HashMap<String, ModuleField>();
        selection = ModuleFieldColumns.MODULE_ID + "=" + moduleId;
        cursor = db.query(MODULE_FIELDS_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleField.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String name = cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.NAME));
            ModuleField moduleField = new ModuleField(name, cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.TYPE)), cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.LABEL)), cursor.getInt(cursor.getColumnIndex(ModuleFieldColumns.IS_REQUIRED)) == 1 ? true
                                            : false);
            cursor.moveToNext();
            fieldNameVsModuleField.put(name, moduleField);
        }
        cursor.close();
        db.close();
        moduleFields.put(moduleName, fieldNameVsModuleField);
        return fieldNameVsModuleField;
    }

    public Map<String, LinkField> getLinkFields(String moduleName) {
        if (linkFields != null) {
            HashMap<String, LinkField> map = linkFields.get(moduleName);
            if (map != null && map.size() > 0)
                return map;
        } else
            linkFields = new HashMap<String, HashMap<String, LinkField>>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = ModuleColumns.MODULE_NAME + "='" + moduleName + "'";
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        String moduleId = cursor.getString(0);
        cursor.close();

        // name of the link field is the key and LinkField is the value
        HashMap<String, LinkField> nameVsLinkField = new HashMap<String, LinkField>();
        selection = LinkFieldColumns.MODULE_ID + "=" + moduleId;
        cursor = db.query(LINK_FIELDS_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.LinkFields.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String name = cursor.getString(1);
            LinkField linkField = new LinkField(name, cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
            cursor.moveToNext();
            nameVsLinkField.put(name, linkField);
        }
        cursor.close();
        db.close();
        linkFields.put(moduleName, nameVsLinkField);
        return nameVsLinkField;
    }

    /*
     * gives all the available user modules
     */
    public List<String> getUserModules() {
        SQLiteDatabase db = getReadableDatabase();
        moduleList = new ArrayList<String>();
        // get a sorted list with module_name OrderBy
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, null, null, null, null, ModuleColumns.MODULE_NAME);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String moduleName = cursor.getString(cursor.getColumnIndex(ModuleColumns.MODULE_NAME));
            moduleList.add(moduleName);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return moduleList;
    }

    public void setUserModules(List<String> moduleNames) throws SugarCrmException {
        boolean hasFailed = false;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (String moduleName : moduleNames) {
            ContentValues values = new ContentValues();
            values.put(ModuleColumns.MODULE_NAME, moduleName);
            long rowId = db.insert(MODULES_TABLE_NAME, "", values);
            if (rowId <= 0) {
                hasFailed = true;
                break;
            }
        }

        if (hasFailed) {
            db.endTransaction();
            db.close();
            throw new SugarCrmException("FAILED to insert Modules!");
        } else {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public void setModuleFieldsInfo(Set<Module> moduleFieldsInfo) throws SugarCrmException {
        boolean hasFailed = false;

        for (Module module : moduleFieldsInfo) {
            // get module row id
            SQLiteDatabase db = getReadableDatabase();
            String selection = ModuleColumns.MODULE_NAME + "='" + module.getModuleName() + "'";
            Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, selection, null, null, null, null);
            cursor.moveToFirst();
            String moduleId = cursor.getString(0);
            cursor.close();
            db.close();

            db = getWritableDatabase();
            db.beginTransaction();
            List<ModuleField> moduleFields = module.getModuleFields();
            for (ModuleField moduleField : moduleFields) {
                ContentValues values = new ContentValues();
                values.put(ModuleFieldColumns.NAME, moduleField.getName());
                values.put(ModuleFieldColumns.LABEL, moduleField.getLabel());
                values.put(ModuleFieldColumns.TYPE, moduleField.getType());
                values.put(ModuleFieldColumns.IS_REQUIRED, moduleField.isRequired());
                values.put(ModuleFieldColumns.MODULE_ID, moduleId);
                long rowId = db.insert(MODULE_FIELDS_TABLE_NAME, "", values);
                if (rowId <= 0) {
                    hasFailed = true;
                    break;
                }
            }

            if (!hasFailed) {
                List<LinkField> linkFields = module.getLinkFields();
                for (LinkField linkField : linkFields) {
                    ContentValues values = new ContentValues();
                    values.put(LinkFieldColumns.NAME, linkField.getName());
                    values.put(LinkFieldColumns.TYPE, linkField.getType());
                    values.put(LinkFieldColumns.RELATIONSHIP, linkField.getRelationship());
                    values.put(LinkFieldColumns.MODULE, linkField.getModule());
                    values.put(LinkFieldColumns.BEAN_NAME, linkField.getBeanName());
                    values.put(ModuleFieldColumns.MODULE_ID, moduleId);
                    long rowId = db.insert(LINK_FIELDS_TABLE_NAME, "", values);
                    if (rowId < 0) {
                        hasFailed = true;
                        break;
                    }
                }
            }

            if (hasFailed) {
                db.endTransaction();
                db.close();
                throw new SugarCrmException("FAILED to insert module fields!");
            } else {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * get a Sync record given syncId and moduleName
     * 
     * @param syncId
     * @param moduleName
     * @return
     */
    public SyncRecord getSyncRecord(long syncId, String moduleName) {
        // TODO -currently we are storing module name in both the fields in database if only orphans
        // are involved, if related items
        // if DB is switched to use null, then change this
        String relatedModuleName = moduleName;
        return getSyncRecord(syncId, moduleName, relatedModuleName);
    }

    /**
     * get a Sync record given syncId and moduleName
     * 
     * @param syncId
     * @param moduleName
     * @return
     */
    public SyncRecord getSyncRecord(long syncId, String moduleName, String relatedModuleName) {
        SyncRecord record = null;
        SQLiteDatabase db = getReadableDatabase();
        String selection = Util.SYNC_ID + "=?" + " AND " + RestUtilConstants.MODULE + "=?"
                                        + " AND " + Util.RELATED_MODULE + "=?";
        String selectionArgs[] = new String[] { "" + syncId, moduleName, relatedModuleName };
        Cursor cursor = db.query(SYNC_TABLE_NAME, Sync.DETAILS_PROJECTION, selection, selectionArgs, null, null, null);
        int num = cursor.getCount();

        if (num > 0) {
            cursor.moveToFirst();
            record = new SyncRecord();
            record._id = cursor.getLong(Sync.ID_COLUMN);
            record.syncId = cursor.getLong(Sync.SYNC_ID_COLUMN);
            record.syncRelatedId = cursor.getLong(Sync.SYNC_RELATED_ID_COLUMN);
            record.syncCommand = cursor.getInt(Sync.SYNC_COMMAND_COLUMN);
            record.moduleName = cursor.getString(Sync.MODULE_NAME_COLUMN);
            record.relatedModuleName = cursor.getString(Sync.RELATED_MODULE_NAME_COLUMN);
            record.status = cursor.getInt(Sync.STATUS_COLUMN);
        }
        cursor.close();
        db.close();

        return record;

    }

    /**
     * gets the unsynced sync records from the sync table
     * 
     * @param moduleName
     * @return
     */
    public Cursor getSyncRecords(String moduleName, int status) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = RestUtilConstants.MODULE + "=?" + " AND " + Util.STATUS + "=?";
        String selectionArgs[] = new String[] { moduleName, "" + status };

        Cursor cursor = db.query(DatabaseHelper.SYNC_TABLE_NAME, Sync.DETAILS_PROJECTION, selection, selectionArgs, null, null, null);
        return cursor;
    }

    public Cursor getConflictingSyncRecords(String moduleName) {
        return getSyncRecords(moduleName, Util.SYNC_CONFLICTS);
    }

    public Cursor getSyncRecordsToSync(String moduleName) {
        return getSyncRecords(moduleName, Util.UNSYNCED);
    }

    public Map<String, ModuleFieldBean> getModuleProjectionInOrder(String moduleName) {
        int moduleId = getModuleId(moduleName);

        Map<String, ModuleFieldBean> moduleFieldMap = new LinkedHashMap<String, ModuleFieldBean>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = ModuleFieldSortOrderColumns.MODULE_ID + "=" + moduleId + "";
        // using the DETAILS_PROJECTION here to select the columns
        Cursor cursor = db.query(DatabaseHelper.MODULE_FIELDS_SORT_ORDER_TABLE_NAME, ModuleFieldSortOrder.DETAILS_PROJECTION, selection, null, null, null, ModuleFieldSortOrder.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        // iterating through the module_fields_sort_order table in the ascending sort order
        for (int i = 0; i < cursor.getCount(); i++) {
            int sortId = cursor.getInt(1);
            int groupId = cursor.getInt(2);
            int moduleFieldId = cursor.getInt(3);

            // get the module field details from the module_fields table for the moduleFieldId
            selection = ModuleFieldColumns.ID + "=" + moduleFieldId + "";
            Cursor moduleFieldCursor = db.query(DatabaseHelper.MODULE_FIELDS_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleField.DETAILS_PROJECTION, selection, null, null, null, null);
            moduleFieldCursor.moveToFirst();
            String moduleFieldName = moduleFieldCursor.getString(1);
            ModuleField moduleFieldObj = new ModuleField(moduleFieldName, moduleFieldCursor.getString(3), moduleFieldCursor.getString(2), moduleFieldCursor.getInt(4) == 1 ? true
                                            : false);
            moduleFieldCursor.close();

            // create ModuleFieldBean to store the ModuleField, its sortOrder and groupId
            ModuleFieldBean moduleFieldBean = new ModuleFieldBean(moduleFieldObj, moduleFieldId, sortId, groupId);
            moduleFieldMap.put(moduleFieldName, moduleFieldBean);

            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return moduleFieldMap;
    }

    /*
     * get the moduleId given the name of the module
     */
    private int getModuleId(String moduleName) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = ModuleColumns.MODULE_NAME + "='" + moduleName + "'";
        // using the DETAILS_PROJECTION here to select the columns
        Cursor cursor = db.query(DatabaseHelper.MODULES_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.Modules.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        int moduleId = cursor.getInt(0);
        cursor.close();
        db.close();

        return moduleId;
    }

    /**
     * // TODO - when do we update ?? - not required -??
     * 
     * @param record
     * @return
     * @throws SugarCrmException
     */
    public int updateSyncRecord(SyncRecord record) throws SugarCrmException {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put(SyncColumns.ID, record._id);
        values.put(SyncColumns.SYNC_ID, record.syncId);
        values.put(SyncColumns.SYNC_RELATED_ID, record.syncRelatedId);
        // values.put(SyncColumns.SYNC_COMMAND, record.syncCommand);
        values.put(SyncColumns.MODULE, record.moduleName);
        values.put(SyncColumns.RELATED_MODULE, record.relatedModuleName);
        values.put(SyncColumns.SYNC_STATUS, record.status);

        int rowId = db.update(SYNC_TABLE_NAME, values, Sync.ID + "=?", new String[] { ""
                                        + record._id });
        if (rowId < 0)
            throw new SugarCrmException("FAILED to update sync record!");
        return rowId;
    }

    /**
     * updateSyncRecord
     * 
     * @param syncRecordId
     * @param values
     * @return
     */
    public int updateSyncRecord(long syncRecordId, ContentValues values) throws SugarCrmException {
        SQLiteDatabase db = getWritableDatabase();
        int rowId = db.update(SYNC_TABLE_NAME, values, Sync.ID + "=?", new String[] { ""
                                        + syncRecordId });
        if (rowId < 0)
            throw new SugarCrmException("FAILED to update sync record!");
        return rowId;
    }

    public long insertSyncRecord(SyncRecord record) throws SugarCrmException {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // values.put(SyncColumns.ID, record._id);
        values.put(SyncColumns.SYNC_ID, record.syncId);
        values.put(SyncColumns.SYNC_RELATED_ID, record.syncRelatedId);
        values.put(SyncColumns.SYNC_COMMAND, record.syncCommand);
        values.put(SyncColumns.MODULE, record.moduleName);
        values.put(SyncColumns.RELATED_MODULE, record.relatedModuleName);
        values.put(SyncColumns.SYNC_STATUS, record.status);

        long rowId = db.insert(SYNC_TABLE_NAME, "", values);
        if (rowId < 0)
            throw new SugarCrmException("FAILED to insert sync record!");
        return rowId;
    }

    /**
     * deletes a syncrecord based on the syncRecdordId (_id)
     * 
     * @param syncRecordId
     * @return
     */
    public int deleteSyncRecord(long syncRecordId) {
        SQLiteDatabase db = getWritableDatabase();
        // String accountId = uri.getPathSegments().get(1);
        int count = db.delete(DatabaseHelper.SYNC_TABLE_NAME, Sync.ID + "=" + syncRecordId, null);
        // + (!TextUtils.isEmpty(where) ? " AND (" + where + ')'
        // : ""), whereArgs);
        return count;
    }

    public void insertActions(String roleId, SugarBean[] roleRelationBeans)
                                    throws SugarCrmException {
        boolean hasFailed = false;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (SugarBean actionBean : roleRelationBeans) {

            ContentValues values = new ContentValues();
            String[] aclActionFields = ACLActions.INSERT_PROJECTION;
            for (int i = 0; i < aclActionFields.length; i++) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, actionBean.getFieldValue(aclActionFields[i]));

                values.put(aclActionFields[i], actionBean.getFieldValue(aclActionFields[i]));
            }

            // get the row id of the role
            String selection = ACLRoleColumns.ROLE_ID + "='" + roleId + "'";
            Cursor cursor = db.query(DatabaseHelper.ACL_ROLES_TABLE_NAME, ACLRoles.DETAILS_PROJECTION, selection, null, null, null, null);
            cursor.moveToFirst();
            int roleRowId = cursor.getInt(0);
            cursor.close();

            values.put(ACLActionColumns.ROLE_ID, roleRowId);
            long rowId = db.insert(ACL_ACTIONS_TABLE_NAME, "", values);
            if (rowId < 0) {
                hasFailed = true;
                break;
            }
        }
        if (hasFailed) {
            db.endTransaction();
            db.close();
            throw new SugarCrmException("FAILED to insert ACL Actions!");
        } else {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public List<String> insertRoles(SugarBean[] roleBeans) throws SugarCrmException {
        boolean hasFailed = false;

        List<String> roleIds = new ArrayList<String>();
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < roleBeans.length; i++) {
            ContentValues values = new ContentValues();
            for (String fieldName : ACLRoles.INSERT_PROJECTION) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, fieldName + " : " + roleBeans[i].getFieldValue(fieldName));

                if (fieldName.equals(ModuleFields.ID)) {
                    roleIds.add(roleBeans[i].getFieldValue(fieldName));
                }
                values.put(fieldName, roleBeans[i].getFieldValue(fieldName));
            }
            long rowId = db.insert(ACL_ROLES_TABLE_NAME, "", values);
            if (rowId < 0) {
                hasFailed = true;
                break;
            }
        }

        if (hasFailed) {
            db.close();
            throw new SugarCrmException("FAILED to insert ACL Roles!");
        } else {
            db.close();
        }

        return roleIds;
    }

    // key : userName value: userValues
    public void insertUsers(Map<String, Map<String, String>> usersList) throws SugarCrmException {
        boolean hasFailed = false;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        for (Entry<String, Map<String, String>> entry : usersList.entrySet()) {
            String userName = entry.getKey();
            ContentValues values = new ContentValues();
            Map<String, String> userListValues = entry.getValue();
            for (Entry<String, String> userEntry : userListValues.entrySet()) {
                values.put(userEntry.getKey(), userEntry.getValue());
            }
            long rowId = db.insert(USERS_TABLE_NAME, "", values);
            if (rowId < 0) {
                hasFailed = true;
                break;
            }
        }
        if (hasFailed) {
            db.endTransaction();
            db.close();
            throw new SugarCrmException("FAILED to insert Users!");
        } else {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    /**
     * executes SQL statements from a SQL file name present in assets folder
     * 
     * @param fileName
     */
    public void executeSQLFromFile(String fileName) throws SugarCrmException {
        SQLiteDatabase db = getWritableDatabase();
        try {
            InputStream is = mContext.getAssets().open(fileName);
            db.beginTransaction();
            /*
             * Use the openFileInput() method the ActivityContext provides. Again for security
             * reasons with openFileInput(...)
             */
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String sql;
            while ((sql = br.readLine()) != null) {
                db.execSQL(sql);
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "read from file: " + sql);
            }

            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            throw new SugarCrmException("FAILED to execute SQL from file");
        }
        db.close();
    }

    /**
     * Returns the beanId id , or null if the item is not found.
     * 
     */
    public String lookupBeanId(String moduleName, String rowId) {
        ContentResolver resolver = mContext.getContentResolver();
        String beanId = null;
        Uri contentUri = getModuleUri(moduleName);
        String[] projection = new String[] { SugarCRMContent.SUGAR_BEAN_ID };

        final Cursor c = resolver.query(contentUri, projection, mSelection, new String[] { rowId }, null);
        try {
            if (c.moveToFirst()) {
                beanId = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return beanId;
    }
}
