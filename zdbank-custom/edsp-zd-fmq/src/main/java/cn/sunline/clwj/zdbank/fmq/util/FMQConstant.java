package cn.sunline.clwj.zdbank.fmq.util;

public class FMQConstant {
	
	/**IP合法性校验*/
	public final static String IP_VAILD = "((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))";
	
	/** “,”号 分隔符 */
	public final static String SEPARATOR_COMMA = ",";
	
	/** “,”号 分隔符 */
	public final static String URL_SPLIT = "/";
	
	/** “:”号 分隔符 */
	public final static String SEPARATOR_COLON = ":";
	/** “.”号 分隔符 */
	public final static String SEPARATOR_POINT = ".";
	
	/**head区域字段标识*/
	public final static String HEADER = "head";
	
	/**commData区域字段标识*/
	public final static String COMMDATA = "commData";
	
	/**body区域字段标识*/
	public final static String BODY = "body";
	
	/**sys区域字段标识*/
	public final static String SYSTEM = "system";
	/**Comm_res区域字段标识*/
	public final static String COMMRES = "commRes";	
	/**交易码ID*/
	public final static String TRAN_CODE = "tranCode";
	/**服务ID*/
	public final static String SERVICE_ID = "serviceId";
	/**全局业务流水*/
	public final static String GLB_SEQ_NO = "glbSeqno";
	/**系统间调用流水*/
	public final static String SYS_SEQ_NO = "sysSeqno";
	/**服务请求者身份*/
	public final static String OPERATION_ID = "operationId";
	/**识别ID*/
	public final static String SIGNID = "signId";
	/**发送方机构ID*/
	public final static String BRANCH_ID = "branchId";
	
	/**内部发送方机构ID*/
	public final static String TRANBR = "tranbr";
	
	/**渠道类型*/
	public final static String SOURCE_TYPE = "sourceType";
	/**终端标识*/
	public final static String WSLD = "wsld";
	/**请求系统编号*/
	public final static String SOURCE_SYSID = "sourceSysid";
	
	/**内部请求系统编号*/
	public final static String INPUCD = "inpucd";
	
	/**交易日期*/
	public final static String SYS_DATE = "sysDate";
	
	/**内部交易日期*/
	public final static String INPUDT = "inpudt";
	
	/**交易时间*/
	public final static String SYS_TIMESTAMP = "sysTimestamp";
	
	/**源节点编号*/
	public final static String SOURCE_BRANCH_NO = "sourceBranchno";
	
	/**目标节点编号*/
	public final static String DEST_BRANCH_NO = "destBranchno";
	
	/**bod MAC 校验值*/
	public final static String MACVAL = "macval";
	
	/**路由类型,比如手机号,用户ID,客户ID*/
	public final static String ROUTE_TYPE = "routeType";
	
	/**路由元素ID*/
	public final static String ROUTE_ID = "routeId";
	
	/**交易成功与否标志，S-成功，F-失败*/
	public final static String RETFLAG = "retFlag";

	/**交易返回代码，错误即错误码，成功即成功响应码*/
	public final static String RETCODE = "retCode";
	
	/**交易返回信息，错误即错误描述，成功即成功描述*/
	public final static String RETMSG = "retMsg";
	
	/**交易返回数据*/
	public final static String RETDATA = "retData";
	
	/**内部交易返回码*/
	public final static String ERORCD = "erorcd";
	
	/**内部交易返状态S、F*/
	public final static String STATUS = "status";
	
	/**内部交易响应信息*/
	public final static String ERORTX = "erortx";
	
	/**网关请求头要素user id*/
	public final static String JRGW_ENTERPRISE_USER_ID = "jrgw-enterprise-user-id";
	
	/**网关请求头要素 user type*/
	public final static String JRGW_USER_ID_TYPE = "jrgw-user-id-type";
	
	/**网关请求头要素 request time */
	public final static String GW_REQUEST_TIME = "gwRequestTs";
	
	/**网关请求头要素 appid */
	public final static String GW_APP_ID = "gwAppId";
	
	public final static String CHARSET = "Charset";
	
	public final static String CONTENT_TYPE = "Content-Type";

	public static final String SERVNO = "servno";
	
	public static final String IS_NOW = "1";
	
	
}
