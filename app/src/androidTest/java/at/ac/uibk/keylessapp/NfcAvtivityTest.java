package at.ac.uibk.keylessapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 08.05.2018.
 */

@RunWith(AndroidJUnit4.class)
public class NfcAvtivityTest {
    @Rule
    public ActivityTestRule<NfcActivity> nfcActivityActivityTestRule= new ActivityTestRule<NfcActivity>(NfcActivity.class);

    private NfcActivity nfcActivity= null;

    @Before
    public void setup() throws Exception{
        nfcActivity= nfcActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= nfcActivity.findViewById(R.id.textViewInformation);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(nfcActivity.findViewById(R.id.textViewAddKeyName));
        assertNotNull(nfcActivity.findViewById(R.id.editTextAddKeyName));
        assertNotNull(nfcActivity.findViewById(R.id.textViewAddKeyLockList));
        assertNotNull(nfcActivity.findViewById(R.id.ListViewAddKeyLockList));
        assertNotNull(nfcActivity.findViewById(R.id.buttonAddKey));
    }

    @After
    public void tearDown() throws Exception{
        nfcActivity= null;
    }



}
