package coop.adventuredevelopment.friendnavigation.Listeners;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import coop.adventuredevelopment.friendnavigation.CreateNewUserActivity;
import coop.adventuredevelopment.friendnavigation.FNLoginActivity;
import coop.adventuredevelopment.friendnavigation.Utils.FNUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by yaohuasun on 11/19/17.
 */

public class NewUserRegistrationListener implements OnCompleteListener<AuthResult>
{
    private DatabaseReference mDatabaseRef;
    private String mUserEmailString;
    private String mUserPasswordString;
    private CreateNewUserActivity mActivity;
    public NewUserRegistrationListener(
            DatabaseReference fireBaseRootRef,
            String userEmail,
            String userPassword,
            CreateNewUserActivity activity
    ){
        mDatabaseRef = fireBaseRootRef;
        mUserEmailString = userEmail;
        mUserPasswordString = userPassword;
        mActivity = activity;
    }

    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful()){
            DatabaseReference mNewUser = mDatabaseRef.child("Users").child(FNUtil.encodeEmail(mUserEmailString));
            mNewUser.child("emailAddr").setValue(mUserEmailString);
            mNewUser.child("receivingMapRequest").setValue("false");
            mNewUser.child("currentChatFriend").setValue("");

            Toast.makeText(mActivity, "Successfully created account", Toast.LENGTH_LONG).show();
            // move to login activity
            mActivity.NavigateToFriendList();
        }
        else
        {
            Toast.makeText(mActivity, "Failed to create account", Toast.LENGTH_LONG).show();
        }
    }
}
