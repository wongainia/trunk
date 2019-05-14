package cn.emoney.acg.page.equipment.klinehero;

import cn.emoney.acg.R;
import cn.emoney.sky.libs.module.Module;
import cn.emoney.sky.libs.page.PageIntent;

public class KLineHeroModule extends Module {

    @Override
    public void initData() {
        setContentView(R.layout.module_klinehero);

        PageIntent intent = new PageIntent(null, KLineHeroPage.class);
        intent.setSupportAnimation(false);
        startPage(R.id.klinehero_frame, intent);
    }

    @Override
    public void initModule() {

    }

}
