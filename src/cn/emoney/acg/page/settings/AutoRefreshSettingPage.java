package cn.emoney.acg.page.settings;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class AutoRefreshSettingPage extends PageImpl {

    private final int KIND_MOBILE_NETWORK = 1;
    private final int KIND_WIFI_NETWORK = 2;

    private LinearLayout mLlContent = null;

    List<ViewHolder> mLstItemMobile = new ArrayList<ViewHolder>();
    List<ViewHolder> mLstItemWifi = new ArrayList<ViewHolder>();
    List<View> mLstDivideLine = new ArrayList<View>();

    int[] aryIntervals = new int[] {0, 5, 10, 30, 60};

    @Override
    protected void initPage() {
        setContentView(R.layout.page_setting_refresh_interval);

        mLlContent = (LinearLayout) findViewById(R.id.setting_refresh_ll_content);
        mLstDivideLine.clear();
        mLstDivideLine.add(findViewById(R.id.setting_refresh_moblie_network_header_downline));
        mLstDivideLine.add(findViewById(R.id.setting_refresh_wifi_network_header_upline));
        mLstDivideLine.add(findViewById(R.id.setting_refresh_wifi_network_header_downline));
        mLstItemMobile.clear();

        int t_titleId = mLlContent.indexOfChild(mLstDivideLine.get(0));
        for (int i = 0; i < 5; i++) {
            LinearLayout item = (LinearLayout) View.inflate(getContext(), R.layout.page_setting_refresh_lineitem, null);
            ViewHolder vh = new ViewHolder();
            vh.view = item;
            if (i == 0) {
                item.findViewById(R.id.lineView).setVisibility(View.GONE);
            }
            vh.vSelectedFlag = item.findViewById(R.id.setting_refresh_lineitem_slected_flag);
            vh.tvLineItemInvertal = (TextView) item.findViewById(R.id.setting_refresh_lineitem_tv);
            vh.vSelectedFlag.setVisibility(View.INVISIBLE);

            int t_interval = aryIntervals[i];
            String sItem = t_interval == 0 ? "不刷新" : t_interval + "秒刷新";
            vh.tvLineItemInvertal.setText(sItem);
            vh.nInterval = t_interval;

            int[] tags = new int[2];
            tags[0] = KIND_MOBILE_NETWORK;
            tags[1] = t_interval;

            vh.view.setTag(tags);

            mLstItemMobile.add(vh);
            mLlContent.addView(vh.view, ++t_titleId);

            vh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLineItemClick(v);
                }
            });
        }

        mLstItemWifi.clear();
        t_titleId = mLlContent.indexOfChild(mLstDivideLine.get(2));
        for (int i = 0; i < 5; i++) {
            LinearLayout item = (LinearLayout) View.inflate(getContext(), R.layout.page_setting_refresh_lineitem, null);
            ViewHolder vh = new ViewHolder();
            vh.view = item;
            if (i == 0) {
                item.findViewById(R.id.lineView).setVisibility(View.GONE);
            }
            vh.vSelectedFlag = item.findViewById(R.id.setting_refresh_lineitem_slected_flag);
            vh.tvLineItemInvertal = (TextView) item.findViewById(R.id.setting_refresh_lineitem_tv);
            vh.vSelectedFlag.setVisibility(View.INVISIBLE);

            int t_interval = aryIntervals[i];
            String sItem = t_interval == 0 ? "不刷新" : t_interval + "秒刷新";
            vh.tvLineItemInvertal.setText(sItem);
            vh.nInterval = t_interval;

            int[] tags = new int[2];
            tags[0] = KIND_WIFI_NETWORK;
            tags[1] = t_interval;

            vh.view.setTag(tags);

            mLstItemWifi.add(vh);
            mLlContent.addView(vh.view, ++t_titleId);

            vh.view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLineItemClick(v);
                }
            });
        }

        bindPageTitleBar(R.id.titlebar);
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onPageResume() {
        super.onPageResume();

        int[] selecteds_mobile = new int[2];
        selecteds_mobile[0] = KIND_MOBILE_NETWORK;
        selecteds_mobile[1] = DataModule.G_MOBLIEREFRESHTIMEINTERVAL;
        updateLineSelected(selecteds_mobile);

        int[] selecteds_wifi = new int[2];
        selecteds_wifi[0] = KIND_WIFI_NETWORK;
        selecteds_wifi[1] = DataModule.G_WIFIREFRESHTIMEINTERVAL;
        updateLineSelected(selecteds_wifi);
    }

    private void updateLineSelected(int[] selecteds) {
        if (selecteds[0] == KIND_MOBILE_NETWORK) {
            for (ViewHolder vh : mLstItemMobile) {
                if (vh.nInterval == selecteds[1]) {
                    vh.vSelectedFlag.setVisibility(View.VISIBLE);
                } else {
                    vh.vSelectedFlag.setVisibility(View.INVISIBLE);
                }
            }
        } else if (selecteds[0] == KIND_WIFI_NETWORK) {
            for (ViewHolder vh : mLstItemWifi) {
                if (vh.nInterval == selecteds[1]) {
                    vh.vSelectedFlag.setVisibility(View.VISIBLE);
                } else {
                    vh.vSelectedFlag.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void onLineItemClick(View view) {
        View vh = view;
        int[] tags = (int[]) view.getTag();
        updateLineSelected(tags);
        if (tags[0] == KIND_MOBILE_NETWORK) {
            DataModule.G_MOBLIEREFRESHTIMEINTERVAL = tags[1];
            getDBHelper().setInt(DataModule.G_KEY_MOBLIEREFRESHTIMEINTERVAL, DataModule.G_MOBLIEREFRESHTIMEINTERVAL);
        } else if (tags[0] == KIND_WIFI_NETWORK) {
            DataModule.G_WIFIREFRESHTIMEINTERVAL = tags[1];
            getDBHelper().setInt(DataModule.G_KEY_WIFIREFRESHTIMEINTERVAL, DataModule.G_WIFIREFRESHTIMEINTERVAL);
        }

    }


    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "行情数据刷新");
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

    class ViewHolder {
        View view = null;
        // LinearLayout llRefreshLineItemBg = null;
        View vSelectedFlag = null;
        TextView tvLineItemInvertal = null;
        int nInterval = -1;
    }
}
