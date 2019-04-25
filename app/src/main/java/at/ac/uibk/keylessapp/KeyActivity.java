package at.ac.uibk.keylessapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LauncherActivity;
import android.app.TimePickerDialog;
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
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.keylessapp.Models.KeyMode;
import at.ac.uibk.keylessapp.Models.SystemKey;
import at.ac.uibk.keylessapp.Models.SystemKeyPermission;
import at.ac.uibk.keylessapp.Models.SystemLock;

/**
 * KeyActivity class is used to display all the keys of a user.
 * User can also edit those keys within this activity.
 *
 * @author Davide De Sclavis
 */
public class KeyActivity extends AppCompatActivity {

    private List<String> stringListOfLocks = new ArrayList<>();
    private List<String> stringListOfLocksForKey = new ArrayList<>();
    private List<SystemLock> systemLocksList = new ArrayList<>();
    private List<SystemLock> systemLockListForKey = new ArrayList<>();
    private List<String> toShow = new ArrayList<>();
    private List<SystemKey> rawKeys = new ArrayList<>();

    public void setKeys(List<String> toShow, List<SystemKey> keys) {
        this.toShow = toShow;
        this.rawKeys = keys;
    }

    /**
     * @method onCreate
     * @param savedInstanceState
     *
     * A new LockListRequest for any given user is executed.
     * Also a new KeyListRequest is executed to get all the keys for the user.
     * Also a SwipeRefresh is implemented so that the user can refresh his settings at any time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_keys);

       //Toolbar
       Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
       setSupportActionBar(toolbar);
       getSupportActionBar().setDisplayShowHomeEnabled(true);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       getLockListForUser();
       new KeyListRequest().execute();
       final SwipeRefreshLayout mSwipeRefreshlayout= (SwipeRefreshLayout) findViewById(R.id.swiperefreshKeys);
        mSwipeRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshlayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshlayout.setRefreshing(false);
                        new KeyListRequest().execute();
                    }
                },700);
            }
        });
    }

    /**
     * method for the toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to execute a new LockListRequest.
     */
    private void getLockListForUser(){
        new LockListRequest().execute();
    }

    /**
     * Method to change the answer of a LockListRequest to a list of Strings to later
     * display them in a table.
     * @param answer A list of SystemLock.
     */
    private void returnLockListForUser(List<SystemLock> answer){
        for (SystemLock l: answer) {
            stringListOfLocks.add(l.getName());
            systemLocksList.add(l);
        }
    }

    /**
     * Method to change the answer of a KeyListRequest to a list of Strings to later
     * displayed them in a table.
     * Also an onItemClickListener for the table is implemented to click on the different items (keys)
     * and edit them.
     * @param answer A list of SystemKey.
     */
    private void showKeyList(final List<SystemKey> answer){
        ListView keys = (ListView) findViewById(R.id.keyList);

        List<String> newList = new ArrayList<>();
        for (SystemKey k : answer) {
            newList.add(k.getKeyName());
        }
        setKeys(newList, answer);
        final CustomKeyListAdapter customAdapter = new CustomKeyListAdapter(this, R.layout.list_item_layout_keys, toShow, answer);
        keys.setAdapter(customAdapter);
    }

