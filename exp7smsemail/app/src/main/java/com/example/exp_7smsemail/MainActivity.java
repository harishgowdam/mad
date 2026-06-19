package com.example.exp_7smsemail;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etPhoneNumber, etOtp, etEmail, etPassword;
    private MaterialButton btnSendOtp, btnVerifyOtp, btnRegister, btnSignIn, btnVerifyEmail;
    private TextView tvSmsStatus, tvEmailStatus;

    private FirebaseAuth mAuth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail);

        tvSmsStatus = findViewById(R.id.tvSmsStatus);
        tvEmailStatus = findViewById(R.id.tvEmailStatus);
    }

    private void setupClickListeners() {
        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnRegister.setOnClickListener(v -> registerUser());
        btnSignIn.setOnClickListener(v -> signInUser());
        btnVerifyEmail.setOnClickListener(v -> sendEmailVerification());
    }

    // ==================== SMS VERIFICATION ====================

    private void sendOtp() {
        String phone = etPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Phone number required");
            etPhoneNumber.requestFocus();
            return;
        }

        if (!phone.startsWith("+")) {
            tvSmsStatus.setText("Include country code (e.g. +91)");
            return;
        }

        tvSmsStatus.setText("Sending OTP...");
        btnSendOtp.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        String code = credential.getSmsCode();
                        if (code != null) {
                            etOtp.setText(code);
                            verifyOtp();
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                        tvSmsStatus.setText("Verification failed: " + e.getMessage());
                        btnSendOtp.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(@NonNull String id,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = id;
                        resendingToken = token;
                        tvSmsStatus.setText("OTP sent successfully! Check your phone.");
                        btnSendOtp.setEnabled(true);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp() {
        String code = etOtp.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            etOtp.setError("Enter OTP");
            etOtp.requestFocus();
            return;
        }

        if (verificationId == null) {
            tvSmsStatus.setText("Request OTP first!");
            return;
        }

        tvSmsStatus.setText("Verifying...");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        tvSmsStatus.setText("SMS Verification successful!\nUser: " +
                                mAuth.getCurrentUser().getPhoneNumber());
                        Toast.makeText(this, "Phone verified!", Toast.LENGTH_SHORT).show();
                    } else {
                        tvSmsStatus.setText("Verification failed: " +
                                task.getException().getMessage());
                    }
                });
    }

    // ==================== EMAIL VERIFICATION ====================

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        tvEmailStatus.setText("Registering...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        tvEmailStatus.setText("Registration successful!\nSending verification email...");

                        sendEmailVerificationInternal(user);
                    } else {
                        tvEmailStatus.setText("Registration failed: " +
                                task.getException().getMessage());
                    }
                });
    }

    private void signInUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            tvEmailStatus.setText("Enter email and password");
            return;
        }

        tvEmailStatus.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String status = "Sign in successful!";
                        if (user != null) {
                            status += "\nEmail: " + user.getEmail();
                            status += "\nVerified: " + user.isEmailVerified();
                        }
                        tvEmailStatus.setText(status);
                    } else {
                        tvEmailStatus.setText("Sign in failed: " +
                                task.getException().getMessage());
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            tvEmailStatus.setText("Sign in first to send verification email");
            return;
        }

        sendEmailVerificationInternal(user);
    }

    private void sendEmailVerificationInternal(FirebaseUser user) {
        if (user == null) return;

        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        tvEmailStatus.setText(tvEmailStatus.getText() +
                                "\nVerification email sent to: " + user.getEmail() +
                                "\nCheck your inbox (and spam folder).");
                        Toast.makeText(this, "Verification email sent!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        tvEmailStatus.setText("Failed to send verification email: " +
                                task.getException().getMessage());
                    }
                });
    }
}
