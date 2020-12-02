package com.ashish.videoprocessingapp.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ashish.videoprocessingapp.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessingActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private Runnable r;
    private FFmpeg ffmpeg;
    private ProgressDialog progressDialog;
    private Uri selectedVideoUri;
    private static final String TAG = "Ashish";
    private static final String FILEPATH = "filepath";
    private TextView tvLeft, tvRight;
    private String filePath;
    private int duration;

    Button browse, trim, addimage;
    EditText videotitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_processing);
        trim = findViewById(R.id.trim);
        browse = findViewById(R.id.browse);
        addimage = findViewById(R.id.addtext);
        videotitle = findViewById(R.id.title);


        tvLeft = findViewById(R.id.tvLeft);
        tvRight = findViewById(R.id.tvRight);

        videoView = findViewById(R.id.videoView);
        rangeSeekBar = findViewById(R.id.rangeSeekBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        //rangeSeekBar.setEnabled(false);

        loadFFMpegBinary();

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23)
                    getPermission();
                else
                    uploadVideo();
            }
        });

        trim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedVideoUri != null) {
                    executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
                } else
                    Toast.makeText(VideoProcessingActivity.this, "Please upload a video", Toast.LENGTH_SHORT).show();
            }
        });

        addimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedVideoUri != null) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else
                    Toast.makeText(VideoProcessingActivity.this, "Please upload a video", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(VideoProcessingActivity.this, params, 100);
        } else
            uploadVideo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    uploadVideo();
                }
            }
            break;
        }
    }

    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);
                videoView.start();

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        duration = mp.getDuration() / 1000;
                        tvLeft.setText("00:00:00");
                        tvRight.setText(getTime(mp.getDuration() / 1000));
                        mp.setLooping(true);

                        rangeSeekBar.setRangeValues(0, duration);
                        rangeSeekBar.setSelectedMinValue(0);
                        rangeSeekBar.setSelectedMaxValue(duration);
                        rangeSeekBar.setEnabled(true);

                        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                            @Override
                            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                                videoView.seekTo((int) minValue * 1000);

                                tvLeft.setText(getTime((int) bar.getSelectedMinValue()));

                                tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                            }
                        });

                        final Handler handler = new Handler();
                        handler.postDelayed(r = new Runnable() {
                            @Override
                            public void run() {

                                if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                                    videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                                handler.postDelayed(r, 1000);
                            }
                        }, 1000);
                    }
                });
            } else if (requestCode == 1) {
                Uri selectedImageuri = data.getData();
                executeAddImageToVideoCommand(selectedImageuri);
                Toast.makeText(VideoProcessingActivity.this, "Image will come at the mid lenght of video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void executeCutVideoCommand(int startMs, int endMs) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        String filePrefix = videotitle.getText().toString();
        String fileExtn = getExtension(selectedVideoUri);
        String yourRealPath = getPath(VideoProcessingActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + "." + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + "." + fileExtn);
        }

        Log.d(TAG, "startTrim: uri: " + selectedVideoUri);
        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
        filePath = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", filePath};
        execFFmpegBinary(complexCommand, fileExtn);

    }

    private void executeAddImageToVideoCommand(Uri selectedimageuri) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        String filePrefix = videotitle.getText().toString();
        String fileExtn = getExtension(selectedVideoUri);
        String yourRealPath = getPath(VideoProcessingActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + "." + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + "." + fileExtn);
        }
        filePath = dest.getAbsolutePath();
        String imgurl = getPath(VideoProcessingActivity.this, selectedimageuri);
        String timesec = String.valueOf(duration / 2);

        //String[] complexCommand ={"-y", "-i", yourRealPath, "-i", imgurl, "-filter_complex","[1:v] format=bgra, rotate=PI/180:c=none:ow=rotw(30*PI/180):oh=roth(30*PI/180) [rotate];[rotate]scale=200:200[scale];[0:v][scale] overlay=40:10","-codec:a","copy", filePath};

        String[] complexCommand = {"-y", "-i", yourRealPath, "-itsoffset", timesec, "-i", imgurl, "-filter_complex", "[1:v] format=bgra, rotate=PI/180:c=none:ow=rotw(30*PI/180):oh=roth(30*PI/180) [rotate];[rotate]scale=200:200[scale];[0:v][scale] overlay=40:10", "-codec:a", "copy", filePath};

        execFFmpegBinary(complexCommand, fileExtn);
    }

    private void executeAddTextToVideoCommand() {
        File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);

        String filePrefix = videotitle.getText().toString();
        String fileExtn = getExtension(selectedVideoUri);
        String yourRealPath = getPath(VideoProcessingActivity.this, selectedVideoUri);
        File dest = new File(moviesDir, filePrefix + "." + fileExtn);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + "." + fileExtn);
        }

        Log.d(TAG, "startTrim: uri: " + selectedVideoUri);
        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        filePath = dest.getAbsolutePath();

        String[] complexCommand = {"-y", "-i", yourRealPath, "-strict", "experimental", "-vf", "[1:v] format=bgra", "drawtext=text='hello': fontcolor=black", "-map", "0:a", "-codec:v", "libx264", "-codec:a", "copy", filePath};
        execFFmpegBinary(complexCommand, fileExtn);
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
        }
    }

    public String getExtension(Uri videouri) {
        String extension;
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension = mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(videouri));
        return extension;
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(VideoProcessingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoProcessingActivity.this.finish();
                    }
                })
                .create()
                .show();

    }

    private void execFFmpegBinary(final String[] command, final String ext) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
                    Intent intent = new Intent(VideoProcessingActivity.this, PreviewActivity.class);
                    intent.putExtra(FILEPATH, filePath);
                    intent.putExtra("title", videotitle.getText().toString());
                    intent.putExtra("ext", ext);
                    startActivity(intent);
                    finish();

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Progress : " + s);
                    Log.d(TAG, "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        /* DocumentProvider */
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
