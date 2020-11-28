package com.example.loginpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class newApplicanceActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST =123 ;
    private ImageView applianceimage;
    private EditText nameofitem;
    private EditText nameofcategory;
    private EditText statusofappliance;
    private Button choose;
    private Button upload;
    private Button create;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef, rootref, applianceref;
    private Uri mImageUri;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private StorageTask mUploadTask;
   // private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_applicance);
        ActivityCompat.requestPermissions(newApplicanceActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        applianceimage = (ImageView) findViewById(R.id.applianceimage);
        nameofitem = (EditText) findViewById(R.id.nameofnewappliance);
        nameofcategory = (EditText) findViewById(R.id.categoryofappliance);
        statusofappliance = (EditText) findViewById(R.id.statusofappliance);
        choose = (Button) findViewById(R.id.choosefile);
        upload = (Button) findViewById(R.id.upload);
        create = (Button) findViewById(R.id.CreateAppliance);
        mStorageRef= FirebaseStorage.getInstance().getReference("uploads");
        rootref = FirebaseDatabase.getInstance().getReference();
        //rootref.child("Appliances").setValue("hello");
        applianceref = rootref.child("Appliances");
        mDatabaseRef= applianceref.child(user.getUid());
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    uploadFile();

            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Appliances.class);
                startActivity(intent);
                finish();
            }
        });
    }




    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(nameofitem.getText() + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Upload up = new Upload(nameofitem.getText().toString().trim(),taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(),
                            statusofappliance.getText().toString(), nameofcategory.getText().toString());

                    mDatabaseRef.child(nameofitem.getText().toString()).child("imageUri").setValue(mImageUri.toString());
                    mDatabaseRef.child(nameofitem.getText().toString()).child("category").setValue(nameofcategory.getText().toString());
                    mDatabaseRef.child(nameofitem.getText().toString()).child("title").setValue(nameofitem.getText().toString());
                    mDatabaseRef.child(nameofitem.getText().toString()).child("status").setValue(statusofappliance.getText().toString());
                    Toast.makeText(newApplicanceActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(newApplicanceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "this is a sample toast", Toast.LENGTH_LONG).show();
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void openFileChooser() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()
                !=null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(applianceimage);
        }
        }
    }
