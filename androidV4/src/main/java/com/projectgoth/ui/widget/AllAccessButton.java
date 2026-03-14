/**
 * Copyright (c) migme 2014
 *
 * AllAccessButton.java
 * Created Aug 27, 2014, 1:49:28 PM
 */
package com.projectgoth.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.NUEManager;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.BaseFragmentActivity;
import com.projectgoth.ui.activity.MenuBarAnimation;
import com.projectgoth.ui.activity.MenuBarAnimation.AnimationType;
import com.projectgoth.ui.adapter.InfiniteViewPagerAdapter;
import com.projectgoth.ui.animation.PulseAnimation;
import com.projectgoth.ui.animation.ReversibleInterpolator;
import com.projectgoth.ui.animation.RotateAndTranslateAnimation;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.listener.DispatchTouchEventBroadcaster;
import com.projectgoth.ui.listener.DispathTouchListener;
import com.projectgoth.ui.widget.allaccessbutton.CollapsibleLayout;
import com.projectgoth.ui.widget.allaccessbutton.CollapsibleLayout.ItemsState;
import com.projectgoth.ui.widget.allaccessbutton.ContextAction;
import com.projectgoth.ui.widget.allaccessbutton.ContextActionListener;
import com.projectgoth.ui.widget.allaccessbutton.ContextActionsLayout;
import com.projectgoth.ui.widget.allaccessbutton.NavigationActionsLayout;
import com.projectgoth.ui.widget.allaccessbutton.NavigationSelectorAnimator;
import com.projectgoth.ui.widget.allaccessbutton.OffsetLayoutParams;
import com.projectgoth.ui.widget.allaccessbutton.OnItemsStateChangedListener;
import com.projectgoth.ui.widget.allaccessbutton.PageData;
import com.projectgoth.ui.widget.tooltip.ToolTip;
import com.projectgoth.ui.widget.tooltip.ToolTipRelativeLayout;
import com.projectgoth.ui.widget.tooltip.ToolTipRelativePositionEnum;
import com.projectgoth.ui.widget.tooltip.ToolTipView;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.AnimUtils;
import com.projectgoth.util.MathUtils;

/**
 * @author angelorohit
 *
 */
