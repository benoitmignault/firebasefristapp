package ca.benoitmignault.mesapprentissagesandroid;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.database.sqlite.SQLiteDatabase;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private String INFO_BD = "INFORMATION_BD";
    private String TAGUSER = "INFORMATION_LOG_USER";
    private User oneUser;
    InformationDATA db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        oneUser = new User();

        db = new InformationDATA(MainActivity.this);
        SQLiteDatabase database = db.getWritableDatabase(); // database sera utile pour parler avec notre DB
        db.onUpgrade(database,1,2); // Faire un reset de la database
        // db.onCreate(database); <--- Ne fait pas erreur de compilation si elle existe deja

        //String email, String fullname, int age, String gender, String city
        db.addUser("b.mignault@gmail.com","Benny Migo", 36, "M", "Montreal");
        db.addUser("marie-eve_dolbec@gmail.com","Marie D", 41, "F","St-Lambert");
        db.addUser("fred.wilson@gmail.com","Monsieur Wilson", 35, "M", "St-Laurent");

        db.onRequestInformation(); // Voir l'information avant les changements

        int idUser = db.getId("b.mignault@gmail.com"); // pour récuperer l'id pour ensuite modifier les informations
        Log.d(INFO_BD, "information ID -> " + idUser);

        User userModify = new User();
        userModify.setEmail("modification@gmail.com");
        userModify.setFullName("Mod Mop");
        userModify.setAge(123);
        userModify.setGender("FFF");
        userModify.setCity("Parisss");

        db.updateItem(userModify, idUser); // Nous allons modifier user en fct du email donner avec la fct getId

        db.onRequestInformation(); // // Voir l'information après les changements

        Toast.makeText(MainActivity.this, "Les email ont été ajoutés", Toast.LENGTH_SHORT).show();
        //db.onUpgrade(data,1,2);

        btnFBLogin = findViewById(R.id.btnFacebookLogin);
        btnEmailLogin = findViewById(R.id.btnEmailLogin);

        btnFBLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
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
        Log.d(TAGUSER, "signInWithCredential -> " + user.getEmail());
        Toast.makeText(MainActivity.this,"Bienvenue " + user.getDisplayName() + " ! Vous êtes connecté avec succès à QuizWin!", Toast.LENGTH_LONG).show();
        Intent newIntent = new Intent(MainActivity.this, LoginFacebookApi.class);
        newIntent.putExtra("email", user.getEmail());
        newIntent.putExtra("frist_name", user.getDisplayName());
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
                            Log.d(TAGUSER, "signInWithCredential -> " + user.getEmail());
                            btnFBLogin.setEnabled(true);
                            updateUI(user);
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
