package com.imaginea.android.sugarcrm.restapi;

import java.util.HashMap;
import java.util.List;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.imaginea.android.sugarcrm.ModuleFields;
import com.imaginea.android.sugarcrm.rest.Rest;
import com.imaginea.android.sugarcrm.rest.SugarBean;

/**
 * ContactsApiTest, tests the rest api calls
 * 
 * @author chander
 * 
 */
public class ContactDetailsApiTest extends RestAPITest {

    String moduleName = "Contacts";

    String[] fields = new String[] {};

    String[] selectFields = { ModuleFields.FIRST_NAME, ModuleFields.LAST_NAME,
            ModuleFields.ACCOUNT_NAME, ModuleFields.PHONE_MOBILE,
            ModuleFields.PHONE_WORK, ModuleFields.EMAIL1 };

    HashMap<String, List<String>> linkNameToFieldsArray = new HashMap<String, List<String>>();

    public final static String LOG_TAG = "ContactDetailsTest";

    @SmallTest
    public void testContactDetail() throws Exception {

        // get only one sugar bean
        final SugarBean[] sBeans = getSugarBeans(0, 1);
        assertTrue(sBeans.length > 0);
        final String beanId = sBeans[0].getBeanId();
        assertNotNull(beanId);
        final SugarBean sBean = getSugarBean(beanId);
        assertNotNull(sBean);
        for (int i = 0; i < selectFields.length; i++) {
            final String fieldValue = sBean.getFieldValue(selectFields[i]);
            Log.i(LOG_TAG, "FieldName:|Field value " + selectFields[i] + ":"
                    + fieldValue);
            // assertNotNull(fieldValue);
        }
    }

    /**
     * demonstrates the usage of RestUtil for contacts List. ModuleFields.NAME
     * or FULL_NAME is not returned by Sugar CRM. The fields that are not
     * returned by SugarCRM can be automated, but not yet generated
     * 
     * @param offset
     * @param maxResults
     * @return
     * @throws Exception
     */
    private SugarBean[] getSugarBeans(final int offset, final int maxResults)
            throws Exception {
        final String query = "", orderBy = "";
        final SugarBean[] sBeans = Rest.getEntryList(url, mSessionId,
                moduleName, query, orderBy, offset + "", selectFields,
                linkNameToFieldsArray, maxResults + "", "");
        return sBeans;
    }

    /**
     * demonstrates the usage of RestUtil for contact detail. ModuleFields.NAME
     * or FULL_NAME is not returned by Sugar CRM. The fields that are not
     * returned by SugarCRM can be automated, but not yet generated
     * 
     * @param beanId
     * @return
     * @throws Exception
     */
    private SugarBean getSugarBean(final String beanId) throws Exception {
        final SugarBean sBean = Rest.getEntry(url, mSessionId, moduleName,
                beanId, selectFields, linkNameToFieldsArray);
        return sBean;
    }

}
