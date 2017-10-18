package com.nova.face;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Handler;
import android.os.Message;

public class GoogleFaceDetect implements FaceDetectionListener {
    private Handler mHander;

    public GoogleFaceDetect(Handler handler) {
        mHander = handler;
    }

    @Override
    public void onFaceDetection(Face[] faces, Camera camera) {
        // TODO Auto-generated method stub
        if (faces != null) {
            Message m = mHander.obtainMessage();
            m.what = EventUtil.UPDATE_FACE_RECT;
            m.obj = faces;
            m.sendToTarget();
        }
    }
}  