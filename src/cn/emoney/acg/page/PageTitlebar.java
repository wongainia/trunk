package cn.emoney.acg.page;

import cn.emoney.sky.libs.bar.Bar;
import cn.emoney.sky.libs.bar.Bar.OnBarMenuSelectedListener;
import cn.emoney.sky.libs.bar.BarMenu;
import cn.emoney.sky.libs.bar.BarMenuItem;
import cn.emoney.sky.libs.page.Page;

public abstract class PageTitlebar extends Page {
	private int mTitlebarId = -1;
	private boolean bNeedUpdate = false;

	/**
	 * 绑定page titebar 即拥有该bar的所有控制权
	 * 
	 * @param barId
	 */
	public void bindPageTitleBar(int barId) {
		if (barId != mTitlebarId) {
			mTitlebarId = barId;
			bNeedUpdate = true;
		}
	}

	/**
	 * 解除绑定bar titebar 解除对该bar的所有控制权
	 * 
	 * @param barId
	 */
	public void unbindPageTitleBar(int barId) {
		mTitlebarId = -1;
	}

	/**
	 * 获取所有被绑定的bar
	 * 
	 * @return
	 */
	public int getBoundBarId() {
		return mTitlebarId;
	}

	/**
	 * 更新已绑定的bar
	 */
	private void onUpdateTitleBars() {
		if (bNeedUpdate == false) {
			return;
		}
		bNeedUpdate = false;
		if (getContentView() == null) {
			return;
		}
		if (mTitlebarId > 0) {
			if (!getUserVisibleHint()) {
				return;
			}
			Bar bar = (Bar) findViewById(mTitlebarId);
			if (bar != null) {
				BarMenu menu = new BarMenu();
				boolean bRet = onCreatePageTitleBarMenu(bar, menu);
				if (bRet) {
					bar.clearBarMenu();
					bar.addMenuItems(menu.getItems());
					bar.setOnBarMenuSelectedListener(new OnBarMenuSelectedListener() {
						@Override
						public void onItemSelected(int index, BarMenuItem item) {
							onPageTitleBarMenuItemSelected(item);
						}
					});
					bar.notifyBarSetChanged();

					onPageTitleBarMenuCreated(bar, menu);
				}

			}
		}
	}

	/**
	 * 标题菜单选中事件回调
	 * 
	 * @param menuitem
	 */
	public void onPageTitleBarMenuItemSelected(BarMenuItem menuitem) {

	}

	/**
	 * 创建标题菜单开始事件回调
	 * 
	 * @param barId
	 * @param menu
	 */
	protected boolean onCreatePageTitleBarMenu(Bar bar, BarMenu menu) {
		return false;
	}

	/**
	 * 创建标题菜单完成事件回调
	 * 
	 * @param barId
	 * @param menu
	 */
	protected void onPageTitleBarMenuCreated(Bar bar, BarMenu menu) {
	}

	@Override
	protected void onPageResume() {
		super.onPageResume();
		if (getUserVisibleHint()) {
			onUpdateTitleBars();
		}
	}
}
