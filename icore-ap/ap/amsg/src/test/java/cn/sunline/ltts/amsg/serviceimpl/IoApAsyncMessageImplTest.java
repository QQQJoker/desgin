package cn.sunline.ltts.amsg.serviceimpl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class IoApAsyncMessageImplTest {

    /*
     * 测试表达式计算
     */
    @Test
    public void test() {
        String temp = "11${p1}33${p2}55${runEvn.acctno}";
        Map<String, Object> map = new HashMap<>();
        map.put("p1", "22");
        map.put("p2", "44");
        Map<String, Object> subMap = new HashMap<>();
        map.put("runEvn", subMap);
        subMap.put("acctno", "abc");
        String msgTemplet = IoApAsyncMessageImpl.getMsgTemplet(map, temp);
        Assert.assertEquals(msgTemplet, "1122334455abc");
    }

}
