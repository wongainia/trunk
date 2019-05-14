package cn.emoney.acg.page.optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.dialog.CustomDialog.CustomDialogListener;
import cn.emoney.acg.dialog.DialogUtils;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.helper.alert.StockAlertManagerV2.Operation;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.quote.SetAlertPage;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuIconItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class EditOptionalPage extends PageImpl {
    public static final String EXTRA_KEY_OPTIONTYPE = "key_optionaltype";
    private static final short REQUEST_TYPE_ALERT_LIST = 1103;
    private static final short REQUEST_TYPE_ALERT_UPDATE = 1104;

    private ListView mListView = null;

    private List<Map<String, Object>> mLstData = new ArrayList<Map<String, Object>>();
    private OptionalAapter mAdapter = null;
    private OptionalInfo mOptionalInfo = null;

    private boolean mHasSorted = false;
    private final String ITEM_GOODS = "item_goods";
    private final String ITEM_IS_CHECKED = "item_is_checked";

    private final String ITEM_TYPE = "item_type";

    private ImageView mIvDel = null;
    private ImageView mIvAddTo = null;
    private PopupWindow mCheckTypeWin = null;
    private ListView mLvChoose = null;
    private PopChooseAdapter mChooseAdapter = null;

    private String mTitle = "编辑所有自选";
    private String mOptionalType = OptionalInfo.TYPE_DEFAULT;

    private List<Map<String, Object>> mLstChooseData = new ArrayList<Map<String, Object>>();

    private void checkButton() {
        List<Map<String, Object>> del = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < mLstData.size(); i++) {
            Map<String, Object> mapTemp = mLstData.get(i);
            boolean isCheckedTemp = (Boolean) mapTemp.get(ITEM_IS_CHECKED);
            if (isCheckedTemp) {
                del.add(mapTemp);
            }
        }

        if (del.size() == 0) {
            mIvDel.setEnabled(false);
            mIvAddTo.setEnabled(false);
        } else {
            mIvDel.setEnabled(true);
            mIvAddTo.setEnabled(true);
        }
    }

    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);
        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_OPTIONTYPE)) {
                mOptionalType = arguments.getString(EXTRA_KEY_OPTIONTYPE);
                mTitle = "编辑" + mOptionalType;
            }
        }
    }

    @Override
    protected void initPage() {

        setContentView(R.layout.page_editoptional);
        mListView = (ListView) findViewById(R.id.pageoptional_lv);
        if (mListView != null) {
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    // TODO Auto-generated method stub
                    Map<String, Object> map = mLstData.get(index);
                    boolean isChecked = (Boolean) map.get(ITEM_IS_CHECKED);
                    map.put(ITEM_IS_CHECKED, !isChecked);

                    mAdapter.notifyDataSetChanged();

                    checkButton();
                }

            });
        }

        mIvDel = (ImageView) findViewById(R.id.item_iv_delzxg);
        if (mIvDel != null) {
            mIvDel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final List<Map<String, Object>> del = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < mLstData.size(); i++) {
                        Map<String, Object> map = mLstData.get(i);
                        boolean isChecked = (Boolean) map.get(ITEM_IS_CHECKED);
                        if (isChecked) {
                            del.add(map);
                        }
                    }

                    if (del.size() == 0) {
                        showTip("请选择您要删除的股票!");
                        return;
                    }

                    doRemoveOptional(del);
                }

            });
        }

        mIvAddTo = (ImageView) findViewById(R.id.item_iv_addtype);
        if (mIvAddTo != null) {
            mIvAddTo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showOptionalTypeWin(v);
                }
            });
        }

        bindPageTitleBar(R.id.page_editoptional_titlebar1);

    }

    private void showOptionalTypeWin(View v) {
        int t_itemH = FontUtils.dip2px(getContext(), 45);

        if (mCheckTypeWin == null) {

            mLvChoose = new ListView(getActivity());
            mLvChoose.setDivider(getContext().getResources().getDrawable(R.drawable.img_light_divider_line_short));
            mLvChoose.setDividerHeight(1);
            mLvChoose.setVerticalScrollBarEnabled(false);
            mLvChoose.setCacheColorHint(0x00000000);
            OptionalInfo typeInfo = DataModule.getInstance().getOptionalInfo();
            List<String> types = typeInfo.getTypesExcept(mOptionalType);
            for (int i = 0; i < types.size(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(ITEM_TYPE, types.get(i));
                mLstChooseData.add(map);
            }
            mChooseAdapter = new PopChooseAdapter(getContext(), mLstChooseData, R.layout.page_editoptional_poplistitem, new String[] {ITEM_TYPE}, new int[] {R.id.item_tv_title});

            mLvChoose.setAdapter(mChooseAdapter);
            mLvChoose.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    // TODO Auto-generated method stub
                    Map<String, Object> map = mLstChooseData.get(index);

                    final String type = (String) map.get(ITEM_TYPE);

                    final ArrayList<Goods> lstGoods = new ArrayList<Goods>();
                    for (int i = 0; i < mLstData.size(); i++) {
                        Map<String, Object> m = mLstData.get(i);
                        boolean checked = (Boolean) m.get(ITEM_IS_CHECKED);
                        if (checked) {
                            Goods g = (Goods) m.get(ITEM_GOODS);
                            lstGoods.add(g);
                        }
                    }

                    if (type.equals(OptionalInfo.TYPE_POSITION)) {
                        int t_has = mOptionalInfo.getCountByType(type);
                        if ((lstGoods.size() + t_has) > 15) {
                            showTip("超过最大持仓个数(15个)");
                            mCheckTypeWin.dismiss();
                            return;
                        }
                    }

                    final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();

                    if (!bIsLogin) {
                        mOptionalInfo.addAll(type, lstGoods);
                        mOptionalInfo.save(getDBHelper());
                        showTip("添加自选成功");
                    } else {
                        LogUtil.easylog("sky", "addZXG(type, lstGoods, new OnOperateZXGListener(), type = " + type);
                        addZXG(type, lstGoods, new OnOperateZXGListener() {
                            @Override
                            public void onOperate(boolean isSuccess, String msg) {
                                // TODO Auto-generated method stub
                                if (isSuccess) {
                                    mOptionalInfo.addAll(type, lstGoods);
                                    mOptionalInfo.save(getDBHelper());
                                    showTip("添加自选成功");
                                } else {
                                    showTip("添加自选失败");
                                }
                            }
                        });
                    }

                    mCheckTypeWin.dismiss();
                }
            });

            mCheckTypeWin = new PopupWindow(getActivity());
            mCheckTypeWin.setWidth(FontUtils.dip2px(getContext(), 128));
            mCheckTypeWin.setHeight(LayoutParams.WRAP_CONTENT);
            mCheckTypeWin.setTouchable(true);
            mCheckTypeWin.setOutsideTouchable(true);
            mCheckTypeWin.setFocusable(true);
            mCheckTypeWin.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.img_editoptional_addtotype_bg));

            mCheckTypeWin.setContentView(mLvChoose);
        }

        int[] location = new int[2];
        v.getLocationOnScreen(location);

        int t_h = t_itemH * mLstChooseData.size();

        int startX = DataModule.SCREEN_WIDTH - FontUtils.dip2px(getContext(), 139);

        if (!mCheckTypeWin.isShowing()) {
            mCheckTypeWin.showAtLocation(v, Gravity.NO_GRAVITY, startX, location[1] - t_h - 20);

        }
    }

    @Override
    protected void initData() {

    }

    protected void onPageResume() {
        super.onPageResume();
        checkButton();
        refreshOptional();

        // 如果缓存中无预警数据，从后台拉取预警数据
        StockAlertManagerV2.getInstance().requestStockAlertsIfCashEmpty(new Operation() {
            @Override
            public void onSuccess(int typeTag) {
                if (typeTag == StockAlertManagerV2.TAG_REQUEST_LIST) {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFail(int retCode) {

            }
        });
    }

    private void refreshOptional() {
        mOptionalInfo = DataModule.getInstance().getOptionalInfo();
        mLstData.clear();

        List<Goods> lstGoods = DataModule.getInstance().getOptionalInfo().getGoodsListByType(mOptionalType);

        if (lstGoods != null) {
            for (int i = 0; i < lstGoods.size(); i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(ITEM_GOODS, lstGoods.get(i));
                map.put(ITEM_IS_CHECKED, false);
                mLstData.add(map);
            }

            if (mAdapter == null) {
                mAdapter = new OptionalAapter();
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 打开预警设置界面
     * */
    private void gotoAlertConfigPage(Goods goods) {
        // 判断预警个股数量是否已达到上限，是否允许继续添加预警
        if (StockAlertManagerV2.getInstance().isSetAlertAllowed(String.valueOf(goods.getGoodsId()))) {
            // 打开预警设置界面
            PageIntent intent = new PageIntent(EditOptionalPage.this, SetAlertPage.class);
            Bundle bundle = new Bundle();
            bundle.putString(SetAlertPage.KEY_STOCK_NAME, goods.getGoodsName());
            bundle.putString(SetAlertPage.KEY_STOCK_CODE, goods.getGoodsCode());
            bundle.putInt(SetAlertPage.KEY_STOCK_ID, goods.getGoodsId());
            intent.setArguments(bundle);
            intent.setSupportAnimation(true);
            intent.needPringLog(true);

            startPage(DataModule.G_CURRENT_FRAME, intent);
        } else {
            // 提示预警设置数量已达上限，不允许继续添加预警
            showTip("预警个股数量已达到上限");
        }
    }

    class OptionalAapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLstData.size();
        }

        @Override
        public Object getItem(int position) {
            return mLstData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_editoptional_listitem, null);
                TextView tvName = (TextView) convertView.findViewById(R.id.item_tv_stockname);
                TextView tvCode = (TextView) convertView.findViewById(R.id.item_tv_stockcode);
                ImageView ivChecked = (ImageView) convertView.findViewById(R.id.item_iv_ischecked);
                ImageView ivTop = (ImageView) convertView.findViewById(R.id.dragdroplistview_top);
                ImageView ivWarning = (ImageView) convertView.findViewById(R.id.item_iv_warning);

                ListCell lc = new ListCell(tvName, tvCode, ivChecked, ivTop, ivWarning);
                convertView.setTag(lc);
            }

            ListCell lc = (ListCell) convertView.getTag();

            final Map<String, Object> item = (Map<String, Object>) getItem(position);
            final Goods g = (Goods) item.get(ITEM_GOODS);
            lc.getTvName().setText(g.getGoodsName());
            lc.getTvCode().setText(g.getGoodsCode());
            boolean isChecked = (Boolean) item.get(ITEM_IS_CHECKED);
            lc.getIvChecked().setSelected(isChecked);

            lc.getIvTop().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Map<String, Object> item = mLstData.remove(position);
                    mLstData.add(0, item);
                    mAdapter.notifyDataSetChanged();

                    mOptionalInfo.reset(mOptionalType);
                    for (int i = 0; i < mLstData.size(); i++) {
                        mOptionalInfo.addGoods(mOptionalType, (Goods) mLstData.get(i).get(ITEM_GOODS));
                    }

                    mOptionalInfo.save(getDBHelper());

                    ArrayList<Object> t_lst_move = new ArrayList<Object>();
                    Goods g = (Goods) item.get(ITEM_GOODS);
                    t_lst_move.add(g);
                    t_lst_move.add(0);

                    String t_type = OptionalInfo.TYPE_KEY_ALL;
                    if (!mOptionalType.equals(OptionalInfo.TYPE_DEFAULT)) {
                        t_type = mOptionalType;
                    }
                    changeZXGSort(t_type, t_lst_move, new OnOperateZXGListener() {

                        @Override
                        public void onOperate(boolean isSuccess, String msg) {
                            if (isSuccess) {
                            }
                        }
                    });

                    mHasSorted = true;
                }
            });

            boolean bWarnSet = StockAlertManagerV2.getInstance().isStockHasSetAlert(g.getGoodsId() + "");
            lc.getIvWarning().setSelected(bWarnSet);
            lc.getIvWarning().setOnClickListener(new OnClickEffectiveListener() {
                @Override
                public void onClickEffective(View v) {
                    gotoAlertConfigPage(g);
                }
            });


            return convertView;
        }

        private class ListCell {
            public ListCell(TextView tvName, TextView tvCode, ImageView ivChecked, ImageView ivTop, ImageView ivWarning) {
                this.tvName = tvName;
                this.tvCode = tvCode;
                this.ivChecked = ivChecked;
                this.ivTop = ivTop;
                this.ivWarning = ivWarning;
            }

            private TextView getTvName() {
                return tvName;
            }

            private TextView getTvCode() {
                return tvCode;
            }

            private ImageView getIvChecked() {
                return ivChecked;
            }

            private ImageView getIvTop() {
                return ivTop;
            }

            private ImageView getIvWarning() {
                return ivWarning;
            }

            TextView tvName = null;
            TextView tvCode = null;
            ImageView ivChecked = null;
            ImageView ivTop = null;
            ImageView ivWarning = null;
        }

    }

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        int itemId = menuitem.getItemId();
        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {

        View leftView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuIconItem mTitleItem = new BarMenuIconItem(1, mTitle);
        mTitleItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(mTitleItem);

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mHasSorted) {
                finish();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    class PopChooseAdapter extends SimpleAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        public PopChooseAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

    }

    private void subDoRemoveOptional(final List<Map<String, Object>> del, final List<Goods> lst_goods_del, final DelOptionalCallBack callBack) {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();

        String t_type = OptionalInfo.TYPE_KEY_ALL;
        if (!mOptionalType.equals(OptionalInfo.TYPE_DEFAULT)) {
            t_type = mOptionalType;
        }

        final String controlType = t_type;

        if (userInfo.isLogined()) {


            List<String> tLstWarnStock = null;


            if (mOptionalType == OptionalInfo.TYPE_DEFAULT) {
                tLstWarnStock = new ArrayList<String>();
                for (Goods g : lst_goods_del) {
                    String sGoodsid = g.getGoodsId() + "";
                    boolean bWarnSet = StockAlertManagerV2.getInstance().isStockHasSetAlert(sGoodsid);
                    if (bWarnSet) {
                        tLstWarnStock.add(sGoodsid);
                    }
                }
            }

            if (tLstWarnStock != null && tLstWarnStock.size() > 0) {
                StockAlertManagerV2.getInstance().removeWarns(tLstWarnStock, new StockAlertManagerV2.Operation() {
                    @Override
                    public void onSuccess(int typeTag) {
                        if (typeTag == StockAlertManagerV2.TAG_REMOVE) {
                            // TODO 删除预警成功再删除自选 需修改预警请求返回方式
                            requestRemoveZXG(del, lst_goods_del, callBack, controlType);
                        }
                    }

                    @Override
                    public void onFail(int retCode) {
                        showTip("删除自选失败");

                    }
                });
            } else {
                requestRemoveZXG(del, lst_goods_del, callBack, t_type);
            }

        } else {
            for (int i = 0; i < del.size(); i++) {
                mLstData.remove(del.get(i));
            }
            mAdapter.notifyDataSetChanged();

            mOptionalInfo.reset(mOptionalType);
            for (int i = 0; i < mLstData.size(); i++) {
                mOptionalInfo.addGoods(mOptionalType, (Goods) mLstData.get(i).get(ITEM_GOODS));
            }

            mOptionalInfo.save(getDBHelper());

            if (callBack != null) {
                callBack.onSuccess();
            }
        }
    }

    private void requestRemoveZXG(final List<Map<String, Object>> del, List<Goods> lst_goods_del, final DelOptionalCallBack callBack, String type) {
        delZXG(type, lst_goods_del, new OnOperateZXGListener() {
            @Override
            public void onOperate(boolean isSuccess, String msg) {
                if (isSuccess) {
                    for (int i = 0; i < del.size(); i++) {
                        mLstData.remove(del.get(i));
                    }
                    mAdapter.notifyDataSetChanged();

                    mOptionalInfo.reset(mOptionalType);
                    for (int i = 0; i < mLstData.size(); i++) {
                        mOptionalInfo.addGoods(mOptionalType, (Goods) mLstData.get(i).get(ITEM_GOODS));
                    }

                    mOptionalInfo.save(getDBHelper());

                    if (callBack != null) {
                        callBack.onSuccess();
                    }
                } else {
                    if (callBack != null) {
                        callBack.onFail();
                    }
                }
            }
        });
    }

    private void doRemoveOptional(final List<Map<String, Object>> del) {
        List<Goods> lst_goods_del = new ArrayList<Goods>();

        boolean bFlag_otherTypeContains = false;

        for (int i = 0; i < del.size(); i++) {
            Goods g = (Goods) del.get(i).get(ITEM_GOODS);
            lst_goods_del.add(g);

            if (mOptionalType.equals(OptionalInfo.TYPE_DEFAULT) && bFlag_otherTypeContains == false) {
                if (null != mOptionalInfo.hasGoodsExceptType(mOptionalType, g.getGoodsId())) {
                    bFlag_otherTypeContains = true;
                }
            }
        }

        final List<Goods> t_lst_good_del = lst_goods_del;
        if (bFlag_otherTypeContains) {
            String msgFormat = "从所有分类中删除%s股票";
            String msg = "";
            if (t_lst_good_del.size() > 1) {
                msg = String.format(msgFormat, "这些");
            } else {
                msg = String.format(msgFormat, "该");
            }

            DialogUtils.showMessageDialog(getActivity(), "提示", msg, "确认", "取消", new CustomDialogListener() {

                @Override
                public void onConfirmBtnClicked() {
                    subDoRemoveOptional(del, t_lst_good_del, new DelOptionalCallBack() {
                        @Override
                        public void onSuccess() {
                            mOptionalInfo.delGoods(OptionalInfo.TYPE_DEFAULT, t_lst_good_del);
                            checkButton();
                            showTip("删除成功");
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFail() {
                            mOptionalInfo.delGoods(OptionalInfo.TYPE_DEFAULT, t_lst_good_del);
                            showTip("删除失败");
                        }
                    });
                }

                @Override
                public void onCancelBtnClicked() {}
            });

        } else {
            subDoRemoveOptional(del, lst_goods_del, new DelOptionalCallBack() {
                @Override
                public void onSuccess() {
                    showTip("删除成功");
                    checkButton();
                }

                @Override
                public void onFail() {

                }
            });
        }

    }

    // 协议不太合理,每次只修改一只
    private void changeZXGSort(String type, ArrayList<Object> lstGoods, final OnOperateZXGListener listener) {
        UserInfo userInfo = DataModule.getInstance().getUserInfo();
        if (!userInfo.isLogined()) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TOKEN, userInfo.getToken());
            jsonObject.put(KEY_CLS, type);

            JSONArray jsonArraySort = new JSONArray();

            JSONObject goods_sort = new JSONObject();
            Goods g = (Goods) lstGoods.get(0);
            int to = (Integer) lstGoods.get(1);
            goods_sort.put(KEY_ID, g.getGoodsId());
            goods_sort.put(KEY_TO, to);
            jsonArraySort.add(goods_sort);

            jsonObject.put(KEY_SORTID, jsonArraySort);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        requestInfo(jsonObject, IDUtils.ID_OPTIONAL_CHANGE_SORT_STOCK, listener, false);
    }

    // 删除自选股操作回调
    public static interface DelOptionalCallBack {
        public void onSuccess();

        public void onFail();
    }
}
