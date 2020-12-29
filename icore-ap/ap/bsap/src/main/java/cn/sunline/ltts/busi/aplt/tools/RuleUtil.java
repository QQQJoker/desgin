package cn.sunline.ltts.busi.aplt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.ClassUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppMeta;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppMetaDao;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRule;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRuleDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineRuntimeContext;

/**
 * 简单规则引擎实现
 * 
 */
public class RuleUtil {

    private static final BizLog bizlog = BizLogUtil.getBizLog(RuleUtil.class);

    
    private static void checkRule(String grupcd) {
        Map<String, Object> customMap=new HashMap<>();
        checkRule(grupcd,customMap);
    }
    /**
     * 检查某一组规则是否Ok
     * 要求：每个规则表达式返回true，否则抛异常
     * 
     * @param grupcd
     * @param customMap 自定义Map及Table需要的条件，如：prodcd
     */
    public static void checkRule(String grupcd, Map<String, Object> customMap) {
    	KnpPara knpPara = CommTools.KnpParaQryByCorpno("RuleGrupcd", "%", "%", "%", true);
    	grupcd = knpPara.getPmval1();//默认规则分组
        List<String> rules = getRulecd(grupcd);
        if (rules == null) {
            bizlog.info("组[%s]下没定义任何规则", grupcd);
            return;
        }

        for (String rulecd : rules) {
            Object val = getValue(grupcd, rulecd, customMap);// 获取OGNL校验结果，这里要求必须是boolean
            bizlog.info("检查规则[%s],结果：[%s]", grupcd + "." + rulecd, val);
            //结果必须为boolean
            if (!(val instanceof Boolean)) {
                throw Aplt.E0413(grupcd + "." + rulecd + "表达式结果类型" + val.getClass() + "不是Boolean");
            }
            if (Boolean.FALSE == val) {
            	AppRule rule = AppRuleDao.selectOne_odb2(grupcd, rulecd, true);
                throw Aplt.E0413(grupcd + "." + rulecd + "表达式结果false，属性["+
            	rule.getRuletx()+"]校验失败，请检查属性值或修改OGNL表达式："+rule.getContxt());
            }
        }
    }

    /**
     * 
     * 通过表达式计算值
     *  
     * @param grupcd
     * @param customMap
     * @param rulecd
     */
    public static Object getValue(String grupcd, String rulecd,Map<String, Object> customMap) {
        AppRule rule = AppRuleDao.selectOne_odb2(grupcd, rulecd, true);
        Object val = evalRuleExpr(rule.getContxt(), customMap);
        return val;
    }

    private static List<String> getRulecd(String grupcd) {
        // 根据规则组代码查询规则编号
        List<AppRule> rules = AppRuleDao.selectAll_odb3(grupcd, false);
        List<String> rulecd = new ArrayList<String>();
        for (AppRule rule : rules) {
            rulecd.add(rule.getRulecd());
        }

        return rulecd;
    }

    /**
     * 
     * @param expr ${TEST_03} == ${TEST_01}
     * @param map 上下文
     * @return
     */
    private static Object evalRuleExpr(String expr, Map<String, Object> customMap) {
    	
        Set<String> metacdList = new HashSet<String>();
        Map<String, Object> ognlContext = new HashMap<>();
        //1 构造 metacdList
        Pattern p=Pattern.compile("\\$\\{(\\w+)\\}");
        Matcher m=p.matcher(expr.trim());
        while(m.find()){
        	metacdList.add(m.group(1).trim());
        }
         
        //  构造ognlContext,
        /**
         * 交易输入/交易属性/运行上线文/表/固定值/自定义
         */
        for (String metacd : metacdList) {
            AppMeta ruleMeta = AppMetaDao.selectOne_odb1(metacd, true);
            switch (ruleMeta.getMetasr()) {
            case FIX:
                ognlContext.put(metacd, ruleMeta.getMetavl());
                break;
            case INPUT:
            	EngineRuntimeContext requestContext = EngineContext.pop().getEngineRuntimeContext();
                Object inputMap = requestContext.getTrxInput();
                Object ret = ExpressionEvaluatorFactory.getInstance().findValue(ruleMeta.getMetavl(), (Map) inputMap, (Map) inputMap);
                ognlContext.put(metacd, ret);
                break;
            case PROP:
                requestContext = EngineContext.pop().getEngineRuntimeContext();
                Map propMap = (Map) requestContext.getTrxProperty();
                ret = ExpressionEvaluatorFactory.getInstance().findValue(ruleMeta.getMetavl(), propMap, propMap);
                ognlContext.put(metacd, ret);
                break;
            case OTHER:
                //ret = ExpressionEvaluatorFactory.getInstance().findValue(ruleMeta.getMetavl(), customMap, customMap);
            	if(customMap.containsKey(ruleMeta.getMetavl())){
            		ognlContext.put(metacd, customMap.get(ruleMeta.getMetavl()));	
            	}
                break;
            case RUNENV:
                RunEnvsComm runEnvs = CommTools.prcRunEnvs();
                ret = ExpressionEvaluatorFactory.getInstance().findValue(ruleMeta.getMetavl(), (Map) runEnvs, (Map) runEnvs);
                ognlContext.put(metacd, ret);
                break;
            case TABLE:
                //Table t = ModelFactoryUtil.getModelFactory().getModel(Table.class, fullId);
                String metavl = ruleMeta.getMetavl();
                String[] tableInfo = getTableInfoByMetavl(metavl);
                String tabName = tableInfo[0];
                String tabField = tableInfo[1];
                String tabOdbName = tableInfo[2];
                Class tableClazz = BaseApltPlugin.getTableClazzByName(tabName);
                if (tableClazz == null)
                    throw ExceptionUtil.wrapThrow("表找不到对应的Class" + tabName);
                Object dbRow = DaoUtil.selectOneByIndex(tableClazz, ClassUtil.getOdbClass(tableClazz, tabOdbName), customMap);
                if (dbRow == null) {
                    bizlog.info("表对应的表达式计算为空," + metavl);
                    ognlContext.put(metacd, "");
                } else {
                    ret = CommUtil.toMap(dbRow).get(tabField);
                    ognlContext.put(metacd, ret);
                }
                break;

            default:
                break;
            }
        }
        
        //不存在数据返回true
        if(CommUtil.isNull(ognlContext)){
        	return true;
        }
        //2 去掉 ${},计息为原生Ognl  ${TEST_03} == ${TEST_01}   ->  TEST_03 == TEST_01
        String onglExpr = expr.trim().replaceAll("\\$\\{", "").replaceAll("\\}", "").trim();
        return _eval(onglExpr, ognlContext);
    }

    /**
     * 
     * @param metavl
     * @return 表名、表字段、odb索引名称
     */
    protected static String[] getTableInfoByMetavl(String source) {
        String[] ret = new String[3];
        ret[0] = source.substring(0, source.indexOf("."));
        ret[1] = source.substring(source.indexOf(".") + 1, source.indexOf("["));
        ret[2] = source.substring(source.indexOf("[") + 1, source.indexOf("]"));
        return ret;
    }

    /**
     * 通过OGNL求表达式的值
     * 
     * @param map
     * @param contxt
     * @return
     */
    private static Object _eval(String ognlExpr, Map<String, Object> ognlContext) {

        Params ps = new Params().addAll(ognlContext);
        ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
        try {
            return ee.findValue(ognlExpr, ps, ps);
        } catch (Exception e) {
            throw new IllegalArgumentException("规则[" + ognlExpr
                    + "]求值错误, cause by: " + e.getMessage(), e);
        }
    }

}
