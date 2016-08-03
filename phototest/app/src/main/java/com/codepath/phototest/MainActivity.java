package com.codepath.phototest;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public final String APP_TAG = "MyCustomApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivPreview.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_VIDEO && resultCode == RESULT_OK
                    && null != data) {
                uri = data.getData();
                String uriStr = uri.toString();
                if (uriStr.contains("video")) {
                    Video video1 = newVideo(uri);
                    Log.d("uri", uri.toString());
                    String[] filePathColumn = {MediaStore.Video.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);
                    if (newfile.exists()) {
                        Date lastModDate = new Date(newfile.lastModified());
                        video1.date = lastModDate.toString();
                    }
                    databaseHelper.addOrUpdateVideo(video1);
                    cursor.close();
                    Intent i = new Intent(MainActivity.this, AddingTagsActivity.class);
                    i.putExtra("VideoUri", video.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);
                }
                else if (uriStr.contains("images")) {
                    Log.d("uriStr", uriStr);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);

                    Photo pho = newPhoto(Uri.parse(imgDecodableString));
                    cursor.close();
                    medias.clear();
                    medias.addAll(databaseHelper.getAllVideos());
                    medias.addAll(databaseHelper.getAllPhotos());
                    mediaAdapter.notifyDataSetChanged();
                    mediaGrid.setAdapter(mediaAdapter);
                    Intent i = new Intent(MainActivity.this,EditActivityPhoto.class);
                    i.putExtra("Image", pho.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);

                }
            }
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {

                    Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                    //Intent i1 = new Intent(MainActivity.this, ImageTest.class);
                    //i1.putExtra("imageUri", takenPhotoUri.getPath());
                    //startActivity(i1);
                    //Log.d("reached", "hi");
                    Photo pho = newPhoto(takenPhotoUri.getPath());

                    medias.clear();
                    medias.addAll(databaseHelper.getAllVideos());
                    medias.addAll(databaseHelper.getAllPhotos());
                    mediaAdapter.notifyDataSetChanged();
                    mediaGrid.setAdapter(mediaAdapter);
                    Intent i = new Intent(MainActivity.this, EditActivityPhoto.class);
                    i.putExtra("Image", pho.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);
                    // by this point we have the camera photo on disk
                    //Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                    // RESIZE BITMAP, see section below
                    // Load the taken image into a preview
                    //
                    //ivPreview.setImageBitmap(takenImage);


//                    Toast.makeText(this, "Picture was taken!", Toast.LENGTH_SHORT).show();
//
//                    uri = data.getData();
//                    Photo photo = newPhoto(uri);
//                    //Video video2 = newVideo(uri);
//                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
//
//                    Cursor cursor = getContentResolver().query(uri,
//                            filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                    imgDecodableString = cursor.getString(columnIndex);
//                    File newfile = new File(imgDecodableString);
//                    if (newfile.exists()) {
//                        Date lastModDate = new Date(newfile.lastModified());
//                        video.date = lastModDate.toString();
//                    }
//                    databaseHelper.addOrUpdatePhoto(photo);
//                    cursor.close();
//                    Intent i = new Intent(MainActivity.this, EditActivityPhoto.class);
//                    i.putExtra("Image", photo.uri);
//                    i.putExtra("main", "yes");
//                    startActivity(i);
//                    Toast.makeText(this, "Photo has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();



                } else {
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == VIDEO_CAPTURE) {
                if (resultCode == RESULT_OK) {
                    uri = data.getData();
                    Video video2 = newVideo(uri);
                    String[] filePathColumn = { MediaStore.Video.Media.DATA };

                    Cursor cursor = getContentResolver().query(uri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgDecodableString = cursor.getString(columnIndex);
                    File newfile = new File(imgDecodableString);
                    if (newfile.exists()) {
                        Date lastModDate = new Date(newfile.lastModified());
                        video.date = lastModDate.toString();
                    }
                    databaseHelper.addOrUpdateVideo(video2);
                    cursor.close();
                    Intent i = new Intent(MainActivity.this, AddingTagsActivity.class);
                    i.putExtra("VideoUri", video2.uri);
                    i.putExtra("main", "yes");
                    startActivity(i);
                    Toast.makeText(this, "Video has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            //Log.d("onactivity", "expection");
        }

    }
}
