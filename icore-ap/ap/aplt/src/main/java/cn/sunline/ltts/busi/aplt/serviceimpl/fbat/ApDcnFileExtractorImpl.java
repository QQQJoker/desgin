package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcx;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcxDao;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileExtractor;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * R/C-DCN文件抽取器实现
 *
 */
@cn.sunline.adp.core.annotation.Generated
public class ApDcnFileExtractorImpl implements cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApDcnFileExtractor {
	/**
	 * 文件抽取
	 *
	 */
	public cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.ExtractFileOut extractFile(
			final cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.ExtractFileIn input) {
		// 根据文件类型获取不同的文件抽取器实现服务ID
		KnpSvcx svcx = KnpSvcxDao.selectOne_odb1(IoApFileExtractor.class.getSimpleName(), input.getBtflmd(),
				false);
		if (svcx == null) {
			throw ApError.Aplt.E0000("文件类型[" + input.getBtflmd() + "]对应的文件抽取服务实现ID未配置，请检查kapp_fwsxdy表配置是否正确！");
		}
		IoApFileExtractor fileExtractor = SysUtil.getInstance(IoApFileExtractor.class, svcx.getSvimky());
		return fileExtractor.extractFile(input);
	}
}
