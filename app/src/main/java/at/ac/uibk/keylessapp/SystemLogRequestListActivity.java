package at.ac.uibk.keylessapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemLogRequest;
import at.ac.uibk.keylessapp.R;

public class SystemLogRequestListActivity extends AppCompatActivity {

    private List<SystemLogRequest> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_log_request_list);
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new SystemLogRequestList().execute();
        final SwipeRefreshLayout mSwipeRefreshlayout= (SwipeRefreshLayout) findViewById(R.id.swipeRefreshRequests);
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshlayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshlayout.setRefreshing(false);
                        new SystemLogRequestList().execute();
                    }
                },700);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onInspect(long id, String text, String message) {
        Intent intent = new Intent(this, SystemLogRequestActivity.class);
        Bundle b = new Bundle();
        b.putLong("id", id);
        b.putString("text", text);
        b.putString("message", message);
        intent.putExtras(b);
        startActivity(intent);
    }


    private void showRequestList(final List<SystemLogRequest> answer){
        ListView rs = (ListView) findViewById(R.id.logRequestList);

        List<String> newList = new ArrayList<>();
        for (SystemLogRequest r : answer) {
            newList.add("Log vom Typ: "+r.getType().toString()+" vom Tag: "+r.getDay().toString());
        }
        this.list = answer;
        final CustomRequestListAdapter customAdapter = new CustomRequestListAdapter(SystemLogRequestListActivity.this, R.layout.list_item_layout_keys, newList, answer);
        rs.setAdapter(customAdapter);
    }

    /**
     * Private class used as a background-task to get a list open system log requests.
     */
    private class SystemLogRequestList extends AsyncTask<Void,Void,String> {

        Map<String, String> request = new HashMap<>();
        List<SystemLogRequest> answer = new ArrayList<>();

        private SystemLogRequestList(){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                String url = getString(R.string.backend_ip)+"/log/requests";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemLogRequest[] response = restTemplate.postForObject(url, request, SystemLogRequest[].class);
                answer.addAll(Arrays.asList(response));

            }catch(Exception e){
                Log.e("SystemLogRequestList", e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            showRequestList(answer);
        }
    }

    public class CustomRequestListAdapter extends ArrayAdapter<String> {
        private Context context;
        private TextView requestText;
        private AppCompatImageButton inspectButton;
        private List<String> requests;
        private List<SystemLogRequest> rawRequests;

        public CustomRequestListAdapter(Context context, int resource, List<String> listValues, List<SystemLogRequest> raw){
            super(context,resource,listValues);
            this.context = context;
            this.requests = listValues;
            this.rawRequests = raw;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){

            final String text = requests.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_layout_requests, null);

            requestText = (TextView) convertView.findViewById(R.id.requestText);
            requestText.setText(text);

            inspectButton = (AppCompatImageButton) convertView.findViewById(R.id.inspectButton);
            inspectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long requestId = rawRequests.get(position).getRequestId();
                    String message = rawRequests.get(position).getMessage();
                    String text = "Log vom Typ: "+rawRequests.get(position).getType().toString()+" vom Tag: "+rawRequests.get(position).getDay().toString();
                    onInspect(requestId, text, message);
                }
            });
            return convertView;
        }
    }
}
