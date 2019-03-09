package ca.benoitmignault.firebaseconnection;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static android.content.pm.PackageManager.GET_SIGNATURES;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private Button btnFBLogin;
    private Button btnEmailLogin;
    private Button btnGoogleLogin;
    private Boolean buttonFB;
    private FirebaseUser user;
    private GoogleSignInAccount account;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private String TAG = "INFORMATION_LOG_MESSAGE";
    private String TAGUSER = "INFORMATION_LOG_USER";
    private User oneUser;
    private static final int RC_SIGN_IN = 9001;

    // Pour trouver la SHA-1
    // 48:A2:CD:71:CB:C8:F9:8F:A5:05:B0:A7:3C:CA:95:FF:1A:07:6A:05
    // C:\Program Files\Java\jdk1.8.0_162\bin> -- Le chemin pour s'y rendre
    // keytool -list -v -keystore %USERPROFILE%/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
    // keytool -list -v -keystore %USERPROFILE%/.android/debug.keystore -exportcert

    // Client ID ->         638663311999-k5l5p5jkcsft00v8qhehr42jsgf38ffq.apps.googleusercontent.com
    // Firebase client ID : 638663311999-i9c95pmj2l90eh8alk17vom58p0rl4t3.apps.googleusercontent.com

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();
        oneUser = new User();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        buttonFB = false;
        btnFBLogin = findViewById(R.id.btnFacebookLogin);
        btnEmailLogin = findViewById(R.id.btnEmailLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        btnGoogleLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        btnFBLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                buttonFB = true;
                btnFBLogin.setEnabled(false);
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(MainActivity.this, "Connexion en cours...", Toast.LENGTH_SHORT).show();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                        btnFBLogin.setEnabled(true);
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        Toast.makeText(MainActivity.this, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                        // ...
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        Toast.makeText(MainActivity.this, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                        // ...
                    }
                });
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK - Si c'est le bouton facebook qui a été caller
        if (buttonFB){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);

        /* Sans firebase
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }*/

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "Google sign in failed" + e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            updateUI(user, account);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this,"Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null, null);
                        }

                        // ...
                    }
                });
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(user, account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null, null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();

        // Comme nous avons deux connexions automatiques, on va aller avec cette logique
        if(user != null && account == null){
            updateUI(user, account);
        } else if (user == null && account != null){
            updateUI(user, account);
        }
    }

    public void updateUI(FirebaseUser user, GoogleSignInAccount account) {
        if (user != null) {
            oneUser.setEmail(user.getEmail());
        } else if (account != null) {
            oneUser.setEmail(account.getEmail());
        }
        //oneUser = getInfoUser(email); - La function que celine va me retourner avec quelques choses

        Toast.makeText(MainActivity.this,"Bienvenue " + user.getDisplayName() + " ! Vous êtes connecté avec succès à QuizWin!", Toast.LENGTH_LONG).show();
        Intent newIntent = new Intent(MainActivity.this, LoginFacebookApi.class);
        newIntent.putExtra("email", oneUser.getEmail());
        startActivity(newIntent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG,"handleFacebookToken -> " + token.toString());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential -> " + "success");
                    user = mAuth.getCurrentUser();
                    btnFBLogin.setEnabled(true);
                    updateUI(user, account);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential -> " + task.getException().toString());
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    btnFBLogin.setEnabled(true);
                }

                // ...
            }
        });
    }


}
