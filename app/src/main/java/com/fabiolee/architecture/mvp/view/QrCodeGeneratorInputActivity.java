package com.fabiolee.architecture.mvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fabiolee.architecture.mvp.R;
import com.fabiolee.architecture.mvp.model.bean.SendMessageRequest;
import com.fabiolee.architecture.mvp.model.bean.SendMessageResponse;
import com.fabiolee.architecture.mvp.model.remote.RetrofitHelper;
import com.fabiolee.architecture.mvp.model.remote.SmsService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author fabio.lee
 */
public class QrCodeGeneratorInputActivity extends AppCompatActivity {
    private static final String TAG = "QrCodeGeneratorInput";
    private static final String DATABASE_SMS_KEY = "sms";
    private static final String DATABASE_SMS_VALUE = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generator_input);
        initFirebaseDatabase();
        initToolbar();
        initContent();
    }

    private void initFirebaseDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference smsReference = database.getReference(DATABASE_SMS_KEY);
        smsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                if (!TextUtils.isEmpty(value)) {
                    sendMessage(value);
                    smsReference.setValue(DATABASE_SMS_VALUE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void sendMessage(String sms) {
        SendMessageRequest request = new SendMessageRequest();
        request.username = "ceo";
        request.password = "9b55148c9acf5d400756ec35eede5ee7e078b0ef";
        request.msgType = "text";
        request.message = sms;
        request.messageTo = "01128085427";
        request.hashKey = "d4ea84745ab2eca7b32c2d1f4a02a669fe1f84cd";
        RetrofitHelper retrofitHelper = new RetrofitHelper();
        SmsService smsService = retrofitHelper.newSmsService();
        smsService.sendMessage(request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<SendMessageResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "#sendMessage() -> onError()", e);
                        Toast.makeText(QrCodeGeneratorInputActivity.this,
                                R.string.msg_sms_sent_fail,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(SendMessageResponse sendMessageResponse) {
                        Log.d(TAG, "response=" + sendMessageResponse);
                        Toast.makeText(QrCodeGeneratorInputActivity.this,
                                R.string.msg_sms_sent_success,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initContent() {
        final Spinner statusSpinner = (Spinner) findViewById(R.id.spinner_status);
        final EditText stationEditText = (EditText) findViewById(R.id.et_station);
        final EditText priceEditText = (EditText) findViewById(R.id.et_price);
        final TextView errorTextView = (TextView) findViewById(R.id.tv_error);
        Button generateButton = (Button) findViewById(R.id.btn_generate);
        FloatingActionButton notifyButton = (FloatingActionButton) findViewById(R.id.btn_notify);

        ArrayAdapter<CharSequence> statusSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status,
                android.R.layout.simple_spinner_item);
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusSpinnerAdapter);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = (String) statusSpinner.getSelectedItem();
                String station = stationEditText.getText().toString();
                String price = formatMoney(priceEditText.getText().toString());
                if (TextUtils.isEmpty(status) || TextUtils.isEmpty(station) || TextUtils.isEmpty(price)) {
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    errorTextView.setVisibility(View.GONE);
                    startActivity(QrCodeGeneratorOutputActivity.getStartIntent(QrCodeGeneratorInputActivity.this,
                            status,
                            station,
                            price));
                }
            }
        });
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QrCodeGeneratorInputActivity.this,
                        QrCodeNotifierActivity.class);
                startActivity(intent);
            }
        });
    }

    private String formatMoney(String text) {
        double number;
        if (TextUtils.isEmpty(text)) {
            number = 0;
        } else {
            try {
                number = Double.parseDouble(text);
            } catch (Exception e) {
                number = 0;
            }
        }
        float epsilon = 0.004f; // 4 tenths of a cent
        String currency = "RM";
        String formatPattern;
        if (Math.abs(Math.round(number) - number) < epsilon) {
            formatPattern = "%s %1.0f"; // RM 100
        } else {
            formatPattern = "%s %1.2f"; // RM 100.10
        }
        return String.format(Locale.ENGLISH, formatPattern, currency, number);
    }
}
