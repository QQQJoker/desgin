package cn.sunline.clwj.oss.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MsFileUtil {
	private static final Logger log = LogManager.getLogger(MsFileUtil.class);

	public boolean startsWithSeparator(String filePath) {
		if (MsStringUtil.isEmpty(filePath)) {
			return false;
		} else {
			return filePath.startsWith(File.separator);
		}
	}

	public boolean endsWithSeparator(String filePath) {
		if (MsStringUtil.isEmpty(filePath)) {
			return false;
		} else {
			return filePath.endsWith(File.separator);
		}
	}

	public String getMD5Checksum(String fileName) {
		if (log.isDebugEnabled())
			log.debug("getMD5Checksum fileName:" + fileName);
		return MsMD5Util.getMD5Checksum(fileName);
	}

	
	public static String getKey(Reader reader) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        StringBuffer sb = new StringBuffer();
        try {
            String keydata;
            while ((keydata = br.readLine()) != null) {
                keydata = keydata.trim();
                if (!keydata.isEmpty()) {
                    sb.append(keydata);
                }
            }
        } finally {
            br.close();
        }
        
        return sb.toString();
    }
}
