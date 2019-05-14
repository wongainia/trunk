package cn.emoney.acg.page.optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.UserInfo;
import cn.emoney.acg.dialog.FixToast;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.util.InputMethodUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.widget.GridViewEx;
import cn.emoney.acg.widget.GridViewEx.OnTouchBlankPositionListener;


/**
 * @ClassName: ChooseOptionalPage
 * @Description:自选分类编辑页面
 * @author xiechengfa
 * @date 2015年11月12日 下午6:12:24
 *
 */
public class ChooseOptionalPage extends PageImpl {
    public final static int RESULT_CHOOSE_OPTIONALTYPE = 1;
    private GridViewEx mGridView = null;
    // private BarMenuIconItem mCenterTitleItem = null;

    private final int MAX_SIZE = 12;// 最大分组数量
    private final String NAME_PRE = "自选";

    private final String ITEM_TITLE = "item_title";
    private final String ITEM_IS_SELECED = "item_is_selected";
    private final String ITEM_IS_EXTRA = "item_is_extra";
    private final String ITEM_IS_CURR = "item_is_curr";// 是否当前选项

    // private LinearLayout mLlOptionalContent = null;
    // private int nameIndex = 1;
    private View scrollView = null;

    private List<String> lstNames = null;
    private List<Map<String, Object>> mLstData = new ArrayList<Map<String, Object>>();

    public static final String EXTRA_KEY_TITLE = "title";
    private OptionalTypeAdapter mAdapter = null;

    private String mTitle = "自选";

    private int mLongSelectedIndex = -1;

    // private int mTxtMain;
    // private int mTxtMain1;

    public ChooseOptionalPage() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void initData() {
        // TODO Auto-generated method stub
        OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
        lstNames = optionalInfo.getTypes();
        for (int i = 0; i < lstNames.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ITEM_TITLE, lstNames.get(i));
            map.put(ITEM_IS_SELECED, false);
            map.put(ITEM_IS_EXTRA, false);
            if (mTitle != null && mTitle.equals(lstNames.get(i))) {
                map.put(ITEM_IS_CURR, true);
            } else {
                map.put(ITEM_IS_CURR, false);
            }

            mLstData.add(map);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ITEM_TITLE, "");
        map.put(ITEM_IS_SELECED, false);
        map.put(ITEM_IS_EXTRA, true);
        map.put(ITEM_IS_CURR, false);

        mLstData.add(map);

