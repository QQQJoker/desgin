package cn.sunline.clwj.zdbank.wangyin;

import java.sql.DriverManager;


public class WangyinDataSourceModel {
	
	private String id;

	private String dbEncoding = "UTF-8";
	
	private String dbType = "mysql";
	
	private boolean readOnly = false;
	
	private String serverId = "default";
	
	private String groupId = "default";
	

	/**
     * 连接URL
     */
    private String connUrl;
    /**
     * jdbc驱动类
     */
    private String driver;

    /**
     * 数据库用户名
     */
    private String username;
    /**
     * 数据库用户口令
     */
    private String password;

    /**
     * 池中最小连接数
     */
    private int minConnNum = 0;
    /**
     * 池中最大连接数
     */
    private int maxConnNum = 10;
    /**
     * 池中最大语句数
     */
    private int maxStmtNum = 100;
    /**
     * 池中最大语句数
     */
    private int maxPreStmtNum = 10;
    /**
     * 连接最大空闲时间(milli sec)(空闲超过该时间的连接将被检测或回收)
     */
    private long maxIdleMilliSec = 300 * 1000;
    /**
     * 等待空闲连接时的超时时间(ms)
     */
    private long checkOutTimeout = 10000;

    /**
     * 关闭连接时自动提交事务
     */
    private boolean commitOnClose = false;

    /**
     * 记录除SQL语句及执行时间外的其他信息
     */
    private boolean verbose = false;

    /**
     * 记录SQL语句及执行时间 //add by wuhq 2011.09.02
     */
    private boolean printSQL = true;

    /**
     * 检测连接是否可用的查询语句
     */
    private String checkStatement;
    
//    /**
//     * 新建链接执行的初始化SQL语句，如set names utf8mb4
//     */
//	private String initSQL;

    /**
     * 0 - no jmx
     * 1 - manage ConnectionFactory instance
     * 2 - manage PooledConnection instance
     */
    private int jmxLevel = 0;

    /**
     * 默认获取的连接的事务模式（true-事务模式，即autocommit=false）
     */
    private boolean transactionMode = false;

    /**
     * lazy init pool
     * true: init min connections in Monitor thread, else do it in new WangyinCP/getConnection() thread
     */
    private boolean lazyInit = false;

    /**
     * printSQL == true时，打印INFO级别的SQL的耗时阈值(ms)
     */
    private long infoSQLThreshold = 10;

    /**
     * printSQL == true时，打印WARN级别的SQL的耗时阈值(ms)
     */
    private long warnSQLThreshold = 100;

    /**
     * Indicates if this is for Oracle.
     */
    private boolean isOracle = false;

    /**
     * Indicates if this is MySQL cp
     */
    private boolean isMySQL = false;

    /**
     * Indicates if this is DB2 cp
     */
    private boolean isDB2 = false;

    /**
     * Indicates if oracle implicit preparedstatement cache needed.
     */
    private boolean useOracleImplicitPSCache = true;

    public int getMinConnNum() {
		return minConnNum;
	}

	public void setMinConnNum(int minConnNum) {
		this.minConnNum = minConnNum;
	}

	public int getMaxConnNum() {
		return maxConnNum;
	}

	public void setMaxConnNum(int maxConnNum) {
		this.maxConnNum = maxConnNum;
	}

	public int getMaxStmtNum() {
		return maxStmtNum;
	}

	public void setMaxStmtNum(int maxStmtNum) {
		this.maxStmtNum = maxStmtNum;
	}

	public int getMaxPreStmtNum() {
		return maxPreStmtNum;
	}

	public void setMaxPreStmtNum(int maxPreStmtNum) {
		this.maxPreStmtNum = maxPreStmtNum;
	}

	public long getMaxIdleMilliSec() {
		return maxIdleMilliSec;
	}

	public void setMaxIdleMilliSec(long maxIdleMilliSec) {
		this.maxIdleMilliSec = maxIdleMilliSec;
	}

	public long getCheckOutTimeout() {
		return checkOutTimeout;
	}

	public void setCheckOutTimeout(long checkOutTimeout) {
		this.checkOutTimeout = checkOutTimeout;
	}

//	public String getInitSQL() {
//		return initSQL;
//	}
//
//	public void setInitSQL(String initSQL) {
//		this.initSQL = initSQL;
//	}

	/**
     * query timeout (seconds)
     */
    private int queryTimeout = 60;
    
    /*
     * default login timeout 10 seconds
     */
    static {
        DriverManager.setLoginTimeout(10);
    }

    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    public void setLoginTimeout(int loginTimeout) {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }


    public String getConnUrl() {
        return connUrl;
    }

