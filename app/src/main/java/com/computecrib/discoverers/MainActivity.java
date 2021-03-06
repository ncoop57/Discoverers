package com.computecrib.discoverers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.TurnBasedMultiplayerClient;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

//import android.support.design.widget.FloatingActionButton;

//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.auth.api.signin.GoogleSignInResult;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
@SuppressLint("RestrictedApi")
public class MainActivity extends AppCompatActivity {
    private static final int INVITE_REQUEST = 103;
    static Challenge currentChallenge;

    private Challenge mTurnData;
    private static final String TAG = "MainActivity";
    private static final int PLACE_PICKER_REQUEST = 102;
    private static final int CHALLENGE_SOLUTION_REQUEST = 101;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_UNUSED = 49001;
    private static final int RC_MAKE_CHALLENGE = 4001;

    // Build a GoogleSignInClient with the options specified by gso.
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private TurnBasedMultiplayerClient mMultiClient;
    private InvitationsClient mInvitationsClient;
    private TurnBasedMatch mTurnBasedMatch;
    private String mPlayerId;
    private SignInButton signInButton;

    public static Boolean successful = null;
    private Button okayButton;
//    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private PlayersClient mPlayersClient;

    @SuppressLint({"RestrictedApi", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
//                .requestIdToken("705477496394-ffiu4vpunk7i401tjairergs3slve0qo.apps.googleusercontent.com")
//                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        // Set the dimensions of the sign-in button.
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signIn();
            }
        });

//        Button signOutButton = findViewById(R.id.sign_out_button);
//        signOutButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                revokeAccess();
//            }
//        });


        final FloatingActionsMenu menuFab = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        FloatingActionButton basicChallengeBtn = (FloatingActionButton) findViewById(R.id.add_basic_challenge);
        basicChallengeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, MakeChallengeActivity.class);
                startActivityForResult(intent, RC_MAKE_CHALLENGE);
                menuFab.collapseImmediately();
            }
        });

        FloatingActionButton inviteFriend = (FloatingActionButton) findViewById(R.id.fab_invite_friend);
        inviteFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onStartMatchClicked();
                menuFab.collapseImmediately();
            }
        });

        FloatingActionButton quizChallengeBtn = (FloatingActionButton) findViewById(R.id.add_quiz_challenge);
        quizChallengeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(MainActivity.this, "Quiz Challenge", Toast.LENGTH_SHORT).show();
                menuFab.collapseImmediately();
            }
        });

        FloatingActionButton seqChallengeBtn = (FloatingActionButton) findViewById(R.id.add_seq_challenge);
        seqChallengeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(MainActivity.this, "Sequential Challenge", Toast.LENGTH_SHORT).show();
                menuFab.collapseImmediately();
            }
        });

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new GamePagerAdapter(getSupportFragmentManager()));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        if(!isSignedIn()){
            signIn();
        }

        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forView(findViewById(R.id.fab_placeholder),
                        "Menu",
                        "Create basic, sequential and quiz-based challenges here to invite your friends to solve.")
                        .transparentTarget(true)
        );

