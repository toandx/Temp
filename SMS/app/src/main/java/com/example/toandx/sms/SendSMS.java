package com.example.toandx.sms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;

public class SendSMS extends AppCompatActivity {

    private EditText tel,msg;
    private String smsNumber,smsText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        tel=(EditText) findViewById(R.id.tel);
        msg=(EditText) findViewById(R.id.msg);
    }
    public void onClick2(View view)
    {
        smsNumber = tel.getText().toString();
        smsText = msg.getText().toString();
        if (smsNumber=="" || smsText=="" ) return;
        //Uri uri = Uri.parse("smsto:" + smsNumber);
        //Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        //intent.putExtra("sms_body", smsText);
        //startActivity(intent);
        //tel.setText("Tel:");
        //msg.setText("Message:");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(smsNumber, null, smsText, null, null);
        Intent intent2=new Intent();
        intent2.putExtra("NUM",smsNumber);
        intent2.putExtra("MSG",smsText);
        setResult(RESULT_OK,intent2);
        finish();
    }
}
