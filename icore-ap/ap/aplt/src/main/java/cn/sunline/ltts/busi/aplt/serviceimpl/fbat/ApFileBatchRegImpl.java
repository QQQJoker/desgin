package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct.IoApWjplrwxxInfo;
//import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatchReg;
import cn.sunline.edsp.base.lang.Options;

/**
 * 文件批量信息登记服务实现 文件批量信息登记服务实现
 * 新的文件批量取代旧的文件批量，删除表，注释代码  xiejun 
 */
//@cn.sunline.adp.core.annotation.Generated
public class ApFileBatchRegImpl implements IoApFileBatchReg {

	@Override
	public void saveIoApWjplrwxx(IoApWjplrwxxInfo wjplrwxx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void appendApWjplrw(IoApWjplrwxxInfo wjplrwxx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IoApWjplrwxxInfo selWjplrwxx(String weituoho, String dcn_num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IoApWjplrwxxInfo selWjplrwWithLock(String weituoho, String dcn_num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Options<IoApWjplrwxxInfo> selIoApWjplrwxxLst(String weituoho) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveIoApWjplxx(IoApWjplrwxxInfo wjplrwxx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IoApWjplrwxxInfo selIoApWjplxx(String weituoho) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IoApWjplrwxxInfo selIoApWjplxxWithLock(String weituoho) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void appendApWjplxx(IoApWjplrwxxInfo wjplrwxx) {
		// TODO Auto-generated method stub
		
	}
	
}

/*public class ApFileBatchRegImpl implements IoApFileBatchReg {
	 *//**
	  * 保存文件批量任务(新增或者更新)
	  *
	  *//*
	@Override
	public void saveIoApWjplrwxx(final IoApWjplrwxxInfo wjplrwxx) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				SysFileTables.kapb_wjplrw kapb_wjplrw = SysFileTables.Kapb_wjplrwDao.selectOneWithLock_odb1(wjplrwxx.getEntrno(), wjplrwxx.getDcnnum(), false);
				//若记录不存在则新增，若存在则更新
				if (kapb_wjplrw == null) {
					kapb_wjplrw = SysUtil.getInstance(SysFileTables.kapb_wjplrw.class);
					CommUtil.copyProperties(kapb_wjplrw, wjplrwxx);
					SysFileTables.Kapb_wjplrwDao.insert(kapb_wjplrw);
				} else {
					CommUtil.copyProperties(kapb_wjplrw, wjplrwxx, true);
					SysFileTables.Kapb_wjplrwDao.updateOne_odb1(kapb_wjplrw);
				}
				return null;
			}
			
		});
		
	}
	
	*//**
	  * 追加的方式保存文件批量任务(新增或者更新)
	  *
	  *//*
	@Override
	public void appendApWjplrw(final IoApWjplrwxxInfo wjplrwxx) {
		
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				SysFileTables.kapb_wjplrw kapb_wjplrw = SysFileTables.Kapb_wjplrwDao.selectOneWithLock_odb1(wjplrwxx.getEntrno(), wjplrwxx.getDcnnum(), false);
				//若记录不存在则新增，若存在则更新
				if (kapb_wjplrw == null) {
					kapb_wjplrw = SysUtil.getInstance(SysFileTables.kapb_wjplrw.class);
					CommUtil.copyProperties(kapb_wjplrw, wjplrwxx);
					SysFileTables.Kapb_wjplrwDao.insert(kapb_wjplrw);
				} else {
					kapb_wjplrw.setSuccnm(wjplrwxx.getSuccnm() + kapb_wjplrw.getSuccnm());
					kapb_wjplrw.setFailnm(wjplrwxx.getFailnm() + kapb_wjplrw.getFailnm());
					kapb_wjplrw.setTotanm(wjplrwxx.getTotanm() + kapb_wjplrw.getTotanm());
					kapb_wjplrw.setErrosk(wjplrwxx.getErrosk());
					kapb_wjplrw.setErrotx(wjplrwxx.getErrotx());
					
					if (wjplrwxx.getFlbtst() == E_FLBTST.CLSB)
						kapb_wjplrw.setFlbtst(E_FLBTST.CLSB);
					SysFileTables.Kapb_wjplrwDao.updateOne_odb1(kapb_wjplrw);
				}
				return null;
			}
			
		});
		
	}

	*//**
	 * 批量文件查询信息
	 *//*
	@Override
	public cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct.IoApWjplrwxxInfo selWjplrwxx(String entrno, String dcnnum) {
		SysFileTables.kapb_wjplrw kapb_wjplrw = SysFileTables.Kapb_wjplrwDao.selectOne_odb1(entrno, dcnnum, false);
		if (kapb_wjplrw == null)
            return null;
		IoApBatchFileStruct.IoApWjplrwxxInfo ret = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
		CommUtil.copyProperties(ret, kapb_wjplrw);
		return ret;
	}

	*//**
	 * 根据委托号查询批量任务列表
	 *//*
	@Override
	public Options<IoApWjplrwxxInfo> selIoApWjplrwxxLst(String entrno) {
		Options<IoApBatchFileStruct.IoApWjplrwxxInfo> ret = new DefaultOptions<IoApBatchFileStruct.IoApWjplrwxxInfo>();
		
		List<SysFileTables.kapb_wjplrw> kapb_wjplrws = SysFileTables.Kapb_wjplrwDao.selectAll_odb2(entrno, false);
		
		for (SysFileTables.kapb_wjplrw kapb_wjplrw : kapb_wjplrws) {
			IoApBatchFileStruct.IoApWjplrwxxInfo info = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
			CommUtil.copyProperties(info, kapb_wjplrw);
			ret.add(info);
			
		}
				
		return ret;
	}

	*//**
	  * 保存文件批量信息(新增或更新)
	  *//*
	@Override
	public void saveIoApWjplxx(final IoApWjplrwxxInfo wjplrwxx) {

		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				SysFileTables.kapb_wjplxx kapb_wjplxx = SysFileTables.Kapb_wjplxxDao.selectOneWithLock_odb1(wjplrwxx.getEntrno(), false);
				//若记录不存在则新增，若存在则更新
				if (kapb_wjplxx == null) {
					kapb_wjplxx = SysUtil.getInstance(SysFileTables.kapb_wjplxx.class);
					CommUtil.copyProperties(kapb_wjplxx, wjplrwxx);
					
					SysFileTables.Kapb_wjplxxDao.insert(kapb_wjplxx);
				} else {
					
					CommUtil.copyProperties(kapb_wjplxx, wjplrwxx, true);
					SysFileTables.Kapb_wjplxxDao.updateOne_odb1(kapb_wjplxx);
				}
				return null;
			}
			
		});
		
	}

    *//**
     * 查询文件批量信息
     *//*
    @Override
    public IoApWjplrwxxInfo selIoApWjplxx(String entrno) {
        SysFileTables.kapb_wjplxx kapb_wjplxx = SysFileTables.Kapb_wjplxxDao.selectOne_odb1(entrno, false);
        if (kapb_wjplxx == null)
            return null;
        IoApBatchFileStruct.IoApWjplrwxxInfo ret = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
        CommUtil.copyProperties(ret, kapb_wjplxx);
        return ret;
    }

    *//**
	  * 追加的方式保存文件批量信息（主表）
	  *
	  *//*
	@Override
	public void appendApWjplxx(final IoApWjplrwxxInfo wjplrwxx) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				SysFileTables.kapb_wjplxx kapb_wjplxx = SysFileTables.Kapb_wjplxxDao.selectOneWithLock_odb1(wjplrwxx.getEntrno(), false);
				//若记录不存在则新增，若存在则更新
				if (kapb_wjplxx == null) {
					kapb_wjplxx = SysUtil.getInstance(SysFileTables.kapb_wjplxx.class);
					CommUtil.copyProperties(kapb_wjplxx, wjplrwxx);
					
					SysFileTables.Kapb_wjplxxDao.insert(kapb_wjplxx);
				} else {
					
					kapb_wjplxx.setSuccnm(wjplrwxx.getSuccnm() + kapb_wjplxx.getSuccnm());
					kapb_wjplxx.setFailnm(wjplrwxx.getFailnm() + kapb_wjplxx.getFailnm());
					kapb_wjplxx.setTotanm(wjplrwxx.getTotanm() + kapb_wjplxx.getTotanm());
					
					if (wjplrwxx.getFlbtst() == E_FLBTST.CLSB)
						kapb_wjplxx.setFlbtst(E_FLBTST.CLSB);
					
					SysFileTables.Kapb_wjplxxDao.updateOne_odb1(kapb_wjplxx);
				}
				return null;
			}
			
		});
	}
	
	*//**
	 * 带锁批量文件查询信息
	 *//*
	@Override
	public IoApWjplrwxxInfo selWjplrwWithLock(String entrno, String dcnnum) {
		SysFileTables.kapb_wjplrw kapb_wjplrw = SysFileTables.Kapb_wjplrwDao.selectOneWithLock_odb1(entrno, dcnnum, false);
		if (kapb_wjplrw == null)
            return null;
		IoApBatchFileStruct.IoApWjplrwxxInfo ret = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
		CommUtil.copyProperties(ret, kapb_wjplrw);
		return ret;
	}

	*//**
     * 带锁查询文件批量信息
     *//*
	@Override
	public IoApWjplrwxxInfo selIoApWjplxxWithLock(String entrno) {
		SysFileTables.kapb_wjplxx kapb_wjplxx = SysFileTables.Kapb_wjplxxDao.selectOneWithLock_odb1(entrno, false);
        if (kapb_wjplxx == null)
            return null;
        IoApBatchFileStruct.IoApWjplrwxxInfo ret = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
        CommUtil.copyProperties(ret, kapb_wjplxx);
        return ret;
	}

}*/