public class AllAccessButton extends ViewGroup
    implements OnItemsStateChangedListener, ContextActionListener, DispathTouchListener, ToolTipView.OnToolTipViewClickedListener {
	
	/**
	 * The maximum amount of time from down-press to up action on the main button that will allow the 
	 * context action menu to be expanded or collapsed. 
	 * Any longer duration will not expand / collapse the context action menu.
	 */
	private static final long CONTEXT_ACTIONS_DISPLAY_DURATION_THRESHOLD = 250; // In ms

    private static final String          LOG_TAG                      = AndroidLogger.makeLogTag(AllAccessButton.class);
	
	private InfiniteViewPager viewPager;
	private ImageView mainButton;
	private View leftChevron;
	private View rightChevron;
	private View navigationSelector;
	private ViewGroup pulseView;
	
	private ContextActionsLayout contextLayout;
	private NavigationActionsLayout navigationLayout;
    
	private static final int NAVIGATE_LEFT_ACTION_ID = 0;
	private static final int NAVIGATE_RIGHT_ACTION_ID = 1;
	
	private final ContextAction leftNavigationAction = new ContextAction(NAVIGATE_LEFT_ACTION_ID, 0, this);
    
	private final ContextAction rightNavigationAction = new ContextAction(NAVIGATE_RIGHT_ACTION_ID, 0, this);
	
    private final List<ContextAction> navigationActions = new ArrayList<ContextAction>();
    
	// ViewPager drag related variables
    private final static int MIN_DRAG_LENGTH = 10;
    private final static int SCROLL_THRESHOLD = 3;
    private final static float SNAP_NORMALIZED_DISTANCE = 0.5f;     // Snap to next page when half way there
    private final static float SNAP_THRESHOLD = 10;
    
    private final static int SNAP_ANIMATION_LENGTH = 200;
    private final static int SELECTOR_INITIAL_ANIMATION_LENGTH = 100;
    private final static int PLUS_ANIMATION_LENGTH = 200;
    private final static int ANIMATION_START_DELAY = 200;
    private final static int PULSE_ANIMATION_LENGTH = 660;
    private final static int PULSE_ANIMATION_DELAY = 300;

    private float startXPos = 0f;
    private float startDragXPos = 0f;
    private float startDragYPos = 0f;
    private float viewPagerNormalizedScrollPosition = 0f;
    private float maxDrag = 1f;
    private float maxFakeDrag = 1f;
    private float fakeDragFactor = 1f;
    private boolean skipActionUpStateToggle = false;
    private boolean allowDrag = false;
    private int dragToPageIndex = 1;
    private float snappingFrom = 0;
    private float snappingTo = 0;
    private float snappingSelectorTo = 0;
    private float exitSnapDistance = 0;
    private long previousDragTime = 0;
    private float previousXPos = 0;
    
    enum SnapType {
        NONE,
        INITIAL,
        SNAP_TO_END,
        SNAP_OFF
    }
    private SnapType snapType = SnapType.NONE;

    // Scrolling variables
    private final static int MIN_SCROLL_LENGTH = 100;
    private final static int MIN_SCROLL_SPEED = 15;
    
    // A value high enough to avoid constantly hiding/showing. Using MAX_VALUE will cause integer overflow
    private final static int HIGH_SCROLL_VALUE = Integer.MAX_VALUE / 2;
    protected boolean trackVerticalScroll = false;
    protected boolean isVerticalScroll = false;
    protected int startScrollX = 0;
    protected int lastScrollY = 0;
    protected int minScrollY = 0;
    protected int maxScrollY = 0;
    protected int scrollOffset = 0;
    
    private static final int DIM_ANIMATION_LENGTH = 200;
	
	/**
	 * The drawing rect of the main button to be hit tested against later.
	 */
	private final Rect mainButtonDrawingRect = new Rect();
    private final Rect mainButtonGlobalRect = new Rect();
    
    private float bottomOffset = 0;
    private float mainButtonExpandedHalfSize = 0;
    private final Point centerOffset = new Point();

    private final MenuBarAnimation bottomMenuBarAnim = new MenuBarAnimation(this);
	
	// Variable used for deferred updates when the pager changes its selected page
	private int pagerSelectedIndex = -1;
	
	private int mainButtonNextImageId = -1;
	private Animation mainButtonBounceAnim = null;
	private Animation leftChevronAnimation = createLeftChevronAnimation();
	private Animation rightChevronAnimation = createRightChevronAnimation();
	private NavigationSelectorAnimator navigationSelectorAnimator = null;
	private final ValueAnimator snapPagerAnimator = ValueAnimator.ofFloat(0f, 1f);
	private final ValueAnimator snapSelectorAnimator = ValueAnimator.ofFloat(0f, 1f);

    private ToolTipView mToolTipView;
    private ToolTipRelativeLayout mToolTipFrameLayout;
    
    private final List<Animation> pulseAnimations = new ArrayList<Animation>();
	private int pulseRingAnimationOffset = 0;
	
	public AllAccessButton(Context context) {		
		super(context);
		init(context);
	}
	
	public AllAccessButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public AllAccessButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
	    navigationActions.add(leftNavigationAction);
	    navigationActions.add(rightNavigationAction);
	    
        if (context instanceof DispatchTouchEventBroadcaster) {
            DispatchTouchEventBroadcaster broadcaster = (DispatchTouchEventBroadcaster) context;
            broadcaster.addDispatchTouchListener(this);
        }
        
        bottomMenuBarAnim.setShowAnimation(AnimationType.SLIDE_FROM_BOTTOM);
        bottomMenuBarAnim.setHideAnimation(AnimationType.SLIDE_TO_BOTTOM);
        bottomMenuBarAnim.setShowLockLength(0);
        
        final View activityRootView = this;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                if (!bottomMenuBarAnim.isShown()) {
                    return;
                }
                
                if (isKeyboardVisible()) {
                    if (getVisibility() != INVISIBLE) {
                        setVisibility(INVISIBLE);
                    }
                } else if (getVisibility() != VISIBLE) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            setVisibility(VISIBLE);
                        }
                    });
                }
            }
        });

	}
	
	private boolean isKeyboardVisible() {
	    Rect r = new Rect();
        View v = AllAccessButton.this;
        v.getWindowVisibleDisplayFrame(r);
        int heightDiff = v.getRootView().getHeight() - (r.bottom - r.top);
        // if more than 100 pixels, its probably a keyboard...
        return (heightDiff > 100);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);

		maxFakeDrag = Config.getInstance().getScreenWidth();
		fakeDragFactor = maxFakeDrag / maxDrag;
		exitSnapDistance = maxDrag * SNAP_NORMALIZED_DISTANCE - Math.abs(SNAP_THRESHOLD);
	}
	
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new OffsetLayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new OffsetLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        Rect rect = new Rect();

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            OffsetLayoutParams params = (OffsetLayoutParams) child.getLayoutParams();
            
            child.measure(
                OffsetLayoutParams.getChildMeasureSpec(params.width, widthSize),
                OffsetLayoutParams.getChildMeasureSpec(params.height, heightSize));
            
            if (child instanceof CollapsibleLayout) {
                int childHalfWidth = child.getMeasuredWidth() / 2;
                int childHalfHeight = child.getMeasuredHeight() / 2;
                rect.union(params.x - childHalfWidth, params.y - childHalfHeight,
                           params.x + childHalfWidth, params.y + childHalfHeight);
            }
        }
        
        widthSize = getCorrectSize(widthMode, widthSize, rect.width());
        heightSize = getCorrectSize(heightMode, heightSize, rect.height());
        
        int yOffset = Math.max(rect.bottom, (int)bottomOffset);

        centerOffset.set(widthSize/2 - rect.centerX(), yOffset);
        
        setMeasuredDimension(widthSize, heightSize);
    }
    
    private static int getCorrectSize(int specMode, int specSize, int size) {
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                return Math.min(size, specSize);
            case MeasureSpec.EXACTLY:
                return specSize;
        }
        return size;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int centerX = centerOffset.x;
        int centerY = b - centerOffset.y - getPaddingBottom();  

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (child instanceof ToolTipRelativeLayout) {
                    child.layout(l, t, r, b);
                } else {
                    OffsetLayoutParams p = (OffsetLayoutParams) child.getLayoutParams();
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    int left = centerX + p.x - childWidth / 2;
                    int top = centerY + p.y - childHeight / 2;

                    child.layout(left, top, left + childWidth, top + childHeight);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Context context = getContext();
        if (context instanceof DispatchTouchEventBroadcaster) {
            DispatchTouchEventBroadcaster broadcaster = (DispatchTouchEventBroadcaster) context;
            broadcaster.removeDispatchTouchListener(this);
        }
        super.onDetachedFromWindow();
    }
    
	public void setViewPager(final InfiniteViewPager pager) {

		viewPager = pager;
		
		if (pager != null) {
    		pager.addOnPageChangeListener(new OnPageChangeListener() {
    		    private boolean isPreviewingActionBar = false;
    		    private boolean hasChangedPage = false;
    		    
    	        @Override
    	        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	            // This method is called within fakeDragBy(...) to indicate what the new scroll values are
    	            viewPagerNormalizedScrollPosition = positionOffset + (float)(position - 1);
    	        }
    
    			@Override
    	        public void onPageSelected(int position) {
    			    hasChangedPage = true;
    				updateContextMenuForFragmentAtSelectedIndex(position);
    	        }
    			
    	        @Override
    	        public void onPageScrollStateChanged(int state) {
    	            switch (state) {
    	            case ViewPager.SCROLL_STATE_IDLE:
    	                if (isPreviewingActionBar && !hasChangedPage) {
    	                    updateActionBarForItem(viewPager.getCurrentItem());
    	                }
                        isPreviewingActionBar = false;
                        hasChangedPage = false;
    	                break;
    	            case ViewPager.SCROLL_STATE_SETTLING:
    	                isPreviewingActionBar = dragToPageIndex != viewPager.getCurrentItem();
    	                hasChangedPage = false;
    	                break;
    	            }
    	        }
    		});

            updateContextMenuForFragmentAtSelectedIndex();
		}
	}
	
	private boolean canUpdateContextActions() {
	    return contextLayout.getItemsState() == ItemsState.COLLAPSED;
	}
	
	private boolean canUpdateNavigationActions() {
        return navigationLayout.getItemsState() == ItemsState.COLLAPSED;
    }
	
    public void updateContextMenuForFragmentAtSelectedIndex() {
        updateContextMenuForFragmentAtSelectedIndex(viewPager.getCurrentItem());
    }

	public void updateContextMenuForFragmentAtSelectedIndex(int selectedIndex) {
	    
	    // Check if we need to defer this update
	    if (!canUpdateContextActions() || !canUpdateNavigationActions()) {
	        // Defer context actions update
	        pagerSelectedIndex = selectedIndex;
	        return;
	    }
	    
        pagerSelectedIndex = -1;

		InfiniteViewPagerAdapter adapter = ((InfiniteViewPagerAdapter) viewPager.getAdapter());
		if (adapter != null) {
		    // Update context actions
			final List<BaseFragment> pagerFragments = adapter.getPagerFragments();
			PageData pageData = pagerFragments.get(selectedIndex).getPageData();
			if (pageData != null) {
			    contextLayout.setActions(pageData.getActions());
			}
			
			// Update navigation actions
			final int fragmentsCount = pagerFragments.size();
			int indexNext = (selectedIndex + 1) % fragmentsCount;
			int indexPrevious = (selectedIndex - 1 + fragmentsCount) % fragmentsCount;
			
			pageData = pagerFragments.get(indexNext).getPageData();
			if (pageData != null && pageData.getAccessButtonIcon() > 0) {
			    rightNavigationAction.imageResourceId = pageData.getAccessButtonIcon();
			}
			pageData = pagerFragments.get(indexPrevious).getPageData();
			if (pageData != null && pageData.getAccessButtonIcon() > 0) {
			    leftNavigationAction.imageResourceId = pageData.getAccessButtonIcon();
			}
			
			navigationLayout.setActions(navigationActions);
		}
	}

	protected void onFinishInflate() {

		contextLayout = (ContextActionsLayout) findViewById(R.id.context_layout);
		contextLayout.setItemsStateChangedListener(this);
		contextLayout.setItemClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
                mainButton.setEnabled(false);
	            collapseNavigation();
	            dimBackground(false);
	        }
	    });
		contextLayout.setAnimationListener(new CollapsibleLayout.AnimationListener() {
		    @Override
		    public void onClickAnimationEnd() {
		        mainButton.setEnabled(true);
		    }
		});
		
        navigationLayout = (NavigationActionsLayout) findViewById(R.id.navigation_layout);
        navigationLayout.setItemsStateChangedListener(this);
        navigationLayout.addAction(leftNavigationAction);
        navigationLayout.addAction(rightNavigationAction);
        maxDrag = navigationLayout.getRadius();

        mainButton = (ImageView) findViewById(R.id.main_button);
        mainButton.setClickable(true);
        mainButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	processMotionEvent(event);
            	return false;
            }
        });

        leftChevron = findViewById(R.id.left_chevron);
        rightChevron = findViewById(R.id.right_chevron);
        // In case the graphical glitches appear again, put the lines below back in
        //if (android.os.Build.VERSION.SDK_INT >= 19) {
        //    leftChevron.setLayerType(LAYER_TYPE_HARDWARE, null);
        //    rightChevron.setLayerType(LAYER_TYPE_HARDWARE, null);
        //}
        
        leftChevronAnimation.setAnimationListener(new AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                leftChevron.setVisibility(INVISIBLE);
                leftChevron.clearAnimation();
                mainButton.setImageResource(mainButtonNextImageId);
            }
        });
        rightChevronAnimation.setAnimationListener(new AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                rightChevron.setVisibility(INVISIBLE);
                rightChevron.clearAnimation();
            }
        });
        
        float collapsedSize = getResources().getDimension(R.dimen.all_access_main_button_dimens);
        float expandedSize = getResources().getDimension(R.dimen.all_access_main_button_expanded_dimens);
        mainButtonExpandedHalfSize = expandedSize / 2; 

        bottomOffset = (int)(mainButtonExpandedHalfSize + getResources().getDimension(R.dimen.large_margin));

        navigationSelector = findViewById(R.id.main_button_ring);
        
        navigationSelectorAnimator = new NavigationSelectorAnimator(navigationSelector, 0, 1);
        navigationSelectorAnimator.setCollapsedSize(collapsedSize);
        navigationSelectorAnimator.setExpandedSize(expandedSize);
        navigationSelectorAnimator.addListener(new AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {
                if (!((NavigationSelectorAnimator)animation).isReversing()) {
                    animateMainButton(true);
                }
            }
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {}            
            @Override public void onAnimationCancel(Animator animation) {}
        });
        
        snapPagerAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float dragOffset = MathUtils.lerp(snappingFrom, snappingTo, animation.getAnimatedFraction());
                fakeDragViewPager(dragOffset);
            }
        });
        snapSelectorAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float dragOffset = MathUtils.lerp(0, snappingSelectorTo, animation.getAnimatedFraction());
                updateNavigationSelector(dragOffset);
            }
        });
        snapSelectorAnimator.addListener(new AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                handleSelectionPulseAnimation();
            }
        });
        
        mainButtonBounceAnim = new ScaleAnimation(1f, 1.2f, 1f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mainButtonBounceAnim.setDuration(100);
        mainButtonBounceAnim.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return 1f - 2f * Math.abs(input - 0.5f);
            }
        });
        
        pulseView = (ViewGroup) findViewById(R.id.pulse_view);
        initializePulseAnimations();

        mToolTipFrameLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipframelayout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(NUEManager.getInstance().shouldShowNUE(LOG_TAG)) {
                    addFirstToolTipView();
                }
            }
        }, Constants.NUE_TOOLTIP_DELAY);
	}
	
	private boolean processMotionEvent(MotionEvent event) {
		// We use MotionEventCompat for backward compatibility.
		final int action = MotionEventCompat.getActionMasked(event);
		final int actionIndex = MotionEventCompat.getActionIndex(event);
		final float xPos = MotionEventCompat.getX(event, actionIndex);
        final float yPos = MotionEventCompat.getY(event, actionIndex);
        
	    switch(action) {
	        case MotionEvent.ACTION_DOWN:
	            if (viewPager.isScrolling()) {
	                skipActionUpStateToggle = true;
	                return false;
	            }
	            
	            // Take left and top into consideration as button may grow as we start dragging
	        	startDragXPos = xPos;
	        	startDragYPos = yPos;
	        	
	        	if (hitTestMainButton((int) xPos, (int) yPos, false)) {
	        	    allowDrag = true;
	        	    stopPulseAnimation();
    	        	final ItemsState state = navigationLayout.getItemsState();
    	        	if (state == ItemsState.EXPANDED || state == ItemsState.EXPANDING) {
    	        	    collapseContext();
    	        		skipActionUpStateToggle = true;
    	        	} else {
    	                expandNavigation();
    	        	}
                    mainButton.startAnimation(mainButtonBounceAnim);
	        	}
		        return true;
	        case MotionEvent.ACTION_MOVE:
	            if (viewPager.isFakeDragging()) {
	                fakeDragViewPagerToX(xPos);
                    previousDragTime = System.currentTimeMillis();
	                previousXPos = xPos;
	            } else if (allowDrag) {
	                float diffX = Math.abs(xPos - startDragXPos);
	                float diffY = Math.abs(yPos - startDragYPos);
	                if (diffX >= MIN_DRAG_LENGTH && diffX > diffY) {
	                    startXPos = xPos;
                        viewPagerNormalizedScrollPosition = 0f;
                        snappingFrom = 0;
                        snappingTo = 0;
                        snappingSelectorTo = 0;
                        snapType = SnapType.NONE;
                        dragToPageIndex = viewPager.getCurrentItem();
                        viewPager.beginFakeDrag();
                        previousDragTime = System.currentTimeMillis();
	                }
    	        }
	            return true;
	        case MotionEvent.ACTION_UP:
	        	if (!skipActionUpStateToggle) {
		        	if (!viewPager.isFakeDragging() &&
		        	    hitTestMainButton((int) xPos, (int) yPos, false) && 
	        			(event.getEventTime() - event.getDownTime()) < 
	        			CONTEXT_ACTIONS_DISPLAY_DURATION_THRESHOLD) {
		        	    
		        	    if (navigationLayout.getItemsState() == ItemsState.EXPANDING ||
		        	        navigationLayout.getItemsState() == ItemsState.EXPANDED) {
		        	        
		        	        expandContext();

		        	        // Remove the delay so that the animation starts right away.
	                        // Important: set delay to 1 because setting it to <= 0 will not start it. 
	                        navigationSelectorAnimator.setStartDelay(1);
		        	    }
		        	} else {
		        	    collapseNavigation();
		        	}
	        	} else {
	        	    collapseNavigation();
	        	}
	        	// Intentional fall-through!
	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_OUTSIDE:
	            if (viewPager.isFakeDragging()) {
	                snapPagerAnimator.cancel();
	                snapSelectorAnimator.cancel();
	                viewPager.endFakeDrag();
	            }
                skipActionUpStateToggle = false;
                allowDrag = false;
	            return true;      
	        default: 
	            return false;
	    }      
	}
	
	private void startSnapAnimation(float xPos, float from, float to, SnapType type) {
	    snapPagerAnimator.cancel();
        snappingFrom = from;
        snappingTo = to;
        snappingSelectorTo = to;
        snapType = type;
        
        handleSelectionPulseAnimation();
        
        float absoluteRemainingDistance = Math.abs(to-from);
        float draggingSpeed = Math.abs((xPos - previousXPos) / (System.currentTimeMillis() - previousDragTime));
        // Calculate animation's duration so that it moves 3 times faster than the user
        int duration = SNAP_ANIMATION_LENGTH;
        if (draggingSpeed > 0.001f) {
            duration = (int) (absoluteRemainingDistance / draggingSpeed * 0.33f);
            duration = Math.min(duration, SNAP_ANIMATION_LENGTH);
        }
        // If duration or distance are too short then snap directly without animation
        if (duration < 30 || absoluteRemainingDistance/maxDrag < 0.1f) {
            fakeDragViewPager(to);
            return;
        }

        snapPagerAnimator.setDuration(duration);
        snapPagerAnimator.start();
	}
	
    private void fakeDragViewPagerToX(float xPos) {
        float dragOffset = MathUtils.clamp(xPos - startXPos, -maxDrag, maxDrag);
        float normalizedDragOffset = dragOffset/maxDrag;
        if (normalizedDragOffset > SNAP_NORMALIZED_DISTANCE) {
            // Snap to next page
            if (snappingTo != maxDrag) {
                startSnapAnimation(xPos, dragOffset, maxDrag, SnapType.SNAP_TO_END);
            }
            return;
        } else if (normalizedDragOffset < -SNAP_NORMALIZED_DISTANCE) {
            // Snap to previous page
            if (snappingTo != -maxDrag) {
                startSnapAnimation(xPos, dragOffset, -maxDrag, SnapType.SNAP_TO_END);
            }
            return;
        } else {
            // Snap back to the user's touch position
            if (snapType == SnapType.SNAP_TO_END) {
                // In order to avoid flickering at SNAP_NORMALIZED_DISTANCE, only return 
                // from the snapped position if offset is below the snap exit distance
                if (dragOffset < exitSnapDistance && dragOffset > -exitSnapDistance) {
                    startSnapAnimation(xPos, snappingTo, dragOffset, SnapType.SNAP_OFF);
                }
                return;
            } else if (snapType == SnapType.SNAP_OFF && snapPagerAnimator.isRunning()) {
                // Update animation target value;
                snappingTo = dragOffset;
                return;
            }
        }
        
        fakeDragViewPager(dragOffset);
    }

    private void fakeDragViewPager(float dragOffset) {
        float newScrollPosition = dragOffset * fakeDragFactor;
        float currentScrollPosition = viewPagerNormalizedScrollPosition * maxFakeDrag;

        // Drag the view pager
        viewPager.fakeDragBy(currentScrollPosition - newScrollPosition);
        
        // Update the action bar if we've passed half a page
        if (dragOffset * 2 > maxDrag) {
            int nextIndex = (viewPager.getCurrentItem() + 1) % viewPager.getChildCount();
            if (dragToPageIndex != nextIndex) {
                dragToPageIndex = nextIndex;
                updateActionBarForItem(dragToPageIndex);
            }
        } else if (dragOffset * 2 < -maxDrag) {
            int previousIndex = (viewPager.getCurrentItem() + viewPager.getChildCount() - 1) % viewPager.getChildCount();
            if (dragToPageIndex != previousIndex) {
                dragToPageIndex = previousIndex;
                updateActionBarForItem(dragToPageIndex);
            }
        } else {
            int currentIndex = viewPager.getCurrentItem();
            if (dragToPageIndex != currentIndex) {
                dragToPageIndex = currentIndex;
                updateActionBarForItem(dragToPageIndex);
            }
        }

        if (snapSelectorAnimator.isRunning()) {
            // If selector is being animated, don't update it here, let the animator update it
            snappingSelectorTo = dragOffset;
        } else {
            // Don't animate the selector, move it directly to where it has to snap
            if (snapPagerAnimator.isRunning()) {
                dragOffset = snappingTo;
            }
            updateNavigationSelector(dragOffset);
        } 
    }
    
    private void updateNavigationSelector(float dragOffset) {
        // Move the ring along only if the navigation icons are fully expanded
        ItemsState state = navigationLayout.getItemsState();
        if (state == ItemsState.EXPANDING) {
            snappingSelectorTo = dragOffset;
        } else if (state == ItemsState.EXPANDED) {
            ViewHelper.setTranslationX(navigationSelector, dragOffset);
        } 
    }
    
    @Override
    public void onStateChanged(View v, ItemsState newState) {
        if (newState == ItemsState.COLLAPSED && pagerSelectedIndex != -1) {
            updateContextMenuForFragmentAtSelectedIndex(pagerSelectedIndex);
        }
        
        if (v == navigationLayout &&
            newState == ItemsState.EXPANDED) {
            
            // Don't animate if navigation buttons haven't fully expanded
            // Don't animate if user hasn't started dragging
            // Don't animate if dragging position hasn't moved from the start
            if (viewPager.isFakeDragging() &&
                snappingSelectorTo != 0) {
            
                if (snapType == SnapType.NONE) {
                    snapType = SnapType.INITIAL;
                }
                snapSelectorAnimator.setDuration(SELECTOR_INITIAL_ANIMATION_LENGTH);
                snapSelectorAnimator.start();
            }            
            
            // Also, dim the background
            dimBackground(true);
        }
    }
    
    @Override
    public void executeAction(int actionId) {
        switch (actionId) {
            case NAVIGATE_LEFT_ACTION_ID:
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                break;
            case NAVIGATE_RIGHT_ACTION_ID:
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                break;
            default:
                break;
        }
        collapseAll();
    }

    @Override
    public boolean onDispatchTouchEvent(MotionEvent event) {
        // How touch events work:
        //   Activity.dispatchTouchEvent
        //     [DispatchTouchListener.onDispatchTouchEvent]
        //     ViewGroup.dispatchTouchEvent
        //     ViewGroup.onInterceptTouchEvent
        //     ViewGroup.onTouchEvent
        
        boolean dispatched = false;

        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                {
                    trackVerticalScroll = false;
                    isVerticalScroll = false;
    
                    int pointerIndex = MotionEventCompat.getActionIndex(event);
                    int x = (int) MotionEventCompat.getX(event, pointerIndex);
                    int y = (int) MotionEventCompat.getY(event, pointerIndex);
    
                    if (!hitTestMainButton(x, y, true) &&
                        !contextLayout.hitTestButtons(x, y, true) &&
                        !navigationLayout.hitTestButtons(x, y, true)) {
                        
                        dispatched = !viewPager.isFakeDragging() &&
                                     (contextLayout.getItemsState() != ItemsState.COLLAPSED ||
                                      navigationLayout.getItemsState() != ItemsState.COLLAPSED);
    
                        collapseAll();
    
                        if (!dispatched &&
                            !viewPager.isFakeDragging()) {
    
                            trackVerticalScroll = true;
                            isVerticalScroll = false;
                            startScrollX = x;
                            lastScrollY = minScrollY = maxScrollY = y;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                trackVerticalScroll = false;
                isVerticalScroll = false;
                if (scrollOffset >= MIN_SCROLL_SPEED) {
                    show();
                } else if (scrollOffset <= -MIN_SCROLL_SPEED) {
                    hide();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (trackVerticalScroll) {
                    int pointerIndex = MotionEventCompat.getActionIndex(event);
                    int x = (int) MotionEventCompat.getX(event, pointerIndex);
                    int y = (int) MotionEventCompat.getY(event, pointerIndex);
                    int scrollX = Math.abs(x - startScrollX);

                    int offsetFromMin = y - minScrollY;
                    int offsetFromMax = y - maxScrollY;

                    if (!isVerticalScroll && scrollX > MIN_SCROLL_LENGTH) {
                        trackVerticalScroll = false;
                    } else {
                        scrollOffset = y - lastScrollY;
                        
                        if (scrollOffset >= SCROLL_THRESHOLD) {
                            // If scrollY is positive user is scrolling up
                            if (offsetFromMin > MIN_SCROLL_LENGTH) {
                                isVerticalScroll = true;
                                minScrollY = HIGH_SCROLL_VALUE;
                                show();
                            }
                            maxScrollY = y;
                        } else if (scrollOffset <= -SCROLL_THRESHOLD) {
                            // If scrollY is negative user is scrolling down
                            if (-offsetFromMax > MIN_SCROLL_LENGTH) {
                                isVerticalScroll = true;
                                maxScrollY = -HIGH_SCROLL_VALUE;
                                hide();
                            }
                            minScrollY = y;
                        }
                    }
                    lastScrollY = y;
                }
                break;
        }
        
        return dispatched;
    }

    private void show() {
        // Start pulse animation
        if (!bottomMenuBarAnim.isShown()) {
            startPulseAnimation(0f, 0, PULSE_ANIMATION_DELAY);
        }

        if (!isKeyboardVisible()) {
            BaseFragmentActivity activity = ApplicationEx.getInstance().getCurrentActivity();
            if (activity != null) {
                activity.getSupportActionBar().show();
            }
            bottomMenuBarAnim.show();
        }
    }
    
    private void hide() {
        stopPulseAnimation();
        
        if (!isKeyboardVisible()) {
            BaseFragmentActivity activity = ApplicationEx.getInstance().getCurrentActivity();
            if (activity != null) {
                activity.getSupportActionBar().hide();
            }

            bottomMenuBarAnim.hide();
        }
    }

    private void collapseAll() {
        collapseNavigation();
        collapseContext();
    }
    
    private void expandNavigation() {
        if (navigationLayout.expand(true)) {
            // Only expand ring if the navigation also has to expand
            expandNavigationSelector();
        }
    }

    private void collapseNavigation() {
        if (navigationLayout.collapse(true)) {
            // Only collapse ring if the navigation also has to collapse
            collapseNavigationSelector();
            animateMainButton(false);
            dimBackground(false);
        }
        stopPulseAnimation();
    }

    private void expandContext() {
        if (contextLayout.expand(true)) {
            dimBackground(true);
        }
    }

    private void collapseContext() {
        contextLayout.collapse(true);
    }
    
    private boolean isDimBackground = false;
    private void dimBackground(final boolean shouldDim) {
        TransitionDrawable transition = (TransitionDrawable) getBackground();
        if (shouldDim && !isDimBackground) {
            transition.startTransition(DIM_ANIMATION_LENGTH);
        } else if (!shouldDim && isDimBackground) {
            transition.reverseTransition(DIM_ANIMATION_LENGTH);
        }
        isDimBackground = shouldDim;
    }
    
    private void expandNavigationSelector() {
        int length = navigationLayout.getExpandAnimLength() - 50;
        navigationSelectorAnimator.cancel();
        navigationSelectorAnimator.setDuration(length);
        navigationSelectorAnimator.setStartDelay(ANIMATION_START_DELAY);
        navigationSelectorAnimator.start();
    }

    private void collapseNavigationSelector() {
        int length = navigationLayout.getCollapseAnimLength() - 50;
        navigationSelectorAnimator.cancel();
        navigationSelectorAnimator.setDuration(length);
        navigationSelectorAnimator.setStartDelay(viewPager.isFakeDragging()? 0 : length);
        navigationSelectorAnimator.reverse();
    }
    
    private boolean hitTestMainButton(int x, int y, boolean global) {
        Rect rect;
        if (global) {
            mainButton.getGlobalVisibleRect(mainButtonGlobalRect);
            rect = mainButtonGlobalRect;
        } else {
            mainButton.getDrawingRect(mainButtonDrawingRect);
            rect = mainButtonDrawingRect;
        }
        
        // Instead of testing for transparent pixel, we cheat by knowing the button shape is round
        int xOffset = x - rect.centerX();
        int yOffset = y - rect.centerY();
        int radius = rect.width() / 2;
        return xOffset * xOffset + yOffset * yOffset <= radius * radius; 
    }

    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addSecondToolTipView();
                NUEManager.getInstance().alreadyShownNUE(LOG_TAG);
            }
        }, Constants.NUE_TOOLTIP_DELAY);
    }


    private void addFirstToolTipView() {
        View customView = LayoutInflater.from(this.getContext()).inflate(R.layout.tooltip_title_subtitle, null);
        ((TextView)customView.findViewById(R.id.title)).setText(I18n.tr("Welcome!"));
        ((TextView)customView.findViewById(R.id.subtitle)).setText(I18n.tr("This is your navigation button."));
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip()
                        .withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW)
                        .withShadow(false),
                mainButton);
        mToolTipView.setOnToolTipViewClickedListener(this);
    }

    private void addSecondToolTipView() {
        View customView = LayoutInflater.from(this.getContext()).inflate(R.layout.tooltip_image_title_subtitle, null);

        ((TextView)customView.findViewById(R.id.title)).setText(I18n.tr("Swipe or tap"));
        ((ImageView)customView.findViewById(R.id.image1)).setImageResource(R.drawable.ad_swipe_white);
        ((ImageView)customView.findViewById(R.id.image2)).setImageResource(R.drawable.ad_tap_white);
        ((TextView)customView.findViewById(R.id.subtitle)).setText(I18n.tr("to hop between\nChat, Feed, and Discover."));
        mToolTipView = mToolTipFrameLayout.showToolTipForView(
                new ToolTip()
                        .withContentView(customView)
                        .withColor(ApplicationEx.getColor(R.color.edit_text_input_color))
                        .withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW)
                        .withShadow(false)
                        .withRelativePosition(ToolTipRelativePositionEnum.UP),
                mainButton);
        mToolTipView.setOnToolTipViewClickedListener(null);
    }
    
    private void updateActionBarForItem(int index) {
        InfiniteViewPagerAdapter adapter = ((InfiniteViewPagerAdapter) viewPager.getAdapter());
        if (adapter != null) {
            // Update context actions
            final List<BaseFragment> pagerFragments = adapter.getPagerFragments();
            ((BaseCustomFragmentActivity) getContext()).updateActionBarForFragment(pagerFragments.get(index));
        }
    }
    
    private void handleSelectionPulseAnimation() {
        if (snapType == SnapType.SNAP_TO_END && navigationLayout.getItemsState() == ItemsState.EXPANDED) {
            startPulseAnimation(snappingTo, -1, 0);
        } else if (snapType == SnapType.SNAP_OFF) {
            stopPulseAnimation();
        }
    }
    
    private void initializePulseAnimations() {
        if (UIUtils.hasHoneycomb()) {
            final int count = pulseView.getChildCount();
            final int animLength = 2 * PULSE_ANIMATION_LENGTH / count;
            pulseRingAnimationOffset = (PULSE_ANIMATION_LENGTH - animLength) / (count - 1);
            final int timeOffset = 2 * pulseRingAnimationOffset; 
            Animation pulseAnimation = null;
            for (int i = 0; i < count; i++) {
                pulseAnimation = new PulseAnimation();
                pulseAnimation.setDuration(animLength);
                pulseAnimation.setStartOffset(timeOffset);
                pulseAnimation.setFillAfter(true);
                pulseAnimations.add(pulseAnimation);
            }
            
            // Set last animation's listener 
            pulseAnimation.setAnimationListener(pulseLastAnimationListener);
        }
    }
    
    private void startPulseAnimation(float translationX, int loopCount, int delay) {
        if (UIUtils.hasHoneycomb()) {
            ViewHelper.setTranslationX(pulseView, translationX);
            pulseView.setVisibility(VISIBLE);

            final int count = pulseView.getChildCount();
            long animTime = pulseView.getDrawingTime() - pulseAnimations.get(0).getStartOffset() + delay;
            for (int i = 0; i < count; i++) {
                Animation pulseAnimation = pulseAnimations.get(i);
                pulseAnimation.setRepeatCount(loopCount < 0? Animation.INFINITE : loopCount);
                pulseAnimation.setStartTime(animTime);

                pulseView.getChildAt(i).setAnimation(pulseAnimation);
                animTime += pulseRingAnimationOffset;
            }
        }
    }
    
    private final AnimationListener pulseLastAnimationListener = new AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {}
        @Override public void onAnimationRepeat(Animation animation) {}
        
        @Override
        public void onAnimationEnd(Animation animation) {
            stopPulseAnimation();
        }
    };

    private void stopPulseAnimation() {
        if (UIUtils.hasHoneycomb()) {
            final int count = pulseView.getChildCount();
            for (int i = 0; i < count; i++) {
                View v = pulseView.getChildAt(i);
                v.clearAnimation();
            }

            pulseView.setVisibility(INVISIBLE);
        }
    }
    
    private void animateMainButton(boolean open) {

        ReversibleInterpolator interpolator;
        interpolator = (ReversibleInterpolator)leftChevronAnimation.getInterpolator();
        if (leftChevron.getAnimation() == null && interpolator.isReversed() != open) {
            return;
        }

        interpolator.reverse(!open);
        interpolator = (ReversibleInterpolator)rightChevronAnimation.getInterpolator();
        interpolator.reverse(!open);

        mainButtonNextImageId = open? R.drawable.ad_main_arrows_orange : R.drawable.ad_main_plus_white;
        mainButton.setImageDrawable(null);

        if (leftChevron.getAnimation() != null) {
            // Reverse animations
            if (leftChevronAnimation.hasStarted()) {
                // Normal animations can't reverse, so we need to reverse the iterator and
                // skip forward the animation so it reverses from the current frame.
                long dt = getDrawingTime();
                long timeToSkip = AnimUtils.getReversedStartTime(leftChevronAnimation,
                        dt, leftChevronAnimation.getDuration());
                leftChevronAnimation.setStartTime(timeToSkip);
                timeToSkip = AnimUtils.getReversedStartTime(rightChevronAnimation,
                        dt, rightChevronAnimation.getDuration());
                rightChevronAnimation.setStartTime(timeToSkip);
            }
            // TransitionDrawable will reverse correctly even if it's playing
            ((TransitionDrawable)mainButton.getBackground()).reverseTransition(PLUS_ANIMATION_LENGTH);
        } else {
            // Start animations
            leftChevron.startAnimation(leftChevronAnimation);
            rightChevron.startAnimation(rightChevronAnimation);
            if (open) {
                ((TransitionDrawable)mainButton.getBackground()).startTransition(PLUS_ANIMATION_LENGTH);
            } else {
                ((TransitionDrawable)mainButton.getBackground()).reverseTransition(PLUS_ANIMATION_LENGTH);
            }
        }
    }
    
    private Animation createLeftChevronAnimation() {
        // These values are hard-coded to match the assets
        return createChevronAnimation(-11f, 5.5f);
    }
    private Animation createRightChevronAnimation() {
        // These values are hard-coded to match the assets
        return createChevronAnimation(11f, -5.5f);
    }
        
    private Animation createChevronAnimation(float dpX, float dpY) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int offsetX = (int) Tools.getPixels(dm, dpX);
        int offsetY = (int) Tools.getPixels(dm, dpY);

        RotateAndTranslateAnimation animation = new RotateAndTranslateAnimation(0, offsetX, 0, offsetY, 0, 135);
        animation.setDuration(PLUS_ANIMATION_LENGTH);
        ReversibleInterpolator ri = new ReversibleInterpolator(animation.getInterpolator());
        ri.reverse(true);   // Set the interpolator as if we just had closed the button
        animation.setInterpolator(ri);
        animation.setFillAfter(true);
        return animation;
    }

}
