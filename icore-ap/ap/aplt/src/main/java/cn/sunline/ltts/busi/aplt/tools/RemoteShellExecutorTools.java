package cn.sunline.ltts.busi.aplt.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;


public class RemoteShellExecutorTools {
    private static final BizLog bizlog = BizLogUtil.getBizLog(RemoteShellExecutorTools.class);
    private Connection conn;
    /** 远程机器IP */
    private String ip;
    /** 用户名 */
    private String osUsername;
    /** 密码 */
    private String password;
    private String charset = Charset.defaultCharset().toString();

    private static final int TIME_OUT = 1000 * 5 * 60;

    /**
     * 构造函数
     * @param ip
     * @param usr
     * @param pasword
     */
    public RemoteShellExecutorTools(String ip, String usr, String pasword) {
        this.ip = ip;
        this.osUsername = usr;
        this.password = pasword;
    }


    /**
    * 登录
    * @return
    * @throws IOException
    */
    private boolean login() throws IOException {
        conn = new Connection(ip);
        conn.connect();
        return conn.authenticateWithPassword(osUsername, password);
    }

    /**
    * 执行脚本
    * 
    * @param cmds
    * @return
    * @throws Exception
    */
    public int exec(String cmds) throws Exception {
    	if(bizlog.isDebugEnabled()){
    		
    		bizlog.debug("------------------exec:----------------------------------"+cmds);
    	}
    	
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outStr = "";
        String outErr = "";
        int ret = -1;
        try {
        if (login()) {
        	if(bizlog.isDebugEnabled()){
        		bizlog.debug("------------------login():---------------------------------------------------");
        	}

        	Session session = conn.openSession();

            session.execCommand(cmds);
            
            stdOut = new StreamGobbler(session.getStdout());
            outStr = processStream(stdOut, charset);
            
            stdErr = new StreamGobbler(session.getStderr());
            outErr = processStream(stdErr, charset);
            
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
            
            ret = session.getExitStatus();
        } else {
            throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
        }
        } finally {
            if (conn != null) {
                conn.close();
            }
            IOUtils.closeQuietly(stdOut);
            IOUtils.closeQuietly(stdErr);
        }
        return ret;
    }

    public String getIp() {
        return ip;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }


    public String getOsUsername() {
        return osUsername;
    }


    public void setOsUsername(String osUsername) {
        this.osUsername = osUsername;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    /**
    * @param in
    * @param charset
    * @return
    * @throws IOException
    * @throws UnsupportedEncodingException
    */
    private String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }
        return sb.toString();
    }
    
     /**
     * <p>Description:	</p>
     * @author 39yangmeng
     * @date   2018年4月6日 
     * @param remoteFile  目标文件地址
     * @param localTargetDirectory  本地文件夹
     * @throws Exception
     */
    public void get(String remoteFile,String localTargetDirectory) throws Exception {
    	if(bizlog.isDebugEnabled()){
    		bizlog.debug("------------------get:---------------------------------------------------");
    	}
         try {
         if (login()) {
        	 if(bizlog.isDebugEnabled()){
         		bizlog.debug("------------------login:---------------------------------------------------");
         	}
        	 
             SCPClient  scpClient = conn.createSCPClient();
//           scpClient.get("/home/odsuser/icmp/ods/20180405/ISOP_MIR_KNAACCT_AVG.IXF", "C:\\Users\\39yangmeng\\Desktop");
             scpClient.get(remoteFile, localTargetDirectory);
         } else {
             throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
         }
         } finally {
             if (conn != null) {
                 conn.close();
                 
             }
         }
     }
    
    /**
     * <p>Description:  </p>
     * @author 39yangmeng
     * @date   2018年4月6日 
     * @param localFile  本地文件
     * @param remoteTargetDirectory  远程文件夹
     * @throws Exception
     */
    public void put(String localFile,String remoteTargetDirectory) throws Exception {
    	if(bizlog.isDebugEnabled()){
    		bizlog.debug("------------------put:---------------------------------------------------");
    	}
     try {
         if (login()) {
        	 
        	 if(bizlog.isDebugEnabled()){
         		bizlog.debug("------------------login:---------------------------------------------------");
         	}
        	 
             SCPClient  scpClient = conn.createSCPClient();
             
//           scpClient.get("/home/odsuser/icmp/ods/20180405/ISOP_MIR_KNAACCT_AVG.IXF", "C:\\Users\\39yangmeng\\Desktop");
             scpClient.put(localFile, remoteTargetDirectory);
         } else {
             throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
         }
         } finally {
             if (conn != null) {
                 conn.close();
             }
         }
     }
    

   public static void main(String args[]) throws Exception {
    
//       RemoteShellExecutorTools executor = new RemoteShellExecutorTools("10.14.134.79", "odsuser", "2018ods");
//         executor.get("/home/odsuser/icmp/ods/20180405/ISOP_MIR_KNAACCT_AVG.IXF", "C:\\Users\\39yangmeng\\Desktop");
//       RemoteShellExecutorTools executor = new RemoteShellExecutorTools("10.14.128.241", "db2inst1", "sunline");
//       executor.put("C:\\Users\\39yangmeng\\Desktop\\ISOP_MIR_KNAACCT_AVG.IXF", "/home/db2inst1/icmpDb2");
//      RemoteShellExecutorTools executor = new RemoteShellExecutorTools("10.14.128.241", "db2inst1", "sunline");
//       executor.exec("sh /home/db2inst1/icmpDb2/icmpshell.sh");

       
   }
}
