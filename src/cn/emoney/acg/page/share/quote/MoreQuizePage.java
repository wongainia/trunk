package cn.emoney.acg.page.share.quote;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.emoney.acg.R;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.data.helper.QuoteHead;
import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Item;
import cn.emoney.acg.data.protocol.quiz.QuizRelatePackage;
import cn.emoney.acg.data.protocol.quiz.QuizRelateReply.QuizRalate_Reply;
import cn.emoney.acg.data.protocol.quiz.QuizRelateRequest.QuizRalate_Request;
import cn.emoney.acg.data.quiz.QuizItemInfo;
import cn.emoney.acg.media.AppMediaPlayer.MyMediaPlayerListener;
import cn.emoney.acg.media.AppMediaPlayerManager;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.quiz.QuizListAdapter;
import cn.emoney.acg.page.quiz.QuizListViewlListener;
import cn.emoney.acg.page.quiz.TeacherDetailPage;
import cn.emoney.acg.view.OnClickEffectiveListener;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.widget.RefreshListView;

public class MoreQuizePage extends PageImpl implements QuizListViewlListener, MyMediaPlayerListener {
    
    public static final String EXTRA_KEY_GOODS_NAME = "key_goods_name";
    public static final String EXTRA_KEY_GOODS_CODE = "key_goods_code";
    
    private final short REQUEST_TYPE_RELATIVE_QUIZE = 3001;
    
    private boolean isRequesting;
    private int playPos = -1;
    private String goodsName, goodsCode;
    
    private ArrayList<QuizItemInfo> listDatas = new ArrayList<QuizItemInfo>();
    private QuizListAdapter adapter;
    private AppMediaPlayerManager mediaPlayerManager;
    
    private RefreshListView listView;
    private View layoutProgressBar;
    private TextView tvEmpty;
    private View layoutLoadMore;
    
    @Override
    protected void initPage() {
        setContentView(R.layout.page_single_list);
        
        initViews();
    }
    
    @Override
    protected void receiveData(Bundle arguments) {
        super.receiveData(arguments);
        
        if (arguments != null) {
            if (arguments.containsKey(EXTRA_KEY_GOODS_NAME)) {
                goodsName = arguments.getString(EXTRA_KEY_GOODS_NAME);
            }
            if (arguments.containsKey(EXTRA_KEY_GOODS_CODE)) {
                goodsCode = arguments.getString(EXTRA_KEY_GOODS_CODE);
            }
            
            requestStockRelativeQuize(0);
        }
    }
    
    @Override
    protected void initData() {}
    
    @Override
    protected void onPageResume() {
        super.onPageResume();
        
        if (mediaPlayerManager != null) {
            mediaPlayerManager.setPageState(true);
        }
    }
    