        refreshGrid();
    }

    @Override
    protected void initPage() {
        // TODO Auto-generated method stub
        setContentView(R.layout.page_chooseoptional);

        mGridView = (GridViewEx) findViewById(R.id.chooseoptionalpage_gridview);
        if (mGridView != null) {
            mAdapter = new OptionalTypeAdapter();
            mGridView.setAdapter(mAdapter);

            // 长压编辑
            mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position != mLstData.size() - 1) {
                        Map<String, Object> map_item = mLstData.get(position);

                        String t_title = (String) map_item.get(ITEM_TITLE);
                        if (t_title.equals(OptionalInfo.TYPE_DEFAULT) || t_title.equals(OptionalInfo.TYPE_POSITION)) {
                            // 所有自选和持仓不能编辑
                            FixToast.createMsg("系统分类不能删除");
                            return true;
                        }

                        if (mLongSelectedIndex >= 0) { // 有已存在的长压item
                            View itemView = mGridView.getChildAt(mLongSelectedIndex);
                            GrideItem gi = (GrideItem) itemView.getTag();
                            String sEt = gi.getEtTitle().getText().toString();

                            boolean bRet = checkTypeLegal(sEt);
                            if (bRet) { // 检查名称合法
                                String oldTitle = (String) mLstData.get(mLongSelectedIndex).get(ITEM_TITLE);
                                if (!oldTitle.equals(sEt)) { // 新名称与旧名称不等,需要修改
                                    updateOptionalType(oldTitle, sEt, position);
                                } else { // 名称相等,直接切换长压item
                                    resetGrideView();
                                    mLstData.get(position).put(ITEM_IS_SELECED, true);
                                    mLongSelectedIndex = position;
                                    refreshGrid();
                                }
                            } else {
                                // 不合法
                                return true;
                            }
                        } else {
                            // 无已存在的长压
                            mLstData.get(position).put(ITEM_IS_SELECED, true);
                            mLongSelectedIndex = position;
                            refreshGrid();
                        }
                    }
                    return true;
                }

            });

            mGridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                    if (mLongSelectedIndex > 0) {
                        View itemView = mGridView.getChildAt(mLongSelectedIndex);
                        GrideItem gi = (GrideItem) itemView.getTag();
                        String sEt = gi.getEtTitle().getText().toString();
                        boolean bRet = checkTypeLegal(sEt);
                        if (bRet) {
                            String oldTitle = (String) mLstData.get(mLongSelectedIndex).get(ITEM_TITLE);
                            if (!oldTitle.equals(sEt)) {
                                updateOptionalType(oldTitle, sEt, -1);
                            } else {
                                resetGrideView();
                            }
                        }
                        return;
                    }

                    if (index != mLstData.size() - 1) { // 跳转分类
                        Map<String, Object> map = mLstData.get(index);
                        String typeName = (String) map.get(ITEM_TITLE);
                        // Bundle bundle = new Bundle();
                        // bundle.putString("type", typeName);
                        // setResult(RESULT_CHOOSE_OPTIONALTYPE, bundle);
                        // finish();
                        startFinishAnimation(typeName);
                    } else { // 添加分类
                        int len = mLstData.size();
                        String newType = getNewOptionName();

                        if (len > MAX_SIZE || TextUtils.isEmpty(newType)) {
                            showTip("分类个数已达到最大");
                            return;
                        }

                        Map<String, Object> newMap = new HashMap<String, Object>();
                        newMap.put(ITEM_TITLE, newType);
                        newMap.put(ITEM_IS_SELECED, true);
                        newMap.put(ITEM_IS_EXTRA, false);
                        newMap.put(ITEM_IS_CURR, false);

                        int position = 0;
                        if (len > 0) {
                            mLstData.add(mLstData.size() - 1, newMap);
                            position = mLstData.size() - 2;
                        } else {
                            mLstData.add(newMap);
                            position = 0;
                        }

                        mLongSelectedIndex = position;
                        refreshGrid();
                    }
                }
            });
        }

        mGridView.setOnTouchBlankPositionListener(new OnTouchBlankPositionListener() {

            @Override
            public boolean onTouchBlankPosition() {
                // TODO Auto-generated method stub
                resetSelectState();
                return true;
            }
        });

        findViewById(R.id.contentLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                resetSelectState();
            }
        });


        findViewById(R.id.headLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startFinishAnimation(mTitle);
            }
        });

        findViewById(R.id.blankView).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onBlankClick();
            }
        });

        findViewById(R.id.blankView2).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onBlankClick();
            }
        });

        // bindBar(R.id.mainpage_titlebar);
        // 进入动画
        scrollView = findViewById(R.id.scrollViewLayout);
        scrollView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_in_top_));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 返回键
            startFinishAnimation(mTitle);
            return true;
        }
        // else if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
        // // 回车键
        // resetSelectState();
        // return true;
        // }
        return super.onKeyUp(keyCode, event);
    }

    private boolean checkTypeLegal(String typename) {
        OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
        List<String> lstTypes = optionalInfo.getTypes();

        if (typename.equals("")) {
            showTip("分类名不能为空");
            return false;
        }

        if (typename.length() > 5) {
            showTip("分类名须小于5个字符");
            return false;
        }

        if (mLstData.size() - 1 == lstTypes.size()) {
            if (!lstTypes.get(mLongSelectedIndex).equals(typename) && lstTypes.contains(typename)) {
                showTip("指定的分类名已存在,请使用其它分类名");
                return false;
            }
        }

        if (mLstData.size() - 1 > lstTypes.size()) {
            if (lstTypes.contains(typename)) {
                showTip("指定的分类名已存在,请使用其它分类名");
                return false;
            } else {
                UserInfo ui = getUserInfo();
                mLstData.get(mLongSelectedIndex).put(ITEM_TITLE, typename);

                if (ui.isLogined()) {
                    final String sType = typename;
                    requestControlOptionalType(1, typename, null, new OnOperateZXGListener() {
                        @Override
                        public void onOperate(boolean isSuccess, String msg) {
                            if (isSuccess) {
                                OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
                                oi.addType(sType);
                                oi.save(getDBHelper());
                            } else {
                                showTip("添加失败");
                            }
                        }
                    });
                } else {
                    optionalInfo.addType(typename);
                    optionalInfo.save(getDBHelper());
                }
                return true;
            }
        }
        return true;
    }

    private void updateOptionalType(final String oldTitle, final String newTitle, final int newLongPosition) {
        UserInfo ui = getUserInfo();
        if (ui.isLogined()) {
            requestControlOptionalType(3, oldTitle, newTitle, new OnOperateZXGListener() {
                @Override
                public void onOperate(boolean isSuccess, String msg) {
                    OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
                    if (isSuccess) {
                        oi.updateTypeName(oldTitle, newTitle);
                        oi.save(getDBHelper());
                        mLstData.get(mLongSelectedIndex).put(ITEM_TITLE, newTitle);
                        mTitle = newTitle;
                        // mCenterTitleItem.setItemName(mTitle);
                    } else {
                        showTip("修改类型名称失败");
                    }

                    resetGrideView();
                    if (newLongPosition >= 0) {
                        mLstData.get(newLongPosition).put(ITEM_IS_SELECED, true);
                        mLongSelectedIndex = newLongPosition;
                        refreshGrid();
                    }
                }
            });
        } else {
            OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
            oi.updateTypeName(oldTitle, newTitle);
            oi.save(getDBHelper());
            mLstData.get(mLongSelectedIndex).put(ITEM_TITLE, newTitle);
            mTitle = newTitle;
            // mCenterTitleItem.setItemName(mTitle);
            resetGrideView();

            if (newLongPosition >= 0) {
                mLstData.get(newLongPosition).put(ITEM_IS_SELECED, true);
                mLongSelectedIndex = newLongPosition;
                refreshGrid();
            }
        }
    }


    private void resetGrideView() {
        InputMethodUtil.closeSoftKeyBoard(this);
        for (int i = 0; i < mLstData.size(); i++) {
            mLstData.get(i).put(ITEM_IS_SELECED, false);
        }
        mLongSelectedIndex = -1;
        refreshGrid();
    }

    @Override
    protected void receiveData(Bundle data) {
        if (data == null) {
            return;
        }

        if (data.containsKey(EXTRA_KEY_TITLE)) {
            mTitle = data.getString(EXTRA_KEY_TITLE);
        }
    }

    @Override
    public int enterAnimation() {
        // TODO Auto-generated method stub
        // return R.anim.slide_in_top;
        return 0;
    }

    @Override
    public int exitAnimation() {
        // TODO Auto-generated method stub
        // return R.anim.slide_out_top;
        return 0;
    }

    @Override
    public int popEnterAnimation() {
        // TODO Auto-generated method stub
        // return R.anim.slide_in_top;
        return 0;
    }

    @Override
    public int popExitAnimation() {
        // TODO Auto-generated method stub
        // return R.anim.slide_out_bottom2;
        return 0;
    }

    private void onBlankClick() {
        if (mLongSelectedIndex >= 0) {
            resetSelectState();
        } else {
            startFinishAnimation(mTitle);
        }
    }

    private void refreshGrid() {
        ViewGroup.LayoutParams lp = mGridView.getLayoutParams();
        int lineNum = mLstData.size() % 3 == 0 ? mLstData.size() / 3 : mLstData.size() / 3 + 1;
        int gridHeight = 2 * Util.getResourcesDimension(R.dimen.choose_gridview_pading_top_bottom) + (Util.getResourcesDimension(R.dimen.choose_item_h) + 2 * Util.getResourcesDimension(R.dimen.choose_item_pading_top_bottom)) * lineNum;
        lp.height = gridHeight;
        mGridView.setLayoutParams(lp);
        mAdapter.notifyDataSetChanged();
    }

    // 获取新创建的名称
    private String getNewOptionName() {
        List<String> tLstNames = DataModule.getInstance().getOptionalInfo().getTypes();

        if (tLstNames == null) {
            return "自选1";
        }

        for (int i = 0; i < MAX_SIZE; i++) {
            String name = NAME_PRE + (i + 1);
            if (!lstNames.contains(name)) {
                return name;
            }
        }

        return null;
    }

    // 重置选中的状态
    private void resetSelectState() {
        if (mLongSelectedIndex >= 0) {
            View itemView = mGridView.getChildAt(mLongSelectedIndex);
            GrideItem gi = (GrideItem) itemView.getTag();
            String sEt = gi.getEtTitle().getText().toString();

            boolean bRet = checkTypeLegal(sEt);
            if (bRet) {
                String oldTitle = (String) mLstData.get(mLongSelectedIndex).get(ITEM_TITLE);
                if (!oldTitle.equals(sEt)) {
                    updateOptionalType(oldTitle, sEt, -1);
                } else {
                    resetGrideView();
                }
            }
        }
    }

    class OptionalTypeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mLstData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mLstData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.page_chooseoptional_griditem, null);
                TextView tvTitle = (TextView) convertView.findViewById(R.id.item_tv_title);
                EditText etTitle = (EditText) convertView.findViewById(R.id.item_et_title);
                ImageView ivDel = (ImageView) convertView.findViewById(R.id.item_iv_del);
                etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)) {
                            // 回车键
                            resetSelectState();
                            return true;
                        }
                        return false;
                    }
                });
                GrideItem gi = new GrideItem(convertView.findViewById(R.id.item_v_bg), tvTitle, etTitle, ivDel);
                convertView.setTag(gi);
            }

            GrideItem gi = (GrideItem) convertView.getTag();

            // gi.getVBg().setBackgroundResource(getTheme().getInputFrame());

            Map<String, Object> map = (Map<String, Object>) getItem(position);
            String title = (String) map.get(ITEM_TITLE);
            boolean isExtra = (Boolean) map.get(ITEM_IS_EXTRA);
            boolean isSelected = (Boolean) map.get(ITEM_IS_SELECED);
            boolean isCurr = (Boolean) map.get(ITEM_IS_CURR);
            final int index = position;

            if (isExtra) {
                // 添加
                gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_extra);
                gi.getTvTitle().setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.img_optional_add, 0, 0);
                gi.getTvTitle().setText("添加分类");
                gi.getTvTitle().setTextColor(RColor(R.color.t3));
                gi.getTvTitle().setVisibility(View.VISIBLE);
                gi.getEtTitle().setVisibility(View.GONE);
                gi.getIvDel().setVisibility(View.INVISIBLE);
                gi.getIvDel().setOnClickListener(null);
                gi.getIvDel().setEnabled(false);
            } else if (isSelected) {
                // 选中
                gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_edit);
                gi.getTvTitle().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                gi.getTvTitle().setTextColor(RColor(R.color.t1));
                gi.getTvTitle().setVisibility(View.GONE);
                gi.getEtTitle().setVisibility(View.VISIBLE);
                gi.getEtTitle().setText(title);
                // gi.getEtTitle().setTextColor(getTheme().getTxtMain());

                gi.getEtTitle().setFocusable(true);
                gi.getEtTitle().setFocusableInTouchMode(true);
                gi.getEtTitle().requestFocus();
                gi.getEtTitle().setSelection(title.length()); // 将光标移至文字末尾

                InputMethodManager inputManager = (InputMethodManager) gi.getEtTitle().getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(gi.getEtTitle(), 0);

                gi.getIvDel().setVisibility(View.VISIBLE);
                gi.getIvDel().setEnabled(true);
                gi.getIvDel().setTag(title);
                gi.getIvDel().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OptionalInfo optionalInfo = DataModule.getInstance().getOptionalInfo();
                        List<String> lstTypes = optionalInfo.getTypes();
                        if (mLstData.size() - 1 > lstTypes.size()) {
                            // 删除新增的未传到服务器的分类
                            mLstData.remove(index);
                            resetGrideView();
                        } else {
                            // 真实的删除分类
                            UserInfo userInfo = getUserInfo();
                            final String sType = (String) v.getTag();

                            if (userInfo.isLogined()) {
                                requestControlOptionalType(2, sType, null, new OnOperateZXGListener() {
                                    @Override
                                    public void onOperate(boolean isSuccess, String msg) {
                                        if (isSuccess) {
                                            mLstData.remove(index);
                                            OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
                                            oi.removeType(sType);
                                            oi.save(getDBHelper());
                                        } else {
                                            showTip("删除失败");
                                        }
                                        resetGrideView();
                                    }
                                });
                            } else {
                                mLstData.remove(index);
                                OptionalInfo oi = DataModule.getInstance().getOptionalInfo();
                                oi.removeType(sType);
                                oi.save(getDBHelper());
                                resetGrideView();
                            }
                        }
                    }
                });
            } else {
                if (isCurr) {
                    // 选中
                    gi.getTvTitle().setTextColor(RColor(R.color.c3));
                    TextPaint tp = gi.getTvTitle().getPaint();
                    tp.setFakeBoldText(true);

                    if (position <= 1) {
                        // 固定
                        gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_fix_curr);
                    } else {
                        // 普通状态
                        gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_normal_curr);
                    }
                } else {
                    gi.getTvTitle().setTextColor(RColor(R.color.t1));
                    TextPaint tp = gi.getTvTitle().getPaint();
                    tp.setFakeBoldText(false);
                    if (position <= 1) {
                        // 固定
                        gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_fix);
                    } else {
                        // 普通状态
                        gi.getBgView().setBackgroundResource(R.drawable.shape_optioin_choose_item_normal);
                    }
                }

                gi.getTvTitle().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                gi.getTvTitle().setVisibility(View.VISIBLE);
                gi.getTvTitle().setText(title);
                gi.getEtTitle().setVisibility(View.GONE);
                gi.getIvDel().setVisibility(View.INVISIBLE);
                gi.getIvDel().setOnClickListener(null);
                gi.getIvDel().setEnabled(false);
            }

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return super.isEnabled(position);
        }
    }

    // 关闭的动画
    private void startFinishAnimation(final String title) {
        scrollView.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_out_bottom);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                scrollView.setVisibility(View.GONE);

                if (title != null && title.trim().length() > 0) {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", title);
                    setResult(RESULT_CHOOSE_OPTIONALTYPE, bundle);
                }
                finish();
            }
        });
        scrollView.setAnimation(animation);

    }

    private class GrideItem {
        private View bgView;
        private TextView tvTitle;
        private EditText etTitle;
        private ImageView ivDel;

        private GrideItem(View bgView, TextView tvTitle, EditText etTitle, ImageView ivDel) {
            this.bgView = bgView;
            this.tvTitle = tvTitle;
            this.etTitle = etTitle;
            this.ivDel = ivDel;
        }

        public View getBgView() {
            return bgView;
        }

        // public void setBgView(View bgView) {
        // this.bgView = bgView;
        // }

        public TextView getTvTitle() {
            return tvTitle;
        }

        public EditText getEtTitle() {
            return etTitle;
        }

        public ImageView getIvDel() {
            return ivDel;
        }
    }
}
