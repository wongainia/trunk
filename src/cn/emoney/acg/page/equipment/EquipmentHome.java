package cn.emoney.acg.page.equipment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.equipment.klinehero.KLineHeroModule;
import cn.emoney.acg.page.equipment.strategy.FLZTPage;
import cn.emoney.acg.page.equipment.strategy.ZDLHPage;
import cn.emoney.acg.page.share.LoginPage;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.ViewUtil;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

/**
 * @ClassName: EquipmentHome
 * @Description:我的装备
 * @author xiechengfa
 * @date 2015年11月20日 下午4:11:57
 *
 */
public class EquipmentHome extends PageImpl {
    // public static final String KEY_QUIZ_LAST_STATUS = "key_quiz_last_status";
    public static final String KEY_QUIZ_USED_COUNT = "key_quiz_used_count";
    public static final String KEY_QUIZ_LIMIT_COUNT = "key_quiz_limit_count";

    private static final int EQUIPMENTHOME_PAGE_CODE = 40000;
    public static final int REFRESH_DATA = 400011;

    // public static boolean isNeedRefresh = true;
    private View blankView = null;
    private View feeLayout = null;
    private ListView mLvContent = null;
    private View mVKHeroContentBg = null;
    private List<EquipmentData> mLstLvEquimpent = new ArrayList<EquipmentData>();
    private EquipmentLvAdapter mLvAdapter = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_equipmenthome);

        mLvContent = (ListView) findViewById(R.id.pageequipment_lv_content);
        View header = View.inflate(getContext(), R.layout.page_equipmenthome_header, null);
        mVKHeroContentBg = header.findViewById(R.id.equipmenthome_header_khero_content);
        feeLayout = header.findViewById(R.id.feeLayout);
        View footerView = View.inflate(getContext(), R.layout.page_equipmenthome_footer, null);
        blankView = footerView.findViewById(R.id.blankView);

        mVKHeroContentBg.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                startModule(KLineHeroModule.class);
            }
        });

        mLvContent.addHeaderView(header, null, true);
        mLvContent.addFooterView(footerView);

        mLvAdapter = new EquipmentLvAdapter();
        mLvContent.setAdapter(mLvAdapter);

        bindPageTitleBar(R.id.page_equipment_titlebar);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPageResume() {
        super.onPageResume();
        refreshEquipmentData();
    }

    private void refreshEquipmentData() {
        // UserInfo userInfo = getUserInfo();

        mLstLvEquimpent.clear();
        mLstLvEquimpent.addAll(SupportEquipment.getInstance().getSupportEquipList());
        createUIByData();
    }

    private void createUIByData() {
        if (mLstLvEquimpent == null || mLstLvEquimpent.size() <= 0) {
            // 无数据
            blankView.setVisibility(View.GONE);
            feeLayout.setVisibility(View.GONE);
        } else {
            blankView.setVisibility(View.VISIBLE);
            feeLayout.setVisibility(View.VISIBLE);
        }
        mLvAdapter.notifyDataSetChanged();
    }

    private void showLoginDialog() {
        DialogUtils.showMessageDialog(getActivity(), "提示", "请先登录后使用相关利器。", "立即登录", "取消", new CustomDialogListener() {
            @Override
            public void onConfirmBtnClicked() {
                // TODO Auto-generated method stub
                LoginPage.gotoLogin(EquipmentHome.this, EQUIPMENTHOME_PAGE_CODE);
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * 处理item点击
     * 
     * @param id
     */
    public void doItemClick(int position) {
        if (mLstLvEquimpent.size() == 0) {
            return;
        }

        if (position < 0 && position > mLstLvEquimpent.size() - 1) {
            return;
        }

        EquipmentData tData = mLstLvEquimpent.get(position);

        if (tData.hasPermission == false) {
            boolean bLogined = DataModule.getInstance().getUserInfo().isLogined();
            if (bLogined == false) {
                // showTip("请先登录账号确认权限");
                showLoginDialog();
            } else {
                showTip("该装备未购买或已过期,去PC商城看看");
            }
            return;
        }

        switch (tData.id) {
            case SupportEquipment.ID_FLZT:
            case SupportEquipment.ID_ZLZC:
            case SupportEquipment.ID_ZLXC:
            case SupportEquipment.ID_ZLQM: {
                PageIntent intent = new PageIntent(this, FLZTPage.class);
                Bundle bundle = new Bundle();
                bundle.putInt(FLZTPage.EXTRA_KEY_TYPE, tData.id);
                intent.setArguments(bundle);
                startPage(DataModule.G_CURRENT_FRAME, intent);
            }
                break;

            case SupportEquipment.ID_CPX: {
                int goodid = getDBHelper().getInt(DataModule.G_KEY_LAST_LOOK_GOODID, 1);
                ArrayList<Goods> lstGoods = getSQLiteDBHelper().queryStockInfosByCode2(Util.FormatStockCode(goodid), 1);
                closeSQLDBHelper();
                if (lstGoods != null && lstGoods.size() > 0) {
                    QuoteJump.gotoQuote(EquipmentHome.this, lstGoods.get(0), TYPE_DAY);
//                    gotoQuote(lstGoods.get(0), TYPE_DAY);
                }
            }
                break;
            case SupportEquipment.ID_ZDLH: {
                PageIntent intent = new PageIntent(this, ZDLHPage.class);
                startPage(DataModule.G_CURRENT_FRAME, intent);
            }
                break;
            default:
                break;
        }
    }

    @Override
    protected View getPageBarMenuProgress() {
        return null;
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "装备");
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

    class EquipmentLvAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int tLen = mLstLvEquimpent.size();
            if (tLen == 0) {
                return 0;
            }
            return (tLen - 1) / 3 + 1;
        }

        @Override
        public EquipmentData getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_equipmenthome_listitem, null);

                LvHolder lvHolder = new LvHolder();
                for (int i = 0; i < 3; i++) {
                    int resId = getResIdByStr("id", "item", i);
                    View itemContent = convertView.findViewById(resId);
                    itemContent.setEnabled(false);
                    ImageView ivIcon = (ImageView) itemContent.findViewById(R.id.item_iv_icon);
                    TextView tvTitle = (TextView) itemContent.findViewById(R.id.item_tv_title);
                    ItemHolder itemHolder = new ItemHolder(position * 3 + i, itemContent, ivIcon, tvTitle);
                    lvHolder.mLstItem.add(itemHolder);
                }

                for (int i = 0; i < 3; i++) {
                    lvHolder.mLstItem.get(i).mContent.setOnClickListener(new OnClickEffectiveListener() {
                        @Override
                        public void onClickEffective(View v) {
                            Object o = ViewUtil.getViewTag(v);
                            if (o != null) {
                                int tIndex = (Integer) o;
                                doItemClick(tIndex);
                            }
                        }
                    });
                }

                convertView.setTag(lvHolder);
            }

            // convertView.setBackgroundColor(getTheme().getBgLine());

            LvHolder lvHolder = (LvHolder) convertView.getTag();
            lvHolder.closeAll();

            for (int i = 0; i < 3; i++) {
                int index = position * 3 + i;
                if (mLstLvEquimpent.size() <= index) {
                    break;
                }
                lvHolder.openItem(i);
                ItemHolder itemHolder = lvHolder.mLstItem.get(i);
                // itemHolder.mContent.setBackgroundResource(getTheme()
                // .getBgEquipmentItem());

                EquipmentData tdata = mLstLvEquimpent.get(index);
                if (tdata == null) {
                    break;
                }
                int iResIcon = 0;
                if (tdata.hasPermission) {
                    iResIcon = getResIdByStr("drawable", "img_equipment_ico_", tdata.id);
                } else {
                    iResIcon = getResIdByStr("drawable", "img_equipment_add_", tdata.id);
                }
                itemHolder.mIvIcon.setImageResource(iResIcon);

                itemHolder.mTvTitle.setText(tdata.title);
            }

            return convertView;
        }

        private class LvHolder {
            public List<ItemHolder> mLstItem = new ArrayList<ItemHolder>(3);

            public LvHolder() {
                mLstItem.clear();
            }

            public void closeAll() {
                for (int i = 0; i < mLstItem.size(); i++) {
                    mLstItem.get(i).mContent.setEnabled(false);
                    mLstItem.get(i).mIvIcon.setVisibility(View.INVISIBLE);
                    mLstItem.get(i).mTvTitle.setVisibility(View.INVISIBLE);
                }
            }

            public void openItem(int i) {
                if (i < 0 || i > mLstItem.size()) {
                    return;
                }
                mLstItem.get(i).mContent.setEnabled(true);
                mLstItem.get(i).mIvIcon.setVisibility(View.VISIBLE);
                mLstItem.get(i).mTvTitle.setVisibility(View.VISIBLE);
            }

        }

        private class ItemHolder {
            public ItemHolder(int id, View content, ImageView ivIcon, TextView tvTitle) {
                mId = id;
                mContent = content;
                mContent.setTag(mId);
                mIvIcon = ivIcon;
                mTvTitle = tvTitle;
            }

            public int mId = -1;
            public View mContent = null;
            public ImageView mIvIcon;
            public TextView mTvTitle;
        }

    }
}
