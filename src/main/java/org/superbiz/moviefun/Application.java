package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    @Bean
    public TransactionOperations albumsTransactionOperations(PlatformTransactionManager albumsTransactionManager) {
        TransactionOperations transactionOperations = new TransactionTemplate(albumsTransactionManager);
        return transactionOperations;
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(PlatformTransactionManager moviesTransactionManager) {
        TransactionOperations transactionOperations = new TransactionTemplate(moviesTransactionManager);
        return transactionOperations;
    }


    @Bean
    public PlatformTransactionManager albumsTransactionManager(EntityManagerFactory albumsContainerEntityManager) {
        JpaTransactionManager manager = new JpaTransactionManager();
        manager.setEntityManagerFactory(albumsContainerEntityManager);
        return manager;
    }

    @Bean
    public PlatformTransactionManager moviesTransactionManager(EntityManagerFactory moviesContainerEntityManager) {
        JpaTransactionManager manager = new JpaTransactionManager();
        manager.setEntityManagerFactory(moviesContainerEntityManager);
        return manager;
    }


    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsContainerEntityManager(DataSource albumsDataSource, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(albumsDataSource);
        entityManagerFactory.setJpaVendorAdapter(adapter);
        entityManagerFactory.setPackagesToScan("org.superbiz.moviefun");
        entityManagerFactory.setPersistenceUnitName("albums");
        return entityManagerFactory;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean moviesContainerEntityManager(DataSource moviesDataSource, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(moviesDataSource);
        entityManagerFactory.setJpaVendorAdapter(adapter);
        entityManagerFactory.setPackagesToScan("org.superbiz.moviefun");
        entityManagerFactory.setPersistenceUnitName("movies");
        return entityManagerFactory;
    }

    @Bean
    DatabaseServiceCredentials databaseServiceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));

        HikariDataSource source = new HikariDataSource();
        source.setDataSource(dataSource);
        return source;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));

        HikariDataSource source = new HikariDataSource();
        source.setDataSource(dataSource);
        return source;
    }

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }
}
