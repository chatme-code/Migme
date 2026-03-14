/**
 * Copyright (c) 2013 Project Goth
 *
 * OriginalPostPreviewHolder.java
 * Created Sep 23, 2014, 5:44:35 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.Post;
import com.projectgoth.util.PostUtils;
import com.projectgoth.util.StringUtils;

/**
 * This class serves as a holder for holder_simplepostpreview. It will populate
 * the view elements for the post whose preview is to be shown.
 * 
 * @author angelorohit
 * 
 */
public class SimplePostPreviewHolder extends BaseViewHolder<Post> {

	private ImageView photo;
	private TextView author;
	private TextView content;
	
	/**
	 * @param rootView
	 */
	public SimplePostPreviewHolder(ViewGroup rootView) {
		super(rootView, false);
		
		photo = (ImageView) rootView.findViewById(R.id.photo);
		author = (TextView) rootView.findViewById(R.id.author);
		content = (TextView) rootView.findViewById(R.id.content);
	}

	@Override
    public void setData(Post post) {
		super.setData(post);
		
		if (post != null) {
			photo.setImageResource(R.drawable.ad_camera_grey);
			
			if (post.getAuthor() != null) {
				author.setText(post.getAuthor().getUsername());				
			}
			
			if (post.getBody() != null) {
				content.setText(StringUtils.decodeHtml(post.getBody()));
			}
			
			photo.setTag(null);
			photo.setVisibility(PostUtils.setThumbnail(photo, post) ? View.VISIBLE : View.GONE);
		}
	}
}
