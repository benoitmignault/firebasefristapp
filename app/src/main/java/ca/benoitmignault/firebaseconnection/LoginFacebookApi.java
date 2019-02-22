package ca.benoitmignault.firebaseconnection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFacebookApi extends AppCompatActivity {

        private TextView loggedInConfirm;
        private Button logoutBtn;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login_facebook_api);

            logoutBtn = findViewById(R.id.btnLogout);

            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutBtn.setEnabled(false);
                    mAuth.signOut(); // Déconnection de firebase
                    LoginManager.getInstance().logOut(); // déconnection de facebook
                    updateUI();
                }
            });

            String welcomePhrase = "Bienvenue " + getIntent().getStringExtra("username") + " ! Vous êtes" +
                    " connecté à QuizWin!";
            loggedInConfirm = findViewById(R.id.txtConfirm);
            loggedInConfirm.setText(welcomePhrase);
        }

        @Override
        public void onStart() {
            super.onStart();
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser == null){
                updateUI();
            }
        }

        public void updateUI(){
            Toast.makeText(LoginFacebookApi.this, "Vous êtes maintenant déconnecté...Au revoir!", Toast.LENGTH_LONG).show();
            Intent newIntent = new Intent(LoginFacebookApi.this, MainActivity.class);
            logoutBtn.setEnabled(true);
            startActivity(newIntent);
            finish();
        }
    }
