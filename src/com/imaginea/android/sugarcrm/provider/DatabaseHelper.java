package com.imaginea.android.sugarcrm.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.imaginea.android.sugarcrm.R;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Accounts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsContactsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsOpportunitiesColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Leads;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LeadsColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LinkFieldColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleFieldColumns;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Modules;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Opportunities;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.OpportunitiesColumns;
import com.imaginea.android.sugarcrm.util.LinkField;
import com.imaginea.android.sugarcrm.util.Module;
import com.imaginea.android.sugarcrm.util.ModuleField;
import com.imaginea.android.sugarcrm.util.SugarCrmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sugar_crm.db";

    // TODO: RESET the database version to 1
    private static final int DATABASE_VERSION = 15;

    public static final String ACCOUNTS_TABLE_NAME = "accounts";

    public static final String CONTACTS_TABLE_NAME = "contacts";

    public static final String ACCOUNTS_CONTACTS_TABLE_NAME = "accounts_contacts";

    public static final String ACCOUNTS_OPPORTUNITIES_TABLE_NAME = "accounts_opportunities";

    public static final String LEADS_TABLE_NAME = "leads";

    public static final String OPPORTUNITIES_TABLE_NAME = "opportunities";

    public static final String MEETINGS_TABLE_NAME = "meetings";

    public static final String CALLS_TABLE_NAME = "calls";

    public static final String MODULES_TABLE_NAME = "modules";

    public static final String MODULE_FIELDS_TABLE_NAME = "module_fields";

    public static final String LINK_FIELDS_TABLE_NAME = "link_fields";

    private static final String TAG = DatabaseHelper.class.getSimpleName();
    
    private String[] defaultSupportedModules = {"Accounts", "Contacts", "Leads", "Opportunities"};
    
    private static HashMap<String, Integer> moduleIcons = new HashMap<String, Integer>();
    
    // TODO - replace with database calls - dynamic module generation
    private static List<String> moduleList;

    private static final HashMap<String, String[]> moduleProjections = new HashMap<String, String[]>();

    private static final HashMap<String, String[]> moduleListSelections = new HashMap<String, String[]>();

    private static final HashMap<String, String> moduleSortOrder = new HashMap<String, String>();

    private static final HashMap<String, Uri> moduleUris = new HashMap<String, Uri>();

    private static final HashMap<String, String> moduleSelections = new HashMap<String, String>();

    private static HashMap<String, HashMap<String, ModuleField>> moduleFields;

    private static final HashMap<String, String[]> moduleRelationshipItems = new HashMap<String, String[]>();

    private static final HashMap<String, String> linkfieldNames = new HashMap<String, String>();

    private static final HashMap<String, String> pathForRelationship = new HashMap<String, String>();

    private static final HashMap<String, String> relationshipForPath = new HashMap<String, String>();

    static {

        moduleIcons.put("Accounts", R.drawable.account);
        moduleIcons.put("Contacts", R.drawable.contacts);
        moduleIcons.put("Leads", R.drawable.leads);
        moduleIcons.put("Opportunities", R.drawable.opportunity);
        moduleIcons.put("Settings", R.drawable.settings);
        
        moduleProjections.put("Accounts", Accounts.DETAILS_PROJECTION);
        moduleProjections.put("Contacts", Contacts.DETAILS_PROJECTION);
        moduleProjections.put("Leads", Leads.DETAILS_PROJECTION);
        moduleProjections.put("Opportunities", Opportunities.DETAILS_PROJECTION);
        // moduleProjections.put(4, Meetings.DETAILS_PROJECTION );

        moduleListSelections.put("Accounts", Accounts.LIST_VIEW_PROJECTION);
        moduleListSelections.put("Contacts", Contacts.LIST_VIEW_PROJECTION);
        moduleListSelections.put("Leads", Leads.LIST_VIEW_PROJECTION);
        moduleListSelections.put("Opportunities", Opportunities.LIST_VIEW_PROJECTION);

        moduleSortOrder.put("Accounts", Accounts.DEFAULT_SORT_ORDER);
        moduleSortOrder.put("Contacts", Contacts.DEFAULT_SORT_ORDER);

        moduleUris.put("Accounts", Accounts.CONTENT_URI);
        moduleUris.put("Contacts", Contacts.CONTENT_URI);
        moduleUris.put("Leads", Leads.CONTENT_URI);
        moduleUris.put("Opportunities", Opportunities.CONTENT_URI);

        // TODO - complete this list
        // moduleRelationshipItems.put("Accounts", new String[] { "Contacts", "Leads",
        // "Opportunities" });
        moduleRelationshipItems.put("Accounts", new String[] { "Contacts", "Opportunities" });
        moduleRelationshipItems.put("Contacts", new String[] { "Leads", "Opportunities" });
        moduleRelationshipItems.put("Leads", new String[] { "Opportunities", "Contacts" });
        moduleRelationshipItems.put("Opportunities", new String[] { "Leads", "Contacts" });

        pathForRelationship.put("Contacts", "contact");
        pathForRelationship.put("Leads", "lead");
        pathForRelationship.put("Opportunities", "opportunity");

        relationshipForPath.put("account", "Accounts");
        relationshipForPath.put("contact", "Contacts");
        relationshipForPath.put("lead", "Leads");
        relationshipForPath.put("opportunity", "Opportunities");

        linkfieldNames.put("Contacts", "contacts");
        linkfieldNames.put("Leads", "leads");
        linkfieldNames.put("Opportunities", "opportunities");
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAccountsTable(db);
        createContactsTable(db);
        createLeadsTable(db);
        createOpportunitiesTable(db);

        createModulesTable(db);
        createModuleFieldsTable(db);
        createLinkFieldsTable(db);
         // create join tables

        createAccountsContactsTable(db);
        createAccountsOpportunitiesTable(db);
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
    
    void dropAccountsOpportunitiesTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_OPPORTUNITIES_TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                                        + ", which will destroy all old data");
        // TODO - do not drop - only for development right now
        dropAllDataTables(db);
        onCreate(db);
    }
    
    private void dropAllDataTables(SQLiteDatabase db)
    {
        dropAccountsTable(db);
        dropContactsTable(db);
        dropLeadsTable(db);
        dropOpportunitiesTable(db);

        dropModulesTable(db);
        dropModuleFieldsTable(db);
        dropLinkFieldsTable(db);


        // drop join tables
        dropAccountsContactsTable(db);
        dropAccountsOpportunitiesTable(db);
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
                                        + AccountsColumns.ASSIGNED_USER_NAME + " TEXT,"
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
                                        + AccountsOpportunitiesColumns.OPPORTUNITY_ID + " INTEGER ,"
                                        + AccountsOpportunitiesColumns.DATE_MODIFIED + " TEXT,"
                                        + AccountsOpportunitiesColumns.DELETED + " INTEGER,"
                                        + " UNIQUE(" + AccountsOpportunitiesColumns.ACCOUNT_ID + ","
                                        + AccountsOpportunitiesColumns.OPPORTUNITY_ID + ")" + ");");
    }

    public static String[] getModuleProjections(String moduleName) {
        return moduleProjections.get(moduleName);
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
        if (moduleName.equals("Accounts")) {
            return AccountsColumns.NAME + " LIKE '%" + searchString + "%'";
        } else if (moduleName.equals("Contacts")) {
            return "(" + ContactsColumns.FIRST_NAME + " LIKE '%" + searchString + "%' OR "
                                            + ContactsColumns.LAST_NAME + " LIKE '%" + searchString
                                            + "%'" + ")";
        } else if (moduleName.equals("Leads")) {
            return "(" + LeadsColumns.FIRST_NAME + " LIKE '%" + searchString + "%' OR "
                                            + LeadsColumns.LAST_NAME + " LIKE '%" + searchString
                                            + "%'" + ")";
        } else if (moduleName.equals("Opportunities")) {
            return OpportunitiesColumns.NAME + " LIKE '%" + searchString + "%'";
        }
        // TODO: similarly for other modules
        return "";
    }

    public String[] getModuleRelationshipItems(String moduleName) {
        return moduleRelationshipItems.get(moduleName);
    }

    public String getPathForRelationship(String moduleName) {
        return pathForRelationship.get(moduleName);
    }

    public List<String> getModuleList() {
        List<String> userModules = getUserModules();
        List<String> supportedModules = Arrays.asList(getSupportedModulesList());
        List<String> modules = new ArrayList<String>();
        for(String module : userModules){
            if(supportedModules.contains(module)){
                modules.add(module);
            }
        }
        return modules;
        //TODO: return the module List after the exclusion of modules from the user moduleList
        //return moduleList;
    }

    public String getLinkfieldName(String moduleName) {
        return linkfieldNames.get(moduleName);
    }

    public String getRelationshipForPath(String path) {
        return relationshipForPath.get(path);
    }

    public String[] getSupportedModulesList(){
        return defaultSupportedModules;
    }
    
    public int getModuleIcon(String moduleName){
        return moduleIcons.get(moduleName);
    }
    
    public ModuleField getModuleField(String moduleName, String fieldName) {
        HashMap<String, ModuleField> nameVsModuleField = moduleFields.get(moduleName);
        if (nameVsModuleField != null && nameVsModuleField.get(fieldName) != null) {
            return nameVsModuleField.get(fieldName);
        } else {
            nameVsModuleField = new HashMap<String, ModuleField>();
        }

        SQLiteDatabase db = getReadableDatabase();
        String selection = ModuleColumns.MODULE_NAME + "='" + moduleName + "'";
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        String moduleId = cursor.getString(0);
        cursor.close();
        db.close();

        db = getReadableDatabase();
        selection = "(" + ModuleFieldColumns.MODULE_ID + "=" + moduleId + " AND "
                                        + ModuleFieldColumns.NAME + "='" + fieldName + "')";
        cursor = db.query(MODULE_FIELDS_TABLE_NAME, com.imaginea.android.sugarcrm.provider.SugarCRMContent.ModuleField.DETAILS_PROJECTION, selection, null, null, null, null);
        cursor.moveToFirst();
        ModuleField moduleField = new ModuleField(cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.NAME)), cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.TYPE)), cursor.getString(cursor.getColumnIndex(ModuleFieldColumns.LABEL)), cursor.getInt(cursor.getColumnIndex(ModuleFieldColumns.IS_REQUIRED)) == 1 ? true
                                        : false);
        cursor.close();
        db.close();
        nameVsModuleField.put(fieldName, moduleField);
        moduleFields.put(moduleName, nameVsModuleField);
        return moduleField;
    }

    public List<String> getUserModules() {
        if (moduleList != null && moduleList.size() != 0)
            return moduleList;

        SQLiteDatabase db = getReadableDatabase();
        moduleList = new ArrayList<String>();
        Cursor cursor = db.query(MODULES_TABLE_NAME, Modules.DETAILS_PROJECTION, null, null, null, null, null);
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
            if (rowId <= 0)
                hasFailed = true;
        }
        if (hasFailed)
            throw new SugarCrmException("FAILED to insert user modules!");
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void setModuleFieldsInfo(Set<Module> moduleFieldsInfo)
                                    throws SugarCrmException {
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
                if (rowId <= 0)
                    hasFailed = true;
            }

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
                if (rowId < 0)
                    hasFailed = true;
            }
            if (hasFailed)
                throw new SugarCrmException("FAILED to insert module fields!");
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }
    
}
