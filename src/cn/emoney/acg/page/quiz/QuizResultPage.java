package cn.emoney.acg.page.quiz;

import android.os.Bundle;
import android.view.KeyEvent;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.quiz.QuizContentInfo;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: QuizResultPage
 * @Description: 问股结果页面
 * @author xiechengfa
 * @date 2015年12月3日 上午9:30:10
 *
 */
public class QuizResultPage extends PageImpl {
    private QuizListImpl quizList = null;
    public final static String INTENT_TYPE = "type";
    public final static String INTENT_IS_FROM_HOME = "is_from_home";
    public final static String INTENT_ITEM = "question";

    public static void startPage(PageImpl page, QuizContentInfo questioin, boolean isAskQuestion) {
        PageIntent intent = new PageIntent(page, QuizResultPage.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_ITEM, questioin);
        bundle.putInt(INTENT_TYPE, QuizListImpl.TYPE_RESULT);
        bundle.putBoolean(INTENT_IS_FROM_HOME, isAskQuestion);
        intent.setArguments(bundle);
        page.startPage(DataModule.G_CURRENT_FRAME, intent);
    }

    @Override
    protected void initPage() {
        quizList = new QuizListImpl(this);
        quizList.initPage();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return quizList.onKeyUp(keyCode, event);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
        quizList.initData();
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        quizList.onPageResume();
    }

    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        // TODO Auto-generated method stub
        quizList.updateFromQuote(pkg);
    }

    @Override
    protected void updateWhenNetworkError(short type) {
        // TODO Auto-generated method stub
        quizList.updateWhenNetworkError(type);
    }

    @Override
    protected void updateWhenDecodeError(short type) {
        // TODO Auto-generated method stub
        quizList.updateWhenDecodeError(type);
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        quizList.onCreatePageTitleBarMenu(bar, menu);
        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        quizList.onPageTitleBarMenuItemSelected(menuitem);
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        super.onPagePause();
        quizList.onPagePause();
    }

    @Override
    protected void onPageDestroy() {
        // TODO Auto-generated method stub
        quizList.onPageDestroy();
        super.onPageDestroy();
    }

}
