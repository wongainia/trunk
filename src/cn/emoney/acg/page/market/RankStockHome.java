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

/**
 * 个股排名
 * */
public class RankStockHome extends PageImpl {

	private int rankStockType = RankStockPage.RANK_TYPE_ZF;

	private ToolBar tbGroups;
	private PageSwitcher pageSwitcher;

	@Override
	protected void initPage() {
		setContentView(R.layout.page_rank_stock_home);

		tbGroups = (ToolBar) findViewById(R.id.page_rankstockhome_toolbar);

		// 初始化股票类别
		tbGroups.setItemTextColor(getResources().getColor(R.color.t1));
		tbGroups.setItemSelectedTextColor(getResources().getColor(R.color.c4));
		tbGroups.setItemTextSize(FontUtils.SIZE_TXT_SELECTBAR);
		tbGroups.setSliderBackgroundResource(R.drawable.item_slider);
		tbGroups.setOnBarMenuSelectedListener(new OnBarMenuSelectedListener() {
			@Override
			public void onItemSelected(int index, BarMenuItem item) {
				pageSwitcher.setCurrentItem(index, false);
			}
		});
		tbGroups.notifyBarSetChanged();

		// 初始化子界面
		pageSwitcher = (PageSwitcher) findViewById(R.id.page_rankstockhome_pageswitcher);

		RankStockPage page = new RankStockPage();
		page.setGroupType(RankStockPage.GROUP_SH_A);
		BarMenuTextItem item = new BarMenuTextItem(0, "上证");
		page.registBar(tbGroups, item);
		pageSwitcher.addPage(page);

		page = new RankStockPage();
		page.setGroupType(RankStockPage.GROUP_SZ_A);
		item = new BarMenuTextItem(1, "深证");
		page.registBar(tbGroups, item);
		pageSwitcher.addPage(page);

		page = new RankStockPage();
		page.setGroupType(RankStockPage.GROUP_SZ_ZXB);
		item = new BarMenuTextItem(2, "中小板");
		page.registBar(tbGroups, item);
		pageSwitcher.addPage(page);

		page = new RankStockPage();
		page.setGroupType(RankStockPage.GROUP_SZ_CYB);
		item = new BarMenuTextItem(3, "创业板");
		page.registBar(tbGroups, item);
		pageSwitcher.addPage(page);

		pageSwitcher.setOnPageSwitchListener(new OnPageSwitchListener() {

			@Override
			public void onPageSelected(int index) {
				tbGroups.setCurrentItem(index);
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

		bindPageTitleBar(R.id.page_rankstockhome_titlebar);
	}

	@Override
	protected void receiveData(Bundle arguments) {
		if (arguments == null) {
			return;
		}

		if (arguments.containsKey(RankStockPage.KEY_RANK_TYPE)) {
			rankStockType = arguments.getInt(RankStockPage.KEY_RANK_TYPE);
		}
	}

	@Override
	protected void initData() {
		for (int i = 0; i < pageSwitcher.getPageCount(); i++) {
			RankStockPage page = (RankStockPage) pageSwitcher.getPage(i);
			page.setRankType(rankStockType);
		}
	}

	@Override
	protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {

		View leftView = LayoutInflater.from(getContext()).inflate(R.layout.include_layout_titlebar_item_back, null);
		BarMenuCustomItem leftItem = new BarMenuCustomItem(0, leftView);
		leftItem.setTag(TitleBar.Position.LEFT);
		menu.addItem(leftItem);

		BarMenuTextItem centerItem = new BarMenuTextItem(1, "个股排名");
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
			SearchPage.gotoSearch(RankStockHome.this);
//			gotoSearch();
		}
	}

}
