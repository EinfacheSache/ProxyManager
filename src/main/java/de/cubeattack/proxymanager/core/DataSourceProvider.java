package de.cubeattack.proxymanager.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

@SuppressWarnings("unused")
public class DataSourceProvider {

    private javax.sql.DataSource source;

    public DataSourceProvider()
    {
        if(!Config.connectMySQL()) return;
        source = connect();
    }

    private DataSource connect()
    {
        Properties props = new Properties();

        props.setProperty("dataSource.user", Config.getMySQLUser());
        props.setProperty("dataSource.password", Config.getMySQLPassword());

        HikariConfig config = new HikariConfig(props);

        config.setJdbcUrl("jdbc:mysql://" + Config.getMySQLHost() + ":" + Config.getMySQLPort() + "/" + Config.getMySQLDatabase() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true");

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(100);

        source = new HikariDataSource(config);

        return source;
    }
    public ResultSet query(String qry)
    {
        try (PreparedStatement st = source.getConnection().prepareStatement(qry, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)){
            return st.executeQuery();
        } catch (SQLException ex) {
            connect();
            Core.severe("Error whiles loading : " + qry + "\n" + ex.getLocalizedMessage());
        }
        return null;
    }

    public void update(String s1,String s2)
    {
        try (Connection conn = source.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Data (keyData, value1) VALUES (?,?)")) {
            stmt.setString(1, s1);
            stmt.setString(2, s2);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            Core.severe("Error whiles updating : " + "\n" + ex.getLocalizedMessage());
        }
    }
}
