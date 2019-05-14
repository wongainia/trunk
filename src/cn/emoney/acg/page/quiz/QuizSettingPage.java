package cn.emoney.acg.page.quiz;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

/**
 * @ClassName: QuizSettingPage
 * @Description:问股设置
 * @author xiechengfa
 * @date 2015年12月16日 下午6:45:39
 *
 */
public class QuizSettingPage extends PageImpl implements OnCheckedChangeListener {
    @Override
    protected void initData() {}

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quiz_setting);
        initViews();
        bindPageTitleBar(R.id.quizSettingTitlebar);
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "问股设置");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);
        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    private void initViews() {
        CheckBox onlineCheckView = (CheckBox) findViewById(R.id.onLineCheckView);
        CheckBox voiceCheckView = (CheckBox) findViewById(R.id.voiceCheckView);
        CheckBox vibrateCheckView = (CheckBox) findViewById(R.id.vibrateCheckView);

        onlineCheckView.setChecked(getDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_ONLINE_STATE, true));
        voiceCheckView.setChecked(getDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_QUESTION_VOICE, true));
        vibrateCheckView.setChecked(getDBHelper().getBoolean(DataModule.G_KEY_QUIZ_SET_QUESTION_VIBRATE, true));

        onlineCheckView.setOnCheckedChangeListener(this);
        voiceCheckView.setOnCheckedChangeListener(this);
        vibrateCheckView.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.onLineCheckView:
                getDBHelper().setBoolean(DataModule.G_KEY_QUIZ_SET_ONLINE_STATE, isChecked);
                break;
            case R.id.voiceCheckView:
                getDBHelper().setBoolean(DataModule.G_KEY_QUIZ_SET_QUESTION_VOICE, isChecked);
                break;
            case R.id.vibrateCheckView:
                getDBHelper().setBoolean(DataModule.G_KEY_QUIZ_SET_QUESTION_VIBRATE, isChecked);
                break;
        }
    }
}
