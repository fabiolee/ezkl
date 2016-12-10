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

import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author fabio.lee
 */
public class QrCodeGeneratorInputActivity extends AppCompatActivity {
    private static final String TAG = "QrCodeGeneratorInput";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generator_input);
        initToolbar();
        initContent();
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
        Button smsButton = (Button) findViewById(R.id.btn_sms);
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
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                SendMessageRequest request = new SendMessageRequest();
                request.username = "ceo";
                request.password = "9b55148c9acf5d400756ec35eede5ee7e078b0ef";
                request.msgType = "text";
                request.message = "Testing";
                request.messageTo = "0169006059";
                request.hashKey = "0af7e3aee276e5387d237b5c2533e378551f6171";
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
                                view.setEnabled(true);
                            }

                            @Override
                            public void onNext(SendMessageResponse sendMessageResponse) {
                                Log.d(TAG, "response=" + sendMessageResponse);
                                Toast.makeText(QrCodeGeneratorInputActivity.this,
                                        R.string.msg_sms_sent_success,
                                        Toast.LENGTH_SHORT).show();
                                view.setEnabled(true);
                            }
                        });
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
