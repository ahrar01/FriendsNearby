package com.ahraar.friendsnearby.OTP;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ahraar.friendsnearby.Activity.MainActivity;
import com.ahraar.friendsnearby.Activity.Register;
import com.ahraar.friendsnearby.R;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class Authentication extends AppCompatActivity {

    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final long ONE_HOUR_MILLI = 60 * 60 * 1000;
    private static final String TAG = "FirebasePhoneNumAuth";
    private static String uniqueIdentifier = null;
    LinearLayout layout1, layout2, layout3;
    StepView stepView;
    Dialog dialog_verifying, profile_dialog;
    private int currentStep = 0;
    private Spinner spinner;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth firebaseAuth;

    private String phoneNumber;
    private Button sendCodeButton;
    private Button verifyCodeButton;

    private Button button3;

    private EditText phoneNum;
    private PinView verifyCodeET;
    private TextView phonenumberText;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private boolean flag; //True -> goes to next screen, False -> Register Screen



    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");

    String phoneNo;


    @SuppressLint("SetTextI18n")
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);


        mAuth = FirebaseAuth.getInstance();
        flag = false;
        layout1 =  findViewById(R.id.layout1);
        layout2 =  findViewById(R.id.layout2);
        layout3 =  findViewById(R.id.layout3);
        sendCodeButton =  findViewById(R.id.submit1);
        verifyCodeButton = findViewById(R.id.submit2);
        button3 =  findViewById(R.id.submit3);
        firebaseAuth = FirebaseAuth.getInstance();
        phoneNum =  findViewById(R.id.phonenumber);
        verifyCodeET = (PinView) findViewById(R.id.pinView);
        phonenumberText =  findViewById(R.id.phonenumberText);
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));


        stepView = findViewById(R.id.step_view);
        stepView.setStepsNumber(3);
        stepView.go(0, true);
        layout1.setVisibility(View.VISIBLE);
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];

                phoneNo = phoneNum.getText().toString();
                String phoneNumber = "+" + code + phoneNo;
                phonenumberText.setText(phoneNumber);

                if (TextUtils.isEmpty(phoneNumber)) {
                    phoneNum.setError("Enter a Phone Number");
                    phoneNum.requestFocus();
                } else if (phoneNumber.length() < 10) {
                    phoneNum.setError("Please enter a valid phone");
                    phoneNum.requestFocus();
                } else {

                    if (currentStep < stepView.getStepCount() - 1) {
                        currentStep++;
                        stepView.go(currentStep, true);
                    } else {
                        stepView.done(true);
                    }
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            Authentication.this,// Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.dialog_pop_up, null);
                AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);

                show.setView(alertLayout);
                show.setCancelable(false);
                dialog_verifying = show.create();
                dialog_verifying.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog_verifying.show();
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String verificationCode = verifyCodeET.getText().toString();
                if (verificationCode.isEmpty()) {
                    Toast.makeText(Authentication.this, "Enter verification code", Toast.LENGTH_SHORT).show();
                } else {

                    LayoutInflater inflater = getLayoutInflater();
                    View alertLayout = inflater.inflate(R.layout.dialog_pop_up, null);
                    AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);

                    show.setView(alertLayout);
                    show.setCancelable(false);
                    dialog_verifying = show.create();
                    dialog_verifying.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog_verifying.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentStep < stepView.getStepCount() - 1) {
                    currentStep++;
                    stepView.go(currentStep, true);
                } else {
                    stepView.done(true);
                }
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.dialog_pop_up, null);
                AlertDialog.Builder show = new AlertDialog.Builder(Authentication.this);
                show.setView(alertLayout);
                show.setCancelable(false);
                profile_dialog = show.create();
                profile_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                profile_dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL);
                profile_dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
                profile_dialog.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        checkLoginStatus();
                        profile_dialog.dismiss();

                    }
                }, 2000);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void checkLoginStatus() {

        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currUser.getUid();

        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("Users");

        Query query = users.whereEqualTo("id", userId);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            flag = true;
                        }
                        if (flag) {
                            //get user details
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        } else {
                            startActivity(new Intent(getApplicationContext(), Register.class));
                        }


                    }
                });

    }





    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            dialog_verifying.dismiss();
                            if (currentStep < stepView.getStepCount() - 1) {
                                currentStep++;
                                stepView.go(currentStep, true);
                            } else {
                                stepView.done(true);
                            }
                            layout1.setVisibility(View.GONE);
                            layout2.setVisibility(View.GONE);
                            layout3.setVisibility(View.VISIBLE);
                            // ...
                        } else {

                            dialog_verifying.dismiss();
                            Toasty.error(Authentication.this, "Something wrong", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }


}

