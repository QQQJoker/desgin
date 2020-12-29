修改版本号：
mvn versions:set -DnewVersion=2.1.0-RELEASE -Pmodules

全量打包：
mvn clean install  -Pmodules

只打包parent
mvn clean install 

打包（带源码）
mvn clean install  -Pmodules  -Psource

