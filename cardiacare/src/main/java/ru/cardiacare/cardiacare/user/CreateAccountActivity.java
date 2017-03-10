package ru.cardiacare.cardiacare.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import ru.cardiacare.cardiacare.R;

/* Экран "Создать аккаунт" */

public class CreateAccountActivity extends AppCompatActivity{

    static public Context mContextCreateAccountActivity;
    EditText etLogin;
    EditText etPassword;
    EditText etPassword2;
    EditText etInviteCode;
    Button Registration;
    public static String emailCreateAccountActivity = "";
    public static String passwordCreateAccountActivity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etLogin = (EditText) this.findViewById(R.id.etEmail);
        etPassword = (EditText) this.findViewById(R.id.etPassword);
        etPassword2 = (EditText) this.findViewById(R.id.etPassword2);
        etInviteCode = (EditText) this.findViewById(R.id.etInviteCode);

        mContextCreateAccountActivity = this;

        Registration = (Button) findViewById(R.id.nextButton);
        Registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emptyCheck(etLogin.getText().toString(), etPassword.getText().toString(), etPassword2.getText().toString(), etInviteCode.getText().toString()))
                    extractLogPass(etLogin.getText().toString(), etPassword.getText().toString(), etPassword2.getText().toString(), etInviteCode.getText().toString());
            }
        });

    }

    void extractLogPass(String login, String password, String password2, String code) {
        JSONObject json = null;
        emailCreateAccountActivity = login;
        passwordCreateAccountActivity = password;

//        etLogin = (EditText) this.findViewById(R.id.etLogin);
//        etPassword = (EditText) this.findViewById(R.id.etPassword);
//        etPassword2 = (EditText) this.findViewById(R.id.etPassword2);
//        etInviteCode = (EditText) this.findViewById(R.id.etInviteCode);
//
//        String login = "";
//        login = etLogin.getText().toString();
//        String password = "";
//        password = etPassword.getText().toString();
//        String code = "";
//        code = etInviteCode.getText().toString();

            String str = "{ \"email\":\"" + login + "\", "
                    + "\"password\":\"" + password + "\", "
                    + "\"inviteCode\":\"" + code + "\"}";

        try {
            json = new JSONObject(str);

            CreateAccountPost ecgPost = new CreateAccountPost();
            ecgPost.execute(json);

        }
        catch (Exception e){}
    }

    private boolean emptyCheck(String login, String password, String password2, String code) {
        if (login.equals("") || password.equals("") || (!password.equals(password2)) || code.equals("") || (login.indexOf("@") == -1)) {
            //Toast.makeText(getApplicationContext(), "Please, fill fields", Toast.LENGTH_LONG).show();
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setMessage(R.string.dialog_authorization_message)
                    .setTitle(R.string.dialog_authorization_title)
                    .setCancelable(true)
                    .setPositiveButton(R.string.dialog_authorization_positive_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
            return false;
        } else
            return true;
    }
}