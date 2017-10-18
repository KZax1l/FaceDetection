package com.nova.face;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

public class FaceView extends android.support.v7.widget.AppCompatImageView {
    private Face[] mFaces;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Drawable mFaceIndicator = null;

    private Paint mPaint;

    private float[] mMouth = new float[2];
    private float[] mLeftEye = new float[2];
    private float[] mRightEye = new float[2];

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFaceIndicator = ContextCompat.getDrawable(context, R.mipmap.ic_face);

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void setFaces(Face[] faces) {
        this.mFaces = faces;
        invalidate();
    }

    public void clearFaces() {
        mFaces = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub  
        if (mFaces == null || mFaces.length < 1) {
            return;
        }
        boolean isMirror = false;
        switch (CameraInterface.getInstance().getCameraId()) {
            case CameraInfo.CAMERA_FACING_BACK:
                isMirror = false; // 后置Camera无需mirror
                break;
            case CameraInfo.CAMERA_FACING_FRONT:
                isMirror = true;  // 前置Camera需要mirror
                break;
        }
        prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
        canvas.save();
        mMatrix.postRotate(0); // Matrix.postRotate默认是顺时针
        canvas.rotate(-0);   // Canvas.rotate()默认是逆时针
        for (Face mFace : mFaces) {
            mRect.set(mFace.rect);
            mMatrix.mapRect(mRect);
            mFaceIndicator.setBounds(Math.round(mRect.left), Math.round(mRect.top),
                    Math.round(mRect.right), Math.round(mRect.bottom));
            mFaceIndicator.draw(canvas);

            mMouth[0] = mFace.mouth.x;
            mMouth[1] = mFace.mouth.y;
            mLeftEye[0] = mFace.leftEye.x;
            mLeftEye[1] = mFace.leftEye.y;
            mRightEye[0] = mFace.rightEye.x;
            mRightEye[1] = mFace.rightEye.y;
            mMatrix.mapPoints(mMouth);
            mMatrix.mapPoints(mLeftEye);
            mMatrix.mapPoints(mRightEye);

            canvas.drawCircle(mMouth[0], mMouth[1], 50f, mPaint);
            canvas.drawCircle(mLeftEye[0], mLeftEye[1], 50f, mPaint);
            canvas.drawCircle(mRightEye[0], mRightEye[1], 50f, mPaint);

            System.out.println("leftEye:[" + mFace.leftEye.x + "," + mFace.leftEye.y + "],"
                    + "rightEye:[" + mFace.rightEye.x + "," + mFace.rightEye.y + "],"
                    + "mouth:[" + mFace.mouth.x + "," + mFace.mouth.y + "],"
                    + "rect:[" + mFace.rect.left + "," + mFace.rect.top + "," + mFace.rect.right + "," + mFace.rect.bottom + "].");
        }
        canvas.restore();
        super.onDraw(canvas);
    }

    public void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                              int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }
}