package cn.emoney.acg.dialog;

/**
 * @ClassName: ListDialogMenuInfo
 * @Description: 带信息提示的列表对话框的菜单详细
 * @author xiechengfa
 * @date 2015年11月13日 下午1:51:22
 */
public class ListDialogMenuInfo {
    private int itemCode = 0;
    private int iconResId = 0;
    private String itemName = null;

    public ListDialogMenuInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ListDialogMenuInfo(int itemCode, String itemName) {
        super();
        this.itemCode = itemCode;
        this.itemName = itemName;
    }

    public ListDialogMenuInfo(int itemCode, int iconResId, String itemName) {
        super();
        this.itemCode = itemCode;
        this.iconResId = iconResId;
        this.itemName = itemName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getItemCode() {
        return itemCode;
    }

    public void setItemCode(int itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
