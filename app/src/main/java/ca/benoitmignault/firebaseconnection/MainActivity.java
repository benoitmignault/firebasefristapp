package ca.benoitmignault.firebaseconnection;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private Button btnFBLogin;
    private Button btnEmailLogin;
    private FirebaseAuth mAuth;
    private String TAG = "INFORMATION_LOG_MESSAGE";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnFBLogin = findViewById(R.id.btnFacebookLogin);
        btnEmailLogin = findViewById(R.id.btnEmailLogin);

        // CallbackManager permet de faire le pont entre facebook et notre application
        mCallbackManager = CallbackManager.Factory.create();

        btnFBLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnFBLogin.setEnabled(false);
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG,"facebookSuccess -> " + loginResult.toString());
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG,"facebook -> " + "onCancel");
                        Toast.makeText(MainActivity.this, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG,"facebookONError -> " +error.toString());
                        Toast.makeText(MainActivity.this, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                        // ...
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    public void updateUI(FirebaseUser user) {
        Toast.makeText(MainActivity.this,"Bienvenue " + user.getDisplayName() + " ! Vous êtes connecté avec succès à QuizWin!", Toast.LENGTH_LONG).show();
        Intent newIntent = new Intent(MainActivity.this, LoginFacebookApi.class);
        newIntent.putExtra("username", user.getDisplayName());
        newIntent.putExtra("email", user.getEmail());
        startActivity(newIntent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG,"handleFacebookToken -> " + token.toString());

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential -> " + "success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            btnFBLogin.setEnabled(true);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential -> " + task.getException().toString());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            btnFBLogin.setEnabled(true);
                        }

                        // ...
                    }
                });
    }

}
