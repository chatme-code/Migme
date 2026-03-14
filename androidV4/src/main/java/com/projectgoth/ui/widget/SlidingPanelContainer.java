package com.projectgoth.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.nineoldandroids.view.ViewHelper;
import com.projectgoth.R;

import java.util.ArrayList;

// To use this class, make the container a SlidingPanelContainer instead of a FrameLayout
//
// Add:
//   xmlns:migme="http://schemas.android.com/apk/res/com.projectgoth"
//
// Make sure that the panels are declared before the main view in the xml,
// and then tag the panel views with a migme:layout_panelLocation attribute.
//
// Valid values for layout_panelLocation are "left", "right", "top", "bottom"
//

public class SlidingPanelContainer extends FrameLayout  {

    // This represents the position of the main visible fragments.
    // It is negative when pulling a fragment from the right/bottom, and positive when from left/top
    private float                currentX           = 0.0f;
    private float                currentY           = 0.0f;

    private float                velocityX          = 0.0f;
    private float                velocityY          = 0.0f;
    private float                finalVelocityX     = 0.0f;
    private float                finalVelocityY     = 0.0f;
    private float                accelerationX      = 0.0f;
    private float                accelerationY      = 0.0f;
    private float                targetX            = 0.0f;
    private float                targetY            = 0.0f;
    private long                 lastUpdateTime     = 0;

    private VelocityTracker      velocityTracker;
    private InputInterceptView   interceptView;
    private LayoutDirection      activePanelDirection           = null;
    private LayoutDirection      pendingActivePanelDirection    = null;
    private LayoutDirection      animationDirection             = null;
    private View                 swipeView                      = null;

    private SlidingPanelContainerListener   listener = EMPTY_LISTENER;

    private final PositionAnimator     positionAnimator         = new PositionAnimator();
    private final StartShowPanelMotion startShowPanelMotion     = new StartShowPanelMotion();

    private static final float   ANIMATION_DURATION     = 0.3f;
    private static final int     MAX_ALPHA              = 128;
    private static final int     SHADOW_WIDTH           = 60;
    private static final int     SHADOW_COLOR           = 0x30000000;
    private static final long    ANIMATION_UPDATE_DELAY = 16;           // 60fps = 16ms per update.

    public interface SlidingPanelContainerListener
    {
        void onPanelOpen(View view, LayoutDirection direction);
        void onPanelClosed(View view, LayoutDirection direction);
    }

    private static final SlidingPanelContainerListener EMPTY_LISTENER = new SlidingPanelContainerListener() {
        @Override public void onPanelOpen(View view, LayoutDirection direction) { }
        @Override public void onPanelClosed(View view, LayoutDirection direction) { }
    };

    public enum LayoutDirection
    {
        UNSPECIFIED(-1, 0, 0, -1),
        LEFT(0, -1, 0, 1),
        RIGHT(1, 1, 0, 0),
        TOP(2, 0, -1, 3),
        BOTTOM(3, 0, 1, 2),
        STATIC(4, 0, 0, 4)
        ;

        int value;
        int xFactor;
        int yFactor;
        int opposite;

        LayoutDirection(int value, int xFactor, int yFactor, int oppositeValue) {
            this.value = value;
            this.xFactor = xFactor;
            this.yFactor = yFactor;
            this.opposite = oppositeValue;
        }

        public int getValue()   { return value; }
        public int getXFactor() { return xFactor; }
        public int getYFactor() { return yFactor; }
        public LayoutDirection getOpposite() { return fromValue(opposite); }

        public static LayoutDirection fromValue(int value) {
            for(LayoutDirection ld : values()) {
                if(ld.value == value) return ld;
            }
            return null;
        }
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        LayoutDirection direction = LayoutDirection.UNSPECIFIED;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SlidingPanelContainer_LayoutParams);

