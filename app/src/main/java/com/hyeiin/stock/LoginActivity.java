package com.hyeiin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnLoginLoading;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (FirebaseManager.isLoggedIn()) {
            FirebaseUser user = FirebaseManager.currentUser();
            if (user != null && user.isEmailVerified()) {
                loadUserAndNavigate();
                return;
            } else {
                FirebaseManager.auth().signOut();
            }
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilUserId);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etUserId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginLoading = findViewById(R.id.btnLoginLoading);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupListeners() {
        etEmail.addTextChangedListener(new ClearError(tilEmail));
        etPassword.addTextChangedListener(new ClearError(tilPassword));

        etPassword.setOnEditorActionListener((v, id, e) -> {
            if (id == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> attemptLogin());

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));
        }

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }
    }

    private void attemptLogin() {
        if (isLoading) return;

        String email = text(etEmail);
        String pw = text(etPassword);

        boolean err = false;
        if (email.isEmpty()) {
            tilEmail.setError("이메일을 입력하세요");
            err = true;
        }
        if (pw.isEmpty()) {
            tilPassword.setError("비밀번호를 입력하세요");
            err = true;
        }
        if (err) return;

        setLoading(true);

        FirebaseManager.auth()
                .signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        setLoading(false);
                        return;
                    }

                    if (!user.isEmailVerified()) {
                        FirebaseManager.auth().signOut();
                        setLoading(false);
                        showVerificationDialog(email, pw);
                        return;
                    }

                    loadUserAndNavigate();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    tilEmail.setError("이메일 또는 비밀번호가 올바르지 않습니다");
                    tilPassword.setError(" ");
                });
    }

    private void showVerificationDialog(String email, String pw) {
        new AlertDialog.Builder(this)
                .setTitle("이메일 인증이 필요합니다")
                .setMessage(
                        "'" + email + "' 로 발송된\n"
                                + "인증 이메일의 링크를 클릭한 후\n"
                                + "다시 로그인해 주세요.\n\n"
                                + "이메일이 오지 않았다면 재발송해 주세요.")
                .setPositiveButton("인증 메일 재발송", (d, w) ->
                        resendVerificationEmail(email, pw))
                .setNegativeButton("확인", null)
                .show();
    }

    private void resendVerificationEmail(String email, String pw) {
        FirebaseManager.auth()
                .signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) return;
                    user.sendEmailVerification()
                            .addOnSuccessListener(v -> {
                                FirebaseManager.auth().signOut();
                                Toast.makeText(
                                        this,
                                        "인증 이메일을 재발송했습니다.\n스팸함도 확인해 주세요.",
                                        Toast.LENGTH_LONG
                                ).show();
                            })
                            .addOnFailureListener(e -> {
                                FirebaseManager.auth().signOut();
                                Toast.makeText(
                                        this,
                                        "재발송 실패: " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                });
    }

    private void showForgotPasswordDialog() {
        View v = android.view.LayoutInflater.from(this)
                .inflate(R.layout.dialog_forgot_password, null);

        TextInputEditText etResetEmail = v.findViewById(R.id.etResetEmail);
        TextInputLayout tilResetEmail = v.findViewById(R.id.tilResetEmail);

        String prefilledEmail = text(etEmail);
        if (!prefilledEmail.isEmpty()) {
            etResetEmail.setText(prefilledEmail);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("비밀번호 찾기")
                .setView(v)
                .setPositiveButton("재설정 메일 발송", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(di -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
                String resetEmail = etResetEmail.getText() != null
                        ? etResetEmail.getText().toString().trim() : "";

                if (resetEmail.isEmpty()) {
                    tilResetEmail.setError("이메일을 입력하세요");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                    tilResetEmail.setError("올바른 이메일 형식이 아닙니다");
                    return;
                }
                tilResetEmail.setError(null);
                sendPasswordResetEmail(resetEmail, dialog);
            });
        });

        dialog.show();
    }

    private void sendPasswordResetEmail(String email, AlertDialog dialog) {
        FirebaseManager.auth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> {
                    dialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("이메일 발송 완료")
                            .setMessage(
                                    "'" + email + "' 로\n"
                                            + "비밀번호 재설정 링크를 발송했습니다.\n\n"
                                            + "이메일의 링크를 클릭하여\n"
                                            + "새 비밀번호를 설정해 주세요.\n\n"
                                            + "스팸함도 확인해 주세요.")
                            .setPositiveButton("확인", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("이메일 발송 완료")
                            .setMessage("해당 이메일로 재설정 링크를 발송했습니다.\n\n"
                                    + "가입된 이메일이 없는 경우 수신되지 않을 수 있습니다.")
                            .setPositiveButton("확인", null)
                            .show();
                });
    }

    private void loadUserAndNavigate() {
        String uid = FirebaseManager.uid();
        if (uid.isEmpty()) {
            setLoading(false);
            return;
        }

        FirebaseManager.db().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        FirebaseManager.auth().signOut();
                        setLoading(false);
                        Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserSession.get()
                            .setUid(uid)
                            .setName(doc.getString("name"))
                            .setEmail(doc.getString("email"))
                            .setRole(doc.getString("role"))
                            .setStoreId(doc.getString("storeId"))
                            .setStoreName(doc.getString("storeName"));

                    startActivity(new Intent(this, WelcomeActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "정보 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean on) {
        isLoading = on;
        btnLogin.setVisibility(on ? View.GONE : View.VISIBLE);
        btnLoginLoading.setVisibility(on ? View.VISIBLE : View.GONE);
        etEmail.setEnabled(!on);
        etPassword.setEnabled(!on);
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    static class ClearError implements TextWatcher {
        final TextInputLayout t;

        ClearError(TextInputLayout t) {
            this.t = t;
        }

        public void beforeTextChanged(CharSequence s, int i, int c, int a) {}

        public void afterTextChanged(Editable s) {}

        public void onTextChanged(CharSequence s, int i, int b, int c) {
            t.setError(null);
            t.setErrorEnabled(false);
        }
    }
}
