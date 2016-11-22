package com.walter.uploadimagesvolley;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 20000;
    private static final String UPLOAD_URL = "https://emob-walteranyika.c9users.io/upload.php";

    Bitmap bitmap;
    ImageView imgView;
    EditText inputName,inputQty;
    String imgPath="";
    CircularProgressView progressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView= (ImageView) findViewById(R.id.imageView);
        inputName= (EditText) findViewById(R.id.inputName);
        inputQty= (EditText) findViewById(R.id.inputQty);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.INVISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       uploadImage();
        return super.onOptionsItemSelected(item);
    }
    //STEP 4 Upload the image + a few other parameters to PHP Server
    private void uploadImage()
    {
        File myFile = new File(imgPath);
        RequestParams params = new RequestParams();
        try
        {
            params.put("fileToUpload", myFile);
            params.put("name",inputName.getText().toString().trim());
            params.put("qty",inputQty.getText().toString().trim());
            AsyncHttpClient client=new AsyncHttpClient();
            progressView.setVisibility(View.VISIBLE);
            progressView.startAnimation();
            client.post(UPLOAD_URL, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                    progressView.setVisibility(View.INVISIBLE);
                    progressView.stopAnimation();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                    progressView.setVisibility(View.INVISIBLE);
                    progressView.stopAnimation();

                }
            });

        } catch(FileNotFoundException e) {}

    }

    //STEP 2 Display gallery to allow the user to choose the photo
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    //STEP 3 Display the selected image on the image view and set the path
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imgView.setImageBitmap(bitmap);
                imgPath=getPath(filePath);
                Log.d("PATH",imgPath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    //STEP 1 click listener to show gallaery
    public void open(View view) {
        showFileChooser();
    }
    //PHP code to receive uploaded image and other data
    //It will store the uploaded image into the uploads folder with a generated  name that consits of a random numbers to avoid clashes
    //It will also insert the other details and the path of the image in the DB
    /*
    *
    * <?php
         if($_SERVER['REQUEST_METHOD']=='POST')
         {
                 $target_dir = "uploads/";
                 //$target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);

                 $name = $_POST['name'];
                 $qty = $_POST['qty'];

                 $path = $_FILES['fileToUpload']['name'];
                 $ext = pathinfo($path, PATHINFO_EXTENSION);
                 $x=rand(100000,10000000);
                 $y=rand(100000,10000000);
                 $new_name=$x."_".$y.".".$ext;
                 $target_file = $target_dir .$new_name;


                 $con = mysqli_connect("localhost","walteranyika","lydia@2010","uploads") or die('unable to connect to db');

                 if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file))
                 {
                   echo "Successfully Uploaded";
                   $sql = "INSERT INTO `stocks`(`id`, `name`, `qty`, `path`) VALUES (null,'$name','$qty','$target_file')";
                   mysqli_query($con,$sql);
                 }else
                 {
                   echo "Sorry, there was an error uploading your file.";
                 }
                 mysqli_close($con);
         }
         else
         {
           echo "Error";
         }
     */
}
