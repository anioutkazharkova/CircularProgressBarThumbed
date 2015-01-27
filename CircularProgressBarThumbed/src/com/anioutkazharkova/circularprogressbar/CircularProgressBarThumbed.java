/**
 * Author Anna Zharkova. 2015.
 * It is my implementation of circular progressbar with customizable thumbs.
 * */

package com.anioutkazharkova.circularprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;

public class CircularProgressBarThumbed extends ProgressBar {

	private static int TOTAL_WIDTH = 30;
	
	private final RectF mCircleBounds = new RectF();
	private final Paint mProgressColorPaint = new Paint();
	private final Paint mBackgroundColorPaint = new Paint();
	private final Paint mThumbColorPaint = new Paint();
	
	//The width of spread including width of thumb in px
	private float mStrokeTotalWidth = TOTAL_WIDTH;
	
	//The width of progress drawable line in dpi
	private float mStrokeWidth = 5;
	
	//The width of progress backgroubd line in dpi
	private float mBackStrokeWidth = 1;

	private int mThumbDrawable;
	private int mThumbColor;
	private Bitmap scaledBitmap;

	private Context mContext;
	private double mRadius;
	
	//The half of thumb in px
	private float mThumbRadius = 30;

	int mColor = 0;
	int mBackColor = 0;
	private double currentX;
	private double currentY;
	
	private boolean mThumbEnabled=true;

	public CircularProgressBarThumbed(Context context) {
		super(context);
		mContext = context;
		init(null, 0);
	}