    @Override
    protected void onPagePause() {
        super.onPagePause();
        
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onPause();
            mediaPlayerManager.setPageState(false);
        }
    }
    
    @Override
    protected void onPageDestroy() {
        super.onPageDestroy();
        
        if (mediaPlayerManager != null) {
            mediaPlayerManager.onReleasePlayer();
            mediaPlayerManager = null;
        }
    }
    
    private void initViews() {
        listView = (RefreshListView) findViewById(R.id.page_singlelist_list);
        layoutProgressBar = findViewById(R.id.page_singlelist_layout_loading);
        tvEmpty = (TextView) findViewById(R.id.page_singlelist_tv_empty);
        
        View listFooter = View.inflate(getContext(), R.layout.include_layout_listfooter_loadmore, null);
        layoutLoadMore = listFooter.findViewById(R.id.layout_listfooter_loading);
        
        listView.setRefreshHeaderHandleImg(R.drawable.img_refresh_arrowdown);
        listView.initWithHeader(R.layout.layout_listview_header);
        listView.setRefreshable(false);
        
        listView.addFooterView(listFooter);
        
        listView.setDivider(null);
        listView.setDividerHeight(0);
        
        adapter = new QuizListAdapter(this, this);
        adapter.setData(listDatas);
        listView.setAdapter(adapter);
        
        tvEmpty.setOnClickListener(new OnClickEffectiveListener() {
            @Override
            public void onClickEffective(View v) {
                if (!isRequesting) {
                    requestStockRelativeQuize(0);
                }
            }
        });
        
        bindPageTitleBar(R.id.page_singlelist_titlebar);
    }
    
    /**
     * 获取个股相关问答列表
     * */
    private void requestStockRelativeQuize(int lastId) {
        // 1. 构造QuoteHead，传入request type
        QuoteHead quoteHead = new QuoteHead(REQUEST_TYPE_RELATIVE_QUIZE);
        // 2. 传入QuoteHead，构造Package
        QuizRelatePackage pkg = new QuizRelatePackage(quoteHead);
        // 3. 构造Request并设置参数
        QuizRalate_Request request = QuizRalate_Request.newBuilder()
                .setTokenId(getToken())
                .setStocks(goodsCode)
                .setNeedCount(20)
                .setLastId(lastId)
                .build();
        // 4. 将Request设置到Package中
        pkg.setRequest(request);
        // 5. 发送请求requestQuote
        requestQuote(pkg, ID_STOCK_QUESTION);
        
        isRequesting = true;
        // 不允许下拉刷新
        listView.setRefreshable(false);
        layoutProgressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRequesting) {
                    isRequesting = false;

                    layoutProgressBar.setVisibility(View.GONE);

                    tvEmpty.setText("加载失败，请点击重试");
                    tvEmpty.setVisibility(View.VISIBLE);

                    listView.setRefreshable(false);
                }
            }
        }, DataModule.REQUEST_MAX_LIMIT_TIME);
    }
    
    @Override
    protected void updateFromQuote(QuotePackageImpl pkg) {
        super.updateFromQuote(pkg);
        
        if (pkg instanceof QuizRelatePackage && isRequesting) {
            isRequesting = false;
            layoutProgressBar.setVisibility(View.GONE);
            
            QuizRelatePackage ddpkg = (QuizRelatePackage) pkg;
            int id = ddpkg.getRequestType();

            if (id == REQUEST_TYPE_RELATIVE_QUIZE) {
                QuizRalate_Reply reply = ddpkg.getResponse();
                
                List<Item> listItems = reply.getItemsList();
                
                if (listItems != null && listItems.size() > 0) {
                    for (int i = 0; i < listItems.size(); i++) {
                        Item item = listItems.get(i);
                        listDatas.add(new QuizItemInfo(QuizItemInfo.TYPE_QUESTION, item));
                    }
                }
            }
            
            adapter.notifyDataSetChanged();
        }
    }
    
    @Override
    protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
        View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
        BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
        leftItem.setTag(TitleBar.Position.LEFT);
        menu.addItem(leftItem);

        BarMenuTextItem centerItem = new BarMenuTextItem(1, goodsName + "  问答");
        centerItem.setTag(TitleBar.Position.CENTER);
        menu.addItem(centerItem);

        return true;
    }

    @Override
    protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {}

    @Override
    public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
        super.onPageTitleBarMenuItemSelected(menuitem);
        int itemId = menuitem.getItemId();

        if (itemId == 0 && mPageChangeFlag == 0) {
            mPageChangeFlag = -1;
            finish();
        }
    }

    @Override
    public void onAppraise(long id, int lev) {
    }

    @Override
    public void onPlayVoice(int pos) {
        if (mediaPlayerManager == null) {
            mediaPlayerManager = new AppMediaPlayerManager(this);
            mediaPlayerManager.setPageState(true);
        }

        if (listDatas.get(pos).getQuizItem() != null) {
            mediaPlayerManager.onStartPlayer(listDatas.get(pos).getQuizItem().getAnswer().getVoiceUrl());
        }

        // 重置上次的状态
        if (playPos >= 0 && playPos != pos) {
            listDatas.get(playPos).getQuizItem().setPlaying(false);
        }

        // 更新列表
        adapter.notifyDataSetChanged();

        this.playPos = pos;
    }

    @Override
    public void onClickHeadIcon(int pos) {
        if (listDatas.get(pos).getQuizItem().getReplier() != null) {
            TeacherDetailPage.startPage(this, listDatas.get(pos).getQuizItem().getReplier().getId(), listDatas.get(pos).getQuizItem().getReplier().getNick());
        }
    }

    @Override
    public void onQuestionClose(int pos) {
    }

    @Override
    public void onPlayerStart() {
        if (listDatas != null) {
            listDatas.get(playPos).getQuizItem().setPlaying(true);
        }

        // 更新列表
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerPause() {
        if (listDatas != null) {
            listDatas.get(playPos).getQuizItem().setPlaying(false);
        }

        // 更新列表
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerCompletion() {
        listDatas.get(playPos).getQuizItem().setPlaying(false);

        // 更新列表
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerError() {
    }

}
