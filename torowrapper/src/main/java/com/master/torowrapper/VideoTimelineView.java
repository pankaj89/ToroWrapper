package com.master.torowrapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class VideoTimelineView extends View {

    private long videoLength = 0;
    private float progressLeft;
    private float progressRight = 1;
    private Paint paint;
    private Paint paint2;
    private boolean pressedLeft;
    private boolean pressedRight;
    private float pressDx;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private VideoTimelineViewDelegate delegate;
    private ArrayList<Bitmap> frames = new ArrayList<>();
    private AsyncTask<Integer, Integer, Bitmap> currentTask;
    private static final Object sync = new Object();
    private long frameTimeOffset;
    private int frameWidth;
    private int frameHeight;
    private int framesToLoad;
    private float maxProgressDiff = 1.0f;
    private float minProgressDiff = 0.0f;
    private boolean isRoundFrames;
    private Rect rect1;
    private Rect rect2;

    public interface VideoTimelineViewDelegate {
        void onLeftProgressChanged(float progress);

        void onRightProgressChanged(float progress);

        void didStartDragging();

        void didStopDragging();
    }

    public VideoTimelineView(Context context) {
        super(context);
        init(context);
    }

    public VideoTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public static float density = 1;

    public void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xffffffff);
        paint2 = new Paint();
        paint2.setColor(0x7f000000);
    }

    public static int dp(float value) {

        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static int dp2(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.floor(density * value);
    }


    //seconds
    public float getLeftProgressInMillis() {
        return videoLength * progressLeft;
    }

    public float getRightProgressInMillis() {
        return videoLength * progressRight;
    }

    public void setMinProgressDiffInMillis(float valueInMillis) {
        setMinProgressDiff(valueInMillis / videoLength);
    }

    public void setMaxProgressDiffInMillis(float valueInMillis) {
        setMaxProgressDiff(valueInMillis / videoLength);
    }

    //seconds end
    public float getLeftProgress() {
        return progressLeft;
    }

    public float getRightProgress() {
        return progressRight;
    }

    public void setMinProgressDiff(float value) {
        minProgressDiff = value;
    }

    public void setMaxProgressDiff(float value) {
        maxProgressDiff = value;
        if (progressRight - progressLeft > maxProgressDiff) {
            progressRight = progressLeft + maxProgressDiff;
            invalidate();
        }
    }

    public void setRoundFrames(boolean value) {
        isRoundFrames = value;
        if (isRoundFrames) {
            rect1 = new Rect(dp(14), dp(14), dp(14 + 28), dp(14 + 28));
            rect2 = new Rect();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        int width = getMeasuredWidth() - dp(32);
        int startX = (int) (width * progressLeft) + dp(16);
        int endX = (int) (width * progressRight) + dp(16);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (mediaMetadataRetriever == null) {
                return false;
            }
            int additionWidth = dp(15);
            if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                if (delegate != null) {
                    delegate.didStartDragging();
                }
                pressedLeft = true;
                pressDx = (int) (x - startX);
                invalidate();
                return true;
            } else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                if (delegate != null) {
                    delegate.didStartDragging();
                }
                pressedRight = true;
                pressDx = (int) (x - endX);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedLeft) {
                if (delegate != null) {
                    delegate.didStopDragging();
                }
                pressedLeft = false;
                return true;
            } else if (pressedRight) {
                if (delegate != null) {
                    delegate.didStopDragging();
                }
                pressedRight = false;
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedLeft) {
                startX = (int) (x - pressDx);
                if (startX < dp(16)) {
                    startX = dp(16);
                } else if (startX > endX) {
                    startX = endX;
                }
                progressLeft = (float) (startX - dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressRight = progressLeft + maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressLeft = progressRight - minProgressDiff;
                    if (progressLeft < 0) {
                        progressLeft = 0;
                    }
                }
                if (delegate != null) {
                    delegate.onLeftProgressChanged(progressLeft);
                }
                invalidate();
                return true;
            } else if (pressedRight) {
                endX = (int) (x - pressDx);
                if (endX < startX) {
                    endX = startX;
                } else if (endX > width + dp(16)) {
                    endX = width + dp(16);
                }
                progressRight = (float) (endX - dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressLeft = progressRight - maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressRight = progressLeft + minProgressDiff;
                    if (progressRight > 1.0f) {
                        progressRight = 1.0f;
                    }
                }
                if (delegate != null) {
                    delegate.onRightProgressChanged(progressRight);
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setVideoPath(String path) {
        destroy();
        mediaMetadataRetriever = new MediaMetadataRetriever();
        progressLeft = 0.0f;
        progressRight = 1.0f;
        try {
            mediaMetadataRetriever.setDataSource(path);
            String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoLength = Long.parseLong(duration);
        } catch (Exception e) {
        }
        invalidate();
    }

    public void setDelegate(VideoTimelineViewDelegate delegate) {
        this.delegate = delegate;
    }

    private void reloadFrames(int frameNum) {
        if (mediaMetadataRetriever == null) {
            return;
        }
        if (frameNum == 0) {
            if (isRoundFrames) {
                frameHeight = frameWidth = dp(56);
                framesToLoad = (int) Math.ceil((getMeasuredWidth() - dp(16)) / (frameHeight / 2.0f));
            } else {
                frameHeight = getMeasuredHeight() - dp(5);
                framesToLoad = (getMeasuredWidth() - dp(16)) / frameHeight;
                frameWidth = (int) Math.ceil((float) (getMeasuredWidth() - dp(16)) / (float) framesToLoad);
            }
            frameTimeOffset = videoLength / framesToLoad;
        }
        currentTask = new AsyncTask<Integer, Integer, Bitmap>() {
            private int frameNum = 0;

            @Override
            protected Bitmap doInBackground(Integer... objects) {
                frameNum = objects[0];
                Bitmap bitmap = null;
                if (isCancelled()) {
                    return null;
                }
                try {
                    bitmap = mediaMetadataRetriever.getFrameAtTime(frameTimeOffset * frameNum * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (isCancelled()) {
                        return null;
                    }
                    if (bitmap != null) {
                        Bitmap result = Bitmap.createBitmap(frameWidth, frameHeight, bitmap.getConfig());
                        Canvas canvas = new Canvas(result);
                        float scaleX = (float) frameWidth / (float) bitmap.getWidth();
                        float scaleY = (float) frameHeight / (float) bitmap.getHeight();
                        float scale = scaleX > scaleY ? scaleX : scaleY;
                        int w = (int) (bitmap.getWidth() * scale);
                        int h = (int) (bitmap.getHeight() * scale);
                        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        Rect destRect = new Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h);
                        canvas.drawBitmap(bitmap, srcRect, destRect, null);
                        bitmap.recycle();
                        bitmap = result;
                    }
                } catch (Exception e) {
                }
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (!isCancelled()) {
                    frames.add(bitmap);
                    invalidate();
                    if (frameNum < framesToLoad) {
                        reloadFrames(frameNum + 1);
                    }
                }
            }
        };
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null);
    }

    public void destroy() {
        synchronized (sync) {
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                    mediaMetadataRetriever = null;
                }
            } catch (Exception e) {
            }
        }
        for (int a = 0; a < frames.size(); a++) {
            Bitmap bitmap = frames.get(a);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    public void clearFrames() {
        for (int a = 0; a < frames.size(); a++) {
            Bitmap bitmap = frames.get(a);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth() - dp(36);
        int startX = (int) (width * progressLeft) + dp(16);
        int endX = (int) (width * progressRight) + dp(16);

        canvas.save();
        canvas.clipRect(dp(16), 0, width + dp(20), getMeasuredHeight());
        if (frames.isEmpty() && currentTask == null) {
            reloadFrames(0);
        } else {
            int offset = 0;
            for (int a = 0; a < frames.size(); a++) {
                Bitmap bitmap = frames.get(a);
                if (bitmap != null) {
                    int x = dp(16) + offset * (isRoundFrames ? frameWidth / 2 : frameWidth);
                    int y = dp(2);
                    if (isRoundFrames) {
                        rect2.set(x, y, x + dp(28), y + dp(28));
                        canvas.drawBitmap(bitmap, rect1, rect2, null);
                    } else {
                        canvas.drawBitmap(bitmap, x, y, null);
                    }
                }
                offset++;
            }
        }

        int top = dp(2);

        canvas.drawRect(dp(16), top, startX, getMeasuredHeight() - top, paint2);
        canvas.drawRect(endX + dp(4), top, dp(16) + width + dp(4), getMeasuredHeight() - top, paint2);

        canvas.drawRect(startX, 0, startX + dp(2), getMeasuredHeight(), paint);
        canvas.drawRect(endX + dp(2), 0, endX + dp(4), getMeasuredHeight(), paint);
        canvas.drawRect(startX + dp(2), 0, endX + dp(4), top, paint);
        canvas.drawRect(startX + dp(2), getMeasuredHeight() - top, endX + dp(4), getMeasuredHeight(), paint);
        canvas.restore();

        canvas.drawCircle(startX + dp(1), getMeasuredHeight() / 2, dp(7), paint);
        canvas.drawCircle(endX + dp(3), getMeasuredHeight() / 2, dp(7), paint);
    }
}
