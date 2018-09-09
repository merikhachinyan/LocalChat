package com.ss.localchat.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;


import com.kyo.keyboard.KeyboardHelper;
import com.ss.localchat.R;

public class KeyboardLayout extends LinearLayout {

	private KeyboardHelper keyboardHelper;
	private View emojiKeyboard;
	private EditText input;
	private View emojiToggleView;
	private View sendButton;

	public KeyboardLayout(Context context) {
		super(context);
		this.init(context);
	}

	public KeyboardLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context);
	}

	public KeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public KeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		this.init(context);
	}

	private void init(Context context) {
		this.setOrientation(VERTICAL);

		emojiKeyboard = this.findViewById(R.id.emoji_keyboard);
		input = (EditText) this.findViewById(R.id.message_input_edit_text_chat_activity);
		emojiToggleView = this.findViewById(R.id.emoji_btn);
		sendButton = this.findViewById(R.id.send_button_chat_activity);
	}

	public void setup(Activity activity) {
		this.setup(activity, null);
	}

	public void setup(Activity activity, RecyclerView recyclerView) {
		keyboardHelper = KeyboardHelper.setup(activity, input, emojiKeyboard, emojiToggleView, recyclerView);
	}

	public EditText getEditText() {
		return input;
	}

	public View getSendButton() {
		return sendButton;
	}

	public boolean onBackPressed() {
		return keyboardHelper.onBackPressed();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (keyboardHelper == null) {
			throw new IllegalArgumentException("Please invoke setup method!");
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
}
