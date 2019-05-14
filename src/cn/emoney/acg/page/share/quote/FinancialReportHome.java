package cn.emoney.acg.page.share.quote;

import android.content.Intent;
import android.os.Bundle;
import cn.emoney.acg.R;
import cn.emoney.sky.libs.module.Module;
import cn.emoney.sky.libs.page.PageIntent;

public class FinancialReportHome extends Module {

    private Bundle bundle;

    @Override
    public void receiveData(Intent intent) {
        super.receiveData(intent);

        bundle = intent.getExtras();
    }

    @Override
    public void initModule() {
        setContentView(R.layout.module_landscape);

        if (bundle != null) {
            PageIntent pi = new PageIntent(null, FinancialReportPage.class);
            pi.setArguments(bundle);
            startPage(R.id.module_landscape_frame, pi);
        } else {
            finish();
        }
    }

    @Override
    public void initData() { }

}
