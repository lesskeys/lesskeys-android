package at.ac.uibk.keylessapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 04.11.2018.
 */

@RunWith(AndroidJUnit4.class)
public class RemoteDoorActivityTest {
    @Rule
    public ActivityTestRule<RemoteDoorActivity> remoteDoorActivityActivityTestRule= new ActivityTestRule<RemoteDoorActivity>(RemoteDoorActivity.class);

    private RemoteDoorActivity remoteDoorActivity=null;

    @Before
    public void setup() throws Exception{
        remoteDoorActivity= remoteDoorActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= remoteDoorActivity.findViewById(R.id.textViewInformationRemoteDoor);
        assertNotNull(view);
    }

    @Test
    public void testContent() {
        assertNotNull(remoteDoorActivity.findViewById(R.id.textViewInformationRemoteDoor));
        assertNotNull(remoteDoorActivity.findViewById(R.id.ListViewRemoteLocksForUser));

    }

    @After
    public void tearDown() throws Exception{
        remoteDoorActivity= null;
    }
}
