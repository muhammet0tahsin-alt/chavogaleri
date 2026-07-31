package com.chavogaleri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sil")
                .setMessage("Bu dosyayı silmek istiyor musun?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    File file = new File(mediaList.get(position));
                    if (file.delete()) {
                        Toast.makeText(this, "Silindi!", Toast.LENGTH_SHORT).show();
                        loadMedia();
                    }
                })
                .setNegativeButton("Hayır", null)
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

            Toast.makeText(this, "Eklendi ve gizlendi!", Toast.LENGTH_SHORT).show();
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