//        new TapTargetSequence(this)
//                .targets(
//                        TapTarget.forView(findViewById(R.id.add_basic_challenge), "Basic Challenge", "Create a basic geographical challenge to invite other players to solve.")
//                                    .transparentTarget(true)
////                                .outerCircleColor(Color.parseColor("#795548"))
////                                .targetCircleColor(Color.parseColor("#FF5722"))
//                        ,TapTarget.forView(findViewById(R.id.add_seq_challenge), "Sequential Challenge", "Create a challenge which spans a sequence of geographically distributed locations.")
//                        .transparentTarget(true),
//                        TapTarget.forView(findViewById(R.id.add_quiz_challenge), "Quiz", "Create a quiz as a bonus challenge.")
//                                .transparentTarget(true)
//                ).listener(new TapTargetSequence.Listener() {
//                    // This listener will tell us when interesting(tm) events happen in regards
//                    // to the sequence
//                    @Override
//                    public void onSequenceFinish() {
//                        // Yay
//                    }
//
//                    @Override
//                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
//
//                    }
//
//                    @Override
//                    public void onSequenceCanceled(TapTarget lastTarget) {
//                        // Boo
//                    }
//                }).start();

    }

    private void onStartMatchClicked() {
        mMultiClient.getSelectOpponentsIntent(1, 7, true)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, INVITE_REQUEST);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("onFailure:", " Could not connect to player");
                        Toast.makeText(MainActivity.this, " Could not connect to player", Toast.LENGTH_LONG);
                    }
                });
    }


    @SuppressLint("RestrictedApi")
    public boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHALLENGE_SOLUTION_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                String loc_name = data.getStringExtra("loc_name");
                String loc_address = data.getStringExtra("loc_address");

                if (loc_name.equals(currentChallenge.getName())
                        && loc_address.equals(currentChallenge.getAddress())) {
                    Toast.makeText(this, "CORRECT", Toast.LENGTH_LONG).show();

                    successful = true;
                    mLeaderboardsClient.submitScore(getString(R.string.leaderboard_high_score), currentChallenge.getReward());
                }
                else {
                    Toast.makeText(this, "INCORRECT", Toast.LENGTH_LONG).show();
                    successful = false;
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }else if (requestCode == RC_MAKE_CHALLENGE) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("result");
                onStartMatchClicked();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } else if (requestCode == RC_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
//                    firebaseAuthWithGoogle(account);
                    onConnected(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                    // ...
                }
        } else if(requestCode == INVITE_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                Log.d(TAG, "onActivityResult: " + invitees);
                TurnBasedMatchConfig tbmConfig = TurnBasedMatchConfig.builder().addInvitedPlayers(invitees).build();
                mMultiClient.createMatch(tbmConfig)
                        .addOnSuccessListener(new OnSuccessListener<TurnBasedMatch>() {
                            @Override
                            public void onSuccess(TurnBasedMatch turnBasedMatch) {
                                onInitiateMatch(turnBasedMatch);
                            }
                        });
            }
        }
    }

    public void onInitiateMatch(TurnBasedMatch match){
        if(match.getData() != null){
            updateMatch(match);
            return;
        }
        startMatch(match);
    }

    public void startMatch(TurnBasedMatch match){
        mTurnData = MainActivity.currentChallenge;
        mTurnBasedMatch = match;
        String myId = mTurnBasedMatch.getParticipantId(mPlayerId);
        mMultiClient.takeTurn(match.getMatchId(), mTurnData.persist(), myId)
        .addOnSuccessListener(new OnSuccessListener<TurnBasedMatch>() {
            @Override
            public void onSuccess(TurnBasedMatch turnBasedMatch) {
                updateMatch(turnBasedMatch);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Could not initiate match, sorry.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateMatch(TurnBasedMatch match){
        mTurnBasedMatch = match;
        int status = match.getStatus();
        int turnStatus = match.getTurnStatus();
        switch(status){
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                Toast.makeText(this, "The match was cancelled.", Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                Toast.makeText(this, "The match got expired", Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if(turnStatus==TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE){
                    Toast.makeText(this, "The game is over", Toast.LENGTH_SHORT).show();
                    break;
                }
                Toast.makeText(this, "You can finish the game now.", Toast.LENGTH_SHORT).show();
        }
        switch (turnStatus){
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = Challenge.unpersist(mTurnBasedMatch.getData());
                Toast.makeText(this, "Your Turn!", Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                Toast.makeText(this, "Their Turn!", Toast.LENGTH_SHORT).show();
                return;
            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                Toast.makeText(this, "You have been invited!", Toast.LENGTH_SHORT).show();
        }
        mTurnData = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();
                        } else {
                            // Player will need to sign-in explicitly using via UI
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (successful != null)
        {
            if (successful)
            {

                Log.d(TAG, "onResume: DISPLAYING SUCCESS FRAG");
                SuccessFragment successFragment = new SuccessFragment();
                // Supply num input as an argument.
                Bundle args = new Bundle();
                args.putBoolean("successful", true);
                successFragment.setArguments(args);
                successFragment.show(getSupportFragmentManager(), "fragment_edit_name");
            } else if (!successful)
            {

                SuccessFragment successFragment = new SuccessFragment();
                // Supply num input as an argument.
                Bundle args = new Bundle();
                args.putBoolean("successful", false);
                successFragment.setArguments(args);
                successFragment.show(getSupportFragmentManager(), "fragment_edit_name");

            }
        }
        signInSilently();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
//        // [START_EXCLUDE silent]
////        showProgressDialog();
//        // [END_EXCLUDE]
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Toast.makeText(MainActivity.this, "Successfully Signed in", Toast.LENGTH_SHORT).show();
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            Toast.makeText(MainActivity.this, "Failed to Sign in", Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                        }
//
//                        // [START_EXCLUDE]
////                        hideProgressDialog();
//                        // [END_EXCLUDE]
//                    }
//                });
//    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        if (user != null) {
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected():" + googleSignInAccount.toString());
        Toast.makeText(this, googleSignInAccount.toString(), Toast.LENGTH_SHORT).show();
        signInButton.setVisibility(View.INVISIBLE);
//        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
        mMultiClient = Games.getTurnBasedMultiplayerClient(this, googleSignInAccount);
        mInvitationsClient = Games.getInvitationsClient(this, googleSignInAccount);
        Games.getPlayersClient(this, googleSignInAccount).getCurrentPlayer()
                .addOnSuccessListener(new OnSuccessListener<Player>() {
                    @Override
                    public void onSuccess(Player player) {
                        mPlayerId = player.getPlayerId();
                    }
                });
//        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(@NonNull Task<Player> task) {
                        String displayName;
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            Exception e = task.getException();
//                            handleException(e, getString(R.string.players_exception));
                            Log.d(TAG, "Players_Exception" + e.getMessage());

                            displayName = "???";
                        }
                        Log.d(TAG, "onConnected():" + displayName);
//                        mMainMenuFragment.setGreeting("Hello, " + displayName);
                    }
                });
//
//
//        // if we have accomplishments to push, push them
//        if (!mOutbox.isEmpty()) {
//            pushAccomplishments();
//            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
//                    Toast.LENGTH_LONG).show();
//        }
//
//        loadAndPrintEvents();
    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        Toast.makeText(this, "onDisconnected()", Toast.LENGTH_SHORT).show();
//
//        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;

    }


    public void onShowLeaderboardsRequested() {
        mLeaderboardsClient.getAllLeaderboardsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_UNUSED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
//                        handleException(e, getString(R.string.leaderboards_exception));
                    }
                });
    }
}
