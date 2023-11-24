package livecast.agent.configuration;

import com.zaxxer.hikari.HikariDataSource;
import livecast.agent.configuration.support.DatabaseProperties;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(value = "livecast.agent.repository.mapper")
public class DatabaseConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);

    private DatabaseProperties databaseProperties;

    @Autowired
    public DatabaseConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @Bean
    public DataSource dataSource() {
        final String dbURL = databaseProperties.getUrl();
        final String dbUser = databaseProperties.getUsername();
        final String dbPass = databaseProperties.getPassword();

        logger.debug("Creating DB DataSource : {}, {}, {}", databaseProperties.getDriverClassName(), dbURL, dbUser);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(databaseProperties.getDriverClassName());
        dataSource.setJdbcUrl(dbURL);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPass);
        dataSource.setMinimumIdle(databaseProperties.getMinIdle());
        dataSource.setMaximumPoolSize(databaseProperties.getMaxPoolSize());
        dataSource.setMaxLifetime(databaseProperties.getMaxLifeTime());

        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource, ApplicationContext applicationContext) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfigLocation(applicationContext.getResource(databaseProperties.getConfigLocation()));

        return sqlSessionFactoryBean;
    }

    @Bean
    public SqlSession sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
