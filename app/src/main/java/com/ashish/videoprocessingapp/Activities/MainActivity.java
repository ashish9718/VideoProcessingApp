package com.ashish.videoprocessingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ashish.videoprocessingapp.Database.DBquerries;
import com.ashish.videoprocessingapp.Model.VideoModel;
import com.ashish.videoprocessingapp.R;
import com.ashish.videoprocessingapp.VideoViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoProcessingActivity.class));
                Toast.makeText(MainActivity.this,"Please slide to view SeekBar.",Toast.LENGTH_SHORT).show();
            }
        });

        recview = findViewById(R.id.recview);
        recview.setLayoutManager(new LinearLayoutManager(this));

        FirestoreRecyclerOptions<VideoModel> options = new FirestoreRecyclerOptions.Builder<VideoModel>()
                .setQuery(db.collection("VIDEOS"), VideoModel.class)
                .build();


        FirestoreRecyclerAdapter<VideoModel, VideoViewHolder> firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<VideoModel, VideoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull VideoModel model) {
                holder.prepareexoplayer(getApplication(), model.getTitle(), model.getUrl(), model.getUsername());
            }

            @NonNull
            @Override
            public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
                return new VideoViewHolder(view);
            }
        };

        firestoreRecyclerAdapter.startListening();
        recview.setAdapter(firestoreRecyclerAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, SigninActivity.class));
            finish();
        } else {
            FirebaseFirestore.getInstance().collection("USERS").document(currentUser.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DBquerries.fullname = task.getResult().getString("name");
                        DBquerries.email = task.getResult().getString("email");
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, SigninActivity.class));
                finish();
                break;

            case R.id.manage_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}



