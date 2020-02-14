package com.dzakdzaks.firebasemessaging;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ==================================//==================================
 * ==================================//==================================
 * Created on Thursday, 13 February 2020 at 13:49.
 * Project Name => FirebaseMessaging
 * Package Name => com.dzakdzaks.firebasemessaging
 * ==================================//==================================
 * ==================================//==================================
 */
class Helper {

    static final int REQUEST_TAKE_PICTURE = 1;

    static final int REQUEST_PICK_PHOTO = 2;

    final static String PATH_CACHE_IMAGE = "/cache_image/";

    final static String PATH_CACHE_IMAGE_COMPRESS = "/cache_image_compress/";

    static void checkPermission(Activity activity, MultiplePermissionsListener listener) {
        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Dexter.withActivity(activity)
                .withPermissions(permission)
                .withListener(listener)
                .check();
    }

    static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    static void pickGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, Helper.REQUEST_PICK_PHOTO);
    }

    static void toSettingApp(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    static void createPDF(Activity activity, String content) {
        /**
         * Creating Document
         */
        Document document = new Document();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fpath = "TR_Document_" + timeStamp + ".pdf";

        // Location to save
        try {

            File file = new File(Environment.getExternalStorageDirectory() + "/Text Recognition");

            if (!file.exists()) {
                file.mkdirs();
            }

            File fileContent = new File(file
                    + File.separator + fpath);

            PdfWriter.getInstance(document, new FileOutputStream(fileContent));
            Toast.makeText(activity, "PDF Created...", Toast.LENGTH_SHORT).show();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }


        // Open to write
        document.open();

        // Document Settings
        document.setPageSize(PageSize.A4);
        document.addCreationDate();
        document.addAuthor("Invent");
        document.addCreator("Orang Invent");


        try {

            /**
             * TITLE
             * How to USE FONT....
             */
            BaseFont urName = BaseFont.createFont("assets/tnr.ttf", "UTF-8", BaseFont.EMBEDDED);

            // Title Order Details...
            // Adding Title....
            Font mOrderDetailsTitleFont = new Font(urName, 36.0f, Font.NORMAL, BaseColor.BLACK);
            // Creating Chunk
            String[] separatedStrings = fpath.split("\\.");
            Chunk mOrderDetailsTitleChunk = new Chunk(separatedStrings[0], mOrderDetailsTitleFont);
            // Creating Paragraph to add...
            Paragraph mOrderDetailsTitleParagraph = new Paragraph(mOrderDetailsTitleChunk);
            // Setting Alignment for Heading
            mOrderDetailsTitleParagraph.setAlignment(Element.ALIGN_CENTER);
            // Finally Adding that Chunk
            document.add(mOrderDetailsTitleParagraph);

            LineSeparator lineSeparator = new LineSeparator();
            lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));
            document.add(new Paragraph(""));
            document.add(new Chunk(lineSeparator));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));

            /***
             * CONTENT
             * Variables for further use....
             */
            BaseColor mColorAccent = new BaseColor(0, 0, 0);
            float mHeadingFontSize = 26.0f;

            Font mOrderIdFont = new Font(urName, mHeadingFontSize, Font.NORMAL, mColorAccent);
            Chunk mOrderIdChunk = new Chunk(content, mOrderIdFont);
            Paragraph mOrderIdParagraph = new Paragraph(mOrderIdChunk);
            document.add(mOrderIdParagraph);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

        document.close();

    }

}
