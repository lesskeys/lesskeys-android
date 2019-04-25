package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.SystemKey;
import at.ac.uibk.keylessapp.Models.SystemLock;
import at.ac.uibk.keylessapp.Models.SystemUser;

/**
 * Created by ldoet on 10.06.2018.
 */

public class SubUserActivity extends AppCompatActivity {

    private List<SystemUser> rawUsers = new ArrayList<>();
    private List<String> rawUsersAsString = new ArrayList<>();
    private List<SystemLock> rawLocks = new ArrayList<>();
    private List<String> rawLocksAsString = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_users);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new SubUserListAndLocksRequest().execute();

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

    protected void showSubUserList(final List<SystemUser> userAnswer, final List<SystemLock> lockAnswer) {
        ListView users = (ListView) findViewById(R.id.subUserList);

        for (SystemUser u : userAnswer) {
            rawUsersAsString.add(u.getEmail());
        }
        for (SystemLock l : lockAnswer) {
            rawLocksAsString.add(l.getName());
        }


        this.rawUsers = userAnswer;
        this.rawLocks = lockAnswer;

        final CustomSubUserAdapter customAdapter = new CustomSubUserAdapter(this,R.layout.list_item_layout_subuser,rawUsersAsString,rawUsers);
        users.setAdapter(customAdapter);
        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new SubUserLocksRequest(userAnswer.get(i).getUserId(), i, adapterView).execute();
            }
        });
    }

    protected void openEditDialog(List<SystemLock> locksForSubUser, int i, AdapterView<?> adapterView) {
        final SystemUser selectedUser = rawUsers.get(i);

        AlertDialog.Builder builder = new AlertDialog.Builder(SubUserActivity.this);
        builder.setTitle(rawUsersAsString.get(i));
        final Context context = adapterView.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final ListView locks = new ListView(SubUserActivity.this);
        TextView infoLocks = new TextView(context);
        infoLocks.setText("Schlösser");

        //locklist
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(SubUserActivity.this, android.R.layout.select_dialog_multichoice, rawLocksAsString);
        locks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        locks.setAdapter(arrayAdapter);
        layout.addView(infoLocks);
        layout.addView(locks);
        for (SystemLock l1 : rawLocks) {
            for (SystemLock l2 : locksForSubUser) {
                if (l1.getLockId() == l2.getLockId()) {
                    locks.setItemChecked(rawLocks.indexOf(l1), true);
                }
            }
        }
        builder.setView(layout);
        builder.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Long> lockstoSend= new ArrayList<>();
                SparseBooleanArray checked= locks.getCheckedItemPositions();
                for (int j = 0; j < locks.getAdapter().getCount(); j++) {
                    if (checked.get(j)) {
                        lockstoSend.add(rawLocks.get(j).getLockId());
                    }
                }
                dialogInterface.dismiss();
                new SubUserEditRequest(selectedUser.getEmail(), lockstoSend).execute();
            }
        });
        builder.show();
    }

    private class SubUserListAndLocksRequest extends AsyncTask<Void,Void,String> {
        private List<SystemUser> users = new ArrayList<>();
        private List<SystemLock> locks = new ArrayList<>();
        private Map<String, String> request = new HashMap<>();

        public SubUserListAndLocksRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String url = getString(R.string.backend_ip)+"/user/subusers";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemUser[] userResponse = restTemplate.postForObject(url, request, SystemUser[].class);
                for (SystemUser u : userResponse) {
                    users.add(u);
                }
                url = getString(R.string.backend_ip)+"/lock/get-for-user";
                SystemLock[] lockResponse = restTemplate.postForObject(url, request, SystemLock[].class);
                for (SystemLock l : lockResponse) {
                    locks.add(l);
                }
                return users.toString();
            }catch(Exception e){
                Log.e("SubUserActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showSubUserList(users, locks);
        }
    }

    private class SubUserLocksRequest extends AsyncTask<Void, Void, String> {
        private List<SystemLock> locks = new ArrayList<>();
        private Map<String, String> request = new HashMap<>();
        private int i;
        private AdapterView<?> adapterView;

        public SubUserLocksRequest(Long subUserId, int i, AdapterView<?> adapterView) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("subUserId", Long.toString(subUserId));
            this.i = i;
            this.adapterView = adapterView;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String url = getString(R.string.backend_ip)+"/lock/get-for-subuser";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemLock[] lockResponse = restTemplate.postForObject(url, request, SystemLock[].class);
                for (SystemLock l : lockResponse) {
                    locks.add(l);
                }
                return locks.toString();
            } catch(Exception e){
                Log.e("SubUserActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            openEditDialog(locks, i, adapterView);
        }
    }

    private class SubUserEditRequest extends AsyncTask<Void, Void, String> {
        private Map<String, Object> request = new HashMap<>();

        public SubUserEditRequest(String subUserName, List<Long> lockIds) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("subUser", subUserName);
            request.put("lockIds", lockIds);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String url = getString(R.string.backend_ip)+"/user/subuser/edit-permission";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.postForObject(url, request, void.class);
            } catch(Exception e){
                Log.e("SubUserActivity", e.getMessage(),e);
            }
            return null;
        }
    }

    /**
     * ListAdapter for every ListItem to display username and a button for soft delete of subuser
     */
    public class CustomSubUserAdapter extends ArrayAdapter<String> {

        private Context context;
        private TextView userName;
        private AppCompatImageButton deleteButton;
        private List<String> users;
        private List<SystemUser> rawUsers;


        public CustomSubUserAdapter(Context context, int resource, List<String> listValues, List<SystemUser> raw) {
            super(context, resource, listValues);
            this.context = context;
            this.users = listValues;
            this.rawUsers = raw;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final String currentName = users.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_layout_subuser, null);


            userName = (TextView) convertView.findViewById(R.id.subUserName);
            userName.setText(currentName);
            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, currentName, Toast.LENGTH_SHORT).show();
                }
            });



            deleteButton = (AppCompatImageButton) convertView.findViewById(R.id.deleteUserButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SubUserActivity.this);
                    LinearLayout layout= new LinearLayout(SubUserActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    alertDialog.setTitle("Benutzer löschen");
                    alertDialog.setMessage("Möchten Sie den Benutzer wirklich löschen?");


                    alertDialog.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new DeleteUserRequest(rawUsers.get(position).getUserId()).execute();
                            Toast.makeText(SubUserActivity.this,"User wurde gelöscht.",Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.show();


                }
            });

            return convertView;
        }
    }


    private class DeleteUserRequest extends AsyncTask<Void, Void, String> {
        private Map<String, Object> request = new HashMap<>();
        private Map<String, String> response= new HashMap<>();

        public DeleteUserRequest(long userid) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("toDisableId", userid);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String url = getString(R.string.backend_ip)+"/user/subuser/disable";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                response=restTemplate.postForObject(url, request, Map.class);
            } catch(Exception e){
                Log.e("SubUserActivity", e.getMessage(),e);
            }
            return null;
        }
    }
}
