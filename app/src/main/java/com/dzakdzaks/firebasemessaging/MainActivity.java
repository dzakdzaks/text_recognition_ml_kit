package com.dzakdzaks.firebasemessaging;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dzakdzaks.firebasemessaging.lib.GraphicOverlay;
import com.dzakdzaks.firebasemessaging.lib.TextGraphic;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.dzakdzaks.firebasemessaging.Helper.REQUEST_TAKE_PICTURE;

public class MainActivity extends AppCompatActivity implements MultiplePermissionsListener {


    private ImageView imgPreview;
    private Button btnTakePicture;
    private Button btnOpenGallery;
    private Button btnDownload;
    private TextView textView;
    private GraphicOverlay graphicOverlay;
    private File fileImage = null;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPreview = findViewById(R.id.image_preview);
        btnTakePicture = findViewById(R.id.btn_take_picture);
        btnOpenGallery = findViewById(R.id.btn_pick_galerry);
        textView = findViewById(R.id.textResult);
        btnDownload = findViewById(R.id.btn_download);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    // Log and toast
                    String msg = getString(R.string.fcm_token, token);
                    Log.d(TAG, msg);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });

        btnTakePicture.setOnClickListener(v -> startCamera());

        btnOpenGallery.setOnClickListener(v -> Helper.pickGallery(this));

        btnDownload.setOnClickListener(v -> {
            if (!textView.getText().toString().equals("")) {
                Helper.createPDF(this, textView.getText().toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Helper.checkPermission(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PICTURE:
                    Uri pictureURI = Uri.fromFile(fileImage);
                    imgPreview.setImageURI(pictureURI);
                    textRecogPhoto(pictureURI);
                    break;
                case Helper.REQUEST_PICK_PHOTO:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        imgPreview.setImageURI(selectedImage);
                        textRecogPhoto(selectedImage);
                    }
                    break;
                default:
                    Toast.makeText(this, "No Option", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted()) {
            btnTakePicture.setEnabled(true);
            btnOpenGallery.setEnabled(true);
            btnDownload.setEnabled(true);
        } else if (report.isAnyPermissionPermanentlyDenied()) {
            Helper.toSettingApp(this);
        } else {
            Toast.makeText(this, "must enable permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
    }

    private void startCamera() {
//        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(i, REQUEST_TAKE_PICTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        if (Helper.isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File pathFile = new File(Environment.getExternalStorageDirectory() + "/DCIM/Text Recognition");
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            fileImage = new File(pathFile + File.separator + imageFileName + ".png");
            Uri photoURI = Uri.fromFile(fileImage);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    photoURI);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivityForResult(takePictureIntent, Helper.REQUEST_TAKE_PICTURE);
        } else {
            Toast.makeText(this, "There is no application for take picture", Toast.LENGTH_SHORT).show();
        }

    }

    private void textRecogPhoto(Uri uri) {
        if (uri != null) {
            FirebaseVisionImage image;
            try {
                image = FirebaseVisionImage.fromFilePath(this, uri);
                FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                        .getOnDeviceTextRecognizer();
                recognizer.processImage(image)
                        .addOnSuccessListener(this::processTextRecognitionResult)
                        .addOnFailureListener(Throwable::printStackTrace);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "bitmap is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this, "No text found", Toast.LENGTH_SHORT).show();
            return;
        }

//        String resultText = texts.getText();
        graphicOverlay.clear();

        StringBuilder stringBuilder = new StringBuilder();
        for (FirebaseVisionText.TextBlock block : texts.getTextBlocks()) {
//            stringBuilder.append(block.getText()).append("\n\n");

            for (FirebaseVisionText.Line line : block.getLines()) {
                stringBuilder.append(line.getText()).append("\n");
                GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, line);
                graphicOverlay.add(textGraphic);

                for (FirebaseVisionText.Element element : line.getElements()) {
//                    stringBuilder.append(element.getText()).append("\n\n");
//                    GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, element);
//                    graphicOverlay.add(textGraphic);
                }
            }
        }

        textView.setText(stringBuilder);
    }

}
