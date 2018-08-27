package com.example.toandx.sms;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.linecorp.linesdk.LineApiResponse;
import com.linecorp.linesdk.api.LineApiClient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private IncomingSms SMS;
    private BroadcastReceiver nhan;
    private ListView list;
    private CustomListAdapter adapter;
    private FirebaseDatabase dataBase;
    private String uid;
    private ArrayList<String> name;
    private ArrayList<String> info;
    private Uri inboxURI;
    private String[] reqCols;
    private Cursor c;
    private String numPhone,body;
    private Bundle data;
    private Message message;
    private Handler handler;
    private DatabaseReference ref;
    private GoogleApiClient mGoogleApiClient;
    private String androidId;
    private LineApiClient lineApiClient;
    final static String CHANNEL_ID="1602879521";
    private void lockScreenOrientation(){
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    // This method is used to reenable orientation changes after an ASyncTask is finished.
    private void unlockScreenOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public class LogoutTask extends AsyncTask<Void, Void, LineApiResponse> {

        final static String TAG = "LogoutTask";

        protected void onPreExecute(){
            lockScreenOrientation();
        }

        protected LineApiResponse doInBackground(Void... params) {
            return lineApiClient.logout();
        }

        protected void onPostExecute(LineApiResponse apiResponse){

            if(apiResponse.isSuccess()){
                Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Logout Failed", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Logout Failed: " + apiResponse.getErrorData().toString());
            }
            unlockScreenOrientation();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");
        name=new ArrayList<String>();
        info=new ArrayList<String>();
        dataBase=FirebaseDatabase.getInstance();
        list=(ListView) findViewById(R.id.listview);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        handler=new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.getData()!=null) {
                    info.add(msg.getData().getString("info"));
                    name.add(msg.getData().getString("name"));
                    adapter.notifyDataSetChanged();
                    list.invalidateViews();
                }
            }
        };



        inboxURI = Uri.parse("content://sms/inbox");
        // List required columns
        reqCols = new String[] {"_id","address", "body" };

        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = getContentResolver();
        c = cr.query(inboxURI, reqCols, null, null, null);
        // Fetch Inbox SMS Message from Built-in Content Provider
        adapter=new CustomListAdapter(this,name,info);
        list.setAdapter(adapter);
        Runnable run1=new Runnable() {
            @Override
            public void run() {

                if (c.moveToFirst())
                {
                    do {
                        numPhone=c.getString(c.getColumnIndex("address"));
                        Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.DATA + "='" + numPhone + "'",
                                null, null);
                        if (phone.moveToFirst()) {
                            String id = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            Cursor phonect = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                                    null, ContactsContract.Contacts._ID + "=" + id, null, null);
                            if (phonect.moveToFirst()) {
                                String s = phonect.getString(phonect.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                numPhone = numPhone + "(" + s + ")";
                            }
                        }
                        //final String abc=numphone;
                        //final String ten1=c.getString(c.getColumnIndex("body"));
                        data=new Bundle();
                        data.putString("info",c.getString(c.getColumnIndex("body")));
                        data.putString("name",numPhone);
                        message=new Message();
                        message.setData(data);
                        handler.sendMessage(message);
                        //String key=ref.push().getKey();
                        //ref.child(key).setValue(new MSG(numphone,c.getString(c.getColumnIndex("body"))));
                        /*list.post(new Runnable() {
                            @Override
                            public void run() {
                                name.add(abc);
                                info.add(ten1);
                                adapter.notifyDataSetChanged();
                                list.invalidateViews();
                            }
                        });*/
                    } while (c.moveToNext());
                }
            }
        };
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(MainActivity.this,Integer.toString(i),Toast.LENGTH_LONG).show();
            }
        });
        SMS=new IncomingSms();
        nhan=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s1=intent.getStringExtra("NUM");
                String s2=intent.getStringExtra("MSG");
                Log.d("LOG","Da nhan");
                    Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,ContactsContract.CommonDataKinds.Phone.DATA+"='"+s1+"'",
                            null,null);
                    if (phone.moveToFirst()) {
                        String id = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                        Cursor phonect=getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                                null,ContactsContract.Contacts._ID+"="+id,null,null);
                        if (phonect.moveToFirst())
                        {
                            String s=phonect.getString(phonect.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            s1=s1+"("+s+")";
                        }
                    }
                    phone.close();
                    name.add(0,s1);
                    info.add(0,s2);
                    Log.d("LOG","Da thuc hien xong");
                adapter.notifyDataSetChanged();
                list.invalidateViews();
                ref=dataBase.getReference("MSG").child(uid).child(androidId);
                String key=ref.push().getKey();
                ref.child(key).setValue(s1,s2);
                Log.d("LOG","Da OK");
            }
        };
        new Thread(run1).start();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(SMS);
        unregisterReceiver(nhan);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(SMS,new IntentFilter("com.example.toandx.sms.IncomingSMS"));
        registerReceiver(nhan,new IntentFilter("com.example.toandx.service.receiver"));
    }
    public void Send(View view)
    {
        Intent intent=new Intent(this,SendSMS.class);
        startActivityForResult(intent,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        list.invalidateViews();

    }
    public void Upload()
    {
        Log.d("LOG","Upload "+uid+" "+androidId+" "+Integer.toString(name.size()));
        ref=dataBase.getReference().child("MSG").child(uid).child(androidId);
        ref.removeValue();
        for(int i=0;i<name.size();++i)
        {
            String key=ref.push().getKey();
            ref.child(key).setValue(new MSG(name.get(i),info.get(i)));
        }

    }
    public void Download()
    {
        ref=dataBase.getReference("MSG").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.clear();
                info.clear();
                for(DataSnapshot phoneId:dataSnapshot.getChildren()) {
                    for (DataSnapshot contact : phoneId.getChildren()) {
                        MSG c = contact.getValue(MSG.class);
                        name.add(c.tel);
                        info.add(c.info);
                    }
                }
                Log.d("LOG","Download Complete "+Integer.toString(name.size()));
                adapter.notifyDataSetChanged();
                list.invalidateViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void Sync(View view)
    {
        if (!isOnline()) Toast.makeText(getApplicationContext(),"No Internet Connection",Toast.LENGTH_LONG).show();
        else {
            Upload();
            Download();
            Toast.makeText(getApplicationContext(),"Sync Complete",Toast.LENGTH_SHORT).show();
        }
    }
    private void goLoginScreen() {
        Intent intent = new Intent(getApplicationContext(), Activity_Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void logOut(View view)
    {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                    }
                });
        //new LogoutTask().execute();
        goLoginScreen();
    }
    @Override
    protected void onStart()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        super.onStart();
    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

}
