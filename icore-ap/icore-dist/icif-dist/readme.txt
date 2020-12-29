规范：整个工程所有模块版本号保持一致

全量打包：
	sunipps-base  
	   mvn clean install
	sunipps-deploy
   		mvn install -Psunipps    #编译
   		mvn install -Pdist       #打包