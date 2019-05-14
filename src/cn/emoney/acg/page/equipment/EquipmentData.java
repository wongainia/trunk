package cn.emoney.acg.page.equipment;

public class EquipmentData {
	public int id;
	public String title;
	public String subTitle;
	public String imgId; /* 装备id&图片后缀 */
	public int noticePoint; /* 红点提示 1显示; 0不显示 */
	public int noticeFlag; /* new,hot提示 0:不提示; 1:new; 2:hot */

	public boolean hasPermission = false;
}