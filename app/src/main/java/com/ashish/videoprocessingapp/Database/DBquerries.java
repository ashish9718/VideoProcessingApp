package com.ashish.videoprocessingapp.Database;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashish.videoprocessingapp.Model.VideoModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;


public class DBquerries {

    public static String email,fullname;

    public static FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public static FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
    public static StorageReference storageReference=FirebaseStorage.getInstance().getReference();

    //delete func
    //my videos func
    //myprofile func

    public static void uploadvideo(String videouri, final String title,final String ext, final Context context,final ProgressDialog pd) {

        final StorageReference uploader = storageReference.child("videos/"+title+"."+ext);
        //uploader.putFile(Uri.parse(videouri))
        uploader.putFile(Uri.fromFile(new File(videouri)))

                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                VideoModel videoModel=new VideoModel(title,uri.toString(),currentUser.getUid(),fullname);
                                firebaseFirestore.collection("VIDEOS").document().set(videoModel)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(context, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(context, "Failed to Upload", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per=(100.0f*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                pd.setMessage("Uploaded :"+(int)per+"%");
            }
        });

    }
}
