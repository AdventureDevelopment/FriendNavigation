package com.example.yaohuasun.friendnavigation.Listeners;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.yaohuasun.friendnavigation.FNFriendListActivity;
import com.example.yaohuasun.friendnavigation.Models.UserModel;
import com.example.yaohuasun.friendnavigation.Utils.FNUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Chris on 11/18/2017.
 */

public class SearchAndAddNewFriendListner implements ValueEventListener {

    private String mCurrentUserEmail;
    private String mSearchUserEmail;
    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;

    public SearchAndAddNewFriendListner(String userEmail,
                                        String searchUserEmail,
                                        Context context,
                                        FirebaseDatabase firebaseDatabase
                              )  {
        mContext = context;
        mCurrentUserEmail = userEmail;
        mSearchUserEmail = searchUserEmail;
        mFirebaseDatabase = firebaseDatabase;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        String encodedUserEmail = FNUtil.encodeEmail(mCurrentUserEmail);
        String encodedSearchUserEmail = FNUtil.encodeEmail(mSearchUserEmail);

        DatabaseReference mFriendMapRef = mFirebaseDatabase.getReference().child("FriendMap").child(encodedUserEmail);

        if ((null == mFriendMapRef) && (dataSnapshot.getValue() == null)){
            Toast.makeText(mContext,"sure user existed? perhaps not", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mFriendMapRef.child("mainUserEmail").setValue(mCurrentUserEmail);
        } catch (Exception e) {

            e.printStackTrace();
        }

        DatabaseReference mFriendListRef = mFriendMapRef.child("FriendList").push();

        UserModel user = dataSnapshot.child(encodedSearchUserEmail).getValue(UserModel.class);

        if (user == null) {
            Toast.makeText(mContext,"perhaps user doesn't exist!", Toast.LENGTH_LONG).show();
            return;
        }

        String useremail = user.getEmailAddr();

        try {
            mFriendListRef.child("friendEmailAddr").setValue(useremail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