    /**
     * If the user clicks on an item(key) a editDialog is opened.
     * In this dialog the selected key can be edited.
     * The startDate and endDate of the entrance qualification can be selected.
     * The ListView locks is used to display the given locks of an user so the user can select
     * which locks can be opened by the selected key.
     * @param locksForKey A list of SystemLock for which the selected key is valid.
     * @param i The index of the key in the table.
     */
    private void openEditDialog(List<SystemLock> locksForKey, int i, final SystemKey selectedKey) {
        Toast.makeText(KeyActivity.this,toShow.get(i),Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(KeyActivity.this);
        builder.setTitle(toShow.get(i));
        final Context context = KeyActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(toShow.get(i));

        final EditText startDate = new EditText(KeyActivity.this);
        final EditText endDate = new EditText(KeyActivity.this);
        final ListView locks = new ListView(KeyActivity.this);

        TextView newline= new TextView(context);
        TextView infoName = new TextView(context);
        TextView infoStartDate = new TextView(context);
        TextView infoEndDate = new TextView(context);
        TextView infoLocks = new TextView(context);

        infoName.setText("Schlüsselname");
        infoStartDate.setText("Start der Gültigkeit");
        infoEndDate.setText("Ende der Gültigkeit");
        infoLocks.setText("Schlösser");

        startDate.setFocusable(false);
        startDate.setKeyListener(null);
        endDate.setFocusable(false);
        endDate.setKeyListener(null);
        startDate.setText(selectedKey.getValidFromString());
        endDate.setText(selectedKey.getValidToString());

        // In Android, months are indexed starting at 0. Thus we need to add a 1 to the month-value from date-picker.
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        startDate.setText(day + "-" + (month+1) + "-" + year);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        endDate.setText(day + "-" + (month+1) + "-" + year);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        //locklist
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(KeyActivity.this,android.R.layout.select_dialog_multichoice, stringListOfLocks);
        locks.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        locks.setAdapter(arrayAdapter);
        layout.addView(newline);
        layout.addView(infoName);
        layout.addView(input);
        layout.addView(infoStartDate);
        layout.addView(startDate);
        layout.addView(infoEndDate);
        layout.addView(endDate);
        layout.addView(infoLocks);
        layout.addView(locks);
        for (int j=0;j<systemLocksList.size();j++){
            for (int x=0; x<locksForKey.size();x++){
                if (systemLocksList.get(j).getLockId()==locksForKey.get(x).getLockId()){
                    locks.setItemChecked(j,true);
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
                String input_text="";
                String input_startDate="";
                String input_endDate="";
                input_text=input.getText().toString();
                input_startDate= startDate.getText().toString();
                input_endDate= endDate.getText().toString();
                String test= input_text+","+input_startDate+","+input_endDate;
                List<Long> lockstoSend= new ArrayList<>();
                SparseBooleanArray checked= locks.getCheckedItemPositions();
                for(int j= 0; j<locks.getAdapter().getCount();j++){
                    if(checked.get(j)){
                        lockstoSend.add(systemLocksList.get(j).getLockId());
                    }
                }
                dialogInterface.dismiss();

                new KeyEditRequest(input_text, input_startDate, input_endDate, selectedKey.getKeyId(),lockstoSend).execute();
                Toast.makeText(KeyActivity.this,test,Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    /**
     * Method, which is invoked when a KeyPermissionRequest is executed.
     * Opens an AlertDialog where a user can edit the valid times for a key.
     * On pressing OK, the changes are sent to the server using the KeyPermissionEditRequest.
     * @param permission The initial permission value returned by the request.
     * @param keyId The id of the key, which is connected to the permission.
     */
    private void openTimeDialog(final SystemKeyPermission permission, final long keyId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(KeyActivity.this);
        builder.setTitle("Tägliche Berechtigungen");
        LinearLayout layout = new LinearLayout(KeyActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Spinner days = new Spinner(KeyActivity.this);
        final EditText fromTime = new EditText(KeyActivity.this);
        final EditText toTime = new EditText(KeyActivity.this);

        // Customization of the EditText fields for the times.
        fromTime.setGravity(Gravity.CENTER_HORIZONTAL);
        fromTime.setFocusable(false);
        fromTime.setKeyListener(null);
        toTime.setGravity(Gravity.CENTER_HORIZONTAL);
        toTime.setFocusable(false);
        toTime.setKeyListener(null);

        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                TimePickerDialog tpd = new TimePickerDialog(KeyActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        String chosenHour = Integer.toString(h).length() > 1 ? Integer.toString(h) : "0"+Integer.toString(h);
                        String chosenMin = Integer.toString(m).length() > 1 ? Integer.toString(m) : "0"+Integer.toString(m);
                        fromTime.setText(chosenHour+":"+chosenMin);
                        permission.setDayFrom(days.getSelectedItem().toString(), chosenHour+":"+chosenMin);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                tpd.show();
            }
        });
        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                TimePickerDialog tpd = new TimePickerDialog(KeyActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        String chosenHour = Integer.toString(h).length() > 1 ? Integer.toString(h) : "0"+Integer.toString(h);
                        String chosenMin = Integer.toString(m).length() > 1 ? Integer.toString(m) : "0"+Integer.toString(m);
                        toTime.setText(chosenHour+":"+chosenMin);
                        permission.setDayTo(days.getSelectedItem().toString(), chosenHour+":"+chosenMin);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                tpd.show();
            }
        });

        // Customization of the Spinner for choosing the days.
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        days.setAdapter(spinnerAdapter);
        days.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String dayName = adapterView.getItemAtPosition(i).toString();
                fromTime.setText(permission.getDayFrom(dayName));
                toTime.setText(permission.getDayTo(dayName));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        layout.addView(days);
        layout.addView(fromTime);
        layout.addView(toTime);

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
                new KeyPermissionEditRequest(keyId, permission).execute();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Method to switch between the keymodes DEACTIVATED, ACTIVATED and CUSTOM.
     * After selecting the mode a KeyModeEditRequest is executed to change the mode.
     * @param key
     */

    public void setKeyMode(final SystemKey key){
        AlertDialog.Builder builder = new AlertDialog.Builder(KeyActivity.this);
        builder.setTitle("Gültigkeit");
        LinearLayout layout = new LinearLayout(KeyActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView textViewMode= new TextView(KeyActivity.this);
        final Spinner mode = new Spinner(KeyActivity.this);

        textViewMode.setText("Modus: " +key.getMode().toLowString());

        mode.setAdapter(new ArrayAdapter<KeyMode>(this, android.R.layout.simple_spinner_item,KeyMode.values()));
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               String keymode= adapterView.getItemAtPosition(i).toString();
                key.setStringMode(keymode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        layout.addView(textViewMode);
        layout.addView(mode);
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
               new KeyModeEditRequest(key).execute();
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Method to change the answer of a LockListForKeyRequest to a list of Strings to later
     * display them.
     * @param answer A list of SystemLock.
     */
    private void returnLockListForKey(List<SystemLock> answer){
        for(SystemLock l: answer){
            stringListOfLocksForKey.add(l.getName());
            systemLockListForKey.add(l);
        }
    }

    /**
     * Private class used as a background-task to get a list of locks for a given key.
     */
    private class KeyListForKey extends AsyncTask<Void,Void,String> {
        private List<SystemLock> answer = new ArrayList<>();
        private Map<String, String> request = new HashMap<>();
        private int i;
        private SystemKey key;

        private KeyListForKey(Long keyId, int i){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("keyId", Long.toString(keyId));
            this.i = i;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                String url = getString(R.string.backend_ip)+"/lock/get-for-key";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemLock[] response = restTemplate.postForObject(url, request, SystemLock[].class);
                answer.addAll(Arrays.asList(response));

                url = getString(R.string.backend_ip)+"/key-by-id";
                key = restTemplate.postForObject(url, request, SystemKey.class);

                return answer.toString();
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            returnLockListForKey(answer);
            openEditDialog(answer, i, key);
        }
    }

    private class KeyPermissionRequest extends AsyncTask<Void,Void,String> {
        private SystemKeyPermission permission;
        private Map<String, String> request = new HashMap<>();
        private long keyId;

        private KeyPermissionRequest(long keyId) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("keyId", Long.toString(keyId));
            this.keyId = keyId;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/key/permission";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                permission = restTemplate.postForObject(url, request, SystemKeyPermission.class);
                return permission.toString();
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            openTimeDialog(permission, keyId);
        }
    }

    private class KeyPermissionEditRequest extends AsyncTask<Void,Void,String> {
        private Map<String, String> request = new HashMap<>();

        private KeyPermissionEditRequest(long keyId, SystemKeyPermission newPermission) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
            request.put("keyId", Long.toString(keyId));
            request.putAll(newPermission.toMap());
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url= getString(R.string.backend_ip)+"/key/edit/entire-permission";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.put(url, request);
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }
    }

    /**
     * Private class used as a background-task to get all the keys for a user.
     */
    private class KeyListRequest extends AsyncTask<Void,Void,String> {
        private List<SystemKey> answer = new ArrayList<>();
        private Map<String, String> request = new HashMap<>();

        private KeyListRequest() {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            request.put("session", sharedPref.getString("savedSession", "-error-"));
            request.put("username", sharedPref.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String url= getString(R.string.backend_ip)+"/keys";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemKey[] response= restTemplate.postForObject(url, request, SystemKey[].class);
                answer.addAll(Arrays.asList(response));
                return answer.toString();
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showKeyList(answer);
        }
    }

    private class KeyModeEditRequest extends AsyncTask<Void,Void,String>{
        private Map<String, Object> requestBody = new HashMap<>();

        public KeyModeEditRequest(SystemKey key){
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            requestBody.put("session", sharedPref.getString("savedSession", "-error-"));
            requestBody.put("username", sharedPref.getString("savedUsername", "-error-"));
            requestBody.put("newMode", key.getMode());
            requestBody.put("keyId", Long.toString(key.getKeyId()));
            requestBody.put("newName",key.getKeyName());
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String url= getString(R.string.backend_ip)+"/key/edit";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.put(url, requestBody);
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }

    }

    /**
     * Private class used as a background-task to send the edited key with all the changes to the server.
     */
    private class KeyEditRequest extends AsyncTask<Void, Void, String> {

        private Map<String, Object> requestBody = new HashMap<>();

        public KeyEditRequest(String newName, String validFrom, String validTo, Long keyId, List<Long> locks) {
            SharedPreferences sharedPref = getSharedPreferences("keyless-pref", Context.MODE_PRIVATE);;
            requestBody.put("session", sharedPref.getString("savedSession", "-error-"));
            requestBody.put("username", sharedPref.getString("savedUsername", "-error-"));
            requestBody.put("newName", newName);
            requestBody.put("validFrom", validFrom);
            requestBody.put("validTo", validTo);
            requestBody.put("isCustom", Boolean.toString(true));
            requestBody.put("keyId", Long.toString(keyId));
            requestBody.put("lockIds", locks);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                final String url= getString(R.string.backend_ip)+"/key/edit";
                RestTemplate restTemplate= new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                restTemplate.put(url, requestBody);
            }catch(Exception e){
                Log.e("KeyActivity", e.getMessage(),e);
            }
            return null;
        }
    }

    /**
     * Private class used as a background-task to get all the locks for a user.
     */
    private class LockListRequest extends AsyncTask<Void, Void, String> {
        private List<SystemLock> answer = new ArrayList<>();
        private Map<String, String> request = new HashMap<>();

        private LockListRequest(){
            SharedPreferences sharedPreferences= getSharedPreferences("keyless-pref",Context.MODE_PRIVATE);
            request.put("session", sharedPreferences.getString("savedSession","-error-"));
            request.put("username", sharedPreferences.getString("savedUsername", "-error-"));
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                final String url = getString(R.string.backend_ip)+"/lock/get-for-user";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                SystemLock[] response = restTemplate.postForObject(url,request,SystemLock[].class);
                answer.addAll(Arrays.asList(response));
                return answer.toString();
            }catch (Exception e){
                Log.e("NfcActivity", e.getMessage(),e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result){
            returnLockListForUser(answer);
        }
    }

    /**
     * ListAdapter for every ListItem to display keyname and buttons to change permissions and
     * validities of a key.
     */
    public class CustomKeyListAdapter extends ArrayAdapter<String> {

        private Context context;
        private TextView keyName;
        private AppCompatImageButton editButton;
        private AppCompatImageButton timeButton;
        private AppCompatImageButton modeButton;
        private List<String> keys;
        private List<SystemKey> rawKeys;


        public CustomKeyListAdapter(Context context, int resource, List<String> listValues, List<SystemKey> raw) {
            super(context, resource, listValues);
            this.context = context;
            this.keys = listValues;
            this.rawKeys = raw;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final String currentName = keys.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_layout_keys, null);


            keyName = (TextView) convertView.findViewById(R.id.keyName);
            keyName.setText(currentName);
            keyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, currentName, Toast.LENGTH_SHORT).show();
                }
            });



            editButton = (AppCompatImageButton) convertView.findViewById(R.id.keyEditButton);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new KeyListForKey(rawKeys.get(position).getKeyId(), position).execute();
                }
            });

            timeButton = (AppCompatImageButton) convertView.findViewById(R.id.keyTimeButton);
            timeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new KeyPermissionRequest(rawKeys.get(position).getKeyId()).execute();
                }
            });
            modeButton= (AppCompatImageButton) convertView.findViewById(R.id.keyModeButton);
            modeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setKeyMode(rawKeys.get(position));
                }
            });
            if(rawKeys.get(position).getMode().toLowString().equals("Disabled")){
                keyName.setTextColor(Color.RED);
            }else if(rawKeys.get(position).getMode().toLowString().equals("Enabled")){
                keyName.setTextColor(Color.GREEN);
            }
            if(rawKeys.get(position).getMode().toLowString().equals("Custom")){
                keyName.setTextColor(Color.rgb(255,105,0));
                timeButton.setVisibility(View.VISIBLE);
                editButton.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }
}