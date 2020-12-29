package cn.sunline.ltts.busi.aplt.tools;
 
// import cn.sunline.bsap.sys.errors.BapError.Bap;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;

 
 
 
 
 
 public class BsapParamTools
 {
   private static String CenterCorpnoId = "bsap.centerCorpnoId";
   private static String BatchFilePathID = "bsap.batchFilePathID";
   
   private static String DefaultTranbr = "bsap.defaultTranbr";
   private static String DefaultTranus = "bsap.defaultTranus";
   
 
   public BsapParamTools() {}
   
   public static String getCenterCorpno()
   {
	   KnpPara corpPara = KnpParaDao.selectOne_odb1(CenterCorpnoId, "%", "%", "%","%", false);
     if (corpPara != null) {
       return corpPara.getPmval1();
     }
     return "340000";
   }
   
 
 
   public static BatchFileRootPath getBatchFilePath()
   {
	   KnpPara para = KnpParaDao.selectOne_odb1(BatchFilePathID, "%", "%", "%","%", false);
     if (para == null) {
      // BapError.Bap.E0011();
    	 
     }
     BatchFileRootPath ret = new BatchFileRootPath();
     ret.remoteRootPath = para.getPmval1();
     ret.localRootPath = para.getPmval2();
     return ret;
   }
   
 
 
 
 
 
 
 
 
   public static String getDefaultTranbr()
   {
     String tranbr = null;
     KnpPara knpPara = KnpParaDao.selectOne_odb1(DefaultTranbr, "%", "%", "%","%", false);
     if (knpPara != null) {
       tranbr = knpPara.getPmval1();
     }
     return tranbr;
   }
   
 
 
 
   public static String getDefaultTranus()
   {
     String tranus = null;
     KnpPara knpPara = KnpParaDao.selectOne_odb1(DefaultTranus, "%", "%", "%","%", false);
     if (knpPara != null) {
       tranus = knpPara.getPmval1();
     }
     return tranus;
   }
   
   public static class BatchFileRootPath
   {
     public String remoteRootPath;
     public String localRootPath;
     
     public BatchFileRootPath() {}
   }
 }

