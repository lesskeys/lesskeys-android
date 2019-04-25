package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import at.ac.uibk.keylessapp.Models.SystemLock;

/**
 * Created by descl on 04.11.2018.
 * Class used to remote open a door which belongs to the user.
 */

public class RemoteDoorActivity extends AppCompatActivity {
    private List<String> stringListOfUserLocks= new ArrayList<>();
    private List<SystemLock> systemLocksOfUser= new ArrayList<>();
    private ListView listOfLocks= null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_door);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new LockListRequest().execute();

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


    protected void returnLockListForUser(List<SystemLock> answer){
        for (SystemLock l: answer){
            stringListOfUserLocks.add(l.getName().toString());
            systemLocksOfUser.add(l);
        }
        if(stringListOfUserLocks != null) {
            listOfLocks = (ListView) findViewById(R.id.ListViewRemoteLocksForUser);
            final CustomLockListAdapter customAdapter = new CustomLockListAdapter(this, R.layout.list_item_layout_locks, stringListOfUserLocks,systemLocksOfUser);
            listOfLocks.setAdapter(customAdapter);

        }
    }


    /**
     * class for requesting the locks of a given user
     */
    private class LockListRequest extends AsyncTask<Void, Void, String> {
        private List<SystemLock> answer= new ArrayList<>();
        private String session;
        private String username;

        private LockListRequest(){
            SharedPreferences sharedPreferences= getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            this.session= sharedPreferences.getString("savedSession","-error-");
            this.username= sharedPreferences.getString("savedUsername", "-error-");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/lock/get-for-user";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, String> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                SystemLock[] response= restTemplate.postForObject(url,request,SystemLock[].class);
                for(SystemLock l: response){
                    answer.add(l);
                }
                return answer.toString();
            }catch (Exception e){
                Log.e("RemoteDoorActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            returnLockListForUser(answer);
        }
    }

    public class CustomLockListAdapter extends ArrayAdapter<String>{
        private Context context;
        private TextView lockName;
        private AppCompatImageButton unlockButton;
        private List<String> locks;
        private List<SystemLock> rawLocks;

        public CustomLockListAdapter(Context context, int resource, List<String> listValues, List<SystemLock> raw){
           super(context,resource,listValues);
           this.context=context;
           this.locks=listValues;
           this.rawLocks=raw;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){

            final String currentName= locks.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_layout_locks, null);

            lockName= (TextView) convertView.findViewById(R.id.lockName);
            lockName.setText(currentName);

            unlockButton= (AppCompatImageButton) convertView.findViewById(R.id.lockUnlockButton);
            unlockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long lockId= rawLocks.get(position).getLockId();
                    new RemoteDoorOpenRequest(lockId).execute();
                }
            });

            return convertView;
        }

    }

    private class RemoteDoorOpenRequest extends AsyncTask<Void, Void, String> {
        private List<SystemLock> answer= new ArrayList<>();
        private String session;
        private String username;
        private long lockId;


        private RemoteDoorOpenRequest(long lockId){
            SharedPreferences sharedPreferences= getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            this.session= sharedPreferences.getString("savedSession","-error-");
            this.username= sharedPreferences.getString("savedUsername", "-error-");
            this.lockId= lockId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/lock/remote-unlock";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Map<String, Object> request= new HashMap<>();
                request.put("session", session);
                request.put("username", username);
                request.put("lockId",lockId);
                restTemplate.put(url,request);
                return "finished";
            }catch (Exception e){
                Log.e("RemoteDoorActivity", e.getMessage(),e);
            }
            return null;
        }

    }

}
