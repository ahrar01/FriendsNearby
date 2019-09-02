package com.ahraar.friendsnearby.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ahraar.friendsnearby.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class Register extends AppCompatActivity {

    private CircleImageView profilePic;
    private FloatingActionButton changeProfilePic;
    private MaterialEditText mName;
    boolean IMAGE_STATUS = false;
    private Uri imageUri, resultUri;
    private InputStream inputStream;
    private Bitmap profilePicture;
    private byte[] thumb_byte;
    private Button button_update;
    private Dialog dialog_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dialog_loading = new Dialog(this);
        init();

    }

    private void init() {
        profilePic = findViewById(R.id.profilePic);
        changeProfilePic = findViewById(R.id.changeProfilePic);
        mName = findViewById(R.id.name);

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Dexter.withActivity(Register.this)
                        .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                // check if all permissions are granted
                                if (report.areAllPermissionsGranted()) {
                                    // do you work now
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, 1000);
                                }

                                // check for permanent denial of any permission
                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    // permission is denied permenantly, navigate user to app settings
                                    Snackbar.make(view, "Kindly grant Required Permission", Snackbar.LENGTH_LONG)
                                            .setAction("Allow", null).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .onSameThread()
                        .check();


                //result will be available in onActivityResult which is overridden
            }
        });

        button_update = findViewById(R.id.button_update);

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString();
                if (Validate(name)&&validateProfile()){

                }
            }
        });

    }

    //Dialog
    private void Dialog_Loading() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_pop_up, null);
        AlertDialog.Builder show = new AlertDialog.Builder(Register.this);
        show.setView(alertLayout);
        show.setCancelable(false);
        dialog_loading = show.create();
        dialog_loading.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog_loading.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            //Image Successfully Selected
            //parsing the Intent data and displaying it in the imageview
            imageUri = data.getData();//Geting uri of the data

            IMAGE_STATUS = true;//setting the flag
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {


                resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();


                try {
                    inputStream = getContentResolver().openInputStream(resultUri);//creating an inputStream
                    profilePicture = BitmapFactory.decodeStream(inputStream);//decoding the input stream to bitmap
                    profilePic.setImageBitmap(profilePicture);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

    private boolean Validate(String name) {
        boolean check = true;

        if (check) {

            if (name.isEmpty()) {
                mName.setError("Cannot Be Empty");
                check = false;
            }

        } else {
            check = true;
        }

        return check;

    }



    private boolean validateProfile() {
        if (!IMAGE_STATUS)
            Toasty.info(Register.this, "Select A Profile Picture", Toast.LENGTH_LONG).show();
        return IMAGE_STATUS;
    }
}
