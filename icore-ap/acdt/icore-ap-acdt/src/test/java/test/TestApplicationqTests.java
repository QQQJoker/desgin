package test;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.gns.AcdtImpl;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年9月9日 下午7:33:27 
* 类说明 会计日期获取单元测试类
*/
@RunWith(PowerMockRunner.class)
@PrepareForTest(DaoUtil.class)
public class TestApplicationqTests {
	public List<AppAcdt> res =new LinkedList<AppAcdt>();
    @Before
    public void Before() {
    	
    	AppAcdt acdt =SysUtil.getInstance(AppAcdt.class);
    	acdt.setLastdt("20200909");
    	acdt.setNextdt("20200911");
    	acdt.setSystdt("20200910");
    	acdt.setValidt("2020-10-10 00:00:00");
    	acdt.setUpdttm("2020-08-28 00:00:00");
    	res.add(acdt);
    	acdt =SysUtil.getInstance(AppAcdt.class);
    	acdt.setLastdt("20200910");
    	acdt.setNextdt("20200912");
    	acdt.setSystdt("20200911");
    	acdt.setValidt("2020-10-11 00:00:00");
    	acdt.setUpdttm("2020-08-28 00:00:00");
    	res.add(acdt);
    	acdt =SysUtil.getInstance(AppAcdt.class);
    	acdt.setLastdt("20200911");
    	acdt.setNextdt("20200913");
    	acdt.setSystdt("20200912");
    	acdt.setValidt("2020-10-12 00:00:00");
    	acdt.setUpdttm("2020-08-28 00:00:00");
    	res.add(acdt);
        PowerMockito.mockStatic(DaoUtil.class);
        PowerMockito.when(DaoUtil.selectAll(AppAcdt.class)).thenReturn(res);
    }
    
    /**
     * 
     * 功能说明：测试本地线程获取
     * @author zhoujiawen
     * 2020年9月10日 下午2:15:48
     */
    @Test
    public void testGetAcdt() {
    	Date date,date1,date2;
    	date =new Date();
        AcdtImpl acdtImpl=new AcdtImpl();
        acdtImpl.getAcdt();
        date1 =new Date();
        AcdtImpl acdtImpl1=new AcdtImpl();
        acdtImpl.getAcdt();
        date2 =new Date();
        System.out.println(date2.getTime()-date1.getTime());
        System.out.println(date1.getTime()-date.getTime());
    }   
    
    @Test
    public void testThread() {
    	for (int i = 0; i <10; i++) {
    		new Thread(new Runnable() {
    			@Override
    			public void run() {
    				Date date,date1;
    		    	date =new Date();
    		        AcdtImpl acdtImpl=new AcdtImpl();
    		        acdtImpl.getAcdt();
    		        date1 =new Date();
    		        System.out.println(Thread.currentThread().getName()+":"+String.valueOf(date1.getTime()-date.getTime()));
    		        date =new Date();
    		        AcdtImpl acdtImpl1=new AcdtImpl();
    		        acdtImpl1.getAcdt();
    		        date1 =new Date();
    		        System.out.println(Thread.currentThread().getName()+":"+String.valueOf(date1.getTime()-date.getTime()));
    		        date =new Date();
    		        AcdtImpl acdtImpl2=new AcdtImpl();
    		        acdtImpl2.clean();
    		        acdtImpl2.getAcdt();
    		        date1 =new Date();
    		        System.out.println(Thread.currentThread().getName()+":"+String.valueOf(date1.getTime()-date.getTime()));
    			}
    		}).start();
		}
    	try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();

		}
    }
}
