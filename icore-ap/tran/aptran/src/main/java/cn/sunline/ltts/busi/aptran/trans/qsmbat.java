package cn.sunline.ltts.busi.aptran.trans;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.ltts.busi.aplt.namedsql.ApltTabDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aptran.type.TaskInfo.QsmbatFileList;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SMBTST;


public class qsmbat {

public static void qsmbat( String busseq,  final cn.sunline.ltts.busi.aptran.trans.intf.Qsmbat.Output output){
	
	List<QsmbatFileList> fileList = new ArrayList<>();
 	
	List<kapb_wjplxxb> btwj = ApltTabDao.selKapbWjplxxbDetl(busseq, false);
	for(kapb_wjplxxb val : btwj){
		QsmbatFileList file = CommTools.getInstance(QsmbatFileList.class);
		file.setBtchno(val.getBtchno());
		file.setFiletp(val.getFiletp());
		if(val.getBtfest() == E_BTFEST.SUCC){

			file.setBtfest(E_SMBTST.SUCC);
		}else if(val.getBtfest() == E_BTFEST.FAIL){
			
			file.setBtfest(E_SMBTST.FAIL);
		}else{
		
			file.setBtfest(E_SMBTST.DOING);
		}
		fileList.add(file);
	}
	
	output.getFileList().addAll(fileList);

}
}
