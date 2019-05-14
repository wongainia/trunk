package cn.emoney.acg.page.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import cn.emoney.acg.R;
import cn.emoney.acg.page.PageImpl;
import cn.emoney.acg.page.share.SearchPage;
import cn.emoney.acg.util.FontUtils;
import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.Bar.OnBarMenuSelectedListener;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuCustomItem;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.bar.BarMenuTextItem;
import cn.emoney.sky.libs.bar.TitleBar;
import cn.emoney.sky.libs.bar.ToolBar;
import cn.emoney.sky.libs.widget.PageSwitcher;
import cn.emoney.sky.libs.widget.PageSwitcher.OnPageSwitchListener;

public class RankBkHome extends PageImpl {

	private int rankType;

	private ToolBar tbCategory;
	private PageSwitcher pageSwitcher;
	public static RankBkHome mInstance;
	
	public RankBkHome() {
		mInstance = this;
	}

	@Override
	protected void initPage() {
		setContentView(R.layout.page_rank_bk_home);

		tbCategory = (ToolBar) findViewById(R.id.page_rankbkhome_toolbar);

		// 初始化版块类别
		tbCategory.setItemTextColor(getResources().getColor(R.color.t1));
		tbCategory.setItemSelectedTextColor(getResources().getColor(R.color.c4));
		tbCategory.setItemTextSize(FontUtils.SIZE_TXT_SELECTBAR);
		tbCategory.setSliderBackgroundResource(R.drawable.item_slider);
		tbCategory.setOnBarMenuSelectedListener(new OnBarMenuSelectedListener() {
			@Override
			public void onItemSelected(int index, BarMenuItem item) {
				pageSwitcher.setCurrentItem(index, false);
			}
		});
		tbCategory.notifyBarSetChanged();

		// 初始化子界面
		pageSwitcher = (PageSwitcher) findViewById(R.id.page_rankbkhome_pageswitcher);

		RankBkPage page = new RankBkPage();
		page.setBKType(RankBkPage.BK_TYPE_HY);
		BarMenuTextItem item = new BarMenuTextItem(0, "行业板块");
		page.registBar(tbCategory, item);
		pageSwitcher.addPage(page);

		page = new RankBkPage();
		page.setBKType(RankBkPage.BK_TYPE_GN);
		item = new BarMenuTextItem(1, "概念板块");
		page.registBar(tbCategory, item);
		pageSwitcher.addPage(page);

		page = new RankBkPage();
		page.setBKType(RankBkPage.BK_TYPE_DQ);
		item = new BarMenuTextItem(2, "地区板块");
		page.registBar(tbCategory, item);
		pageSwitcher.addPage(page);

		pageSwitcher.setOnPageSwitchListener(new OnPageSwitchListener() {

			@Override
			public void onPageSelected(int index) {
				tbCategory.setCurrentItem(index);
			}

			@Override
			public void onPageScrolled(int i, float v, int i2) {
			}

			@Override
			public void onPageScrollStateChanged(int i) {
			}
		});
		registViewWithPage(pageSwitcher);
		pageSwitcher.notifyPageSetChanged();

		bindPageTitleBar(R.id.page_rankbkhome_titlebar);
	}

	@Override
	protected void receiveData(Bundle arguments) {
		if (arguments == null) {
			return;
		}

		if (arguments.containsKey(RankBkPage.KEY_RANK_TYPE)) {
			rankType = arguments.getInt(RankBkPage.KEY_RANK_TYPE);
		}
	}

	@Override
	protected void initData() {
		for (int i = 0; i < pageSwitcher.getPageCount(); i++) {
			RankBkPage page = (RankBkPage) pageSwitcher.getPage(i);
			page.setRankType(rankType);
		}
	}

	@Override
	protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {

		View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
		BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
		leftItem.setTag(TitleBar.Position.LEFT);
		menu.addItem(leftItem);

		BarMenuTextItem centerItem = new BarMenuTextItem(1, "版块排名");
		centerItem.setTag(TitleBar.Position.CENTER);
		menu.addItem(centerItem);

		View rightItemView = View.inflate(getContext(), R.layout.include_layout_titlebar_item_search, null);
		BarMenuCustomItem rightItem = new BarMenuCustomItem(2, rightItemView);
		rightItem.setTag(TitleBar.Position.RIGHT);
		menu.addItem(rightItem);

		return true;
	}

	@Override
	protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {

	}

	@Override
	public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {
		super.onPageTitleBarMenuItemSelected(menuitem);

		int menuId = menuitem.getItemId();

		if (menuId == 0 && mPageChangeFlag == 0) {
			mPageChangeFlag = -1;
			finish();
		} else if (menuId == 2 && mPageChangeFlag == 0) {
			mPageChangeFlag = -1;
			SearchPage.gotoSearch(RankBkHome.this);
//			gotoSearch();
		}
	}

}
