package cn.sunline.clwj.oss.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "ms.oss", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix = "ms.oss", name = {
			"enabled" }, matchIfMissing = false, havingValue = "true")
	public OssConfig initOssConfig() {
		return new OssConfig();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "ms.oss.nfs", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix = "ms.oss.nfs", name = {
			"enabled" }, matchIfMissing = false, havingValue = "true")
	public NfsConfig initNfsConfig() {
		return new NfsConfig();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "ms.oss.ftp", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix = "ms.oss.ftp", name = {
			"enabled" }, matchIfMissing = false, havingValue = "true")
	public FtpConfig initFtpConfig() {
		return new FtpConfig();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "ms.oss.sftp", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix = "ms.oss.sftp", name = {
			"enabled" }, matchIfMissing = false, havingValue = "true")
	public SftpConfig initSftpConfig() {
		return new SftpConfig();
	}
	
	@Bean
	@ConfigurationProperties(prefix = "ms.oss.sshj", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix = "ms.oss.sshj", name = {
			"enabled" }, matchIfMissing = false, havingValue = "true")
	public SftpConfig initSshjConfig() {
		return new SftpConfig();
	}
}
