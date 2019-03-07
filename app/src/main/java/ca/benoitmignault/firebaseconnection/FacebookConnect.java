package ca.benoitmignault.firebaseconnection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FacebookConnect {

    private String email, firstName, lastName;
    private static Activity MYACTIVITY = null;
    static User ONEUSER = null;
    private static CallbackManager MCALLBACKMANAGER = null;
    private static String TAG = "INFORMATION_LOG_MESSAGE";
    private static String TAGUSER = "INFORMATION_USER";

    public FacebookConnect() {
        this.email = "";
        this.firstName = "";
        this.lastName = "";
    }

    public FacebookConnect(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void getConnectionWithFacebook(Activity myActivity, CallbackManager mCallbackManager){
        MCALLBACKMANAGER = mCallbackManager;
        MYACTIVITY = myActivity;
        ONEUSER = new User();

        LoginManager.getInstance().logInWithReadPermissions(MYACTIVITY, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(MCALLBACKMANAGER, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"facebookSuccess3 -> " + loginResult.toString());
                // App code
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG,"facebookSuccess4 -> " + response.toString());
                        getDataFromFacebook(object);

                        // Ici devra être appel de la function getUser(this.email, this.first_name, this.last_name); Qui va me retourner l'object User en entier....

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields","id,email,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"facebook -> " + "onCancel");
                Toast.makeText(MYACTIVITY, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"facebookONError -> " +error.toString());
                Toast.makeText(MYACTIVITY, "Une erreur s'est produite, veuillez réessayer plus tard...", Toast.LENGTH_LONG).show();
                // ...
            }
        });
    }

    public void getDataFromFacebook(JSONObject object) {
        try{
            String emailJSON = object.getString("email");
            String fristNameJSON = object.getString("first_name");
            String lastNameJSON = object.getString("last_name");

            Log.d(TAGUSER,"facebookSuccess -> " + emailJSON);
            Log.d(TAGUSER,"facebookSuccess -> " + fristNameJSON);
            Log.d(TAGUSER,"facebookSuccess -> " + lastNameJSON);

            //Log.d(TAGUSER,"facebookSuccess -> " + object.getString("email"));
            // Log.d(TAGUSER,"facebookSuccess -> " + object.getString("first_name"));
            //Log.d(TAGUSER,"facebookSuccess -> " + object.getString("last_name"));
            this.setEmail(emailJSON);
            this.setFirstName(fristNameJSON);
            this.setLastName(lastNameJSON);

            Log.d(TAGUSER,"facebookSuccess -> " + getEmail());
            Log.d(TAGUSER,"facebookSuccess -> " + getFirstName());
            Log.d(TAGUSER,"facebookSuccess -> " + getLastName());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