            int layoutDirectionValue = a.getInt(R.styleable.SlidingPanelContainer_LayoutParams_layout_panelLocation, LayoutDirection.UNSPECIFIED.getValue());
            direction = LayoutDirection.fromValue(layoutDirectionValue);

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, LayoutDirection layoutDirection) {
            super(width, height);
            this.direction = layoutDirection;
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(FrameLayout.LayoutParams p) {
            super(p);
        }
    }

    private enum InterceptState {
        YET_TO_DETERMINE,
        CAPTURE,
        FORWARD
    }

    private class InputInterceptView extends View {

        private InterceptState interceptState = InterceptState.YET_TO_DETERMINE;
        private ArrayList<byte[]> queuedEvents = new ArrayList<byte[]>();

        private float lastX;
        private float lastY;

        private static final float MOTION_THRESHOLD_RADIUS = 10.0f;
        private static final long FORWARD_EVENTS_DELAY = 300;

        ForwardDelay forwardDelay = new ForwardDelay();

        InputInterceptView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(queuedEvents.isEmpty()) {
                    if(isPotentialPanelSwipe(event)) {
                        interceptState = InterceptState.YET_TO_DETERMINE;
                        lastX = event.getX();
                        lastY = event.getY();
                        queueEvent(event);
                        postDelayed(forwardDelay, FORWARD_EVENTS_DELAY);
                    } else {
                        interceptState = InterceptState.FORWARD;
                        forwardMotionEvent(event);
                    }
                } else if(interceptState == InterceptState.YET_TO_DETERMINE) {
                    // If it's non empty, then we've had two presses now
                    forwardQueuedEvents();
                    forwardMotionEvent(event);
                }
                return true;

            case MotionEvent.ACTION_UP:
                switch(interceptState) {
                case FORWARD:
                    forwardMotionEvent(event);
                    break;

                case CAPTURE:
                    endMotion();
                    break;

                case YET_TO_DETERMINE:
                    forwardQueuedEvents();
                    forwardMotionEvent(event);
                    break;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                switch(interceptState) {

                case CAPTURE:
                    {
                        float currentX = event.getX();
                        float currentY = event.getY();
                        float dx = currentX - lastX;
                        float dy = currentY - lastY;
                        updateMotion(dx, dy, event);
                        lastX = currentX;
                        lastY = currentY;
                    }
                    break;

                case YET_TO_DETERMINE:
                    {
                        float currentX = event.getX();
                        float currentY = event.getY();

                        float dx = currentX - lastX;
                        float dy = currentY - lastY;
                        if(dx*dx + dy*dy >= MOTION_THRESHOLD_RADIUS*MOTION_THRESHOLD_RADIUS) {
                            final LayoutDirection direction = Math.abs(dx) > Math.abs(dy) ?
                                            (dx > 0 ? LayoutDirection.LEFT : LayoutDirection.RIGHT) :
                                            (dy > 0 ? LayoutDirection.TOP : LayoutDirection.BOTTOM);

                            if(beginMotion(direction, dx, dy, event)) {
                                interceptState = InterceptState.CAPTURE;
                                queuedEvents.clear();
                                lastX = currentX;
                                lastY = currentY;
                                removeCallbacks(forwardDelay);
                            } else {
                                forwardQueuedEvents();
                                forwardMotionEvent(event);
                            }
                        } else {
                            queueEvent(event);
                        }
                    }
                    break;

                case FORWARD:
                    forwardMotionEvent(event);
                    break;
                }
                return true;

            default:
                return forwardMotionEvent(event);
            }
        }

        private void queueEvent(MotionEvent event) {
            // event structures are reused/modified, so we have to parcel them in order to replay them later.
            Parcel parcel = Parcel.obtain();
            event.writeToParcel(parcel, 0);
            queuedEvents.add(parcel.marshall());
            parcel.recycle();
        }

        private void forwardQueuedEvents() {
            interceptState = InterceptState.FORWARD;
            removeCallbacks(forwardDelay);
            for(byte[] data : queuedEvents) {
                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(data, 0, data.length);
                parcel.setDataPosition(0);
                MotionEvent event = MotionEvent.CREATOR.createFromParcel(parcel);
                parcel.recycle();
                forwardMotionEvent(event);
                event.recycle();
            }
            queuedEvents.clear();
        }

        @Override
        public void onDraw(Canvas canvas) {
            drawInterceptView(canvas);
        }

        private class ForwardDelay implements Runnable {
            @Override public void run() {
                forwardQueuedEvents();
            }
        }
    }

    private View findViewWithDirection(LayoutDirection direction) {
        int numberOfChildren = getChildCount();
        for(int i = 0; i < numberOfChildren; ++i) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if(layoutParams.direction == direction) return child;
        }
        return null;
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        FrameLayout.LayoutParams p = super.generateDefaultLayoutParams();
        if(p != null) {
            return new LayoutParams(p);
        }
        return null;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public void addView (View child, int index, ViewGroup.LayoutParams params) {
        // This is to ensure that out InputInterceptView is always on top
        if(index < 0) index = getChildCount()-1;

        super.addView(child, index, params);
        LayoutParams layoutParams = (LayoutParams) params;
        if(layoutParams.direction != LayoutDirection.UNSPECIFIED) {
            if(layoutParams.gravity == -1) {
                // This is to fix margins not being processed on older versions.
                // For more information, see:
                // http://stackoverflow.com/questions/5401952/framelayout-margin-not-working
                layoutParams.gravity = Gravity.FILL;
            }
            child.setVisibility(View.GONE);
        }
    }

    public SlidingPanelContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        interceptView = new InputInterceptView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, LayoutDirection.STATIC);
        super.addView(interceptView, 0, layoutParams);
    }

    public void setListener(SlidingPanelContainerListener listener) {
        this.listener = listener == null ? EMPTY_LISTENER : listener;
    }

    public void setSwipeView(View view) { swipeView = view; }

    private boolean beginMotion(LayoutDirection direction, float dx, float dy, MotionEvent event) {
        if(activePanelDirection == null) {
            View panelView = findViewWithDirection(direction);
            if(panelView == null) return false;

            activePanelDirection = direction;
            pendingActivePanelDirection = null;
            panelView.setVisibility(View.VISIBLE);
            listener.onPanelOpen(panelView, direction);
        } else {
            if(direction != activePanelDirection.getOpposite()) return false;
        }
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.clear();
        removeCallbacks(positionAnimator);
        updateMotion(dx, dy, event);
        return true;
    }

    private void updateMotion(float dx, float dy, MotionEvent event) {

        if(event != null) velocityTracker.addMovement(event);

        View panelView = findViewWithDirection(activePanelDirection);

        switch(activePanelDirection) {
        case BOTTOM:
            {
                currentY += dy;
                float useY = Math.max(-panelView.getHeight(), Math.min(currentY, 0));
                setChildTranslation(0, useY, 0, (panelView.getHeight()+useY)/4);
            }
            break;
        case TOP:
            {
                currentY += dy;
                float useY = Math.max(0, Math.min(currentY, panelView.getHeight()));
                setChildTranslation(0, useY, 0, (useY-panelView.getHeight())/4);
            }
            break;
        case LEFT:
            {
                currentX += dx;
                float useX = Math.max(0, Math.min(currentX, panelView.getWidth()));
                setChildTranslation(useX, 0, (useX-panelView.getWidth())/4, 0);
            }
            break;
        case RIGHT:
            {
                currentX += dx;
                float useX = Math.max(-panelView.getWidth(), Math.min(currentX, 0));
                setChildTranslation(useX, 0, (panelView.getWidth()+useX)/4, 0);
            }
            break;
        default:
            break;
        }
        interceptView.invalidate();
    }

    private void endMotion() {
        velocityTracker.computeCurrentVelocity(1000, 20000);
        velocityX = velocityTracker.getXVelocity();   // This is number of pixels per second (1000 parameter above)
        velocityY = velocityTracker.getYVelocity();   // This is number of pixels per second (1000 parameter above)
        velocityTracker.clear();
        velocityTracker.recycle();


        // Calculate displacement given the velocity.
        //
        // Assuming linear deceleration slowdown,
        //
        //   s = 0.5(u+v)t
        //
        // Using v = 0, t = ANIMATION_DURATION in seconds:
        //
        // => s = 0.5ut

        float sx = 0.5f * ANIMATION_DURATION * velocityX;
        float sy = 0.5f * ANIMATION_DURATION * velocityY;

        View panelView = findViewWithDirection(activePanelDirection);

        targetX = 0;
        targetY = 0;

        switch (activePanelDirection) {
        case TOP:
        case BOTTOM:
            {
                int yFactor = activePanelDirection.getYFactor();
                float testY = currentY*yFactor;

                // Panel is already fully hidden
                if(testY >= 0) {
                    currentY = 0;
                    panelView.setAnimation(null);
                    panelView.setVisibility(View.GONE);
                    listener.onPanelClosed(panelView, activePanelDirection);
                    activePanelDirection = null;
                    return;
                }

                // Panel is fully visible
                int height = panelView.getHeight();
                if(-testY >= height) {
                    currentX = height * -yFactor;
                    return;
                }

                // Panel is partially visible.
                // Determine whether it is more visible or hidden, taking velocity into account
                velocityX = 0;
                if((sy + currentY) * -yFactor >= height/2) {
                    targetY = height * -yFactor;
                    animationDirection = activePanelDirection.getOpposite();
                } else {
                    animationDirection = activePanelDirection;
                }
            }
            break;

        case LEFT:
        case RIGHT:
            {
                int xFactor = activePanelDirection.getXFactor();
                float testX = currentX*xFactor;

                // Panel is already fully hidden
                if(testX >= 0) {
                    currentX = 0;
                    panelView.setAnimation(null);
                    panelView.setVisibility(View.GONE);
                    listener.onPanelClosed(panelView, activePanelDirection);
                    activePanelDirection = null;
                    return;
                }

                // Panel is fully visible
                int width = panelView.getWidth();
                if(-testX >= width) {
                    currentX = width * -xFactor;
                    return;
                }

                // Panel is partially visible.
                // Determine whether it is more visible or hidden, taking velocity into account
                velocityY = 0;
                if((sx + currentX) * -xFactor >= width/2) {
                    targetX = width * -xFactor;
                    animationDirection = activePanelDirection.getOpposite();
                } else {
                    animationDirection = activePanelDirection;
                }
            }
            break;
        default:
            break;
        }

        startMotionUpdate();
    }

    private void startMotionUpdate() {
        // s = 0.5(u+v)t
        // v = 2s/t-u
        finalVelocityX = 2*(targetX-currentX)/ANIMATION_DURATION - velocityX;
        finalVelocityY = 2*(targetY-currentY)/ANIMATION_DURATION - velocityY;
        accelerationX = (finalVelocityX - velocityX)/ANIMATION_DURATION;
        accelerationY = (finalVelocityY - velocityY)/ANIMATION_DURATION;

        if(accelerationX * animationDirection.getXFactor() <= 0) accelerationX = 0;
        if(accelerationY * animationDirection.getYFactor() <= 0) accelerationY = 0;

        lastUpdateTime = System.currentTimeMillis();
        postDelayed(positionAnimator, ANIMATION_UPDATE_DELAY);

    }

    private class StartShowPanelMotion implements Runnable {
        @Override public void run() {
            targetX   = 0;
            targetY   = 0;
            velocityX = 0;
            velocityY = 0;
            
            final View panelView = findViewWithDirection(activePanelDirection);
            if(panelView == null) {
                activePanelDirection = null;
                animationDirection = null;
            } else {
                targetX = -activePanelDirection.getXFactor()*panelView.getWidth();
                targetY = -activePanelDirection.getYFactor()*panelView.getHeight();
                startMotionUpdate();
            }
        }
    }

    public void showPanel(LayoutDirection direction) {
        if(activePanelDirection == direction) {
            // We're already showing the requested direction!
            return;
        }
        if(activePanelDirection != null) {
            // A different panel is showing. close it first.
            targetX = 0;
            targetY = 0;
            velocityX = 0;
            velocityY = 0;
            pendingActivePanelDirection = direction;
            animationDirection = activePanelDirection;
            startMotionUpdate();
        } else {
            // Show the requested panel
            activePanelDirection = direction;
            animationDirection = activePanelDirection.getOpposite();
            final View panelView = findViewWithDirection(direction);
            panelView.setVisibility(View.VISIBLE);

            listener.onPanelOpen(panelView, direction);

            post(startShowPanelMotion);
        }
    }

    private class PositionAnimator implements Runnable {
        @Override public void run() {
            long newTime = System.currentTimeMillis();
            float dt = (newTime - lastUpdateTime) * 0.001f;
            lastUpdateTime = newTime;

            velocityX += accelerationX * dt;
            velocityY += accelerationY * dt;
            float dx = velocityX * dt;
            float dy = velocityY * dt;
            updateMotion(dx, dy, null);

            boolean needsUpdate = false;
            View panelView = findViewWithDirection(activePanelDirection);
            switch(animationDirection) {
            case TOP:
                needsUpdate = currentY > targetY;
                break;

            case BOTTOM:
                needsUpdate = currentY < targetY;
                break;

            case LEFT:
                needsUpdate = currentX > targetX;
                break;

            case RIGHT:
                needsUpdate = currentX < targetX;
                break;
            default:
                break;
            }

            if(needsUpdate) {
                postDelayed(positionAnimator, ANIMATION_UPDATE_DELAY);
            } else {
                currentX = targetX;
                currentY = targetY;

                // If the animation direction is the same as the side of the panel,
                // then we have just closed it.
                if(animationDirection == activePanelDirection) {
                    panelView.setAnimation(null);
                    panelView.setVisibility(View.GONE);

                    listener.onPanelClosed(panelView, activePanelDirection);

                    activePanelDirection = null;
                }

                if(pendingActivePanelDirection != null) {
                    final View newPanelView = findViewWithDirection(pendingActivePanelDirection);
                    newPanelView.setVisibility(View.VISIBLE);

                    activePanelDirection = pendingActivePanelDirection;
                    pendingActivePanelDirection = null;
                    animationDirection = activePanelDirection.getOpposite();

                    listener.onPanelOpen(panelView, activePanelDirection);

                    post(startShowPanelMotion);
                }
            }
        }
    }

    private void setChildTranslation(float dx, float dy, float panelDx, float panelDy) {
        int numberOfChildren = getChildCount();
        for(int i = 0; i < numberOfChildren; ++i) {
            View child = getChildAt(i);

            if(child.getVisibility() != View.VISIBLE) continue;

            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if(layoutParams.direction == activePanelDirection) {
                ViewHelper.setTranslationX(child, panelDx);
                ViewHelper.setTranslationY(child, panelDy);
            } else if(layoutParams.direction == LayoutDirection.UNSPECIFIED) {
                ViewHelper.setTranslationX(child, dx);
                ViewHelper.setTranslationY(child, dy);
            }
        }
    }

    private void drawInterceptView(Canvas canvas) {
        if(activePanelDirection == null) return;

        Paint p = new Paint();
        View panelView = findViewWithDirection(activePanelDirection);

        switch(activePanelDirection) {
        case TOP:
            {
                int height = panelView.getHeight();
                if(height == 0) height = 1;
                float useY = Math.max(0, Math.min(currentY, height));
                int alpha = (int) (height-useY) * MAX_ALPHA / height;
                float bottom = getTop() + useY;
                if(alpha != 0) {
                    p.setColor(Color.argb(alpha, 0, 0, 0));
                    canvas.drawRect(0, getTop(), getRight(), bottom, p);
                }
                p.setColor(Color.BLACK);
                p.setShader(new LinearGradient(0, bottom, 0, bottom-SHADOW_WIDTH, SHADOW_COLOR, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(0, bottom-SHADOW_WIDTH, getRight(), bottom, p);
            }
            break;

        case BOTTOM:
            {
                int height = panelView.getHeight();
                if(height == 0) height = 1;
                float useY = Math.max(-height, Math.min(currentY, 0));
                int alpha = (int) (height+useY) * MAX_ALPHA / height;
                float top = getBottom() + useY;
                if(alpha != 0) {
                    p.setColor(Color.argb(alpha, 0, 0, 0));
                    canvas.drawRect(0, top, getRight(), getBottom(), p);
                }
                p.setColor(Color.BLACK);
                p.setShader(new LinearGradient(0, top, 0, top+SHADOW_WIDTH, SHADOW_COLOR, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(0, top, getRight(), top+SHADOW_WIDTH, p);
            }
            break;

        case LEFT:
            {
                int width = panelView.getWidth();
                if(width == 0) width = 1;
                float useX = Math.max(0, Math.min(currentX, width));
                int alpha = (int) (width-useX) * MAX_ALPHA / width;
                float right = getLeft() + useX;
                if(alpha != 0) {
                    p.setColor(Color.argb(alpha, 0, 0, 0));
                    canvas.drawRect(getLeft(), 0, right, getBottom(), p);
                }
                p.setColor(Color.BLACK);
                p.setShader(new LinearGradient(right, 0, right-SHADOW_WIDTH, 0, SHADOW_COLOR, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(right-SHADOW_WIDTH, 0, right, getBottom(), p);
            }
            break;
        
        case RIGHT:
            {
                int width = panelView.getWidth();
                if(width == 0) width = 1;
                float useX = Math.max(-width, Math.min(currentX, 0));
                int alpha = (int) (width+useX) * MAX_ALPHA / width;
                float left = getRight() + useX;
                if(alpha != 0) {
                    p.setColor(Color.argb(alpha, 0, 0, 0));
                    canvas.drawRect(left, 0, getRight(), getBottom(), p);
                }
                p.setColor(Color.BLACK);
                p.setShader(new LinearGradient(left, 0, left+SHADOW_WIDTH, 0, SHADOW_COLOR, 0, Shader.TileMode.CLAMP));
                canvas.drawRect(left, 0, left+SHADOW_WIDTH, getBottom(), p);
            }
            break;
        default:
                break;
        }
    }

    private boolean dispatchEventToTranslatedView(View v, MotionEvent event) {
        if(v.getVisibility() != View.VISIBLE) return false;

        float x = event.getX();
        float tx = ViewHelper.getTranslationX(v);
        float left = v.getLeft() + tx;
        if(x < left) return false;

        float right = v.getRight() + tx;
        if(x > right) return false;

        float y = event.getY();
        float ty = ViewHelper.getTranslationY(v);
        float top = v.getTop() + ty;
        if(y < top) return false;

        float bottom = v.getBottom() + ty;
        if(y > bottom) return false;

        event.offsetLocation(-left, -top);
        boolean result = v.dispatchTouchEvent(event);
        event.offsetLocation(left, top);
        return result;
    }

    private boolean isPotentialPanelSwipe(MotionEvent event) {
        if(swipeView != null && activePanelDirection == null) {

            int[] swipeViewLocation = new int[2];
            int[] interceptViewLocation = new int[2];
            swipeView.getLocationOnScreen(swipeViewLocation);
            interceptView.getLocationOnScreen(interceptViewLocation);

            float x = event.getX() + interceptViewLocation[0] - swipeViewLocation[0];
            float y = event.getY() + interceptViewLocation[1] - swipeViewLocation[1];
            return x >= 0 && y >= 0 && x < swipeView.getWidth() && y < swipeView.getHeight();
        }
        return true;
    }

    private boolean forwardMotionEvent(MotionEvent event) {
        if(activePanelDirection != null) {
            View v = findViewWithDirection(activePanelDirection);
            return dispatchEventToTranslatedView(v, event);
        } else {

            int numberOfChildren = getChildCount();

            // Use -2 as we know our intercept view is always the last one.
            for(int i = numberOfChildren - 2; i >= 0; --i) {
                View v = getChildAt(i);
                if(dispatchEventToTranslatedView(v, event)) {
                    return true;
                }
            }
            return false;
        }

    }

}
