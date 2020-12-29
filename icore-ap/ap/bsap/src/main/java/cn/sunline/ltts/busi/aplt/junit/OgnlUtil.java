package cn.sunline.ltts.busi.aplt.junit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ognl.ListPropertyAccessor;
import ognl.NullHandler;
import ognl.Ognl;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;

public class OgnlUtil {
    /**
     * 避免工具类实例化 <br/>
     */
    private OgnlUtil() {
        super();
    }

    public static void init() {
        OgnlRuntime.setPropertyAccessor(List.class, new ListPropertyAccessor() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object getProperty(Map context, Object target, Object name)
                    throws OgnlException {
                try {
                    Object ret = super.getProperty(context, target, name);
                    return ret;
                } catch (java.lang.IndexOutOfBoundsException e) {
                    if (target instanceof List && name instanceof Integer) {
                        HashMap ret = new HashMap();
                        ((List) target).add((Integer) name, ret);
                        return ret;
                    }
                    throw e;
                }
            }

            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public void setProperty(Map context, Object target, Object name,
                    Object value) throws OgnlException {
                List list = (List) target;
                try {
                    super.setProperty(context, target, name, value);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    list.add(value);
                }
            }
        });
        OgnlRuntime.setNullHandler(Map.class, new NullHandler() {
            @Override
            public Object nullPropertyValue(Map context, Object target,
                    Object property) {
                if (property instanceof String) {
                    HashMap<Object, Object> hashMap = new HashMap<>();
                    ((Map) target).put(property, hashMap);
                    return hashMap;
                }
                return null;
            }

            @Override
            public Object nullMethodResult(Map context, Object target,
                    String methodName, Object[] args) {
                return null;
            }
        });
    }

    /**
     * 根据OGNL表达式从对象中获取指定字段并按K-V形式放在Map中 <br/>
     * 
     * @param map
     *        待放置结果的Map对象
     * @param exp
     *        OGNL表达式（用于在对象中定位字段）
     * @param valueObj
     */
    public static void setValue(Map<String, Object> map, String exp,
            Object valueObj) {
        try {
            Ognl.setValue(exp, map, valueObj);
            // Ognl.setValue(exp, map, map, valueObj);
        } catch (OgnlException e) {
            throw ExceptionUtil.wrapThrow(e);
        }
    }

    /**
     * 根据OGNL表达式从Map中获取指定字段的值 <br/>
     * 
     * @param map
     *        放置值的Map对象
     * @param exp
     *        OGNL表达式（用于在Map中定位字段）
     * @return
     */
    public static Object getValue(Map<String, Object> map, String exp) {
        try {
            // OgnlRuntime.setCompiler(compiler);
            return Ognl.getValue(exp, map);
        } catch (OgnlException e) {
            throw ExceptionUtil.wrapThrow(e);
        }
    }

    public static void main(String[] args) {
        Map root = new HashMap();
        setValue(root, "comm_req.inpusq", "xx");
        System.out.println(getValue(root, "comm_req.inpusq"));
    }
}
