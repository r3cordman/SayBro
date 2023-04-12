package com.example.saybro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.saybro.databinding.ActivityNewsFeedBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class NewsFeedActivity extends AppCompatActivity {
    ActivityNewsFeedBinding binding;
    private PostsAdapter postsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityNewsFeedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore
                .getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel userModel=documentSnapshot.toObject(UserModel.class);
                        if (userModel.getUserProfile()!=null){
                            Glide.with(NewsFeedActivity.this).load(userModel.getUserProfile()).into(binding.imageView);
                        }
                    }
                });

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(NewsFeedActivity.this,ProfileActivity.class);
                intent.putExtra("id",FirebaseAuth.getInstance().getUid());
                startActivity(intent);
            }
        });

        postsAdapter=new PostsAdapter(this);
        binding.postRecyclerView.setAdapter(postsAdapter);
        binding.postRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadPosts();

        binding.goCreatePost
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(NewsFeedActivity.this,CreatePostActivity.class));
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id= item.getItemId();
        if (id==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(NewsFeedActivity.this,LoginActivity.class));
            finish();
            return true;
        }
        return false;
    }

    private void loadPosts(){
        FirebaseFirestore.getInstance()
                .collection("Posts")
                .orderBy("postingTime", Query.Direction.DESCENDING)
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
                        Toast.makeText(NewsFeedActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}