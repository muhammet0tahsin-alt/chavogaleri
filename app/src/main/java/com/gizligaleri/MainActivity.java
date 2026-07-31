package com.chavogaleri;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private MediaAdapter adapter;
    private ArrayList<String> mediaList = new ArrayList<>();
    private File safeFolder;

    private ActivityResultLauncher<String[]> pickMedia = registerForActivityResult(
        new ActivityResultContracts.OpenMultipleDocuments(),
        uris -> {
            if (uris != null) {
                for (Uri uri : uris) {
                    moveToSafe(uri);
                }
                loadMedia();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        safeFolder = new File(getFilesDir(), "chavo_safe");
        if (!safeFolder.exists()) safeFolder.mkdirs();

        File noMedia = new File(safeFolder, ".nomedia");
        if (!noMedia.exists()) {
            try { noMedia.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }

        gridView = findViewById(R.id.gridView);
        adapter = new MediaAdapter(this, mediaList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ViewerActivity.class);
            intent.putExtra("path", mediaList.get(position));
            intent.putExtra("position", position);
            intent.putStringArrayListExtra("list", mediaList);
            startActivity(intent);
        });

        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            String path = mediaList.get(position);
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Ne yapmak istiyorsun?")
                .setItems(new String[]{"Galeriye geri al", "Sil"}, (dialog, which) -> {
                    if (which == 0) {
                        restoreToGallery(path);
                    } else {
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Emin misin?")
                            .setMessage("Bu dosya tamamen silinecek!")
                            .setPositiveButton("Evet sil", (d, w) -> {
                                File file = new File(path);
                                if (file.delete()) {
                                    Toast.makeText(this, "Silindi!", Toast.LENGTH_SHORT).show();
                                    loadMedia();
                                }
                            })
                            .setNegativeButton("Hayır", null)
                            .show();
                    }
                })
                .show();
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> pickMedia.launch(new String[]{"image/*", "video/*"}));

        loadMedia();
    }

    private void moveToSafe(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            String ext = ".jpg";
            if (mimeType != null && mimeType.startsWith("video")) ext = ".mp4";

            String name = "media_" + System.currentTimeMillis() + ext;
            File dest = new File(safeFolder, name);

            InputStream in = getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();

            Toast.makeText(this, "Eklendi! Galeriden orijinali silebilirsin.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreToGallery(String path) {
        try {
            File file = new File(path);
            boolean isVideo = path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi");

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, isVideo ? "video/mp4" : "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, 
                isVideo ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES);

            Uri collection = isVideo 
                ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Uri newUri = getContentResolver().insert(collection, values);
            if (newUri != null) {
                OutputStream out = getContentResolver().openOutputStream(newUri);
                FileInputStream in = new FileInputStream(file);
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
                out.close();

                file.delete();
                Toast.makeText(this, "Galeriye geri alındı!", Toast.LENGTH_SHORT).show();
                loadMedia();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void loadMedia() {
        mediaList.clear();
        File[] files = safeFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.getName().equals(".nomedia")) {
                    mediaList.add(f.getAbsolutePath());
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
