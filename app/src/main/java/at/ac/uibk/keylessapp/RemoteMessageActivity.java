package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.uibk.keylessapp.Models.SystemRingMessage;

public class RemoteMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_message);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayReceivedMessages();
        getRemoteMessages();

        final SwipeRefreshLayout mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.remoteMessagesSwipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        displayReceivedMessages();
                        getRemoteMessages();
                    }
                },700);
            }
        });
    }

    //Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayReceivedMessages() {
        SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
        Set<String> messages = sharedPref.getStringSet("messages", new HashSet<String>());
        ArrayList<String> messageArray = new ArrayList<>();
        messageArray.addAll(messages);

        ListView list = (ListView) findViewById(R.id.remoteMessageList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_multichoice, messageArray);
        list.setAdapter(arrayAdapter);
    }
    public void getRemoteMessages(){
        new RemoteMessageRequest().execute();
    }

    public void showMessages(List<String> messages){
        ListView messageList= (ListView) findViewById(R.id.remoteMessageList);
        final ArrayAdapter adapter= new ArrayAdapter(this,android.R.layout.simple_list_item_1,messages);
        messageList.setAdapter(adapter);
    }

    /**
     * Class for sending messagehistory requests to the server
     */

    private class RemoteMessageRequest extends AsyncTask<Void,Void,String> {

        private Map<String,String> request = new HashMap<>();
        private List<SystemRingMessage> answer= new ArrayList<>();

        public RemoteMessageRequest() {
            SharedPreferences sharedPref= getSharedPreferences("keyless-pref",Context.MODE_PRIVATE);
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url = getString(R.string.backend_ip) + "/messages";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemRingMessage[] response= restTemplate.postForObject(url,request,SystemRingMessage[].class);
                for (SystemRingMessage m : response) {
                    answer.add(m);
                }
                return "successful";
            }catch(Exception e){
                Log.e("RemoteMessageActivity",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            List<String> toDisplay= new ArrayList<>();
            if (result!= null){
                for (SystemRingMessage m: answer){
                    toDisplay.add(m.getSenderName() +" |Text: "+ m.getRingMessage()+ " |"+ m.getTimestamp().toString());
                }
                showMessages(toDisplay);
            }
        }
    }
}
