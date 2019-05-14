package cn.emoney.acg.page.equipment.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.optional.OnOperateZXGListener;
import cn.emoney.acg.page.optional.OptionalInfo;
import cn.emoney.acg.page.share.quote.QuoteJump;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;

public class ZDLHDetailPage extends PageImpl {
    private final int ADD = 1;
    private final int DEL = -1;

    private TextView mTvTitle = null;
    private TextView mTvTime = null;

    private TextView mTvContent = null;

    private ImageView mTvAddZXG = null;

    private Map<String, String> mMapInfo = null;

    private Goods mCurGood = null;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_equipment_zdlh_detail);

        mTvTitle = (TextView) findViewById(R.id.zdlhdetail_tv_title);
        mTvTime = (TextView) findViewById(R.id.zdlhdetail_tv_time);
        mTvContent = (TextView) findViewById(R.id.zdlhdetail_tv_content);

        mTvAddZXG = (ImageView) findViewById(R.id.zdlhdetail_tv_addzxg);

        findViewById(R.id.zdlhdetail_tv_gotoquote).setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (mCurGood != null) {
                    QuoteJump.gotoQuote(ZDLHDetailPage.this, mCurGood);
//                    gotoQuote(mCurGood);
                }
            };
        });

        mTvAddZXG.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (mCurGood == null || !DataModule.getInstance().getUserInfo().isLogined()) {
                    return;
                }

                final OptionalInfo opInfo = DataModule.getInstance().getOptionalInfo();
                int tag = (Integer) v.getTag();
                if (tag == ADD) {
                    addZXG(OptionalInfo.TYPE_KEY_ALL, mCurGood, new OnOperateZXGListener() {
                        @Override
                        public void onOperate(boolean isSuccess, String msg) {
                            if (isSuccess) {
                                if (opInfo.addGoods(OptionalInfo.TYPE_DEFAULT, mCurGood)) {
                                    opInfo.save(getDBHelper());
                                    showTip("添加自选成功");
                                    updateAddZXGBtn(DEL);
                                } else {
                                    showTip("添加自选失败!");
                                }
                            } else {
                                showTip(msg);
                            }
                        }
                    });
                } else if (tag == DEL) {
                    delZXG(OptionalInfo.TYPE_KEY_ALL, mCurGood, new OnOperateZXGListener() {
                        @Override
                        public void onOperate(boolean isSuccess, String msg) {
                            if (isSuccess) {
                                if (opInfo.delGoods(OptionalInfo.TYPE_DEFAULT, mCurGood)) {
                                    opInfo.save(getDBHelper());
                                    showTip("删除自选成功");
                                    updateAddZXGBtn(ADD);
                                } else {
                                    showTip("删除自选失败!");
                                }
                            } else {
                                showTip(msg);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void initData() {
        if (mMapInfo != null) {
            try {
                if (mTvTitle != null) {
                    mTvTitle.setText(mMapInfo.get("detailTitle"));
                }

                if (mTvTime != null) {
                    mTvTime.setText(DataUtils.formatDateY_M_D(DataModule.G_CURRENT_SERVER_DATE + "", "-"));
                }

                if (mTvContent != null) {
                    mTvContent.setText(mMapInfo.get("content"));
                }

                String stockId = mMapInfo.get("stockId");

                String t_gid = null;
                ArrayList<Goods> lst = null;
                if (!stockId.startsWith("6")) {
                    t_gid = Util.FormatStockCode("1" + stockId);
                } else {
                    t_gid = Util.FormatStockCode(stockId);
                }

                lst = getSQLiteDBHelper().queryStockInfosByCode2(t_gid, 1);

                Goods g = null;
                if (lst != null && lst.size() > 0) {
                    mCurGood = lst.get(0);

                    if (DataModule.getInstance().getOptionalInfo().hasGoods(OptionalInfo.TYPE_DEFAULT, mCurGood.getGoodsId()) >= 0) {
                        updateAddZXGBtn(DEL);
                    } else {
                        updateAddZXGBtn(ADD);
                    }

                }

            } catch (Exception e) {
            }

        }

    }

    private void updateAddZXGBtn(int tag) {
        if (tag == ADD) {
            mTvAddZXG.setImageResource(R.drawable.img_quote_option_add);
        } else if (tag == DEL) {
            mTvAddZXG.setImageResource(R.drawable.img_quote_option_delete_blue);
        }
        mTvAddZXG.setTag(tag);
    }

    public void setData(Map<String, String> map) {
        mMapInfo = map;
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem mItemTitle = new BarMenuTextItem(1, "重大利好");
        mItemTitle.setTag(TitleBar.Position.CENTER);
        menu.addItem(mItemTitle);

        return true;
    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        LogUtil.easylog("sky", "FLZTPage -> onPageBarMenuItemSelected");
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    @Override
    protected void onPagePause() {
        // TODO Auto-generated method stub
        super.onPagePause();
        if (mMapInfo != null) {
            onSaveReadRecord(mMapInfo.get("newsId"));
        }
    }

    private void onSaveReadRecord(String newsId) {
        List<String> mLstReaded = null;
        String[] aryReaded = getDBHelper().getStringArray(DataModule.G_KEY_ZDLH_READ_RECORD, new String[] {});
        if (aryReaded != null) {
            mLstReaded = new ArrayList<String>(Arrays.asList(aryReaded));
        }

        if (mLstReaded != null) {
            if (mLstReaded.contains(newsId)) {
                return;
            }

            mLstReaded.add(0, newsId);
            if (mLstReaded.size() > DataModule.G_MAX_READSTATE_COUNT) {
                mLstReaded = mLstReaded.subList(0, DataModule.G_MAX_READSTATE_COUNT);
            }
        } else {
            mLstReaded = new ArrayList<String>();
            mLstReaded.add(newsId);
        }

        String[] newArys = (String[]) mLstReaded.toArray(new String[mLstReaded.size()]);
        getDBHelper().setStringArray(DataModule.G_KEY_ZDLH_READ_RECORD, newArys);
    }
}
