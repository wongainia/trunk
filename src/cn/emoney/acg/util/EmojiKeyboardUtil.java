package cn.emoney.acg.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.emoney.acg.R;
import cn.emoney.sky.cmojicon.fixemojitvres.Emojicon;
import cn.emoney.sky.cmojicon.fixemojitvres.People;
import cn.emoney.sky.fixcmojitv.EmojiAdapter;
import cn.emoney.sky.fixcmojitv.EmojiconEditText;

public class EmojiKeyboardUtil {
	private LinearLayout mLlInputTool = null;
	private GridView mEmojiKeyboard = null;
	private boolean mIsContainEmoji = true;
	private EmojiconEditText mEmojiEt = null;
	private ImageView mIvKeyboardSwitcher = null;
	private ImageView mIvSendMsg = null;

	private Activity mActivity;
	private Context mContext;

	private int mKeyboardType = 0;

	public EmojiKeyboardUtil(Activity activity, Context context, View contentView, boolean isContainEmoji) {
		this.mActivity = activity;
		this.mContext = context;
		this.mLlInputTool = (LinearLayout) contentView.findViewById(R.id.ll_input_tool_layout);

		this.mEmojiEt = (EmojiconEditText) contentView.findViewById(R.id.item_emojiet_input);
		this.mIvKeyboardSwitcher = (ImageView) contentView.findViewById(R.id.item_iv_switcher);
		this.mIvSendMsg = (ImageView) contentView.findViewById(R.id.item_iv_send);
		this.mIsContainEmoji = isContainEmoji;
		if (isContainEmoji) {
			this.mEmojiKeyboard = (GridView) contentView.findViewById(R.id.emoji_keyboard_layout);
		} else {
			this.mIvKeyboardSwitcher.setVisibility(View.GONE);
		}

		init();
	}

	public EmojiKeyboardUtil(Activity activity, Context context, View contentView) {
		this(activity, context, contentView, true);
	}

	private void init() {
		mLlInputTool.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		clearInputFocus();
		// setInputFocusable(false);
		mLlInputTool.setVisibility(View.INVISIBLE);
		mKeyboardType = -1;

		if (mIsContainEmoji) {
			mIvKeyboardSwitcher.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mKeyboardType == 0) {
						openEmojiKeyboard();
					} else if (mKeyboardType == 1) {
						openKeyboard();
					}
				}
			});
		}

		mIvSendMsg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnSendKeyListener != null) {
					String tInputMsg = "";
					if (mEmojiEt != null) {
						tInputMsg = mEmojiEt.getText().toString();
					}
					mOnSendKeyListener.gotInputMsg(tInputMsg);
				}
			}
		});

		if (mIsContainEmoji && mEmojiKeyboard != null) {
			mEmojiKeyboard.setAdapter(new EmojiAdapter(mContext, People.DATA, false));
			mEmojiKeyboard.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Emojicon icon = (Emojicon) parent.getItemAtPosition(position);
					input(icon);
				}

			});
		}

	}

	public void closeKeyboard() {
		mKeyboardType = -1;
		closeSoftKeyBoard();
		mLlInputTool.setVisibility(View.INVISIBLE);
		if (mEmojiKeyboard != null) {
			mEmojiKeyboard.setVisibility(View.GONE);
		}
		clearInputFocus();
	}

	public void openKeyboard() {
		mKeyboardType = 0;
		setInputFocusable(true);
		clearInputFocus();
		if (mIsContainEmoji && mEmojiKeyboard != null) {
			mEmojiKeyboard.setVisibility(View.GONE);
			mIvKeyboardSwitcher.setImageResource(R.drawable.selector_btn_keyboard_switcher_emoji);
		}
		mLlInputTool.setVisibility(View.VISIBLE);
		switchSoftKeyBoard();
	}

	public void openEmojiKeyboard() {
		if (!mIsContainEmoji || mEmojiKeyboard == null) {
			return;
		}
		mKeyboardType = 1;
		clearInputFocus();
		setInputFocusable(false);
		closeSoftKeyBoard();
		mLlInputTool.setVisibility(View.VISIBLE);
		mIvKeyboardSwitcher.setImageResource(R.drawable.selector_btn_keyboard_switcher_sys);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mEmojiKeyboard.setVisibility(View.VISIBLE);
					}
				});
			}
		}, 200);

	}

	public void hideEmojiBoard() {
		if (mEmojiKeyboard != null) {
			mEmojiKeyboard.setVisibility(View.GONE);
		}
	}

	public int getKeyboardType() {
		return mKeyboardType;
	}

	public void requestInputFocus() {
		if (mEmojiEt != null) {
			mEmojiEt.requestFocus();
			mEmojiEt.requestFocus();
			mEmojiEt.findFocus();
		}
	}

	public void clearInputFocus() {
		if (mEmojiEt != null) {
			mEmojiEt.clearFocus();

		}
	}

	public void setInputHint(String hint) {
		if (mEmojiEt != null) {
			mEmojiEt.setHint(hint);
		}
	}

	public void setInputFocusable(boolean b) {
		if (mEmojiEt != null) {
			mEmojiEt.setFocusable(b);
			mEmojiEt.setFocusableInTouchMode(b);
		}
	}

	public void clearInput() {
		if (mEmojiEt != null) {
			mEmojiEt.setText("");
		}
	}

	public boolean closeSoftKeyBoard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
		// 得到InputMethodManager的实例
		boolean bRet = imm.isActive();
		if (bRet) {
			// 如果存在关闭
			if (mActivity.getCurrentFocus() != null) {
				imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				return true;
			} else {
				return false;
			}

			// 切换开启和关闭
			// imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
			// InputMethodManager.HIDE_NOT_ALWAYS);
			// 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
		}

		return false;
	}

	public void switchSoftKeyBoard() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
		// 得到InputMethodManager的实例
		boolean bRet = imm.isActive();
		if (bRet) {
			// 切换开启和关闭
			// 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void input(Emojicon emojicon) {
		if (mEmojiEt == null || emojicon == null) {
			return;
		}

		int start = mEmojiEt.getSelectionStart();
		int end = mEmojiEt.getSelectionEnd();
		if (start < 0) {
			mEmojiEt.append(emojicon.getEmoji());
		} else {
			mEmojiEt.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
		}
	}

	public static interface OnSendKeyListener {
		public void gotInputMsg(String inputMsg);
	}

	private OnSendKeyListener mOnSendKeyListener = null;

	public void setOnSendKeyListener(OnSendKeyListener listener) {
		mOnSendKeyListener = listener;
	}

}
