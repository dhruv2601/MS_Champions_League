package org.andresoviedo.app.model3D;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.camerakit.type.CameraSize;
import com.jpegkit.Jpeg;
import com.jpegkit.JpegImageView;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.andresoviedo.dddmodel2.R;

import java.io.File;
import java.io.FileOutputStream;


public class CameraActivity extends Activity {

    private Button btnCapture;
    private String filePath;

    private CameraKitView cameraView;
    private AppCompatTextView facingText;
    private AppCompatTextView flashText;
    private AppCompatTextView previewSizeText;
    private AppCompatTextView photoSizeText;

    private Button flashOnButton;
    private Button flashOffButton;

    private Button photoButton;

    private Button facingFrontButton;
    private Button facingBackButton;

    private Button permissionsButton;
    private String address = "";

    private void launchDialog() {
        new LovelyTextInputDialog(this, R.style.EditTextTintTheme)
            .setTopColorRes(R.color.darkDeepOrange)
            .setTitle(R.string.text_input_title)
            .setMessage(R.string.text_input_message)
            .setCancelable(false)
            .setIcon(R.drawable.ic_assignment_white_36dp)
            .setInputFilter(R.string.text_input_error_message, new LovelyTextInputDialog.TextFilter() {
                @Override
                public boolean check(String text) {
                    return text.matches("\\w+");
                }
            })
            .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                @Override
                public void onTextInputConfirmed(String text) {
                    address = text;
                    Toast.makeText(CameraActivity.this, text, Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        launchDialog();
        cameraView = findViewById(R.id.camera);
        facingText = findViewById(R.id.facingText);
        flashText = findViewById(R.id.flashText);
        previewSizeText = findViewById(R.id.previewSizeText);
        photoSizeText = findViewById(R.id.photoSizeText);

        photoButton = findViewById(R.id.photoButton);
        photoButton.setOnClickListener(photoOnClickListener);

        flashOnButton = findViewById(R.id.flashOnButton);
        flashOffButton = findViewById(R.id.flashOffButton);

        flashOnButton.setOnClickListener(flashOnOnClickListener);
        flashOffButton.setOnClickListener(flashOffOnClickListener);

        facingFrontButton = findViewById(R.id.facingFrontButton);
        facingBackButton = findViewById(R.id.facingBackButton);

        facingFrontButton.setOnClickListener(facingFrontOnClickListener);
        facingBackButton.setOnClickListener(facingBackOnClickListener);

        permissionsButton = findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.requestPermissions(CameraActivity.this);
            }
        });

        cameraView.setPermissionsListener(new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {
                permissionsButton.setVisibility(View.GONE);
            }

            @Override
            public void onPermissionsFailure() {
                permissionsButton.setVisibility(View.VISIBLE);
            }
        });

        cameraView.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {
                Log.v("CameraKitView", "CameraListener: onOpened()");
            }

            @Override
            public void onClosed() {
                Log.v("CameraKitView", "CameraListener: onClosed()");
            }
        });

        cameraView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                Log.v("CameraKitView", "PreviewListener: onStart()");
                updateInfoText();
            }

            @Override
            public void onStop() {
                Log.v("CameraKitView", "PreviewListener: onStop()");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView view, final byte[] photo) {
                    final Jpeg jpeg = new Jpeg(photo);
                    final byte[] photoByte = jpeg.getJpegBytes();

                    // capturedImage contains the image from the CameraKitView.
                    File savedPhoto = new File(Environment.getExternalStorageDirectory(), "image_" + System.currentTimeMillis() % 10000 + ".jpg");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                        filePath = savedPhoto.getPath();
                        Log.d("ZlTag", "filePath: " + filePath);
                        //todo check if photo exists and remove it?
                        outputStream.write(photoByte);
                        outputStream.close();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                moveToLocalModalFetcher();
                            }
                        }, 2000);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private View.OnClickListener flashOnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cameraView.getFlash() != CameraKit.FLASH_ON) {
                cameraView.setFlash(CameraKit.FLASH_ON);
                updateInfoText();
            }
        }
    };

    private View.OnClickListener flashOffOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cameraView.getFlash() != CameraKit.FLASH_OFF) {
                cameraView.setFlash(CameraKit.FLASH_OFF);
                updateInfoText();
            }
        }
    };

    private View.OnClickListener facingFrontOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraView.setFacing(CameraKit.FACING_FRONT);
        }
    };

    private View.OnClickListener facingBackOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cameraView.setFacing(CameraKit.FACING_BACK);
        }
    };

    private void updateInfoText() {
        String facingValue = cameraView.getFacing() == CameraKit.FACING_BACK ? "BACK" : "FRONT";
        facingText.setText(Html.fromHtml("<b>Facing:</b> " + facingValue));

        String flashValue = "OFF";
        switch (cameraView.getFlash()) {
            case CameraKit.FLASH_OFF: {
                flashValue = "OFF";
                break;
            }

            case CameraKit.FLASH_ON: {
                flashValue = "ON";
                break;
            }

            case CameraKit.FLASH_AUTO: {
                flashValue = "AUTO";
                break;
            }

            case CameraKit.FLASH_TORCH: {
                flashValue = "TORCH";
                break;
            }
        }
        flashText.setText(Html.fromHtml("<b>Flash:</b> " + flashValue));

        CameraSize previewSize = cameraView.getPhotoResolution();
        if (previewSize != null) {
            previewSizeText.setText(Html.fromHtml(String.format("<b>Preview Resolution:</b> %d x %d", previewSize.getWidth(), previewSize.getHeight())));
        }

        CameraSize photoSize = cameraView.getPhotoResolution();
        if (photoSize != null) {
            photoSizeText.setText(Html.fromHtml(String.format("<b>Photo Resolution:</b> %d x %d", photoSize.getWidth(), photoSize.getHeight())));
        }
    }

    private void moveToLocalModalFetcher() {
        Intent i = new Intent(CameraActivity.this, LocalModalFetcherAct.class);
        i.putExtra("path", filePath);
        i.putExtra("add", address);
        startActivity(i);
    }
}