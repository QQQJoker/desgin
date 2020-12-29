package cn.sunline.ltts.busi.aplt.spi;

import cn.sunline.edsp.base.factories.SPI;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;

@SPI
public interface KnsTranManager {

	public KnsTran getKnsTran() ;
}
