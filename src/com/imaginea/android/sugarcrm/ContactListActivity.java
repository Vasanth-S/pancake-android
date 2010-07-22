package com.imaginea.android.sugarcrm;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
import com.imaginea.android.sugarcrm.util.Util;

/**
 * ContactListActivity
 * 
 * @author chander
 */
public class ContactListActivity extends ListActivity {

    private ListView mListView;

    private View mEmpty;

    private View mListFooterView;

    private TextView mListFooterText;

    private View mListFooterProgress;

    private Menu mMenu;

    private boolean mBusy = false;

    private String mModuleName;

    private Uri mModuleUri;

    private boolean mStopLoading = false;

    private Uri mIntentUri;

    // we don't make this final as we may want to use the sugarCRM value dynamically
    public static int mMaxResults = 20;

    public final static String LOG_TAG = "ContactListActivity";

    private DatabaseHelper mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_list);

        mDbHelper = new DatabaseHelper(getBaseContext());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mModuleName = "Contacts";
        if (extras != null) {
            mModuleName = extras.getString(RestUtilConstants.MODULE_NAME);
        }

        TextView tv = (TextView) findViewById(R.id.headerText);
        tv.setText(mModuleName);

        mListView = getListView();

        mIntentUri = intent.getData();
        // mListView.setOnScrollListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                openDetailScreen(position);
            }
        });

        // button code in the layout - 1.6 SDK feature to specify onClick
        mListView.setItemsCanFocus(true);
        mListView.setFocusable(true);
        mEmpty = findViewById(R.id.empty);
        mListView.setEmptyView(mEmpty);
        registerForContextMenu(getListView());

        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            Log.d(LOG_TAG, "Instance count:" + getInstanceCount());
        Log.d(LOG_TAG, "ModuleName" + mModuleName);

        mModuleUri = mDbHelper.getModuleUri(mModuleName);
        if (intent.getData() == null) {
            intent.setData(mModuleUri);
        }
        // TODO - optimize this, if we sync up a dataset, then no need to run detail projectio
        // nhere, just do a list projection
        Cursor cursor;
        // if the parentModuleName is not null, then it is the list of relationship beans
        String parentModuleName = extras.getString(RestUtilConstants.PARENT_MODULE_NAME);

        cursor = managedQuery(getIntent().getData(), DatabaseHelper.getModuleProjections(mModuleName), null, null, mDbHelper.getModuleSortOrder(mModuleName));

        // CRMContentObserver observer = new CRMContentObserver()
        // cursor.registerContentObserver(observer);
        GenericCursorAdapter adapter;
        String[] moduleSel = mDbHelper.getModuleListSelections(mModuleName);
        if (moduleSel.length >= 2)
            adapter = new GenericCursorAdapter(this, R.layout.contact_listitem, cursor, moduleSel, new int[] {
                    android.R.id.text1, android.R.id.text2 });
        else
            adapter = new GenericCursorAdapter(this, R.layout.contact_listitem, cursor, moduleSel, new int[] { android.R.id.text1 });
        setListAdapter(adapter);

        TextView tv1 = (TextView) (mEmpty.findViewById(R.id.mainText));

        if (adapter.getCount() == 0) {
            mListView.setVisibility(View.GONE);
            mEmpty.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            tv1.setVisibility(View.VISIBLE);
            if (mIntentUri != null) {
                tv1.setText("No " + mModuleName + " found");
            }
        } else {
            mEmpty.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            tv1.setVisibility(View.GONE);
        }

        mListFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_footer, mListView, false);
        getListView().addFooterView(mListFooterView);
        mListFooterText = (TextView) findViewById(R.id.status);
        // setListAdapter();

        mListFooterProgress = mListFooterView.findViewById(R.id.progress);
    }

    /**
     * GenericCursorAdapter
     */
    private final class GenericCursorAdapter extends SimpleCursorAdapter {

        private int realoffset = 0;

        private int limit = 20;

        public GenericCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);
            int count = getCursor().getCount();
            Log.d(LOG_TAG, "Get Item" + getItemId(position));
            if (!mBusy && position != 0 && position == count - 1) {
                mBusy = true;
                realoffset += count;
                Uri uri = getIntent().getData();
                Uri newUri = Uri.withAppendedPath(Contacts.CONTENT_URI, realoffset + "/" + limit);
                Log.d(LOG_TAG, "Changing cursor:" + newUri.toString());
                final Cursor cursor = managedQuery(newUri, Contacts.LIST_PROJECTION, null, null, Contacts.DEFAULT_SORT_ORDER);
                CRMContentObserver observer = new CRMContentObserver(new Handler() {

                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Log.d(LOG_TAG, "Changing cursor: in handler");
                        if (cursor.getCount() < mMaxResults)
                            mStopLoading = true;
                        changeCursor(cursor);
                        mListFooterText.setVisibility(View.GONE);
                        mListFooterProgress.setVisibility(View.GONE);
                        mBusy = false;
                    }
                });
                cursor.registerContentObserver(observer);
            }
            if (mBusy) {
                mListFooterProgress.setVisibility(View.VISIBLE);
                mListFooterText.setVisibility(View.VISIBLE);
                mListFooterText.setText("Loading...");
                // Non-null tag means the view still needs to load it's data
                // text.setTag(this);
            }
            return v;
        }
    }

    /**
     * opens the Detail Screen
     * 
     * @param position
     */
    void openDetailScreen(int position) {
        Intent detailIntent = new Intent(ContactListActivity.this, AccountDetailsActivity.class);

        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        // SugarBean bean = (SugarBean) getListView().getItemAtPosition(position);
        // TODO
        Log.d(LOG_TAG, "beanId:" + cursor.getString(1));
        detailIntent.putExtra(Util.ROW_ID, cursor.getString(0));
        detailIntent.putExtra(RestUtilConstants.BEAN_ID, cursor.getString(1));
        detailIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
        cursor.close();
        
        startActivity(detailIntent);
    }

    /**
     * opens the Edit Screen
     * 
     * @param position
     */
    private void openEditScreen(int position) {
        Intent detailIntent = new Intent(ContactListActivity.this, EditDetailsActivity.class);

        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            Log.d(LOG_TAG, "beanId:" + cursor.getString(1));
        detailIntent.putExtra(Util.ROW_ID, cursor.getString(0));
        if (mIntentUri != null)
            detailIntent.setData(mIntentUri);

        detailIntent.putExtra(RestUtilConstants.BEAN_ID, cursor.getString(1));
        detailIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
        cursor.close();
        
        startActivity(detailIntent);
    }

    /**
     * deletes an item
     * 
     * @param position
     */
    void deleteItem(int position) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        // TODO
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            Log.d(LOG_TAG, "beanId:" + cursor.getString(1));

        if (mDbHelper == null)
            mDbHelper = new DatabaseHelper(getBaseContext());

        mModuleUri = mDbHelper.getModuleUri(mModuleName);
        Uri deleteUri = Uri.withAppendedPath(mModuleUri, cursor.getString(0));
        getContentResolver().registerContentObserver(deleteUri, false, new DeleteContentObserver(new Handler()));
        ServiceHelper.startServiceForDelete(getBaseContext(), deleteUri, mModuleName, cursor.getString(1));
        // getContentResolver().delete(mModuleUri, SugarCRMContent.RECORD_ID, new String[] {
        // cursor.getString(0) });
        // detailIntent.putExtra(RestUtilConstants.ID, cursor.getString(0));
        // detailIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
        // startActivity(detailIntent);
    }

    private class DeleteContentObserver extends ContentObserver {

        public DeleteContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(LOG_TAG, "Received onCHange");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        String[] modules = { mModuleName };
        appData.putString(RestUtilConstants.MODULE_NAME, mModuleName);
        appData.putStringArray(RestUtilConstants.MODULES, modules);
        appData.putInt(RestUtilConstants.OFFSET, 0);
        appData.putInt(RestUtilConstants.MAX_RESULTS, 20);

        startSearch(null, false, appData, false);
        return true;
    }

    // We can stop loading once we do not get the
    // if (sBeans.length < mMaxResults)
    // mStopLoading = true;
    // @Override
    // protected void onPostExecute(Object result) {
    // super.onPostExecute(result);
    // if (isCancelled())
    // return;
    // int retVal = (Integer) result;
    //
    // switch (retVal) {
    // case Util.FETCH_FAILED:
    //
    // mEmpty.findViewById(R.id.progress).setVisibility(View.GONE);
    // TextView tv = (TextView) (mEmpty.findViewById(R.id.mainText));
    // tv.setVisibility(View.VISIBLE);
    // tv.setText(R.string.loadFailed);
    // TextView footer = (TextView) findViewById(R.id.status);
    // footer.setVisibility(View.VISIBLE);
    // footer.setText(R.string.loadFailed);
    //
    // break;
    //
    // case Util.REFRESH_LIST:
    // mBusy = false;
    // mStatus.setVisibility(View.GONE);
    // mListView.setVisibility(View.VISIBLE);
    // int firstPos = getListView().getFirstVisiblePosition();
    // setListAdapter(mAdapter);
    // getListView().setSelection(firstPos);
    // mAdapter.notifyDataSetChanged();
    // break;
    // default:
    //
    // }
    // }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuHelper.onPrepareOptionsMenu(this, menu, mModuleName);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hold on to this
        mMenu = menu;

        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.home:
            Intent myIntent = new Intent(ContactListActivity.this, DashboardActivity.class);
            ContactListActivity.this.startActivity(myIntent);
            return true;
        case R.id.search:
            onSearchRequested();
            return true;
        case R.id.addItem:
            myIntent = new Intent(ContactListActivity.this, EditDetailsActivity.class);
            myIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
            if (mIntentUri != null)
                myIntent.setData(mIntentUri);
            // myIntent.putExtra(RestUtilConstants.LINK_FIELD_NAME,
            // DatabaseHelper.getRelationshipName(mModuleName));
            ContactListActivity.this.startActivity(myIntent);

            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.options);
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "Bad menuInfo", e);
            return;
        }

        menu.add(1, R.string.view, 2, R.string.view);

        menu.add(2, R.string.edit, 3, R.string.edit);
        menu.add(3, R.string.delete, 4, R.string.delete);

        if (mDbHelper == null)
            mDbHelper = new DatabaseHelper(getBaseContext());

        // TODO
        if (mDbHelper.getModuleField(mModuleName, ModuleFields.PHONE_WORK) != null)
            menu.add(4, R.string.call, 4, R.string.call);
        if (mDbHelper.getModuleField(mModuleName, ModuleFields.EMAIL1) != null)
            menu.add(5, R.string.email, 4, R.string.email);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;

        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, "bad menuInfo", e);
            return false;
        }
        // ListView listView = getListView();

        int position = info.position;
        switch (item.getItemId()) {
        case R.string.view:
            openDetailScreen(position);
            return true;

        case R.string.edit:
            openEditScreen(position);
            return true;

        case R.string.delete:
            deleteItem(position);
            return true;

        case R.string.call:
            callNumber(position);
            return true;

        case R.string.email:
            sendMail(position);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void callNumber(int position) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        int index = cursor.getColumnIndex(ModuleFields.PHONE_WORK);
        String number = cursor.getString(index);
        cursor.close();
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            Log.d(LOG_TAG, "Work number to call:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    public void sendMail(int position) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        // emailAddress
        int index = cursor.getColumnIndex(ModuleFields.EMAIL1);
        String emailAddress = cursor.getString(index);
        cursor.close();
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            Log.d(LOG_TAG, "email :" + emailAddress);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + emailAddress));
        startActivity(intent);
    }
}
