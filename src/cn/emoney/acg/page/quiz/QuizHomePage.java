package cn.emoney.acg.page.quiz;

import java.util.ArrayList;

import android.support.v4.app.FragmentTransaction;
import cn.emoney.acg.R;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.page.PageImpl;

/**
 * @ClassName: QuizAllHomePage
 * @Description:问题容器页面，根据身份切换普通用户还是老师的页面
 * @author xiechengfa
 * @date 2015年12月17日 下午2:49:04
 *
 */
public class QuizHomePage extends PageImpl {
    private final int TYPE_NONE = 0;
    private final int TYPE_NORMAL = 1;
    private final int TYPE_TEACHER = 2;
    private int currType = TYPE_NONE;
    private PageImpl page = null;

    @Override
    protected void initData() {}

    @Override
    protected void initPage() {
        setContentView(R.layout.page_quiz_all_home);
        initQuizOfRole();
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        initQuizOfRole();

        if (page instanceof QuizLatestListPage) {
            ((QuizLatestListPage) page).onResumeOfSwitcher();
        } else if (page instanceof TeacherHomePage) {
            ((TeacherHomePage) page).onResumeOfSwitcher();
        }
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        // super.onPagePause();
        if (page instanceof QuizLatestListPage) {
            ((QuizLatestListPage) page).onPauseOfSwitcher();
        } else if (page instanceof TeacherHomePage) {
            ((TeacherHomePage) page).onPauseOfSwitcher();
        }
    }

    @Override
    public void onReceivedBroadcast(String action) {
        super.onReceivedBroadcast(action);
        if (action != null && action.equals(BroadCastName.BCDC_CHANGE_LOGIN_STATE_QIZE)) {
            // 判断登录身份
            initQuizOfRole();
        }
    }

    @Override
    public ArrayList<String> getRegisterBcdc() {
        ArrayList<String> lstBcdc = new ArrayList<String>();
        lstBcdc.add(BroadCastName.BCDC_CHANGE_LOGIN_STATE_QIZE);
        return lstBcdc;
    }

    // 判断登录身份
    private void initQuizOfRole() {
        if (getUserInfo().isRoleTeacher()) {
            // 老师身份
            if (currType == TYPE_TEACHER) {
                return;
            }

            replaceFragment(new TeacherHomePage());
            currType = TYPE_TEACHER;
        } else {
            // 普通身份
            if (currType == TYPE_NORMAL) {
                return;
            }

            replaceFragment(new QuizLatestListPage());
            currType = TYPE_NORMAL;
        }
    }

    private void replaceFragment(PageImpl page) {
        this.page = page;
        FragmentTransaction transaction = getModule().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.quizFramentLayout, page);
        transaction.commitAllowingStateLoss();
    }
}
