package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.adp.boot.cedar.launch.batch.BatchExecutorImpl;
import cn.sunline.ltts.busi.aplt.junit.AbstractTest;

public class BsapBatchIDERunner extends BatchExecutorImpl {
    @Override
    public void run(String[] args) {
        AbstractTest.ideRun=true;
        super.run(args);
    }
}
