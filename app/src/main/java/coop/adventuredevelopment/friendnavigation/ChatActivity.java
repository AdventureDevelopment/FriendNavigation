package coop.adventuredevelopment.friendnavigation;

import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import coop.adventuredevelopment.friendnavigation.Listeners.CreateChatListener;
import coop.adventuredevelopment.friendnavigation.Listeners.DestroyChatListener;
import coop.adventuredevelopment.friendnavigation.Listeners.IncomingNavigationListener;
import coop.adventuredevelopment.friendnavigation.Listeners.ProposeNavigationListener;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import coop.adventuredevelopment.friendnavigation.Models.MeetRequestModel;
import coop.adventuredevelopment.friendnavigation.Models.MessageModel;
import coop.adventuredevelopment.friendnavigation.Models.UserModel;
import coop.adventuredevelopment.friendnavigation.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import coop.adventuredevelopment.friendnavigation.Utils.FNUtil;

public class ChatActivity extends AppCompatActivity {

    private FirebaseListAdapter<MessageModel> mMessageListAdapter;
    private String basicChatFriend;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicChatDatabaseRef;
    private DatabaseReference mMessageDataBaseReference;
    private DatabaseReference mMeetRequestMessageRef;
    private DatabaseReference mUserRef;
    private ValueEventListener mMeetRequestRefListener;
    private ListView mMessageList;
    private TextView mMessageField;
    private String mSearchChatIdResult;
    private String mChatId;
    private String mCurrentUserEmail;
    private MeetRequestModel mCurrentMeetRequest;
    private String mReceivingMeetRequest;
    private UserModel mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mBasicChatDatabaseRef = mFirebaseDatabase.getReference().child("BasicChat");
        mMessageList = (ListView) findViewById(R.id.messageListView);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        mCurrentUserEmail = mFirebaseAuth.getCurrentUser().getEmail().trim();
        mSearchChatIdResult = null;

        Log.i("position1000", "mCurrentUserEmail is" + mCurrentUserEmail);
        Intent intent = this.getIntent();

        mUserRef = mFirebaseDatabase.getReference().child("Users");

        mUserRef.orderByChild("emailAddr").equalTo(mCurrentUserEmail).addListenerForSingleValueEvent(
            new CreateChatListener(mBasicChatDatabaseRef, mCurrentUserEmail, this)
        );

        mReceivingMeetRequest = "false";
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        detachNavigationRefListener();
        mMessageListAdapter.cleanup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUserRef.orderByChild("emailAddr").equalTo(mCurrentUserEmail).addListenerForSingleValueEvent(
                new DestroyChatListener(mMeetRequestMessageRef, mUserRef, mCurrentUserEmail)
        );

        detachNavigationRefListener();
        mMessageListAdapter.cleanup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // respond to up/home button to go back to parent activity
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(View view){

       if (mMessageDataBaseReference == null) {
            return;
        }

        mMessageField = (TextView)findViewById(R.id.messageToSend);
        final DatabaseReference pushRef = mMessageDataBaseReference.push();
        final String pushKey = pushRef.getKey();

        String messageString = mMessageField.getText().toString();
        mMessageField.setText("");

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String timestamp = dateFormat.format(date);

        MessageModel message = new MessageModel(mCurrentUserEmail,messageString,timestamp);

        mMessageDataBaseReference
                .push()
                .setValue(message);
    }

    public void proposeNavigation(View view){
        mUserRef.orderByChild("emailAddr").equalTo(mCurrentUserEmail).addListenerForSingleValueEvent(
            new ProposeNavigationListener(mMeetRequestMessageRef,
                    mUserRef,
                    mCurrentUserEmail,
                    this
                    )
        );
    }

    private void attachNavigationRefListener()
    {
        if(null != mMeetRequestRefListener) {
            return;
        }

        mMeetRequestRefListener = mMeetRequestMessageRef.addValueEventListener(
            new IncomingNavigationListener (
                mUser,
                this
            )
        );
    }

    private void detachNavigationRefListener(){
        if (null!= mMeetRequestRefListener)
        {
            mMeetRequestMessageRef.removeEventListener(mMeetRequestRefListener);
            mMeetRequestRefListener = null;
        }
    }

    public void SetupChatAdapter(DatabaseReference databaseReference) {
        mMessageDataBaseReference = databaseReference;
        mMessageListAdapter = CreateChatAdapter(databaseReference);
        mMessageList.setAdapter(mMessageListAdapter);
    }

    public void NavigateToRequestActivity(boolean isInitator) {
        Intent intent = new Intent(ChatActivity.this, RequestActivity.class);
        intent.putExtra("isInitiator", isInitator);
        startActivity(intent);
    }

    public void setCurrentUser(UserModel user) {
        mUser = user;
    }

    public void setCurrentMeetRequest(MeetRequestModel currentMeetRequest) {
        mCurrentMeetRequest = currentMeetRequest;
    }

    public void setMeetRequestMessageRef(DatabaseReference databaseReference) {
        mMeetRequestMessageRef = databaseReference;
        detachNavigationRefListener();
        attachNavigationRefListener();
    }

    private FirebaseListAdapter<MessageModel> CreateChatAdapter(DatabaseReference databaseReference) {
        return new FirebaseListAdapter<MessageModel>(ChatActivity.this, MessageModel.class, R.layout.message_item, mMessageDataBaseReference) {
            @Override
            protected void populateView(View view, MessageModel model, int position) {
                LinearLayout messageLine = (LinearLayout) view.findViewById(R.id.messageLine);
                TextView messgaeText = (TextView) view.findViewById(R.id.messageTextView);
                TextView senderText = (TextView) view.findViewById(R.id.senderTextView);
                TextView timeText = (TextView) view.findViewById(R.id.timestampTextView);
                LinearLayout individMessageLayout = (LinearLayout) view.findViewById(R.id.individMessageLayout);
                Log.i("positionQ", "in pupulate view Value is" + model.toString());
                messgaeText.setText(model.getMessage());
                senderText.setText(model.getSenderEmail());
                timeText.setText(model.getTimestamp());

                String senderEmail = model.getSenderEmail().trim();

                if (mCurrentUserEmail.equals(senderEmail)) {
                    // move message to the right
                    messageLine.setGravity(Gravity.RIGHT);
                } else {
                    messageLine.setGravity(Gravity.LEFT);
                }
            }
        };
    }
}
