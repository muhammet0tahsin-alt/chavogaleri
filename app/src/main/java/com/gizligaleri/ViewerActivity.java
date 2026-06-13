package com.chavogaleri;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        String path = getIntent().getStringExtra("path");

        ImageView imageView = findViewById(R.id.imageView);
        VideoView videoView = findViewById(R.id.videoView);

        if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi")) {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            MediaController mc = new MediaController(this);
            videoView.setMediaController(mc);
            videoView.setVideoPath(path);
            videoView.start();
        } else {
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }
}
