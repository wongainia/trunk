package cn.emoney.acg.data;

import android.graphics.Color;


public class IndicatorColor {
    public static final int[] COLORS = new int[] {0xfff078e1, 0xffe94b35, 0xff8f3faf, 0xfff69400, 0xff00bd9c, 0xff3d2b3d, 0xff2ea3f4, 0xff0000ff, 0xff87db46, 0xff2ad8e3, 0xff800080, 0xffa00000, 0xff408080, 0xff008000, 0xff000080};

    public static int getColorBySort(int sortId) {
        if (sortId >= 0 && sortId < COLORS.length) {
            return COLORS[sortId];
        }

        return Color.BLACK;
    }

}
