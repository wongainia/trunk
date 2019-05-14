package cn.emoney.acg.page.optional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.Goods;
import cn.emoney.acg.data.IDUtils;
import cn.emoney.acg.data.protocol.info.GlobalMessage.MessageCommon;
import cn.emoney.acg.data.protocol.info.GlobalMessagePackage;
import cn.emoney.acg.data.protocol.info.InfoPackageImpl;
import cn.emoney.acg.helper.alert.StockAlertManagerV2;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.page.share.quote.SetAlertPage;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.page.PageIntent;
import cn.emoney.sky.libs.widget.RefreshListView;

/**
 * 编辑持仓
 * */
public class EditPositionPage extends PageImpl {

    /**
     * 进入界面时所有item的值构成的hashcode
     * */
    private int originHashCode;

    private RefreshListView listView;
    private ImageView ivDeleteZxg, ivAddType;

    private List<PositioinCellBean> listDatas = new ArrayList<PositioinCellBean>();
    private PositionListAdapter adapter;

    private PopupWindow mCheckTypeWin;
    private ListView mLvChoose;
    private PopChooseAdapter mChooseAdapter;
    private String mOptionalType = OptionalInfo.TYPE_POSITION;
    private final String ITEM_TYPE = "item_type";
    private List<Map<String, Object>> mLstChooseData = new ArrayList<Map<String, Object>>();
    private OptionalInfo optionalInfo;

