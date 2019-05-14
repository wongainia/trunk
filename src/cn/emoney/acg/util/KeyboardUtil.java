package cn.emoney.acg.util;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.sky.libs.db.GlobalDBHelper;

public class KeyboardUtil implements OnClickListener {
    public static final int TYPE_DIGIT = 0;
    public static final int TYPE_ENGLISH = 1;

    private Context myContext;
    private View myActivity;
    private LinearLayout keyboardDigit;// 数字键盘
    private LinearLayout keyboardEnglish;// 字母键盘
    private KeyboardListener mKeyboardListener = null;
    private GlobalDBHelper mDBHelper = null;

    private EditText ed;
    private int mMaxInputLen = 99999;

    public KeyboardUtil(View contentView, Context context, EditText editText) {
        this.myActivity = contentView;
        this.myContext = context;
        this.ed = editText;
        keyboardDigit = (LinearLayout) myActivity.findViewById(R.id.activity_search_keyboard_digit);
        keyboardEnglish = (LinearLayout) myActivity.findViewById(R.id.activity_search_keyboard_english);
        keyboardDigit.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        keyboardEnglish.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        initKey();

    }

    public void setMaxInputLen(int len) {
        mMaxInputLen = len;
    }

    public void setOnkeyboardListener(KeyboardListener listener) {
        this.mKeyboardListener = listener;
    }

    private void initKey() {
        // TODO Auto-generated method stub
        myActivity.findViewById(R.id.tv11).setOnClickListener(this);
        myActivity.findViewById(R.id.tv12).setOnClickListener(this);
        myActivity.findViewById(R.id.tv13).setOnClickListener(this);
        myActivity.findViewById(R.id.tv14).setOnClickListener(this);
        myActivity.findViewById(R.id.tv15).setOnClickListener(this);

        myActivity.findViewById(R.id.tv21).setOnClickListener(this);
        myActivity.findViewById(R.id.tv22).setOnClickListener(this);
        myActivity.findViewById(R.id.tv23).setOnClickListener(this);
        myActivity.findViewById(R.id.tv24).setOnClickListener(this);
        myActivity.findViewById(R.id.tv25).setOnClickListener(this);

        myActivity.findViewById(R.id.tv31).setOnClickListener(this);
        myActivity.findViewById(R.id.tv32).setOnClickListener(this);
        myActivity.findViewById(R.id.tv33).setOnClickListener(this);
        myActivity.findViewById(R.id.tv34).setOnClickListener(this);
        myActivity.findViewById(R.id.tv35).setOnClickListener(this);

        myActivity.findViewById(R.id.tv41).setOnClickListener(this);
        myActivity.findViewById(R.id.tv42).setOnClickListener(this);
        myActivity.findViewById(R.id.tv43).setOnClickListener(this);
        myActivity.findViewById(R.id.tv44).setOnClickListener(this);
        myActivity.findViewById(R.id.tv45).setOnClickListener(this);

        myActivity.findViewById(R.id.tv11_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv12_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv13_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv14_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv15_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv16_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv17_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv18_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv19_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv110_english).setOnClickListener(this);

        myActivity.findViewById(R.id.tv21_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv22_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv23_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv24_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv25_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv26_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv27_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv28_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv29_english).setOnClickListener(this);

        myActivity.findViewById(R.id.tv31_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv32_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv33_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv34_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv35_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv36_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv37_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv38_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv39_english).setOnClickListener(this);

        myActivity.findViewById(R.id.tv41_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv42_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv43_english).setOnClickListener(this);
        myActivity.findViewById(R.id.tv44_english).setOnClickListener(this);

    }

    public void setEditText(EditText ed) {
        this.ed = ed;
    }

    private void insert2Et(String str) {
        if (ed.getText().toString().length() >= mMaxInputLen) {
            return;
        }

        Editable editable = ed.getText();
        int start = ed.getSelectionStart();
        editable.insert(start, str);
    }

    @Override
    public void onClick(View v) {
        Editable editable = ed.getText();
        int start = ed.getSelectionStart();
        InputMethodManager m = (InputMethodManager) myContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (v.getId()) {
            case R.id.tv11:
                insert2Et("600");
                break;
            case R.id.tv12:
                insert2Et("1");
                break;
            case R.id.tv13:
                insert2Et("2");
                break;
            case R.id.tv14:
                insert2Et("3");
                break;
            case R.id.tv15:
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
                break;

            case R.id.tv21:
                insert2Et("000");
                break;
            case R.id.tv22:
                insert2Et("4");
                break;
            case R.id.tv23:
                insert2Et("5");
                break;
            case R.id.tv24:
                insert2Et("6");
                break;
            case R.id.tv25:
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(0, start);
                    }
                }
                break;

