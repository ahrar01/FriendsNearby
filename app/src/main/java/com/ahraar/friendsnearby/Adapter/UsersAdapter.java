package com.ahraar.friendsnearby.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahraar.friendsnearby.Model.Users;
import com.ahraar.friendsnearby.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends FirestoreRecyclerAdapter<Users, UsersAdapter.UsersHolder> {
    private FirebaseFirestore firebaseFirestore;
    FirebaseUser currUser ;

    public UsersAdapter(@NonNull FirestoreRecyclerOptions<Users> options) {
        super(options);
        firebaseFirestore = FirebaseFirestore.getInstance();
        currUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onBindViewHolder(@NonNull final UsersAdapter.UsersHolder usersHolder, int i, @NonNull final Users users) {

        usersHolder.mUserName.setText(users.getName());
        usersHolder.mContact.setText(users.getContact());
        String photo = users.getPhoto().toString();
        Picasso.get().load(photo).placeholder(R.drawable.user_placeholder).into(usersHolder.mUserImage);


        String userId = currUser.getUid();
        firebaseFirestore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Double latitude = documentSnapshot.getDouble("latitude");
                Double longitude = documentSnapshot.getDouble("longitude");

                LatLng fromPosition = new LatLng(latitude, longitude);
                LatLng toPosition = new LatLng(users.getLatitude(), users.getLongitude());
                double distance = SphericalUtil.computeDistanceBetween(fromPosition,toPosition);

                usersHolder.mDistance.setText(formatNumber(distance));

            }
        });

    }

    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }

        return String.format("%4.3f%s", distance, unit);
    }

    @NonNull
    @Override
    public UsersAdapter.UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user,
                parent, false);
        return new UsersAdapter.UsersHolder(v);
    }

    public class UsersHolder extends RecyclerView.ViewHolder {
        public CircleImageView mUserImage;
        public TextView mUserName;
        public TextView mContact;
        public TextView mDistance;
        public UsersHolder(@NonNull View itemView) {
            super(itemView);
            mUserImage = itemView.findViewById(R.id.user_list_image);
            mUserName = itemView.findViewById(R.id.user_list_name);
            mContact = itemView.findViewById(R.id.user_contact);
            mDistance = itemView.findViewById(R.id.user_distance);
        }
    }
}
