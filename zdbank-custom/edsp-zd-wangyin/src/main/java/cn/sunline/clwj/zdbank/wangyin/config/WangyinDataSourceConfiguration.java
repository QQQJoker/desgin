package cn.sunline.clwj.zdbank.wangyin.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.wangyin.commons.cp.WangyinCPDataSource;

import cn.sunline.adp.dao.datasource.constants.MgrDatasourceState;
import cn.sunline.adp.dao.datasource.druid.DruidDataSourceModel;
import cn.sunline.adp.dao.datasource.druid.dbpass.DBPassSecurity;
import cn.sunline.adp.dao.datasource.dynamic.DataSourceModel;
import cn.sunline.adp.dao.datasource.dynamic.DataSourceModels;
import cn.sunline.clwj.zdbank.wangyin.WangyinDataSourceConfigProperties;
import cn.sunline.clwj.zdbank.wangyin.WangyinDataSourceModel;
import cn.sunline.edsp.base.factories.FactoriesLoader;


@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ConditionalOnProperty(prefix="adp.datasource.wangyin", name="enabled", matchIfMissing=false, havingValue="true")
public class WangyinDataSourceConfiguration {
	
	@Autowired
	private WangyinDataSourceConfigProperties dataSourceConfigProperties;

	@Primary
	@Bean("wangyin")
	public DataSourceModels initDataSourceModels() {
		List<WangyinDataSourceModel> modelList = dataSourceConfigProperties.getDataSourceModel();
		List<DataSourceModel> dataSourceModels = new ArrayList<>();
		// build dataSource
		for(WangyinDataSourceModel model : modelList) {
			WangyinCPDataSource dataSourceInstance = new WangyinCPDataSource();
			setWangyinDatasourceProperties(dataSourceInstance, model);
			DataSourceModel dataSourceModel = new DataSourceModel(model.getId(), dataSourceInstance);
			dataSourceModel.setDataBaseEncoding(model.getDbEncoding());
			dataSourceModel.setDataBaseType(model.getDbType());
			dataSourceModel.setReadOnly(model.isReadOnly());
			dataSourceModel.setDatasourceState(MgrDatasourceState.ONLINE);
			dataSourceModel.setServerId(model.getServerId());
			dataSourceModel.setGroup(model.getGroupId());
			dataSourceModel.setDelayFlag(false);
			dataSourceModels.add(dataSourceModel);
		}
		return new DataSourceModels(dataSourceModels);
	}
	
	private void setWangyinDatasourceProperties(WangyinCPDataSource dataSourceInstance, WangyinDataSourceModel model) {
		try {
//			decryptPassword(model);
			
			Class<?> dataSourceClazz = dataSourceInstance.getClass();
			Method[] methods = model.getClass().getMethods();
			for(Method method : methods) {
				String getMethodName="";
				String methodName = method.getName();
				// set方法  public void
				if(methodName.startsWith("set") 
						&& !methodName.equals("setId")
						&& Modifier.isPublic(method.getModifiers()) 
						&& (method.getReturnType() == void.class)) {
					try {
						// 根据model的set方法，得到DataSource的set方法对象
						Method set = dataSourceClazz.getMethod(methodName, method.getParameterTypes());
						// 根据model的set方法，得到模型的get方法对象
						Method get = null;
						try {
							// 根据model的set方法，得到模型的get方法名
							getMethodName = "get" + methodName.substring(3);
							get = model.getClass().getMethod(getMethodName, new Class[] {});
						}catch (Exception e) {
							getMethodName = "is" + methodName.substring(3);
							get = model.getClass().getMethod(getMethodName, new Class[] {});
						}
						
						// 把模型的get方法返回值赋值给DataSource的set方法
						set.invoke(dataSourceInstance, get.invoke(model, new Object[] {}));
					} catch(NoSuchMethodException | SecurityException e) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("initial datasource exception.", e);
		}
	}
	
	 
	//解密操作
	public void decryptPassword(DruidDataSourceModel model) {
		
		DBPassSecurity security = FactoriesLoader.getFactoryById(DBPassSecurity.class, model.getEncryptType());
		String decPasssword = security.decrypt(model.getPassword(), model.getPublicKey(), model.getPrivateKey());

		model.setPassword(decPasssword);
	}
}
