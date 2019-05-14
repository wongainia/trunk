package cn.emoney.acg.page.my;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import cn.emoney.acg.R;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.RequestBuilder;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.quote.DynaValueDataPackage;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.widget.RefreshListView;

public class MyTeacherPage extends PageImpl {
    
    private TeacherListAdapter adapter;
    
    private RefreshListView listView;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_myteacher);
        
        initViews();
    }

    @Override
    protected void initData() {
    }
    
    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "我的大师");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

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
        listView = (RefreshListView) findViewById(R.id.page_myteacher_list);
        
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        
        adapter = new TeacherListAdapter();
        listView.setAdapter(adapter);
        
        bindPageTitleBar(R.id.page_myteacher_titlebar);
    }
    
    /**
     * 请求我的大师列表
     * */
    private void requestMyTeachers() {
//        DynaValueDataPackage pkg = new DynaValueDataPackage(new QuoteHead(REQUEST_TYPE_QUOTATION));
//        pkg.setRequest(RequestBuilder.dynaValueDataRequestBuilder(4, 0, goodsId, reqFileds, -9999, true, 0, 0, 0, 0));
//        requestQuote(pkg, IDUtils.DynaValueData);
    }
    
    private class TeacherListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.page_myteacher_listitem, parent, false);
            }
            
            return convertView;
        }
        
    }

}
