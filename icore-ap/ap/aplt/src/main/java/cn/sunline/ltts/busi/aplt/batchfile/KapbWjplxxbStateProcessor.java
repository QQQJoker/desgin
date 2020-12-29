package cn.sunline.ltts.busi.aplt.batchfile;

import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 文件批量表状态处理
 * 
 * @author jizhirong
 *
 */
public class KapbWjplxxbStateProcessor {
	private kapb_wjplxxb wjplxxb;
	public KapbWjplxxbStateProcessor(kapb_wjplxxb wjplxxb){
		this.wjplxxb = wjplxxb;
	}
	/**
	 * 拆分交易失败
	 */
	public void splitFail(Throwable t){
		wjplxxb.setErrotx("文件拆分失败："+t.getMessage());
		wjplxxb.setBtfest(E_BTFEST.FAIL);
	}
	
	/**
	 * 拆分交易成功
	 */
	public void splitSuccess(Throwable t){
		wjplxxb.setErrotx("文件拆分成功，待下发");
		wjplxxb.setBtfest(E_BTFEST.WAIT_DISTRIBUTE);
	}
	
	
	/**
	 * 读取文件交易失败
	 */
	public void readFail(){
		
	}
	
	/**
	 * 处理失败
	 */
	public void handleFail(){
		
	}
	
	/**
	 * 写交易失败
	 */
	public void writeFail(){
		
	}
	
	/**
	 * 合并失败
	 */
	public void mergeFail(){
		
	}
}
