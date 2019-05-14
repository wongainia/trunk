package cn.emoney.acg.page.motif;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class CreateGroupPage extends PageImpl {

    @Override
    protected void initPage() {
        setContentView(R.layout.page_create_group);
        
        initViews();
        
        bindPageTitleBar(R.id.page_creategroup_titlebar);
    }

    @Override
    protected void initData() {}
    
    private void initViews() {
        
        // 点击空白区域隐藏软键盘
        findViewById(R.id.page_creategroup_layout_root).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodUtil.closeSoftKeyBoard(CreateGroupPage.this);
                return false;
            }
        });
    }
    
    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        
        View leftItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView tvLeftTitle = (TextView) leftItemView.findViewById(R.id.tv_titlebar_text);
        tvLeftTitle.setText("取消");

        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftItemView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);
        
        BarMenuTextItem centerItem = new BarMenuTextItem(1, "创建组合");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);
        
        View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_txt, null);
        TextView tvTitle = (TextView) rightItemView.findViewById(R.id.tv_titlebar_text);
        tvTitle.setText("提交");

        BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
        rightItem.setTag(TitleBar.Position.RIGHT);
        menu.addItem(rightItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();

        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            // 退出界面时隐藏软键盘
            InputMethodUtil.closeSoftKeyBoard(this);
            finish();
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            showTip("建设中");
        }
    }

}
