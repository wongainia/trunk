package cn.emoney.acg.data;

import java.util.HashMap;
import java.util.Map;

import cn.emoney.acg.page.equipment.SupportEquipment;

public class QuoteSelectionStrategy {
    private static QuoteSelectionStrategy mInstance = null;

    public static final int ID_LNJB = 3; // 量能巨变
    public static final int ID_PZTP = 7; // 盘整突破
    public static final int ID_BDGZ = 15; // 波段跟庄
    public static final int ID_SLBW = 16; // 神龙摆尾
    public static final int ID_XBTC = 18; // 先拔头筹
    public static final int ID_FLZT = 21; // 飞龙在天
    public static final int ID_ZLZC = 24; // 主力增仓
    public static final int ID_ZLXC = 11; // 主力吸筹(筹码收集)
    public static final int ID_ZLQM = 25; // 主力强买

    private Map<Integer, Integer> mMapStrategyProtocol = null;

    private QuoteSelectionStrategy() {
        mMapStrategyProtocol = new HashMap<Integer, Integer>();
        mMapStrategyProtocol.put(SupportEquipment.ID_FLZT, ID_FLZT);
        mMapStrategyProtocol.put(SupportEquipment.ID_ZLQM, ID_ZLQM);
        mMapStrategyProtocol.put(SupportEquipment.ID_ZLXC, ID_ZLXC);
        mMapStrategyProtocol.put(SupportEquipment.ID_ZLZC, ID_ZLZC);
    }

    public static QuoteSelectionStrategy getInstance() {
        if (mInstance == null) {
            mInstance = new QuoteSelectionStrategy();
        }

        return mInstance;
    }

    public int getStrategyIdByProductId(int pid) {
        int ret = -1;
        if (mMapStrategyProtocol.containsKey(pid)) {
            ret = mMapStrategyProtocol.get(pid);
        }

        return ret;
    }

}
