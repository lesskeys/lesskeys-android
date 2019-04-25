package at.ac.uibk.keylessapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ListView;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 08.05.2018.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    private MainActivity mainActivity= null;


    Instrumentation.ActivityMonitor monitorNfcActivity= getInstrumentation().addMonitor(NfcActivity.class.getName(),null,false);
    Instrumentation.ActivityMonitor monitorUserActivity= getInstrumentation().addMonitor(UserActivity.class.getName(),null,false);
    Instrumentation.ActivityMonitor monitorKeyActivity= getInstrumentation().addMonitor(KeyActivity.class.getName(),null, false);
    Instrumentation.ActivityMonitor monitorAccessProtocolActivity= getInstrumentation().addMonitor(AccessProtocolActivity.class.getName(),null, false);
    Instrumentation.ActivityMonitor monitorRemoteMessageActivity= getInstrumentation().addMonitor(RemoteMessageActivity.class.getName(),null,false);
    Instrumentation.ActivityMonitor monitorSubUserActivity= getInstrumentation().addMonitor(SubUserActivity.class.getName(),null,false);
    Instrumentation.ActivityMonitor monitorFoundKeyActivity= getInstrumentation().addMonitor(FoundKeyActivity.class.getName(),null,false);
    Instrumentation.ActivityMonitor monitorRemoteDoorActivity= getInstrumentation().addMonitor(RemoteDoorActivity.class.getName(),null,false);


    @Before
    public void setup() throws Exception{
        mainActivity= mainActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view = mainActivity.findViewById(R.id.textNFCoff);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(mainActivity.findViewById(R.id.textNFCoff));
        assertNotNull(mainActivity.findViewById(R.id.textNFCon));
        assertNotNull(mainActivity.findViewById(R.id.button_add));
        assertNotNull(mainActivity.findViewById(R.id.button_usersettings));
        assertNotNull(mainActivity.findViewById(R.id.button_keys));
        assertNotNull(mainActivity.findViewById(R.id.button_accessprotocol));
        assertNotNull(mainActivity.findViewById(R.id.button_remoteMessages));
        assertNotNull(mainActivity.findViewById(R.id.button_subUser));
        assertNotNull(mainActivity.findViewById(R.id.buttonFoundKey));
    }

    @Test
    public void testLaunchOfNfcActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_add));
        onView(withId(R.id.button_add)).perform(click());
        Activity nfcActivity= getInstrumentation().waitForMonitorWithTimeout(monitorNfcActivity,5000);
        assertNotNull(nfcActivity);
    }

    @Test
    public void testLaunchOfUserActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_usersettings));
        onView(withId(R.id.button_usersettings)).perform(click());
        Activity userActivity= getInstrumentation().waitForMonitorWithTimeout(monitorUserActivity,5000);
        assertNotNull(userActivity);
    }

    @Test
    public void testLaunchOfKeyActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_keys));
        onView(withId(R.id.button_keys)).perform(click());
        Activity keyActivity= getInstrumentation().waitForMonitorWithTimeout(monitorKeyActivity,5000);
        assertNotNull(keyActivity);
    }

    @Test
    public void testLaunchOfAccessProtocolActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_accessprotocol));
        onView(withId(R.id.button_accessprotocol)).perform(click());
        Activity accessProtocolActivity= getInstrumentation().waitForMonitorWithTimeout(monitorAccessProtocolActivity, 5000);
        assertNotNull(accessProtocolActivity);
    }

    @Test
    public void testLaunchOfRemoteMessageActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_remoteMessages));
        onView(withId(R.id.button_remoteMessages)).perform(click());
        Activity remoteMessageActivity= getInstrumentation().waitForMonitorWithTimeout(monitorRemoteMessageActivity,5000);
        assertNotNull(remoteMessageActivity);
    }

    @Test
    public void testLaunchOfSubUserActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_subUser));
        onView(withId(R.id.button_subUser)).perform(click());
        Activity subUserActivity= getInstrumentation().waitForMonitorWithTimeout(monitorSubUserActivity,5000);
        assertNotNull(subUserActivity);
    }

    @Test
    public void testLaunchOfFoundKeyActivity(){
        assertNotNull(mainActivity.findViewById(R.id.buttonFoundKey));
        onView(withId(R.id.buttonFoundKey)).perform(click());
        Activity foundKeyActivity= getInstrumentation().waitForMonitorWithTimeout(monitorFoundKeyActivity,5000);
        assertNotNull(foundKeyActivity);
    }

    @Test
    public void testLaunchOfRemoteDoorActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_remote_doors));
        onView(withId(R.id.button_remote_doors)).perform(click());
        Activity remoteDoorActivity= getInstrumentation().waitForMonitorWithTimeout(monitorRemoteDoorActivity,5000);
        assertNotNull(remoteDoorActivity);
    }

    @Test
    public void testLaunchAndListViewOfSubUserActivity(){
        assertNotNull(mainActivity.findViewById(R.id.button_subUser));
        onView(withId(R.id.button_subUser)).perform(click());
        Activity subUserActivity= getInstrumentation().waitForMonitorWithTimeout(monitorSubUserActivity,5000);
        assertNotNull(subUserActivity);

        assertNotNull(subUserActivity.findViewById(R.id.subUserList));
        Instrumentation instrumentation= InstrumentationRegistry.getInstrumentation();
        final ListView listView= (ListView) subUserActivity.findViewById(R.id.subUserList);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                int position=0;
                listView.performItemClick(listView.getChildAt(position),position,listView.getAdapter().getItemId(position));
            }
        });
        onView(withText("OK")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
    }
    @After
    public void tearDown() throws Exception{
        mainActivity= null;
    }


}