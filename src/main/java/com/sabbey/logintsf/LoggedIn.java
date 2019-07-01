package com.sabbey.logintsf;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit2.Call;
import retrofit2.Response;

public class LoggedIn extends AppCompatActivity {

    TextView name, email;
    ImageView pic;
    Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId("442977416479964");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_logged_in);

        name = findViewById(R.id.textView);
        email = findViewById(R.id.email);
        pic = findViewById(R.id.image);
        logoutButton = findViewById(R.id.signOut);

        putInfo();

        onSignOut();

    }

    public void putInfo() {
        Intent intent = getIntent();
        name.setText("Name: " + intent.getStringExtra("name"));
        email.setText("Email: " + intent.getStringExtra("email"));
        String platform = intent.getStringExtra("platform");
        if (platform.equals("facebook"))
            new DownLoadImageTask(pic).execute(intent.getStringExtra("url"));
        else if (platform.equals("twitter")) {
            twitter();
        } else if (platform.equals("google")) {
            new DownLoadImageTask(pic).execute(intent.getStringExtra("url").concat("?sz=600"));
        }
    }

    public void onSignOut() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoggedIn.this, gso);
                mGoogleSignInClient.signOut();
                Toast.makeText(LoggedIn.this, "Signed out", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void twitter() {
        TwitterSession session = TwitterCore.getInstance()
                .getSessionManager().getActiveSession();
        TwitterCore.getInstance().getApiClient(session).getAccountService().verifyCredentials(true, false, true).enqueue(new retrofit2.Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    //If it succeeds creating a User object from userResult.data
                    User user = response.body();
                    //Getting the profile image url
                    new DownLoadImageTask(pic).execute(user.profileImageUrl.replace("_normal", ""));

                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to fetch profile picture", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.three_dots_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.delete)
        {

            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Do you really want to delete the account from firebase?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            delete();
                        }})
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }

        return true;
    }

    public void delete()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            LoginManager.getInstance().logOut();
                            TwitterCore.getInstance().getSessionManager().clearActiveSession();
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build();
                            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoggedIn.this, gso);
                            mGoogleSignInClient.signOut();
                            Toast.makeText(LoggedIn.this, "Deleted", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                        else {
                            Toast.makeText(LoggedIn.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