	public CircularProgressBarThumbed(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs, 0);

	}

	public CircularProgressBarThumbed(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs, defStyle);

	}

	private void init(AttributeSet attrs, int style) {
		
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircularProgressBarThumbed, style, 0);
		
		String color = a
				.getString(R.styleable.CircularProgressBarThumbed_progressColor);
		if (color == null) {
			mProgressColorPaint.setColor(getResources().getColor(
					R.color.default_progress_color));
		} else {
			mProgressColorPaint.setColor(Color.parseColor(color));
		}
		color = a
				.getString(R.styleable.CircularProgressBarThumbed_backgroundColor);
		if (color == null) {
			mBackgroundColorPaint.setColor(getResources().getColor(
					R.color.default_background_color));
		} else {
			mBackgroundColorPaint.setColor(Color.parseColor(color));
		}
		color = a.getString(R.styleable.CircularProgressBarThumbed_thumbColor);
		if (color == null) {
			mThumbColorPaint.setColor(getResources().getColor(
					R.color.default_thumb_color));
		} else {
			mThumbColorPaint.setColor(Color.parseColor(color));
		}

		mStrokeWidth = DpToPx(mContext, a.getInt(
				R.styleable.CircularProgressBarThumbed_strokeWidth,
				(int) mStrokeWidth));
		mBackStrokeWidth = DpToPx(mContext, a.getInt(
				R.styleable.CircularProgressBarThumbed_backStrokeWidth,
				(int) mBackStrokeWidth));
		mProgressColorPaint.setAntiAlias(true);
		mProgressColorPaint.setStyle(Paint.Style.STROKE);
		mProgressColorPaint.setStrokeWidth(mStrokeWidth);
		mProgressColorPaint.setMaskFilter(new BlurMaskFilter(2, Blur.INNER));
		mBackgroundColorPaint.setAntiAlias(true);
		mBackgroundColorPaint.setStyle(Paint.Style.STROKE);
		mBackgroundColorPaint.setStrokeWidth(mBackStrokeWidth);

		mThumbColorPaint.setColorFilter(new PorterDuffColorFilter(
				mThumbColorPaint.getColor(), PorterDuff.Mode.SRC_IN));
		String thumbName = a
				.getString(R.styleable.CircularProgressBarThumbed_thumb);
		if (thumbName == null) {

			mThumbDrawable = R.drawable.circle_white;

		} else {

			mThumbDrawable = getResourceDrawableName(thumbName);
		}
		
		mThumbEnabled=a.getBoolean(R.styleable.CircularProgressBarThumbed_thumbEnabled, mThumbEnabled);
		a.recycle();
		scaledBitmap=createThumb(mThumbDrawable);
	}

	private Bitmap createThumb(int drawable)
	{
		Bitmap icon = BitmapFactory.decodeResource(getResources(),
				drawable);		

		scaledBitmap = Bitmap.createScaledBitmap(icon, (int) mThumbRadius * 2,
				(int) mThumbRadius * 2, true);
		icon.recycle();
		icon = null;
		return scaledBitmap;
	}
	private int getResourceDrawableName(String name) {
		String[] tokens = name.split("/");
		name = (tokens[tokens.length - 1].split("\\."))[0];
		tokens = null;
		return getResources().getIdentifier(name, "drawable",
				mContext.getPackageName());
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		canvas.drawArc(mCircleBounds, 0, 360, false, mBackgroundColorPaint);

		int prog = getProgress();
		float scale = getMax() > 0 ? (float) prog / getMax() * 360 : 0;

		canvas.drawArc(mCircleBounds, 270, scale, false, mProgressColorPaint);
		
		if (mThumbEnabled)
		{
		canvas.save();
		currentX = (mRadius - mThumbRadius) + (mRadius - mStrokeTotalWidth)
				* Math.cos(Math.toRadians(Math.abs(-270 - scale)));
		currentY = (mRadius - mThumbRadius) + (mRadius - mStrokeTotalWidth)
				* Math.sin(Math.toRadians(Math.abs(-270 - scale)));
		
		Matrix matrix = new Matrix();
		matrix.postTranslate(-mThumbRadius,-mThumbRadius);
		matrix.postRotate(scale-90);
		matrix.postTranslate((int)currentX+mThumbRadius, (int)currentY+mThumbRadius);
		
		canvas.drawBitmap(scaledBitmap, matrix,mThumbColorPaint);
		
		canvas.restore();
		}
		/*Paint mMarkerPaint=new Paint(mThumbColorPaint);
		mMarkerPaint.setStrokeWidth(5);
		for(int i=0;i<=360;i+=90)
		{
		float x=(float) ((mRadius) + (mRadius - mStrokeTotalWidth)
		* Math.cos(Math.toRadians(Math.abs(-270+i))));
		float y= (float) ((mRadius - mThumbRadius) + (mRadius - mStrokeTotalWidth)
			* Math.sin(Math.toRadians(Math.abs(-270+i))));
		canvas.save();
		canvas.drawLine(x, y, x, y+mThumbRadius*2,mMarkerPaint );
		canvas.restore();
		}*/
		super.onDraw(canvas);
	}

	public synchronized void setProgressColor(int color) {
		mColor = color;
		mProgressColorPaint.setColor(getResources().getColor(mColor));
		invalidate();
	}

	public synchronized void setBackProgressColor(int color) {
		mBackColor = color;
		mBackgroundColorPaint.setColor(getResources().getColor(mBackColor));
		invalidate();
	}

	public synchronized void setThumb(int thumb) {
		mThumbDrawable = thumb;
		scaledBitmap=createThumb(mThumbDrawable);
		invalidate();
	}

	public synchronized void setThumbColor(int color) {
		mThumbColor = color;
		mThumbColorPaint.setColor(getResources().getColor(mThumbColor));
		invalidate();
	}

	public synchronized void setProgressDrawableWidth(int strokeWidth) {
		mStrokeWidth = strokeWidth;
		mProgressColorPaint
				.setStrokeWidth(DpToPx(mContext, (int) mStrokeWidth));
		invalidate();
	}
	
	public synchronized void setThumbEnabled(boolean flag)
	{
		mThumbEnabled=flag;
		invalidate();
	}

	public synchronized void setBackgroundDrawableWidth(int strokeWidth) {
		mBackStrokeWidth = strokeWidth;
		mBackgroundColorPaint.setStrokeWidth(DpToPx(mContext,
				(int) mBackStrokeWidth));
		invalidate();
	}
	public synchronized void setShowThumbOnly(boolean flag)
	{
		if (flag)
		{
			mColor=mProgressColorPaint.getColor();
			mProgressColorPaint.setColor(mBackgroundColorPaint.getColor());
			mProgressColorPaint.setStrokeWidth(mBackStrokeWidth);
			
		}
		else
		{
			mProgressColorPaint.setColor(mColor);
			mProgressColorPaint.setStrokeWidth(mStrokeWidth);
		}
		invalidate();
	}

	@Override
	public synchronized void setProgress(int progress) {
		// TODO Auto-generated method stub
		super.setProgress(progress);
		invalidate();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		
		int addition = (int) (2 * mStrokeTotalWidth);
		setMeasuredDimension(min + addition, min + addition);
		mCircleBounds.set(TOTAL_WIDTH, TOTAL_WIDTH, min + TOTAL_WIDTH, min
				+ TOTAL_WIDTH);
		mRadius = (int) (getMeasuredWidth() / 2);

	}

	public static int DpToPx(Context context, int value) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		float scale = displayMetrics.density;
		int pixels = (int) (value * scale + 0.5f);
		return pixels;

	}	
	

}
