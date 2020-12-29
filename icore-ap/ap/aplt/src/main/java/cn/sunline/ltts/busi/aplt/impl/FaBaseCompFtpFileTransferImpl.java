package cn.sunline.ltts.busi.aplt.impl;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.service.transfer.ApacheFtpFileTransferImpl;
import cn.sunline.ltts.busi.aplt.component.FaBaseComp.FileTransfer;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRemoteFileList;
import cn.sunline.adp.cedar.base.BaseConst;
import cn.sunline.adp.cedar.base.exception.EdspServiceException;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class FaBaseCompFtpFileTransferImpl extends ApacheFtpFileTransferImpl implements FileTransfer {
    
    private static final BizLog bizlog = BizLogUtil.getBizLog(FaBaseCompFtpFileTransferImpl.class);
    
    private static final Integer DEFAULT_CONNECT_TIMEOUT_IN_MS = Integer.valueOf(5000);
    private static final Integer DEFAULT_DATA_TIMEOUT_IN_MS = Integer.valueOf(60000);
    private static final Integer DEFAULT_RETRY_TIME = Integer.valueOf(5);
    private static final Integer DEFAULT_RETRY_INTERVAL = Integer.valueOf(5000);
    
    private Integer connectTimeoutInMs = DEFAULT_CONNECT_TIMEOUT_IN_MS;
    private Integer dataTimeoutInMs = DEFAULT_DATA_TIMEOUT_IN_MS;
    private Integer retryTime = DEFAULT_RETRY_TIME;
    private Integer retryInterval = DEFAULT_RETRY_INTERVAL;
    private Boolean binaryMode = Boolean.valueOf(true);
    protected String localEncoding = "GB18030";
    protected String remoteEncoding = "ISO8859-1";

    private FTPClient login() {
        FTPClient ftp = new FTPClient();
        try
        {
          bizlog.info("ftp远程连接ip["+getIp()+"]port["+getPort().intValue()+"]user["+getUser()+"]passwd["+getPassword()+"]");    
          connect(ftp, (CommUtil.isNull(getRetryTime())?this.retryTime:getRetryTime()).intValue(), 
                  (CommUtil.isNull(getRetryInterval())?this.retryInterval:getRetryInterval()).intValue());
          
          ftp.setSoTimeout(this.dataTimeoutInMs.intValue());
          ftp.setControlEncoding(remoteEncoding);
          
          if (!ftp.login(getUser(), getPassword()))
          {
            throw new RuntimeException(String.format(BaseConst.SimpleFTPClient07, new Object[] { getUser(), getPassword() }));
          }
          
          if ((StringUtil.isNotEmpty(getLocalWorkDir())) && (!ftp.changeWorkingDirectory(getLocalWorkDir())))
          {
            throw new RuntimeException(String.format(BaseConst.SimpleFTPClient08, new Object[] { getLocalWorkDir() }));
          }
          
          if ((this.binaryMode != null) && (!this.binaryMode.booleanValue())) {
            if (!ftp.setFileType(0))
            {
              throw new RuntimeException(String.format(BaseConst.SimpleFTPClient09, new Object[] { Integer.valueOf(0) }));
            }
          }
          else if (!ftp.setFileType(2))
          {
            throw new RuntimeException(String.format(BaseConst.SimpleFTPClient09, new Object[] { Integer.valueOf(2) }));
          }
          
          ftp.setBufferSize(1048576);
        }
        catch (SocketException e) {
          throw new RuntimeException(BaseConst.SimpleFTPClient10, e);
        }
        catch (IOException e) {
          throw new RuntimeException(BaseConst.SimpleFTPClient10, e);
        }
        
        return ftp;
      }
    
    private void connect(FTPClient ftp, int retryTime, int retryInterval)
    {
      int retryCount = 0;
      try
      {
        ftp.setDefaultTimeout(this.connectTimeoutInMs.intValue());
        ftp.setDataTimeout(this.dataTimeoutInMs.intValue());
        ftp.setConnectTimeout(this.connectTimeoutInMs.intValue());
        ftp.connect(getIp(), getPort().intValue());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
          throw new RuntimeException("Can't Connect to :" + getIp());
        }
        
        return;
      }
      catch (Exception e) {
        while (retryCount < retryTime) {
          try {
            Thread.sleep(retryInterval);
          }
          catch (InterruptedException localInterruptedException) {}
          retryCount++;
        }
        
  
        throw new RuntimeException(e);
      }
    }
    
    
    @Override
    public String setLocalDirectory(String result) throws EdspServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemoteDirectory() throws EdspServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBatchLocalDir() throws EdspServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBatchRemoteDir() throws EdspServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApRemoteFileList getRemoteFileList(String remoteDir, String fileRegs) throws EdspServiceException {
        
        ApRemoteFileList apRemoteFileList = CommTools.getInstance(ApRemoteFileList.class);
        List<String> listNames = new ArrayList<String>();
        
        FTPClient ftp = login();
        FTPFile[] list;
        try {
            list = ftp.listFiles(remoteDir);
            for(int i = 0; i < list.length; i++ ){
                if(list[i].getName().endsWith(fileRegs)){
                    listNames.add(list[i].getName());
                    bizlog.info(remoteDir+"目录下文件第"+i+"个：["+list[i].getName()+"]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        apRemoteFileList.getFile_name().addAll(listNames);
        
        return apRemoteFileList;
    }

}
