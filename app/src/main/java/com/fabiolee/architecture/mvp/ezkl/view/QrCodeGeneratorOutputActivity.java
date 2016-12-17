package com.fabiolee.architecture.mvp.ezkl.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fabiolee.architecture.mvp.ezkl.R;
import com.fabiolee.architecture.mvp.ezkl.model.bean.SendMessageRequest;
import com.fabiolee.architecture.mvp.ezkl.model.bean.SendMessageResponse;
import com.fabiolee.architecture.mvp.ezkl.model.remote.RetrofitHelper;
import com.fabiolee.architecture.mvp.ezkl.model.remote.SmsService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Arrays;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author fabio.lee
 */
public class QrCodeGeneratorOutputActivity extends AppCompatActivity {
    private static final String TAG = "QrCodeGeneratorOutput";
    private static final String DATABASE_SMS_KEY = "sms";
    private static final String DATABASE_SMS_VALUE = "";
    private static final String DATABASE_SCANNER_KEY = "scanner";
    private static final String DATABASE_SCANNER_VALUE_DONE = "done";
    private static final String DATABASE_SCANNER_VALUE_NEW = "new";

    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    public static final String PARAM_STATUS = "param_status";
    public static final String PARAM_STATION = "param_station";
    public static final String PARAM_PRICE = "param_price";

    private String status;
    private String station;
    private String price;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    public static Intent getStartIntent(Context context, String status, String station, String price) {
        Intent intent = new Intent(context, QrCodeGeneratorOutputActivity.class);
        intent.putExtra(PARAM_STATUS, status);
        intent.putExtra(PARAM_STATION, station);
        intent.putExtra(PARAM_PRICE, price);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generator_output);
        if (initIntent()) {
            return;
        }
        initFirebaseAuth();
        initQrCode();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private boolean initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            status = intent.getStringExtra(PARAM_STATUS);
            station = intent.getStringExtra(PARAM_STATION);
            price = intent.getStringExtra(PARAM_PRICE);
        }
        if (TextUtils.isEmpty(status) || TextUtils.isEmpty(station) || TextUtils.isEmpty(price)) {
            finish();
            return true;
        }
        return false;
    }

    private void initFirebaseAuth() {
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signedIn:" + user.getUid());
                    writeAndReadDatabase();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signedOut");
                    signInAnonymously();
                }
            }
        };
    }

    private void initQrCode() {
        TextView statusTextView = (TextView) findViewById(R.id.tv_status);
        ImageView qrCodeImageView = (ImageView) findViewById(R.id.qr_code);
        TextView stationTextView = (TextView) findViewById(R.id.tv_station);
        statusTextView.setText(status);
        try {
            String secretKey = "7sv4AS6sGK";
            String encodeText = TextUtils.join(",", Arrays.asList(status, station, price, secretKey));
            Log.d(TAG, "encodeText=" + encodeText);
            Bitmap bitmap = encodeAsBitmap(encodeText);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e(TAG, "QR code #encodeAsBitmap(String) error.", e);
        }
        stationTextView.setText(station);
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        Resources resources = getResources();
        int blackColor = resources.getColor(R.color.black);
        int whiteColor = resources.getColor(R.color.white);
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? blackColor : whiteColor;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private void signInAnonymously() {
        auth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(QrCodeGeneratorOutputActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeAndReadDatabase();
                        }
                    }
                });
    }

    private void writeAndReadDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        initSmsReference(database);
        initScannerReference(database);
    }

    private void initSmsReference(FirebaseDatabase database) {
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

    private void initScannerReference(FirebaseDatabase database) {
        DatabaseReference scannerReference = database.getReference(DATABASE_SCANNER_KEY);
        scannerReference.setValue(DATABASE_SCANNER_VALUE_NEW);
        scannerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                if (DATABASE_SCANNER_VALUE_DONE.equalsIgnoreCase(value)) {
                    Toast.makeText(QrCodeGeneratorOutputActivity.this, R.string.msg_thank_you, Toast.LENGTH_SHORT)
                            .show();
                    finish();
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
                        Toast.makeText(QrCodeGeneratorOutputActivity.this,
                                R.string.msg_sms_sent_fail,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(SendMessageResponse sendMessageResponse) {
                        Log.d(TAG, "response=" + sendMessageResponse);
                        Toast.makeText(QrCodeGeneratorOutputActivity.this,
                                R.string.msg_sms_sent_success,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
