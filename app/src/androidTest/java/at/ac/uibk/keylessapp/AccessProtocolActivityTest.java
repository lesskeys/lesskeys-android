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
public class AccessProtocolActivityTest {
    @Rule
    public ActivityTestRule<AccessProtocolActivity> accessProtocolActivityActivityTestRule= new ActivityTestRule<AccessProtocolActivity>(AccessProtocolActivity.class);

    private AccessProtocolActivity accessProtocolActivity= null;

    @Before
    public void setup() throws Exception{
        accessProtocolActivity= accessProtocolActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= accessProtocolActivity.findViewById(R.id.testTextViewAccessProtocol);
        assertNotNull(view);
    }

    @Test
    public void testContent() {
        assertNotNull(accessProtocolActivity.findViewById(R.id.testTextViewAccessProtocol));
        assertNotNull(accessProtocolActivity.findViewById(R.id.protocolList));
    }

    @After
    public void tearDown() throws Exception{
        accessProtocolActivity= null;
    }

}
