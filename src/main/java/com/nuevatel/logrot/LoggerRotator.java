/**
 * 
 */
package com.nuevatel.logrot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import com.nuevatel.common.ds.DataSourceManagerConfigurator;
import com.nuevatel.common.ds.DataSourceManagerImpl;
import com.nuevatel.common.ds.JDBCProperties;
import com.nuevatel.common.util.StringUtils;
import com.nuevatel.common.util.date.DateFormatter;
import com.nuevatel.common.util.io.IOUtils;

/**
 * @author dayler
 *
 */
public class LoggerRotator {

    /**
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) {
        InputStream isProp = null;
        InputStream isScript = null;
        Connection conn = null;
        Reader tmp = null;
        boolean debug = false;

        try {
            if (args.length == 0) {
                System.out.println("No input parameters. Use embebed values for testing purpose.");
                isProp = LoggerRotator.class.getResourceAsStream("/logrotate.properties");
            } else if (args.length > 0) {
                String propertiesPath = args[0];
                System.out.println(String.format("Loading properties from %s ...", propertiesPath));
                isProp = new FileInputStream(propertiesPath);
            }

            Properties prop = new Properties();
            prop.load(isProp);

            String sqlScriptPath = null;
            if (StringUtils.isEmptyOrNull((sqlScriptPath = prop.getProperty("logrotate.sqlscript")))) {
                isScript = LoggerRotator.class.getResourceAsStream("/rotation_script.sql");
            } else {
                isScript = new FileInputStream(sqlScriptPath);
            }

            JDBCProperties jdbcProp = new JDBCProperties(prop);
            DataSourceManagerConfigurator configurator = new DataSourceManagerConfigurator();
            configurator.configure(jdbcProp).build();
            String template = IOUtils.inputStreamToString(isScript);
            debug = Boolean.parseBoolean(prop.getProperty("debug"));
            String schemaName = prop.getProperty("logrotate.schema");
            String tableList = prop.getProperty("logrotate.mergedtables");
            String suffixPatter = prop.getProperty("logrotate.datepattern");
            String[] tables = tableList.split(";");
            String suffix = DateFormatter.CUSTOM.format(new Date(), suffixPatter);
            conn = new DataSourceManagerImpl().getConnection();
            MysqlScriptExecutor executor = new MysqlScriptExecutor(conn, false, false);

            // Get delimiter
            Boolean fullLineDelimiter = Boolean.parseBoolean(prop.getProperty("logrotate.fulllinedelimiter", "false"));
            String tmpDelimiter = null;
            if (!StringUtils.isBlank((tmpDelimiter = prop.getProperty("logrotate.sqlscript")))) {
                executor.setDelimiter(tmpDelimiter, fullLineDelimiter);
            }

            for (String tName : tables) {
                String script = template.replaceAll("\\$schema\\$", schemaName)
                                    .replaceAll("\\$tableName\\$", tName)
                                    .replaceAll("\\$suffix\\$", suffix);
                if (debug) {
                    System.out.println("Script:"+ script);
                }
                tmp = new StringReader(script);
                executor.runScript(tmp);
            }

        } catch (Throwable ex) {
            System.out.println("Failed to execute program.");
            System.out.println("Exception: " + ex);
        } finally {
            try {
                if (isProp != null) {
                    isProp.close();
                }

                if (isScript != null) {
                    isScript.close();
                }

                if (conn != null) {
                    conn.close();
                }

                if (tmp != null) {
                    tmp.close();
                }
            } catch (IOException ex) {
                System.out.println("Failed to close resource...");
            } catch (SQLException ex) {
                System.out.println("Failed to close connection resource...");
            }
        }
    }
}
