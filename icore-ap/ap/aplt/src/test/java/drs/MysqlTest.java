package drs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mysql.jdbc.Driver;

public class MysqlTest {

    @Test
    public void testMysqlMaxConnection() throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        String driverName = "com.mysql.jdbc.Driver";
        String URL = "jdbc:mysql://10.22.10.120:3306/cbmain_dev2";
        String username = "cbmain_dev2";
        String password = "cbmain_dev2";
        //加载驱动
        Driver.class.forName(driverName);
        List<Connection> conns = new ArrayList<>();
        try {
            for (int i = 0; i < 3000; i++) {

                //建立连接
                Connection conn = DriverManager.getConnection(URL, username, password);
                conns.add(conn);
                System.out.println("建立连接：" + conn);

            }
        } finally {
            for (Connection conn : conns) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
