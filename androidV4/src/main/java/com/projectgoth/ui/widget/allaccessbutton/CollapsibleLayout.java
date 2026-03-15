package com.projectgoth.ui.widget.allaccessbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.animation.ReversibleInterpolator;
import com.projectgoth.util.AnimUtils;

import java.util.Iterator;
import java.util.List;

public abstract class CollapsibleLayout extends ViewGroup {

    public interface AnimationListener {
        public void onClickAnimationEnd();
    };

    public enum ItemsState {
        EXPANDING, EXPANDED, COLLAPSING, COLLAPSED 
    };

    //
    // Statics
    //
    protected static final int DEFAULT_ANGLE_TO_ARC_CENTER = 90;
    protected static final int DEFAULT_ARC_RADIUS = 70;
    protected static final int DEFAULT_ITEM_SIZE = 40;

    protected static final int DEFAULT_LAYOUT_PADDING = 0;
    protected static final int DEFAULT_LAYOUT_PADDING_BETWEEN_ITEMS = 0;
    
	protected static final int ITEM_CLICKED_ANIM_LENGTH = 300;		// In ms
	protected static final int ITEM_FADE_AWAY_ANIM_LENGTH = 200;	// In ms
	
    //
    // Finals
    //
    protected final Animation.AnimationListener showHideItemAnimationListener = new ShowHideItemAnimationListener();
	protected final Animation.AnimationListener itemClickAnimationListener = new ItemClickAnimationListener();
    protected final OnClickListener internalItemClickListener = new OnItemClickListener();
    
    protected final ReversibleInterpolator expandItemsInterpolator = new ReversibleInterpolator(null);
    protected final ReversibleInterpolator collapseItemsInterpolator = new ReversibleInterpolator(null);
	
	protected final Animation animItemClicked = initItemClickAnimations(ITEM_CLICKED_ANIM_LENGTH, true);
	protected final Animation animItemNotClicked = initItemClickAnimations(ITEM_FADE_AWAY_ANIM_LENGTH, false);

    private final Rect hitTestRect = new Rect();

    //
    // Member variables
    //
    protected float angleToArcCenter = (float) Math.toRadians(DEFAULT_ANGLE_TO_ARC_CENTER);
    protected int arcRadius = DEFAULT_ARC_RADIUS;		// Distance from the layout center to an item's center 
    protected int itemSize = DEFAULT_ITEM_SIZE;
    
    protected int layoutPadding = DEFAULT_LAYOUT_PADDING;
    protected int layoutPaddingBetweenItems = DEFAULT_LAYOUT_PADDING_BETWEEN_ITEMS;

    protected int backgroundResourceId = 0; 
    protected int animatedRadius = 0;
    protected float angleBetweenItems = 0;
    
    private ContextAction actionToExecute = null;
    protected ItemsState itemsState = ItemsState.COLLAPSED;
    
    protected OnItemsStateChangedListener onItemsStateChangedListener = null;
    protected OnClickListener externalItemClickListener = null;
    protected AnimationListener externalAnimationListener = null;

    // Animation lengths and delays (in ms)
    protected int expandItemsAnimLength = 100;
    protected int collapseItemsAnimLength = 100;
    
    // Layout and measurement
    protected int layoutWidth = 0;
    protected int layoutHeight = 0;
    protected final Point layoutOffset = new Point(0,0);
    
    public CollapsibleLayout(Context context) {
        super(context);
    }

