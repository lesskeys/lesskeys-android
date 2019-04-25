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
public class SubUserActivityTest {
    @Rule
    public ActivityTestRule<SubUserActivity> subUserActivityActivityTestRule= new ActivityTestRule<SubUserActivity>(SubUserActivity.class);

    private SubUserActivity subUserActivity= null;

    @Before
    public void setup() throws Exception{
        subUserActivity= subUserActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= subUserActivity.findViewById(R.id.testTextViewSubUserList);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(subUserActivity.findViewById(R.id.testTextViewSubUserList));
        assertNotNull(subUserActivity.findViewById(R.id.subUserList));

    }

    @After
    public void tearDown() throws Exception{
        subUserActivity= null;
    }
}
