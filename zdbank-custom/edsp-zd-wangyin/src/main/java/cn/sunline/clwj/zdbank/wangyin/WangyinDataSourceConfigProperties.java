package cn.sunline.clwj.zdbank.wangyin;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix="adp.datasource.wangyin", ignoreUnknownFields = true)
@Configuration
public class WangyinDataSourceConfigProperties {
	private List<WangyinDataSourceModel> dataSourceModel;

	public List<WangyinDataSourceModel> getDataSourceModel() {
		return dataSourceModel;
	}

	public void setDataSourceModel(List<WangyinDataSourceModel> dataSourceModel) {
		this.dataSourceModel = dataSourceModel;
	}
}
