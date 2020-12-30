package gltran;

import org.junit.Test;

import cn.sunline.ltts.busi.aplt.junit.BatchTest;

public class GlBatchTest extends BatchTest{

	@Test
	public void test_gl08(){
		setRunMode(RunMode.trans);
		initTxnPkg("gl08.json");
		call();
	}

	@Test
	public void test_gl89(){
		setRunMode(RunMode.trans);
		initTxnPkg("gl89.json");
		call();
	}
	
	@Test
	public void test_gl85(){
		setRunMode(RunMode.trans);
		initTxnPkg("gl85.json");
		call();
	}
	@Test
	public void test_group() {
		setRunMode(RunMode.group);
		initTxnPkg("group930.json");
		call();
	}
}
