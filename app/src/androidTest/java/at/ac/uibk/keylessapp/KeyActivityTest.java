package at.ac.uibk.keylessapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Key;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by descl on 03.11.2018.
 */
@RunWith(AndroidJUnit4.class)
public class KeyActivityTest {
    @Rule
    public ActivityTestRule<KeyActivity> keyActivityActivityTestRule= new ActivityTestRule<KeyActivity>(KeyActivity.class);

    private KeyActivity keyActivity= null;

    @Before
    public void setup() throws Exception{
        keyActivity= keyActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){
        View view= keyActivity.findViewById(R.id.testTextViewKeyList);
        assertNotNull(view);
    }

    @Test
    public void testContent(){
        assertNotNull(keyActivity.findViewById(R.id.testTextViewKeyList));
        assertNotNull(keyActivity.findViewById(R.id.keyList));

    }

    @After
    public void tearDown() throws Exception{
        keyActivity= null;
    }

}
