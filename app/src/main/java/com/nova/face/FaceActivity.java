package com.nova.face;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Administrator on 2017-10-17.
 *
 * @author kzaxil
 * @since 1.0.0
 */
public class FaceActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private FaceView faceView;
    private MainHandler mMainHandler;
    private SurfaceHolder mSurfaceHolder;
    private GoogleFaceDetect googleFaceDetect;

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case EventUtil.UPDATE_FACE_RECT:
                    Face[] faces = (Face[]) msg.obj;
                    faceView.setFaces(faces);
                    break;
                case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
                    startGoogleFaceDetect();
                    break;
            }
            super.handleMessage(msg);
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ac_camera);
        faceView = (FaceView) findViewById(R.id.iv_face);
        mSurfaceHolder = ((SurfaceView) findViewById(R.id.sv_face)).getHolder();
        mSurfaceHolder.addCallback(this);
        mMainHandler = new MainHandler();
        googleFaceDetect = new GoogleFaceDetect(mMainHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGoogleFaceDetect();
        mMainHandler.removeCallbacksAndMessages(null);
    }

    private void startGoogleFaceDetect() {
        Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
        if (params.getMaxNumDetectedFaces() > 0) {
            if (faceView != null) {
                faceView.clearFaces();
                faceView.setVisibility(View.VISIBLE);
            }
            CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(googleFaceDetect);
            CameraInterface.getInstance().getCameraDevice().startFaceDetection();
        }
    }

    private void stopGoogleFaceDetect() {
        Camera.Parameters params = CameraInterface.getInstance().getCameraParams();
        if (params.getMaxNumDetectedFaces() > 0) {
            CameraInterface.getInstance().getCameraDevice().setFaceDetectionListener(null);
            CameraInterface.getInstance().getCameraDevice().stopFaceDetection();
            faceView.clearFaces();
        }
    }

    private void faceDetect() {
        if (CameraInterface.getInstance().doOpenCamera(this, null, Camera.CameraInfo.CAMERA_FACING_FRONT)) {
            mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 500);
            CameraInterface.getInstance().doStartPreview(mSurfaceHolder, 1.333f);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        faceDetect();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraInterface.getInstance().doStopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // Camera权限回调
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 表示用户授权
                faceDetect();
            } else {
                // 用户拒绝授权
            }
        }
    }
}
