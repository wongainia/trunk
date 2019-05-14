package cn.emoney.acg.page.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuItem;

/**
 * @ClassName: QuizLatestListPage
 * @Description:问股最新列表
 * @author xiechengfa
 * @date 2015年12月3日 上午9:30:10
 */
public class QuizLatestListPage extends PageImpl {
    private QuizListImpl quizList = null;

    @Override
    protected void initPage() {
        quizList = new QuizListImpl(this);
        quizList.initPage();
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
        quizList.initData();
    }

    @Override
    protected void onPageResume() {
        quizList.onPageResume();
        super.onPageResume();
    }

    public void onResumeOfSwitcher() {
        quizList.onPageResume();
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        quizList.onPagePause();
        super.onPagePause();
    }

    public void onPauseOfSwitcher() {
        quizList.onPagePause();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }


    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
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
    protected void onPageDestroy() {
        // TODO Auto-generated method stub
        super.onPageDestroy();
        quizList.onPageDestroy();
        LogUtil.easylog("***************test2 onPageDestroy");
    }
}
