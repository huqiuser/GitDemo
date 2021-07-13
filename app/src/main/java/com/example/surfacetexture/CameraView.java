package com.example.surfacetexture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
	public final static String TAG="GameDisplay";
	
	private static final int MAGIC_TEXTURE_ID = 1;
	public static final int DEFAULT_WIDTH=800;
	public static final int DEFAULT_HEIGHT=480;
	public SurfaceHolder gHolder;
	public  SurfaceTexture gSurfaceTexture;
	public Camera gCamera;
	public byte gBuffer[];
	public int textureBuffer[];
	//public ProcessThread gProcessThread;
	private int bufferSize;
	private Camera.Parameters parameters;
	public int previewWidth, previewHeight;
	public int screenWidth, screenHeight;
	public Bitmap gBitmap;
	private Rect gRect;
	
	public CameraView(Context context,int screenWidth,int screenHeight) {
		super(context);
		gHolder=this.getHolder();
		gHolder.addCallback(this);
		//gHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		gSurfaceTexture=new SurfaceTexture(MAGIC_TEXTURE_ID);
		this.screenWidth=screenWidth;
		this.screenHeight=screenHeight;
		gRect=new Rect(0,0,screenWidth,screenHeight);
		Log.v(TAG, "GameDisplay initialization completed");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(TAG, "GameDisplay surfaceChanged");
		parameters = gCamera.getParameters();	
		List<Size> preSize = parameters.getSupportedPreviewSizes();
		previewWidth = preSize.get(0).width;
		previewHeight = preSize.get(0).height;
		for (int i = 1; i < preSize.size(); i++) {
			double similarity = Math
					.abs(((double) preSize.get(i).height / screenHeight)
							- ((double) preSize.get(i).width / screenWidth));
			if (similarity < Math.abs(((double) previewHeight / screenHeight)
									- ((double) previewWidth / screenWidth))) {
				previewWidth = preSize.get(i).width;
				previewHeight = preSize.get(i).height;
			}
		}
		gBitmap= Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
		parameters.setPreviewSize(previewWidth, previewHeight);
		gCamera.setParameters(parameters);
		bufferSize = previewWidth * previewHeight;
		textureBuffer=new int[bufferSize];
		bufferSize  = 2*bufferSize * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
		gBuffer = new byte[bufferSize];
		gCamera.addCallbackBuffer(gBuffer);
		gCamera.setPreviewCallbackWithBuffer(this);
		gCamera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(TAG, "GameDisplay surfaceCreated");
		if (gCamera == null) {
			gCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
		}
		try {
			gCamera.setDisplayOrientation(0);
			gCamera.setPreviewTexture(gSurfaceTexture);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(TAG, "GameDisplay surfaceDestroyed");
		gCamera.stopPreview();
		gCamera.release();
	}

	public static int calculateCameraPreviewOrientation(Activity activity) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.v(TAG, "GameDisplay onPreviewFrame");

//		Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
//		BitmapFactory.Options newOpts = new BitmapFactory.Options();
//		newOpts.inJustDecodeBounds = true;
//		YuvImage yuvimage = new YuvImage(
//				data,
//				ImageFormat.NV21,
//				previewSize.width,
//				previewSize.height,
//				null);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
//		byte[] rawImage = baos.toByteArray();
//		//将rawImage转换成bitmap
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inPreferredConfig = Bitmap.Config.RGB_565;
//		Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
//		gBitmap = bitmap;


		camera.addCallbackBuffer(gBuffer);

		for(int i=0;i<textureBuffer.length;i++)
		{
			int R=data[i]<<16;
			int G=data[i]<<8;
			int B=data[i];
			textureBuffer[i]=0xff000000|(R+G+B);
		}
		gBitmap.setPixels(textureBuffer, 0, previewWidth, 0, 0, previewWidth, previewHeight);
		synchronized (gHolder)
		{		
			Canvas canvas = this.getHolder().lockCanvas();
			canvas.drawBitmap(gBitmap, null,gRect, null);
			this.getHolder().unlockCanvasAndPost(canvas);
		}
		
	}
}