    @Override
    protected void initPage() {
        setContentView(R.layout.page_edit_position);

        listView = (RefreshListView) findViewById(R.id.page_edit_position_list);
        ivDeleteZxg = (ImageView) findViewById(R.id.page_edit_position_item_iv_delzxg);
        ivAddType = (ImageView) findViewById(R.id.page_edit_position_item_iv_addtype);

        View listFooter = LayoutInflater.from(getContext()).inflate(R.layout.page_edit_position_listfooter, null);
        listView.addFooterView(listFooter);
        adapter = new PositionListAdapter(getContext(), listDatas);
        listView.setAdapter(adapter);

        optionalInfo = DataModule.getInstance().getOptionalInfo();

        // 设置删除点击事件
        ivDeleteZxg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取选中的cell的集合，然后执行删除操作
                final List<PositioinCellBean> deleteDatas = new ArrayList<PositioinCellBean>();
                for (int i = 0; i < listDatas.size(); i++) {
                    PositioinCellBean bean = listDatas.get(i);
                    if (bean.isChecked) {
                        deleteDatas.add(bean);
                    }
                }
                deletePositions(deleteDatas);
            }
        });

        // 设置加入分类点击事件
        ivAddType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionalTypeWin(v);
            }
        });

        // 点击空白处，隐藏输入法
        findViewById(R.id.layout_main).setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent mv) {
                InputMethodUtil.closeSoftKeyBoard(EditPositionPage.this);

                return false;
            }
        });

        bindPageTitleBar(R.id.page_edit_position_titlebar);
    }

    @Override
    protected void initData() {}

    @Override
    protected void onPageResume() {
        super.onPageResume();

        refreshFooterBar();
        refreshPosition();

        // 如果缓存中预警数据，从后台拉取预警数据
        StockAlertManagerV2.getInstance().requestStockAlertsIfCashEmpty(new StockAlertManagerV2.Operation() {

            @Override
            public void onSuccess(int typeTag) {
                if (typeTag == StockAlertManagerV2.TAG_REQUEST_LIST) {
                    refreshPosition();
                }
            }

            @Override
            public void onFail(int retCode) {

            }
        });
    }

    @Override
    protected void onPagePause() {
        super.onPagePause();

        InputMethodUtil.closeSoftKeyBoard(EditPositionPage.this);
    }

    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, "编辑持仓");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_search, null);
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
            // 1. 获取修改的数据
            // 2. 修改持仓数和成本价
            int destHashCode = getHashCode();
            if (originHashCode != destHashCode) {
                requestEditPosition(getEditPositions());
            } else {
                mPageChangeFlag = -1;
                finish();
            }
        } else if (itemId == 2 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            SearchPage.gotoSearch(EditPositionPage.this);
            // gotoSearch();
        }
    }

    private JSONArray getEditPositions() {
        JSONArray arrayPositions = new JSONArray();

        for (PositioinCellBean bean : listDatas) {
            JSONArray arrayItem = new JSONArray();
            arrayItem.add(0, "" + bean.goods.getGoodsId());
            arrayItem.add(1, bean.position + "#" + bean.cost);

            arrayPositions.add(arrayItem);
        }

        return arrayPositions;
    }

    /**
     * 刷新持仓列表显示
     * */
    private void refreshPosition() {
        List<Goods> listGoods = DataModule.getInstance().getOptionalInfo().getGoodsListByType(OptionalInfo.TYPE_POSITION);
        if (listGoods != null && listGoods.size() > 0) {
            listDatas.clear();
            for (int i = 0; i < listGoods.size(); i++) {
                Goods goods = listGoods.get(i);
                boolean isHasSetAlert = StockAlertManagerV2.getInstance().isStockHasSetAlert("" + goods.getGoodsId());
                PositioinCellBean bean = new PositioinCellBean(false, isHasSetAlert, goods);
                listDatas.add(bean);
            }
            originHashCode = getHashCode();

            adapter.notifyDataSetChanged();
        }
    }

    private int getHashCode() {
        StringBuilder sb = new StringBuilder();

        for (PositioinCellBean bean : listDatas) {
            sb.append(bean.goods.getGoodsId());
            sb.append(bean.position);
            sb.append(bean.cost);
        }

        return sb.toString().hashCode();
    }

    /**
     * 删除选中的持仓
     * */
    private void deletePositions(final List<PositioinCellBean> listDeleteCells) {
        List<Goods> listGoods = new ArrayList<Goods>();
        for (int i = 0; i < listDeleteCells.size(); i++) {
            listGoods.add(listDeleteCells.get(i).goods);
        }

        if (isLogined()) {

            delZXG(OptionalInfo.TYPE_POSITION, listGoods, new OnOperateZXGListener() {
                @Override
                public void onOperate(boolean isSuccess, String msg) {
                    if (isSuccess) {
                        for (int i = 0; i < listDeleteCells.size(); i++) {
                            listDatas.remove(listDeleteCells.get(i));
                        }
                        adapter.notifyDataSetChanged();

                        OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
                        optionalInfo.reset(OptionalInfo.TYPE_POSITION);
                        for (int i = 0; i < listDatas.size(); i++) {
                            optionalInfo.addGoods(OptionalInfo.TYPE_POSITION, listDatas.get(i).goods);
                        }
                        optionalInfo.save(getDBHelper());

                        showTip("删除成功");
                        refreshFooterBar();
                    } else {
                        showTip("删除自选失败");
                    }
                }
            });
        } else {
            for (int i = 0; i < listDeleteCells.size(); i++) {
                listDatas.remove(listDeleteCells.get(i));
            }
            adapter.notifyDataSetChanged();

            OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
            optionalInfo.reset(OptionalInfo.TYPE_POSITION);
            for (int i = 0; i < listDatas.size(); i++) {
                optionalInfo.addGoods(OptionalInfo.TYPE_POSITION, listDatas.get(i).goods);
            }
            optionalInfo.save(getDBHelper());

            showTip("删除成功");
            refreshFooterBar();
        }
    }

    /**
     * 修改持仓数和成本价
     * */
    private void requestEditPosition(JSONArray arrayPositions) {
        if (arrayPositions != null && arrayPositions.size() > 0 && isLogined()) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(KEY_TOKEN, getToken());
                jsonObject.put(KEY_CLS, OptionalInfo.TYPE_POSITION);
                jsonObject.put(KEY_IDS, arrayPositions);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            requestInfo(jsonObject, IDUtils.ID_OPTIONAL_CHANGE_DETAIL_STOCK);
        }
    }

    /**
     * 处理修改持仓数和成本价的返回数据
     * */
    @Override
    protected void updateFromInfo(InfoPackageImpl pkg) {
        super.updateFromInfo(pkg);
        int id = pkg.getRequestType();

        if (id == IDUtils.ID_OPTIONAL_CHANGE_DETAIL_STOCK) {
            GlobalMessagePackage gmp = (GlobalMessagePackage) pkg;
            MessageCommon mc = gmp.getResponse();

            if (mc != null && mc.getMsgData() != null) {
                String msgData = mc.getMsgData();

                try {
                    JSONObject objReturn = JSONObject.parseObject(msgData);

                    if (objReturn != null && objReturn.containsKey("result")) {
                        int retCode = objReturn.getIntValue("result");

                        if (retCode == 0) {
                            // 修改成功
                            showTip("修改成功");

                            for (PositioinCellBean bean : listDatas) {
                                bean.goods.setPositionAmount(bean.position);
                                bean.goods.setPositionPrice(bean.cost);
                            }

                            mPageChangeFlag = -1;
                            finish();
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 修改失败
            showTip("修改失败");
            mPageChangeFlag = -1;
            finish();            
        }
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
                    Map<String, Object> map = mLstChooseData.get(index);

                    final String type = (String) map.get(ITEM_TYPE);

                    final ArrayList<Goods> lstGoods = new ArrayList<Goods>();
                    for (int i = 0; i < listDatas.size(); i++) {
                        PositioinCellBean bean = listDatas.get(i);
                        if (bean.isChecked) {
                            lstGoods.add(bean.goods);
                        }
                    }

                    final boolean bIsLogin = DataModule.getInstance().getUserInfo().isLogined();

                    if (!bIsLogin) {
                        optionalInfo.addAll(type, lstGoods);
                        optionalInfo.save(getDBHelper());
                        showTip("添加到" + type + "成功");
                    } else {
                        LogUtil.easylog("sky", "addZXG(type, lstGoods, new OnOperateZXGListener(), type = " + type);
                        addZXG(type, lstGoods, new OnOperateZXGListener() {
                            @Override
                            public void onOperate(boolean isSuccess, String msg) {
                                if (isSuccess) {
                                    optionalInfo.addAll(type, lstGoods);
                                    optionalInfo.save(getDBHelper());
                                    showTip("添加到" + type + "成功");
                                } else {
                                    showTip("添加到" + type + "失败");
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

    class PopChooseAdapter extends SimpleAdapter {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        public PopChooseAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

    }

    /**
     * 打开预警设置界面
     * */
    private void gotoAlertConfigPage(Goods goods) {
        // 判断预警个股数量是否已达到上限，是否允许继续添加预警
        if (StockAlertManagerV2.getInstance().isSetAlertAllowed(String.valueOf(goods.getGoodsId()))) {
            // 打开预警设置界面
            PageIntent intent = new PageIntent(EditPositionPage.this, SetAlertPage.class);
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

    /**
     * 刷新底部栏
     * */
    private void refreshFooterBar() {
        // 删除自选，加入分类是否可点击，如果有股票被勾选，显示为蓝色，如果无股票被勾选，显示为灰色
        if (listDatas != null && listDatas.size() > 0) {
            for (int i = 0; i < listDatas.size(); i++) {
                if (listDatas.get(i).isChecked) {
                    ivDeleteZxg.setEnabled(true);
                    ivAddType.setEnabled(true);
                    return;
                }
            }
        }
        ivDeleteZxg.setEnabled(false);
        ivAddType.setEnabled(false);
    }

    class PositionListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<PositioinCellBean> listDatas;

        public PositionListAdapter(Context context, List<PositioinCellBean> listDatas) {
            inflater = LayoutInflater.from(context);
            this.listDatas = listDatas;
        }

        @Override
        public int getCount() {
            return listDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return listDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_edit_position_listitem, null);

                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            // 初始化各项显示的值
            final PositioinCellBean bean = listDatas.get(position);
            if (bean.isChecked) {
                vh.imgCheck.setImageResource(R.drawable.img_check_checked);
            } else {
                vh.imgCheck.setImageResource(R.drawable.img_check_uncheck);
            }
            vh.tvGoodsName.setText(bean.goodsName);
            vh.tvGoodsCode.setText(bean.goodsCode);
            vh.etPosition.setText(bean.position);
            vh.etCost.setText(bean.cost);
            if (bean.isHasSetAlert) {
                vh.imgAlert.setImageResource(R.drawable.img_alert_seted);
            } else {
                vh.imgAlert.setImageResource(R.drawable.img_alert_unset);
            }
            vh.etPosition.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bean.position = s.toString().trim();
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {}
            });
            vh.etCost.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bean.cost = s.toString().trim();
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable s) {}
            });

            // 设置点击事件
            final ImageView imgCheck = vh.imgCheck;
            vh.viewCheck.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.isChecked) {
                        // 如果已经选中，取消选中
                        bean.isChecked = false;
                        imgCheck.setImageResource(R.drawable.img_check_uncheck);
                    } else {
                        // 如果未选中，选中
                        bean.isChecked = true;
                        imgCheck.setImageResource(R.drawable.img_check_checked);
                    }

                    refreshFooterBar();
                }
            });

            vh.viewAlert.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到设置预警界面
                    gotoAlertConfigPage(bean.goods);
                }
            });

            vh.layout.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodUtil.closeSoftKeyBoard(EditPositionPage.this);

                    return false;
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public View layout;

            public View viewCheck, viewAlert;
            public ImageView imgCheck, imgAlert;
            public TextView tvGoodsName, tvGoodsCode;
            public EditText etPosition, etCost;

            public ViewHolder(View view) {
                layout = view;

                viewCheck = view.findViewById(R.id.page_edit_position_listitem_layout_check);
                viewAlert = view.findViewById(R.id.page_edit_position_listitem_layout_alert);
                imgCheck = (ImageView) view.findViewById(R.id.page_edit_position_listitem_img_check);
                imgAlert = (ImageView) view.findViewById(R.id.page_edit_position_listitem_img_alert);
                tvGoodsName = (TextView) view.findViewById(R.id.page_edit_position_listitem_tv_goods_name);
                tvGoodsCode = (TextView) view.findViewById(R.id.page_edit_position_listitem_tv_goods_code);
                etPosition = (EditText) view.findViewById(R.id.page_edit_position_listitem_et_position);
                etCost = (EditText) view.findViewById(R.id.page_edit_position_listitem_et_cost);
            }
        }

    }

    /**
     * 存储持仓列表第一项的数据
     * */
    private class PositioinCellBean {
        public boolean isChecked, isHasSetAlert;
        public String goodsName, goodsCode, position, cost;
        public Goods goods;

        public PositioinCellBean(boolean isChecked, boolean isHasSetAlert, Goods goods) {
            this.isChecked = isChecked;
            this.isHasSetAlert = isHasSetAlert;
            this.goods = goods;

            this.goodsName = goods.getGoodsName();
            this.goodsCode = goods.getGoodsCode();
            this.position = goods.getPositionAmount();

            DecimalFormat df = new DecimalFormat("0.00");
            float price = Float.valueOf(goods.getPositionPrice());
            this.cost = df.format(price);
        }

    }


}
