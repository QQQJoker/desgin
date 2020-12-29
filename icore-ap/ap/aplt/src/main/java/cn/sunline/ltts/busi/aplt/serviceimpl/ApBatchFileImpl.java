package cn.sunline.ltts.busi.aplt.serviceimpl;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ap.iobus.servicetype.IoApBatchFile;
import cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct.IoApPlwjXinxInfo;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_plwjrwDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_plwjrw;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BTFLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BTFLTP;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

@cn.sunline.adp.core.annotation.Generated
public class ApBatchFileImpl implements IoApBatchFile {

//	@Override
//	public IoApPlwjXinxInfo selPlwjXinx(Long jilucxuh) {
//
//		kapb_plwjrw tblPlxx = Kapb_plwjrwDao.selectOne_odb1(sjilucxuh, false);
//
//		if (CommUtil.isNull(tblPlxx))
//			return null;
//
//		IoApPlwjXinxInfo cplPlxx = SysUtil.getInstance(IoApPlwjXinxInfo.class);
//		cplPlxx.setDqdcnhao(tblPlxx.getdcn_num());
//		cplPlxx.setFilescid(tblPlxx.getFilescid());
//		cplPlxx.setJiaoyirq(tblPlxx.getTran_date());
//		cplPlxx.setJilucxuh(tblPlxx.getJilucxuh());
//		cplPlxx.setPlwenjlb(tblPlxx.getPlwenjlb());
//		cplPlxx.setPlwjztbz(tblPlxx.getPlwjztbz());
//		cplPlxx.setWjcsminc(tblPlxx.getWjcsminc());
//		cplPlxx.setZhjiriqi(tblPlxx.getZhjiriqi());
//		cplPlxx.setZongbish(tblPlxx.getZongbish());
//		cplPlxx.setWenjshux(tblPlxx.getWenjshux());
//
//		return cplPlxx;
//	}
//
//	@Override
//	public void updPlwjXinx(String filescid, Long yduqbish, E_PLWJSJZT plwjsjzt) {
//
//		kapb_plwjrw tblPlwsdj = Kapb_plwjrwDao.selectOne_odb1(filescid, true);
//
//		tblPlwsdj.setPlwjztbz(plwjsjzt);
//
//		Kapb_plwjrwDao.updateOne_odb1(tblPlwsdj);
//
//	}

	@Override
	public Options<IoApPlwjXinxInfo> selPlwjXinxList(E_BTFLST btflst, E_BTFLTP btfltp,Long qishibis, Long chxunbis) {

		List<kapb_plwjrw> lstPlxx = Kapb_plwjrwDao.selectPage_odb2(btfltp, btflst, qishibis, chxunbis,false);

		if (CommUtil.isNull(lstPlxx))
			return null;

		Options<IoApPlwjXinxInfo> lstPlwjXx = new DefaultOptions<IoApPlwjXinxInfo>();

		for (kapb_plwjrw tblPlxx : lstPlxx) {

			IoApPlwjXinxInfo cplPlxx = SysUtil.getInstance(IoApPlwjXinxInfo.class);

			cplPlxx.setCdcnno(tblPlxx.getCdcnno());
			cplPlxx.setFileid(tblPlxx.getFileid());
			cplPlxx.setTrandt(tblPlxx.getTrandt());
			cplPlxx.setRecdno(tblPlxx.getRecdno());
			cplPlxx.setBtfltp(tblPlxx.getBtfltp());
			cplPlxx.setFilena(tblPlxx.getFilena());
			cplPlxx.setBtflst(tblPlxx.getBtflst());
			cplPlxx.setHostdt(tblPlxx.getHostdt());
			cplPlxx.setFilepr(tblPlxx.getFilepr());
			lstPlwjXx.add(cplPlxx);
		}
		return lstPlwjXx;
	}

}
