package com.lynn.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.config.AudioPickerConfig;
import com.lynn.filepicker.config.OtherFilePickerConfig;
import com.lynn.filepicker.config.VideoPickerConfig;
import com.lynn.filepicker.entity.AudioFile;
import com.lynn.filepicker.entity.ImageFile;
import com.lynn.filepicker.entity.OtherFile;
import com.lynn.filepicker.entity.VideoFile;

import java.util.ArrayList;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_pick_image).setOnClickListener(this);
        findViewById(R.id.btn_pick_audio).setOnClickListener(this);
        findViewById(R.id.btn_pick_video).setOnClickListener(this);
        findViewById(R.id.btn_pick_file).setOnClickListener(this);
        mTvResult = (TextView) findViewById(R.id.tv_result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pick_image:
                FilePicker.pickImage(this).subscribe(new Consumer<ArrayList<ImageFile>>() {
                    @Override
                    public void accept(ArrayList<ImageFile> imageFiles) throws Exception {
                        StringBuilder builder = new StringBuilder();
                        for (ImageFile file : imageFiles) {
                            String path = file.getPath();
                            builder.append(path + "\n");
                        }
                        mTvResult.setText(builder.toString());
                    }
                });
                break;
            case R.id.btn_pick_video:
                FilePicker.pickVideo(this, new VideoPickerConfig.Builder()
                        .build())
                        .subscribe(new Consumer<ArrayList<VideoFile>>() {
                            @Override
                            public void accept(ArrayList<VideoFile> videoFiles) throws Exception {
                                StringBuilder builder = new StringBuilder();
                                for (VideoFile file : videoFiles) {
                                    String path = file.getPath();
                                    builder.append(path + "\n");
                                }
                                mTvResult.setText(builder.toString());
                            }
                        });
                break;
            case R.id.btn_pick_audio:
                FilePicker.pickAudio(this, new AudioPickerConfig.Builder()
                        .isNeedRecord(true)
                        .maxNumber(5)
                        .steepToolBarColor(getResources().getColor(R.color.colorPrimary))
                        .build())
                        .subscribe(new Consumer<ArrayList<AudioFile>>() {
                            @Override
                            public void accept(ArrayList<AudioFile> imageFiles) throws Exception {
                                StringBuilder builder = new StringBuilder();
                                for (AudioFile file : imageFiles) {
                                    String path = file.getPath();
                                    builder.append(path + "\n");
                                }
                                mTvResult.setText(builder.toString());
                            }
                        });
                break;
            case R.id.btn_pick_file:
                FilePicker.pickOtherFile(this, new OtherFilePickerConfig.Builder()
                        .suffix(new String[]{"xlsx", "xls", "doc", "docx", "ppt", "pptx", "pdf"})
                        .maxNumber(5)
                        .build())
                        .subscribe(new Consumer<ArrayList<OtherFile>>() {
                            @Override
                            public void accept(ArrayList<OtherFile> otherFiles) throws Exception {
                                StringBuilder builder = new StringBuilder();
                                for (OtherFile file : otherFiles) {
                                    String path = file.getPath();
                                    builder.append(path + "\n");
                                }
                                mTvResult.setText(builder.toString());
                            }
                        });
                break;
        }
    }


}
