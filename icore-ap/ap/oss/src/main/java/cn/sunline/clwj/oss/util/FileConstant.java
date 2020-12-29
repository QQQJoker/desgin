package cn.sunline.clwj.oss.util;

/**
 * <p>
 * 文件功能说明：全局常量定义
 * </p>
 * 
 * @Author dingmk
 *         <p>
 *         <li>2018年03月22日-下午16:42:22</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20180322 dingmk：创建</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FileConstant {

	/** 默认协议Id */
	public static final String DEFAULT_PROTOCOL_ID = "default";

	/** 默认协议Id，从setting文件中获取默认id */
	public static String defaultProtocolId = DEFAULT_PROTOCOL_ID;

	/** 文件服务扩展点Id */
	public static final String FILE_SERVICE_EXTENSION_POINT = "file.comm_server";

	/** 文件服务扩展Id */
	public static final String FILE_SERVICE_EXTENSION_POINT_IMPL = ".impl";

	/** 本地编码 */
	public static final String LOCAL_ENCODING = "GB18030";

	/** 远程utf8编码 */
	public static final String REMOTE_ENCODING_UTF = "UTF8";

	/** 远程iso编码 */
	public static final String REMOTE_ENCODING_ISO = "ISO8859-1";

	/** 远程目录分隔符 */
	public static final char COMM_SEPARATOR = '/';

	/** 远程文件操作状态 */
	public static final String FILE_OK = ".ok";

	public static final String SimpleFTPClient17 = "删除远程文件%s失败";
	public static final String SimpleFTPClient01 = "FTP关闭失败";
	public static final String SimpleFTPClient02 = "FTP服务器[%s], 用户[%s], 远程文件[%s], 本地文件[%s] 下载成功,耗时 %s ms。";
	public static final String SimpleFTPClient03 = "文件输出流关闭失败";
	public static final String SimpleFTPClient04 = "FTP服务器[%s], 用户[%s], 远程文件[%s], 本地文件[%s] 上传成功, 耗时 %s ms。";
	public static final String SimpleFTPClient05 = "文件输入流关闭失败";

}
