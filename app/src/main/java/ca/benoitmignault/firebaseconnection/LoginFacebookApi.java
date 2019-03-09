package ca.benoitmignault.firebaseconnection;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFacebookApi extends AppCompatActivity {

    private TextView loggedInConfirm;
    private Button logoutButton, signUpButton;
    private FirebaseAuth mAuth;
    private EditText fristName, lastName, email, password;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore quizWinBD; // ma BD en soit :)
    private String TAG = "INFORMATION_LOG_MESSAGE";
    NewAccount oneAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_facebook_api);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance(); // Important d'avoir ça sinon ça plante à tout coup
        quizWinBD = FirebaseFirestore.getInstance();

        String welcomePhrase = "Bienvenue " + getIntent().getStringExtra("frist_name") + " ! Vous êtes" +
                " connecté à QuizWin!";

        logoutButton = findViewById(R.id.btnLogout);
        signUpButton = findViewById(R.id.btnSignUp);
        loggedInConfirm = findViewById(R.id.txtConfirm);
        loggedInConfirm.setText(welcomePhrase);
        fristName = findViewById(R.id.champFristName);
        lastName = findViewById(R.id.champLastName);
        email = findViewById(R.id.champEmail);
        password = findViewById(R.id.champPassword);
        oneAccount = new NewAccount();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpButton.setEnabled(false);
                getValues(); // associer les valeurs des champs avec mes attribus de mon object
                creationAccount();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutButton.setEnabled(false);
                mAuth.signOut(); // Déconnection de firebase
                LoginManager.getInstance().logOut(); // déconnection de facebook
                updateUI();
            }
        });

    }

    private void setValuesOnEditText() {
        email.setText("");
        fristName.setText("");
        lastName.setText("");
        password.setText("");
        email.setText(getIntent().getStringExtra("email"));
        fristName.setText(getIntent().getStringExtra("frist_name"));
        lastName.setText(getIntent().getStringExtra("last_name"));
        password.setText("Aucun password");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Si le currentUser n'est pas null alors on rempli l'information, sinon on le kickout de la page
        if(currentUser == null){
            updateUI();
        } else {
            setValuesOnEditText(); // Au moment arrivé ici l'info est sauvegardé...
        }
    }

    public void updateUI(){
        Toast.makeText(LoginFacebookApi.this, "Vous êtes maintenant déconnecté...Au revoir!", Toast.LENGTH_LONG).show();
        Intent newIntent = new Intent(LoginFacebookApi.this, MainActivity.class);
        logoutButton.setEnabled(true);
        startActivity(newIntent);
        finish();
    }

    // Méthode pour aller setter les valeurs saisies par l'utilisateur à l'object crée
    private void getValues(){
        oneAccount.setEmail(email.getText().toString());
        oneAccount.setFirstName(fristName.getText().toString());
        oneAccount.setLastName(lastName.getText().toString());
        oneAccount.setPassword(password.getText().toString());
    }

    public void creationAccount(){
        // Create a new user with a first and last name
        Map<String, Object> oneUser = new HashMap<>();
        oneUser.put("email", email.getText().toString());
        oneUser.put("fristname", fristName.getText().toString());
        oneUser.put("lastname", lastName.getText().toString());
        oneUser.put("password", password.getText().toString());

        // Add a new document with a generated ID
        quizWinBD.collection("users")
                .add(oneUser)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID -> " + documentReference.getId());
                        Toast.makeText(LoginFacebookApi.this, "Création d'account réussi !", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to read value -> " + e);
                        Toast.makeText(LoginFacebookApi.this, "Problème avec la création... !", Toast.LENGTH_SHORT).show();
                    }

                });
        signUpButton.setEnabled(true);
    }
}
