package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

public class BsapProcessManagerICore extends BsapProcessManagerDefault {

    /**
     * 
     * <p>Title:getCorpno </p>
     * <p>Description:	获取交易法人实现方法
     *      核心交易法人是必须上送的，所以不做内部处理
     *      交易法人为空才调入改方法，所以抛异常处理
     * </p>
     * @author cuijia
     * @date   2017年6月15日 
     * @param dataArea
     * @return
     * (non-Javadoc)
     * @see cn.sunline.ltts.busi.aplt.impl.BsapProcessManagerDefault#getCorpno(cn.sunline.adp.cedar.base.engine.data.DataArea)
     */
	@Override
	public String getCorpno(DataArea dataArea) {
	    throw ApError.Aplt.E0064();
	}

}
