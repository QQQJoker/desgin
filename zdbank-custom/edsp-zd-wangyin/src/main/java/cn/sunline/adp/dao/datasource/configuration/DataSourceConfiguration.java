package cn.sunline.adp.dao.datasource.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cn.sunline.adp.dao.datasource.dynamic.DataSourceModel;
import cn.sunline.adp.dao.datasource.dynamic.DataSourceModels;
import cn.sunline.adp.dao.datasource.dynamic.DynamicDataSource;
import cn.sunline.adp.dao.datasource.dynamic.DynamicDataSourceConfigProperties;

/**
 * 
 * druid连接池配置
 * 
 * @author zhangchangbin
 *
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ConditionalOnProperty(prefix="adp.datasource", name="enabled", matchIfMissing=false, havingValue="true")
public class DataSourceConfiguration {

	@Autowired
	private DynamicDataSourceConfigProperties dataSourceConfigProperties;

	@Resource
//	@Qualifier(value="druid")
	private DataSourceModels dataSourceModels;

	@Primary
	@Bean("dataSource")
	public DataSource initDataSource() {
		String defaultDataSourceId = dataSourceConfigProperties.getDefaultDataSourceId();
		Map<Object, Object> dataSourceMap = new HashMap<Object, Object>();
		List<DataSourceModel> dataSourceModelsList = dataSourceModels.getDataSourceModels();
		if (dataSourceModelsList != null && dataSourceModelsList.size() > 0) {
			for (DataSourceModel dataSourceModel : dataSourceModelsList ) {
				dataSourceMap.put(dataSourceModel.getId(), dataSourceModel.getDataSource());
			}
		}
		DynamicDataSource dataSource = new DynamicDataSource();
		dataSource.setDefaultTargetDataSource(dataSourceMap.get(defaultDataSourceId));
		dataSource.setTargetDataSources(dataSourceMap);
		return dataSource;
	}
}
