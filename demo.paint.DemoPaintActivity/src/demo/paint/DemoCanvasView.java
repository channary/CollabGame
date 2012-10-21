package demo.paint;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class DemoCanvasView extends View {
	
	@SuppressWarnings("unused")
	private static final String TAG = "demo.paint.DemoCanvasView";
	private Bitmap mBitmap;
	private Paint mPaint;
	private Path mPath;
	private ScaleGestureDetector mScaleGestureDetector;
	private static float mScale = 100.0f;
	private List<Path> mPaths;

	public DemoCanvasView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DemoCanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DemoCanvasView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mPaint = new Paint();
		mPaint.setFilterBitmap(true);
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(5.0f);
		mPaint.setStyle(Style.STROKE);
		mPaint.setTextSize(36.0f);
		
		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				mScale*=detector.getScaleFactor();
				mPath = null;
				invalidate();
				return true;
			}
		});
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.scale(mScale/100.0f, mScale/100.0f);
		if(mBitmap!=null){
			canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		}
		
		if(mPaths!=null){
			for(int i = 0; i<mPaths.size();i++){
				canvas.drawPath(mPaths.get(i), mPaint);
			}
		}
		
		if(mPath!=null){
			canvas.drawPath(mPath, mPaint);
		}

		canvas.restore();
		
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(36.0f);
		
		canvas.drawText("Zoom="+mScale+"%", 10, 36, paint);
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		mScaleGestureDetector.onTouchEvent(event);
		if(mScaleGestureDetector.isInProgress()){
			return true;
		}
		
		switch(event.getActionMasked()){
		case MotionEvent.ACTION_DOWN:
			mPath = new Path();
			mPath.moveTo(event.getX()*100.0f/mScale, event.getY()*100.0f/mScale);
			break;
		case MotionEvent.ACTION_MOVE:
			if(mPath!=null){
				mPath.lineTo(event.getX()*100.0f/mScale, event.getY()*100.0f/mScale);
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mPath!=null && mPaths!=null){
				mPath.lineTo(event.getX()*100.0f/mScale, event.getY()*100.0f/mScale);
				mPaths.add(mPath);
				mPath = null;
			}
//			mPath.close();
			break;
		}
		invalidate();
		return true;
	}

	public Paint getPaint() {
		return mPaint;
	}

	public void setPaint(Paint paint) {
		mPaint = paint;
	}

	public List<Path> getPaths() {
		return mPaths;
	}

	public void setPaths(List<Path> paths) {
		mPaths = paths;
	}

}
