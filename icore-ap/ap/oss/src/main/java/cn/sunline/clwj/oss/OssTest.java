package cn.sunline.clwj.oss;
//package cn.sunline.ltts.busi.aplt.oss;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import cn.sunline.ltts.busi.aplt.oss.api.OssFactory;
//import cn.sunline.ltts.busi.aplt.oss.model.MsFileInfo;
//import cn.sunline.ltts.busi.aplt.oss.model.MsTransferFileInfo;
//import cn.sunline.ltts.busi.aplt.oss.spi.MsTransfer;
//
//@SpringBootApplication
//public class OssTest {
//
//	public static void test() {
//		MsTransfer create = OssFactory.get().create();
//		if (create == null) {
//			System.out.println("null");
//		}
//		MsTransferFileInfo upFile = new MsTransferFileInfo();
//		upFile.setLocalFile(new MsFileInfo("F:/dd", "aa.txt"));
//		upFile.setRemoteFile(new MsFileInfo("F:/dd", "aaa.txt"));
//		create.upload(upFile );
//	}
//	
//	public static void main(String[] args) {
//		SpringApplication.run(OssTest.class, args);
//		test();
//		System.out.println("===");
//	}
//}
