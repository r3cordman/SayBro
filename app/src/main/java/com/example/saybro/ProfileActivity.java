package com.example.saybro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.saybro.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    private PostsAdapter postsAdapter;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postsAdapter=new PostsAdapter(this);
        binding.postsRecycler.setAdapter(postsAdapter);
        binding.postsRecycler.setLayoutManager(new LinearLayoutManager(this));
        userId=getIntent().getStringExtra("id");

        loadUserData();
        loadPosts();
    }

    private void loadUserData() {
        binding.userName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel userModel=documentSnapshot.toObject(UserModel.class);
                        binding.userName.setText(userModel.getUserName());
                        if (userModel.getUserBio()!=null){
                            binding.userBio.setText(userModel.getUserBio());
                        }else {
                            binding.userBio.setText(R.string.bio);
                        }
                        if (userModel.getUserProfile()!=null){
                            Glide.with(ProfileActivity.this).load(userModel.getUserProfile())
                                    .into(binding.userProfile);
                        }
                        if (userModel.getUserCover()!=null){
                            Glide.with(ProfileActivity.this).load(userModel.getUserCover())
                                    .into(binding.coverPhoto);
                        }
                    }
                });
    }
    private void loadPosts(){
        FirebaseFirestore.getInstance()
                .collection("Posts")
                .whereEqualTo("userId",userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        postsAdapter.clearPosts();
                        List<DocumentSnapshot> dsList=queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot ds:dsList){
                            PostModel postModel=ds.toObject(PostModel.class);
                            postsAdapter.addPost(postModel);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}