    public CollapsibleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ContextActionsLayout, 0, 0);
            
            int degreesToArcCenter = a.getInt(R.styleable.ContextActionsLayout_degreesToArcCenter, DEFAULT_ANGLE_TO_ARC_CENTER);
            angleToArcCenter = (float) Math.toRadians(degreesToArcCenter % 360);
            
            int defaultArcRadius = Tools.getPixels(DEFAULT_ARC_RADIUS);
            setRadius(a.getDimensionPixelSize(R.styleable.ContextActionsLayout_arcRadius, defaultArcRadius), false);
            
            int defaultItemSizePx = Tools.getPixels(DEFAULT_ITEM_SIZE);
            setItemSize(Math.max(a.getDimensionPixelSize(R.styleable.ContextActionsLayout_itemSize, defaultItemSizePx), 0), false);
            
            int defaultLayoutPadding = Tools.getPixels(DEFAULT_LAYOUT_PADDING);
            layoutPadding = a.getDimensionPixelSize(R.styleable.ContextActionsLayout_layoutPadding, defaultLayoutPadding);
            
            int defaultLayoutPaddingBetweenItems = Tools.getPixels(DEFAULT_LAYOUT_PADDING_BETWEEN_ITEMS);
            layoutPaddingBetweenItems = a.getDimensionPixelSize(R.styleable.ContextActionsLayout_layoutPaddingBetweenItems, defaultLayoutPaddingBetweenItems);

            a.recycle();

            updateAngleBetweenItems();
        }
    }

    private void updateMeasurement() {
        int halfSize = itemSize / 2;
        final int count = getChildCount();
        float angle = angleToArcCenter + angleBetweenItems * (count - 1) * 0.5f;

        Rect rect = new Rect();
        if (count > 0) {
            Point pt = calculateItemPosition(arcRadius, angle);
            rect.left = rect.right = pt.x;
            rect.top = rect.bottom = pt.y;
            for (int i = 1; i < count; i++) {
                angle -= angleBetweenItems;
                pt = calculateItemPosition(arcRadius, angle);
                rect.union(pt.x, pt.y);
            }

            int inset = -(halfSize+layoutPadding);
            rect.inset(inset, inset);
        }
        rect.union(-halfSize, -halfSize, halfSize, halfSize);
        
        layoutWidth = rect.width();
        layoutHeight = rect.height();
        layoutOffset.x = -rect.left;
        layoutOffset.y = -rect.top;
        
        LayoutParams p = getLayoutParams();
        if (p != null) {
            OffsetLayoutParams params = (OffsetLayoutParams) p;
            params.x = (rect.left + rect.right) / 2;
            params.y = (rect.top + rect.bottom) / 2;
        }
    }
    
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(layoutWidth, layoutHeight);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(
				MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY));
		}
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int centerX = layoutOffset.x;
        final int centerY = layoutOffset.y;
        final boolean expanded = (itemsState == ItemsState.EXPANDED || itemsState == ItemsState.COLLAPSING);
        final int count = getChildCount();
        if (count > 0) {
            boolean reversing = expandItemsInterpolator.isReversed() || collapseItemsInterpolator.isReversed();
            if (expanded ^ reversing) {
                float angle = angleToArcCenter + angleBetweenItems * (count - 1) * 0.5f;
                for (int index = 0; index < count; index++) {
                    Rect frame = calculateItemRect(centerX, centerY, arcRadius, angle);
                    getChildAt(index).layout(frame.left, frame.top, frame.right, frame.bottom);
                    angle -= angleBetweenItems;
                }
            } else {
            	Rect frame = calculateItemRect(centerX, centerY);
                for (int index = 0; index < count; index++) {
                    getChildAt(index).layout(frame.left, frame.top, frame.right, frame.bottom);
                }
            }
        }
    }
    
    protected void bindItemAnimations() {
    	final int count = getChildCount();
    	if (count > 0) {
    	    Animation oldAnim = getChildAt(count-1).getAnimation();
    	
            if (oldAnim != null) {
                ReversibleInterpolator interpolator = (ReversibleInterpolator)oldAnim.getInterpolator();
    
            	// Reverse the current animations by reversing the interpolator 
            	interpolator.reverse();
    	        
    	        // Re-start the current animation, adjusting the start time
    		    for (int index = 0; index < count; index++) {
    		    	oldAnim = getChildAt(index).getAnimation();
    		    	long timeToSkip = AnimUtils.getReversedStartTime(oldAnim, getDrawingTime(), oldAnim.getDuration());
    		    	oldAnim.setStartTime(timeToSkip);
    		    }
    	    	
    		    // Swap animation listener from last to first child or vice versa 
    		    if (interpolator.isReversed()) {
    		    	getChildAt(count-1).getAnimation().setAnimationListener(null);
    		    	getChildAt(0).getAnimation().setAnimationListener(showHideItemAnimationListener);
    		    } else {
    		    	getChildAt(0).getAnimation().setAnimationListener(null);
    		    	getChildAt(count-1).getAnimation().setAnimationListener(showHideItemAnimationListener);
    		    }
        	} else {
        		// Reset reversing flags
        		expandItemsInterpolator.reverse(false);
        		collapseItemsInterpolator.reverse(false);
        		
            	final Point from = new Point(0,0);
            	final boolean expand = (itemsState == ItemsState.EXPANDING);
    
                // Create expand/shrink animations
            	Animation animation = null;
                float angle = angleToArcCenter + angleBetweenItems * (count - 1) * 0.5f;
    		    for (int index = 0; index < count; index++) {
    		    	View item = getChildAt(index);
    		    	Point to = calculateItemPosition(arcRadius, angle);
    		    	if (expand) {
    			        animation = createExpandAnimation(-from.x, to.x, from.y, to.y);
                        if (animation != null) {
                            expandItemsInterpolator.setInterpolator(animation.getInterpolator());
                            animation.setInterpolator(expandItemsInterpolator);
                        }
    		    	} else {
    			        animation = createCollapseAnimation(from.x, -to.x, -from.y, -to.y); 
                        if (animation != null) {
                            collapseItemsInterpolator.setInterpolator(animation.getInterpolator());
                            animation.setInterpolator(collapseItemsInterpolator);
                        }
    		    	}

    		    	item.startAnimation(animation);
    		        
    		        angle -= angleBetweenItems;
    		    }
    	    	
    	        // Set an animation listener on the last item (since it'll be the last one to finish)
    	        if (animation != null) {
    	        	animation.setAnimationListener(showHideItemAnimationListener);
    	    	}
        	}
    	}
    }
    
    private void updateAngleBetweenItems() {
    	angleBetweenItems = calculateAngleBetweenItems();
    }
    
    protected float calculateAngleBetweenItems() {
        return 2.f * (float)Math.asin((itemSize + layoutPaddingBetweenItems)/(2.f * arcRadius));
    }
    
    public void setActions(List<ContextAction> actions) {
    	int index = 0;
    	final int childCount = getChildCount();
    	final int minCount = Math.min(actions.size(), childCount);
    	Iterator<ContextAction> actionsIterator = actions.iterator();
    	// Recycle current child views
    	for (; index < minCount; index++) {
            recycleAction((ImageView) getChildAt(index), actionsIterator.next());
    	}
    	// Remove or add children accordingly
    	if (index < childCount){
    		removeViews(index, childCount - index);
    	} else if (actionsIterator.hasNext()) {
    		do {
    		    addActionInternal(actionsIterator.next());
    		} while (actionsIterator.hasNext());
    	}
    	
    	updateMeasurement();
    }
    
    public void addAction(ContextAction action) {
        addActionInternal(action);
        updateMeasurement();
    }
    
    private void addActionInternal(ContextAction action) {
        ImageView item = new ImageView(getContext());
        setContentDescription(item, action);
    	item.setBackgroundResource(backgroundResourceId);
    	item.setScaleType(ImageView.ScaleType.CENTER);
        item.setOnClickListener(internalItemClickListener);
        if (android.os.Build.VERSION.SDK_INT >= 19)
            item.setLayerType(LAYER_TYPE_HARDWARE, null);
        
        recycleAction(item, action);
        addView(item);
    }
    
    protected void recycleAction(ImageView item, ContextAction action) {
        setContentDescription(item, action);
        item.setImageResource(action.imageResourceId);
        item.setTag(action);
    }

    private void setContentDescription(ImageView item, ContextAction action){
        String contentDescription = null;
        switch(action.imageResourceId){
            case R.drawable.ad_post_white:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_post_white);
                break;
            case R.drawable.ad_camera_white:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_camera_white);
                break;
            case R.drawable.ad_search_white:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_search_white);
                break;
            case R.drawable.ad_explore_orange:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_explore_orange);
                break;
            case R.drawable.ad_feed_orange:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_feed_orange);
                break;
            case R.drawable.ad_userinvite_white:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_userinvite_white);
                break;
            case R.drawable.ad_chatadd_white:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_ad_chatadd_white);
                break;
            default:
                contentDescription = getResources().getString(R.string.collapsible_layout_java_cd_navigation_button);
                break;
        }
        item.setContentDescription(contentDescription);
    }

    public void setDirection(int degrees) {
        if (angleToArcCenter == degrees)
        	return;

        angleToArcCenter = (float) Math.toRadians(degrees);
        requestLayout();
    }
    
    public int getDirection() {
    	return (int) Math.toDegrees(angleToArcCenter);
    }

    public void setRadius(int radius) {
        setRadius(radius, true);
    }
    public void setRadius(int radius, boolean requestLayout) {
        if (arcRadius == radius)
        	return;

        arcRadius = radius;
        updateAngleBetweenItems();
        updateMeasurement();
        
        if (requestLayout) {
            requestLayout();
        }
    }
    
    public int getRadius() {
    	return arcRadius;
    }

    public void setItemSize(int size) {
        setItemSize(size, true);
    }
    public void setItemSize(int size, boolean requestLayout) {
        if (itemSize == size || size < 0)
            return;

        itemSize = size;
        updateAngleBetweenItems();
        updateMeasurement();
        
        if (requestLayout) {
            requestLayout();
        }
    }

    public int getItemSize() {
        return itemSize;
    }
    
    protected void setItemsState(ItemsState newState) {
        itemsState = newState;
        onItemsStateChanged(newState);
    }

    public ItemsState getItemsState() {
    	return itemsState;
    }
    
    public void setItemClickListener(OnClickListener listener) {
    	externalItemClickListener = listener;
    }

    public OnClickListener getItemClickListener() {
    	return externalItemClickListener;
    }
    
    public void setAnimationListener(AnimationListener listener) {
        externalAnimationListener = listener;
    }

    public AnimationListener getAnimationListener() {
        return externalAnimationListener;
    }
    
    public void setItemsStateChangedListener(OnItemsStateChangedListener listener) {
        onItemsStateChangedListener = listener;
    }

    public OnItemsStateChangedListener getItemsStateChangedListener() {
        return onItemsStateChangedListener;
    }
    
    public int getExpandAnimLength() {
        return expandItemsAnimLength;
    }

    public int getCollapseAnimLength() {
        return collapseItemsAnimLength;
    }

    public boolean isExpandable() {
        // It's expandable if it is not expanded nor expanding nor the click animation is playing
        return itemsState != ItemsState.EXPANDED && itemsState != ItemsState.EXPANDING && !isClickAnimationPlaying();
    }
    
    public boolean isCollapsible() {
        // It's collapsible if it is not collapsed nor collapsing nor the click animation is playing
        return itemsState != ItemsState.COLLAPSED && itemsState != ItemsState.COLLAPSING && !isClickAnimationPlaying();
    }
                
    public boolean isClickAnimationPlaying() {
        return animItemClicked != null && animItemClicked.hasStarted() && !animItemClicked.hasEnded();
    }
    
    public boolean expand(boolean showAnimation) {
        if (isExpandable()) {
            switchState(showAnimation);
            return true;
        }
        return false;
    }
    
    public boolean collapse(boolean showAnimation) {
        if (isCollapsible()) {
            switchState(showAnimation);
            return true;
        }
        return false;
    }

    public void switchState(boolean showAnimation) {
        
        if (getChildCount() == 0) {
            showAnimation = false;
        }

        ItemsState newState = ItemsState.COLLAPSED;
    	switch (itemsState) {
        case EXPANDING:
        case EXPANDED:
            newState = showAnimation? ItemsState.COLLAPSING : ItemsState.COLLAPSED;
        	break;
        case COLLAPSING:
        case COLLAPSED:
            newState = showAnimation? ItemsState.EXPANDING : ItemsState.EXPANDED;
        	break;
        default:
            newState = ItemsState.COLLAPSED;
        }

    	if (newState != itemsState) {
        	setItemsState(newState);
    
            if (showAnimation) {
            	bindItemAnimations();
            } else { 
                requestLayout();
            }
        
            invalidate();
        }
    }
    
    public boolean hitTestButtons(int x, int y, boolean global) {
        // Instead of testing for transparent pixel, we cheat by knowing the button shape is round
        final int itemCount = getChildCount();
        if (global) {
            for (int i = 0; i < itemCount; i++) {
                getChildAt(i).getGlobalVisibleRect(hitTestRect, null);
                int xOffset = x - hitTestRect.centerX();
                int yOffset = y - hitTestRect.centerY();
                int radius = hitTestRect.width() / 2;
                if (xOffset * xOffset + yOffset * yOffset <= radius * radius) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < itemCount; i++) {
                getChildAt(i).getDrawingRect(hitTestRect);
                int xOffset = x - hitTestRect.centerX();
                int yOffset = y - hitTestRect.centerY();
                int radius = hitTestRect.width() / 2;
                if (xOffset * xOffset + yOffset * yOffset <= radius * radius) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void executeItemAction() {
        if (actionToExecute != null) {
            actionToExecute.execute();
            actionToExecute = null;
        }
    }

    protected void onShowHideAnimationsEnd() {
        // Clear anims
        clearAnimations();
        
        // Reset reversing flags
        expandItemsInterpolator.reverse(false);
        collapseItemsInterpolator.reverse(false);

        // Update items state
        if (itemsState == ItemsState.EXPANDING) {
            setItemsState(ItemsState.EXPANDED);
        } else if (itemsState == ItemsState.COLLAPSING) {
            setItemsState(ItemsState.COLLAPSED);
        }

        requestLayout();
    }
    
    protected void onItemClickAnimationEnd() {
    	clearAnimations();
    	
        if (externalAnimationListener != null) {
            externalAnimationListener.onClickAnimationEnd();
        }

        executeItemAction();
        collapse(false);
    }
    
    protected void onItemsStateChanged(ItemsState newState) {
        if (newState == ItemsState.COLLAPSED && getVisibility() != GONE) {
            setVisibility(GONE);
        } else if (newState == ItemsState.EXPANDING && getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        } 
        if (onItemsStateChangedListener != null) {
            onItemsStateChangedListener.onStateChanged(this, newState);
        }
    }
    
	protected Rect calculateItemRect(int centerX, int centerY, int radius, float angle) {
		double itemX = centerX + radius * Math.cos(angle);
		double itemY = centerY - radius * Math.sin(angle);
		double halfSize = itemSize * 0.5;

		return new Rect(
			(int)(itemX - halfSize), (int)(itemY - halfSize),
			(int)(itemX + halfSize), (int)(itemY + halfSize));
	}
	protected Rect calculateItemRect(int centerX, int centerY) {
		int halfSize = (int)(itemSize * 0.5f);
		return new Rect(
			centerX - halfSize, centerY - halfSize,
			centerX + halfSize, centerY + halfSize);
	}
	
	protected static Point calculateItemPosition(int radius, float angle) {
		double itemX = radius * Math.cos(angle);
		double itemY = -radius * Math.sin(angle);
		return new Point((int)itemX, (int)itemY);
	}

    protected void clearAnimations() {
        final int itemCount = getChildCount();
        for (int i = 0; i < itemCount; i++) {
        	getChildAt(i).clearAnimation();
        }
    }
    
    private Animation initItemClickAnimations(int duration, boolean isClicked) {
        Animation anim = createItemClickAnimations(duration, isClicked);
        if (isClicked && anim != null) {
            anim.setAnimationListener(itemClickAnimationListener);
        }
        return anim;
    }

    protected abstract Animation createItemClickAnimations(int duration, boolean isClicked);
    protected abstract Animation createExpandAnimation(float fromX, float toX, float fromY, float toY);
    protected abstract Animation createCollapseAnimation(float fromX, float toX, float fromY, float toY);
    
    class ItemClickAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}
        @Override
        public void onAnimationRepeat(Animation animation) {}
        @Override
        public void onAnimationEnd(Animation animation) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                	onItemClickAnimationEnd();
                }
            }, 0);
        }
    }

    class ShowHideItemAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}
        @Override
        public void onAnimationRepeat(Animation animation) {}
        @Override
        public void onAnimationEnd(Animation animation) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                	onShowHideAnimationsEnd();
                }
            }, 0);
        }
    }
    
    public class OnItemClickListener implements OnClickListener {
		@Override
		public void onClick(final View itemClicked) {
		    if (itemsState != ItemsState.EXPANDED || 
		        (animItemClicked != null && animItemClicked.hasStarted() && !animItemClicked.hasEnded())) {
		        return;
		    }
		    
            // Ping the listeners
            if (externalItemClickListener != null) {
                externalItemClickListener.onClick(itemClicked);
            }
            
			// Set the animation for the clicked item
			itemClicked.setAnimation(animItemClicked);

			// Set the animation for items that were not clicked
			int itemCount = getChildCount();
			for (int i = 0; i < itemCount; i++) {
				View item = getChildAt(i);
				if (itemClicked != item) {
					item.setAnimation(animItemNotClicked);
				}
			}

			// Retrieve item's action
			actionToExecute = null;
            Object tag = itemClicked.getTag();
            if (tag != null && tag instanceof ContextAction) {
                actionToExecute = (ContextAction) tag;
            }

			// Restart all animations
            if (animItemNotClicked != null) {
                animItemNotClicked.start();
            }
			if (animItemClicked != null) {
			    animItemClicked.start();
			} else {
	            // Execute action if there is no animation, otherwise wait for the animation to finish
			    onItemClickAnimationEnd();
			}

			invalidate();
		}
    }

}
