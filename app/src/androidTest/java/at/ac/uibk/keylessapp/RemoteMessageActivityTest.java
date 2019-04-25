package at.ac.uibk.keylessapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 03.11.2018.
 */

@RunWith(AndroidJUnit4.class)
public class RemoteMessageActivityTest {
    @Rule
    public ActivityTestRule<RemoteMessageActivity> remoteMessageActivityActivityTestRule= new ActivityTestRule<RemoteMessageActivity>(RemoteMessageActivity.class);

    private RemoteMessageActivity remoteMessageActivity= null;

    @Before
    public void setup() throws Exception{
        remoteMessageActivity= remoteMessageActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= remoteMessageActivity.findViewById(R.id.textHeaderRemoteMessage);
        assertNotNull(view);
    }

    @Test
    public void testContent() {
        assertNotNull(remoteMessageActivity.findViewById(R.id.textHeaderRemoteMessage));
        assertNotNull(remoteMessageActivity.findViewById(R.id.remoteMessageList));
    }

    @After
    public void tearDown() throws Exception{
        remoteMessageActivity= null;
    }

}
