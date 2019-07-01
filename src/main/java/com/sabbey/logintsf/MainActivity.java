package com.sabbey.logintsf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class MainActivity extends AppCompatActivity {

    LoginButton fLoginButton;
    CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private TwitterLoginButton TLoginButton;
    private SignInButton gSignIn;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private String TWITTER_KEY = "yrJdpgq1b4JRINB3E9t847TiG";
    private String TWITTER_SECRET = "r97ljlvwOVp95Kd7amCHStOYE6ZmIwqQl91t9QVOSikgIJEp7w";
    String twitterEmail = "";
    final int GOOGLE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config();
        setContentView(R.layout.activity_main);


        mCallbackManager = CallbackManager.Factory.create();
        fLoginButton = findViewById(R.id.login_button);
        TLoginButton = findViewById(R.id.tLogin);
        fLoginButton.setReadPermissions("email", "public_profile");
        gSignIn = findViewById(R.id.google);
        mAuth = FirebaseAuth.getInstance();

        //For facebook
        callBack();

        //For twitter
        twitter();

        //For google
        gSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

    }


    public void config() {
        FacebookSdk.setApplicationId("442977416479964");
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Initialize twitter SDK
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(TWITTER_KEY,
                        TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    //For facebook
    public void callBack() {

        fLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = AccessToken.getCurrentAccessToken();
                handleFacebookAccessToken(token);
            }

            @Override
            public void onCancel() {
                Log.d("Tag", "facebook:onCancel");
                Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                LoginManager.getInstance().logOut();
            }
        });

    }

    // For facebook
    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(MainActivity.this, "Authentication Successful.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);


                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "fail" + " signInWithCredential:failure " + task.getException().getMessage(), Toast.LENGTH_LONG)
                                    .show();
                            LoginManager.getInstance().logOut();
                        }

                        // ...
                    }
                });
    }

    // For twitter
    public void twitter() {

        TLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                //Log.d(TAG, "twitterLogin:success" + result);

                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    // For twitter
    private void handleTwitterSession(final TwitterSession session) {

        twitterEmail = getEmail(session);
        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(MainActivity.this, "Authentication successful.",
                                    Toast.LENGTH_SHORT).show();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user.getEmail() == null)
                                user.updateEmail(twitterEmail);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(MainActivity.this, "fail" + " signInWithCredential:failure " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG)
                                    .show();
                            TwitterCore.getInstance().getSessionManager().clearActiveSession();

                        }

                        // ...
                    }
                });
    }

    // For twitter
    public String getEmail(TwitterSession session) {
        TwitterAuthClient twitterAuthClient = new TwitterAuthClient();

        twitterAuthClient.requestEmail(session, new com.twitter.sdk.android.core.Callback<String>() {
            @Override
            public void success(Result<String> emailResult) {

                twitterEmail = emailResult.data;
            }

            @Override
            public void failure(TwitterException e) {

                twitterEmail = "Not found";

            }
        });

        return twitterEmail;
    }

    //For Google
    public void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE);
    }

    //For Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Authentication Successful.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            mGoogleSignInClient.signOut();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
            Toast.makeText(MainActivity.this, "Already signed in",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //For fb
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        //For twitter
        TLoginButton.onActivityResult(requestCode, resultCode, data);

        //For google
        if (requestCode == GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void updateUI(FirebaseUser user) {

        AccessToken token = AccessToken.getCurrentAccessToken();
        TwitterSession session = TwitterCore.getInstance()
                .getSessionManager().getActiveSession();
        Intent intent = new Intent(MainActivity.this, LoggedIn.class);
        // For facebook
        if (token != null) {
            String image_url = "https://graph.facebook.com/" + token.getUserId() + "/picture?height=500";
            intent.putExtra("url", image_url);
            intent.putExtra("platform", "facebook");
        }

        // For twitter
        else if (session != null) {
            intent.putExtra("platform", "twitter");
        } else {

            intent.putExtra("platform", "google");
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
            if (acct != null) {
                intent.putExtra("url", acct.getPhotoUrl().toString());
            }
        }
        intent.putExtra("name", user.getDisplayName());
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }


}




