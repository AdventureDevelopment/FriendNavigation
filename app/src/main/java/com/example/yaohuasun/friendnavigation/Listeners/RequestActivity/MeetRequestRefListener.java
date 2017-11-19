package com.example.yaohuasun.friendnavigation.Listeners.RequestActivity;

import android.util.Log;

import com.example.yaohuasun.friendnavigation.Models.MeetRequestModel;
import com.example.yaohuasun.friendnavigation.RequestActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by unger on 11/19/2017.
 */

public class MeetRequestRefListener implements ValueEventListener {

    private MeetRequestModel mCurrentMeetRequest;
    private RequestActivity mRequestActivity;

    public MeetRequestRefListener (RequestActivity RequestActivity) {
        mRequestActivity = RequestActivity;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.exists()) {
            Log.i("position09191", "in reqActivity, dataSnapShot is " + dataSnapshot.toString());

            mCurrentMeetRequest = dataSnapshot.getValue(MeetRequestModel.class);
            String initiatorEmail = mCurrentMeetRequest.getInitiatorEmailAddr();
            String initiatorState = mCurrentMeetRequest.getInitiatorState();
            String responderEmail = mCurrentMeetRequest.getResponderEmailaddr();
            String responderState = mCurrentMeetRequest.getResponderState();
            if (responderState.equals("true")){
                if (!initiatorState.equals("true")){
                    Log.i("DEAD11", "unExpected situation,wrong, initiatorEmail is" + initiatorEmail + "initiatorState is"+initiatorState);
                }

                mRequestActivity.navigateToStartActivity("true");
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}