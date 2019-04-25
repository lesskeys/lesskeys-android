package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 03.11.2018.
 */
@RunWith(AndroidJUnit4.class)
public class UserActivityTest {
    @Rule
    public ActivityTestRule<UserActivity> userActivityActivityTestRule= new ActivityTestRule<UserActivity>(UserActivity.class);

    private UserActivity userActivity= null;

    Instrumentation.ActivityMonitor monitorUserActivity= getInstrumentation().addMonitor(UserActivity.class.getName(),null,false);

    @Before
    public void setup() throws Exception{
        userActivity= userActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= userActivity.findViewById(R.id.textViewInfoName);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(userActivity.findViewById(R.id.textViewInfoName));
        assertNotNull(userActivity.findViewById(R.id.editTextName));
        assertNotNull(userActivity.findViewById(R.id.textViewInfoEMail));
        assertNotNull(userActivity.findViewById(R.id.editTextEMail));
        assertNotNull(userActivity.findViewById(R.id.buttonPassword));
        assertNotNull(userActivity.findViewById(R.id.textViewNewUser));
        assertNotNull(userActivity.findViewById(R.id.buttonNewUser));
    }

    @Test
    public void testPasswordButton(){
        assertNotNull(userActivity.findViewById(R.id.buttonPassword));
        onView(withId(R.id.buttonPassword)).perform(click());
        onView(withText("Altes Passwort")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).perform(click());
    }

    @Test
    public void testNewUserButton(){
        assertNotNull(userActivity.findViewById(R.id.buttonNewUser));
        onView(withId(R.id.buttonNewUser)).perform(click());
        onView(withText("Vorname")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button2)).perform(click());
    }
    @After
    public void tearDown() throws Exception{
        userActivity= null;
    }
}