            case R.id.tv31:
                insert2Et("002");
                break;
            case R.id.tv32:
                insert2Et("7");
                break;
            case R.id.tv33:
                insert2Et("8");
                break;
            case R.id.tv34:
                insert2Et("9");
                break;
            case R.id.tv35:
                hideKeyboard();
                break;

            case R.id.tv41:
                insert2Et("300");
                break;
            case R.id.tv42:
                showKeyboardEnglish();
                break;
            case R.id.tv43:
                insert2Et("0");
                break;
            case R.id.tv44:
                hideKeyboard();
                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.tv45:
                if (mKeyboardListener != null) {
                    mKeyboardListener.onFuncKeyClick(R.id.tv44, "Enter");
                }
                break;

            case R.id.tv11_english:
                insert2Et("q");
                break;
            case R.id.tv12_english:
                insert2Et("w");
                break;
            case R.id.tv13_english:
                insert2Et("e");
                break;
            case R.id.tv14_english:
                insert2Et("r");
                break;
            case R.id.tv15_english:
                insert2Et("t");
                break;
            case R.id.tv16_english:
                insert2Et("y");
                break;
            case R.id.tv17_english:
                insert2Et("u");
                break;
            case R.id.tv18_english:
                insert2Et("i");
                break;
            case R.id.tv19_english:
                insert2Et("o");
                break;
            case R.id.tv110_english:
                insert2Et("p");
                break;

            case R.id.tv21_english:
                insert2Et("a");
                break;
            case R.id.tv22_english:
                insert2Et("s");
                break;
            case R.id.tv23_english:
                insert2Et("d");
                break;
            case R.id.tv24_english:
                insert2Et("f");
                break;
            case R.id.tv25_english:
                insert2Et("g");
                break;
            case R.id.tv26_english:
                insert2Et("h");
                break;
            case R.id.tv27_english:
                insert2Et("j");
                break;
            case R.id.tv28_english:
                insert2Et("k");
                break;
            case R.id.tv29_english:
                insert2Et("l");
                break;

            case R.id.tv31_english:// 清空
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(0, start);
                    }
                }
                break;
            case R.id.tv32_english:
                insert2Et("z");
                break;
            case R.id.tv33_english:
                insert2Et("x");
                break;
            case R.id.tv34_english:
                insert2Et("c");
                break;
            case R.id.tv35_english:
                insert2Et("v");
                break;
            case R.id.tv36_english:
                insert2Et("b");
                break;
            case R.id.tv37_english:
                insert2Et("n");
                break;
            case R.id.tv38_english:
                insert2Et("m");
                break;
            case R.id.tv39_english:
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
                break;

            case R.id.tv41_english:
                hideKeyboard();
                break;
            case R.id.tv42_english:
                showKeyboard();
                break;
            case R.id.tv43_english:
                hideKeyboard();
                m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.tv44_english:
                if (mKeyboardListener != null) {
                    mKeyboardListener.onFuncKeyClick(R.id.tv44_english, "Enter");
                }
                break;

            default:
                break;
        }
    }

    // 显示数字键盘 默认
    public void showKeyboard() {
        hideKeyboard();
        int visibility = keyboardDigit.getVisibility();
        ed.setSelection(ed.getText().length());
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            keyboardDigit.setVisibility(View.VISIBLE);
            getDBHelper().setInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 0);
        }

    }

    // 显示字母键盘
    public void showKeyboardEnglish() {
        hideKeyboard();
        int visibility = keyboardEnglish.getVisibility();
        ed.setSelection(ed.getText().length());
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            keyboardEnglish.setVisibility(View.VISIBLE);
            getDBHelper().setInt(DataModule.G_KEY_LAST_KEYBOARD_TYPE, 1);
        }

    }

    // 隐藏键盘
    public void hideKeyboard() {
        int visibility = keyboardDigit.getVisibility();
        if (visibility == View.VISIBLE) {
            keyboardDigit.setVisibility(View.GONE);
        }
        int visibility2 = keyboardEnglish.getVisibility();
        if (visibility2 == View.VISIBLE) {
            keyboardEnglish.setVisibility(View.GONE);
        }
    }

    // 键盘是否已经在显示
    public boolean isKeyboardShow() {
        int visibility = keyboardDigit.getVisibility();
        int visibility2 = keyboardEnglish.getVisibility();
        if (visibility == View.VISIBLE || visibility2 == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    public interface KeyboardListener {
        void onFuncKeyClick(int keyId, String keyName);
    }

    public GlobalDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new GlobalDBHelper(myContext, DataModule.DB_GLOBAL);
        }
        return mDBHelper;
    }
}