    public void setConnUrl(String connUrl) {
        this.connUrl = connUrl;
        if (this.connUrl != null && this.connUrl.trim().length() != 0) {
            String[] buf = this.connUrl.split(":");
            if (buf.length < 2) {
                return;
            }
            String dbf = buf[1];
            if (dbf.compareToIgnoreCase("oracle") == 0) {
                isOracle = true;
            } else if (dbf.compareToIgnoreCase("mysql") == 0) {
                isMySQL = true;
            } else if (dbf.compareToIgnoreCase("db2") == 0) {
                isDB2 = true;
            }
            if (this.checkStatement == null || this.checkStatement.trim().length() == 0) {
                //if checkStatement NOT be set, auto-set by url
                if (isDB2) {
                    checkStatement = "values(current timestamp)";
                } else if (isOracle) {
                    checkStatement = "select systimestamp from dual";
                } else if (isMySQL) {
                    checkStatement = "select now()";
                }
            }
            if (this.driver == null || this.driver.trim().length() == 0) {
                //if driver NOT be set, auto-set by url
                if (isOracle) {
                    driver = "oracle.jdbc.driver.OracleDriver";
                } else if (isMySQL) {
                    driver = "com.mysql.jdbc.Driver";
                }
            }
        }
    }

    /*
     * 2012-11-12 zhangyao 支持url参数的注入，保持一其它数据库连接池一致
     */
    public String getUrl() {
        return getConnUrl();
    }

    public void setUrl(String url) {
        setConnUrl(url);
    }

    public long getWarnSQLThreshold() {
        return warnSQLThreshold;
    }

    public void setWarnSQLThreshold(long warnSQLThreshold) {
        this.warnSQLThreshold = warnSQLThreshold;
    }

    public long getInfoSQLThreshold() {
        return infoSQLThreshold;
    }

    public void setInfoSQLThreshold(long infoSQLThreshold) {
        this.infoSQLThreshold = infoSQLThreshold;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
//        if (username != null && username.trim().length() > 0) {
//            this.connectionProperties.setProperty("user", username);
//        }
    }

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

    public int getMinConnections() {
        return minConnNum;
    }

    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMinConnections(int minConnections) {
        this.minConnNum = minConnections;
    }

    public int getMaxConnections() {
        return maxConnNum;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnNum = maxConnections;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isPrintSQL() {
        return printSQL;
    }

    public void setPrintSQL(boolean printSQL) {
        this.printSQL = printSQL;
    }

    public boolean isCommitOnClose() {
        return commitOnClose;
    }

    public void setCommitOnClose(boolean commitOnClose) {
        this.commitOnClose = commitOnClose;
    }

    public long getIdleTimeoutSec() {
        return maxIdleMilliSec / 1000;
    }

    public long getIdleTimeoutMilliSec() {
        return maxIdleMilliSec;
    }

    public void setIdleTimeoutSec(long idleTimeoutSec) {
        this.maxIdleMilliSec = idleTimeoutSec * 1000;
    }

    public long getCheckoutTimeoutMilliSec() {
        return checkOutTimeout;
    }

    public void setCheckoutTimeoutMilliSec(long checkoutTimeoutMilliSec) {
        this.checkOutTimeout = checkoutTimeoutMilliSec;
    }

    public int getMaxStatements() {
        return maxStmtNum;
    }

    public void setMaxStatements(int maxStatements) {
        this.maxStmtNum = maxStatements;
    }

    public int getMaxPreStatements() {
        return maxPreStmtNum;
    }

    public void setMaxPreStatements(int maxPreStatements) {
        this.maxPreStmtNum = maxPreStatements;
    }

    public String getCheckStatement() {
        return checkStatement;
    }

    public void setCheckStatement(String checkStatement) {
        this.checkStatement = checkStatement;
    }

    public boolean isTransactionMode() {
        return transactionMode;
    }

    public void setTransactionMode(boolean transactionMode) {
        this.transactionMode = transactionMode;
    }

    public int getJmxLevel() {
        return jmxLevel;
    }

    public void setJmxLevel(int jmxLevel) {
        this.jmxLevel = jmxLevel;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public boolean isMySQL() {
        return isMySQL;
    }

    public boolean isOracle() {
        return isOracle;
    }

    public boolean isDB2() {
        return isDB2;
    }

    public boolean isUseOracleImplicitPSCache() {
        return useOracleImplicitPSCache;
    }

    public void setUseOracleImplicitPSCache(boolean useOracleImplicitPSCache) {
        this.useOracleImplicitPSCache = useOracleImplicitPSCache;
    }

    public WangyinDataSourceModel() {
//        this.poolName = poolName;
    }
	
	
	public String getDbEncoding() {
		return dbEncoding;
	}

	public void setDbEncoding(String dbEncoding) {
		this.dbEncoding = dbEncoding;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

}
