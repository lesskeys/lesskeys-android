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
 * Created by descl on 03.11.2018.
 */
@RunWith(AndroidJUnit4.class)
public class FoundKeyActivityTest {
    @Rule
    public ActivityTestRule<FoundKeyActivity> foundKeyActivityActivityTestRule= new ActivityTestRule<FoundKeyActivity>(FoundKeyActivity.class);

    private FoundKeyActivity foundKeyActivity= null;

    @Before
    public void setup() throws Exception{
        foundKeyActivity= foundKeyActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= foundKeyActivity.findViewById(R.id.textViewInformationFoundKey);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(foundKeyActivity.findViewById(R.id.textViewInformationFoundKey));
        assertNotNull(foundKeyActivity.findViewById(R.id.textViewHoldKey));
        assertNotNull(foundKeyActivity.findViewById(R.id.textViewKeyName));
    }

    @After
    public void tearDown() throws Exception{
        foundKeyActivity= null;
    }

}
