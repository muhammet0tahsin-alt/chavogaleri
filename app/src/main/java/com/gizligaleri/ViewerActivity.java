package com.chavogaleri;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import java.io.File;
import java.util.ArrayList;

public class ViewerActivity extends AppCompatActivity {

    private ArrayList<String> list;
    private int position;
    private ImageView imageView;
    private VideoView videoView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);

        list = getIntent().getStringArrayListExtra("list");
        position = getIntent().getIntExtra("position", 0);

        showMedia(position);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                float diff = e2.getX() - e1.getX();
                if (Math.abs(diff) > 100) {
                    if (diff < 0 && position < list.size() - 1) {
                        position++;
                        showMedia(position);
                    } else if (diff > 0 && position > 0) {
                        position--;
                        showMedia(position);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void showMedia(int pos) {
        String path = list.get(pos);
        videoView.stopPlayback();

        if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi") || path.endsWith(".3gp")) {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            MediaController mc = new MediaController(this);
            mc.setAnchorView(videoView);
            videoView.setMediaController(mc);
            videoView.setVideoURI(Uri.fromFile(new File(path)));
            videoView.start();
        } else {
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
