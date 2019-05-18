package uk.gov.dwp.workflow;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uk.gov.dwp.workflow.support.CorsFilter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Properties;

@SpringBootApplication
public class WorkflowServer {

    @Autowired
    ApplicationContext context;


    public static void main(String[] args) {
        System.setProperty("hawtio.authenticationEnabled", "false");
        System.setProperty("management.security.enabled","false");
        System.setProperty("management.contextPath","");
        SpringApplication.run(WorkflowServer.class, args);

    }

    @Bean
    public ServletRegistrationBean ServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new RestfulServer(context), "/STU3/*");
        registration.setName("FhirServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    CorsConfigurationSource
    corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    @Bean
    @Primary
    public FhirContext FhirContextBean() {
        return FhirContext.forDstu3();
    }

    @Bean("CTXR4")
    public FhirContext FhirContextBeanr4() {
        return FhirContext.forR4();
    }


    @Bean
    @Primary
    public IGenericClient getGPCConnection(FhirContext ctx) {

        IGenericClient client = ctx.newRestfulGenericClient(HapiProperties.getGpConnectServer());

        return client;
    }


    @Bean
    public FilterRegistrationBean corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter());
        bean.setOrder(0);
        return bean;
    }


    @Bean(name = "datasource.activiti")
    public DataSource activitiDataSource() {

        BasicDataSource retVal = new BasicDataSource();
        try {
            Driver driver = (Driver) Class.forName(HapiProperties.getDataSourceDriver()).getConstructor().newInstance();
            retVal.setDriver(driver);
            retVal.setUrl(HapiProperties.getDataSourceUrl());
            retVal.setUsername(HapiProperties.getDataSourceUsername());
            retVal.setPassword(HapiProperties.getDataSourcePassword());
            retVal.setMaxTotal(HapiProperties.getDataSourceMaxPoolSize());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return retVal;
    }


    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }



    @Bean
    public EntityManagerFactory entityManagerFactory() {
        //  final Database database = Database.valueOf(vendor.toUpperCase());

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("DWP_FHIR_PU");
        // factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("uk.gov.dwp.workflow.entity");
        factory.setDataSource(activitiDataSource());
        factory.setPersistenceProvider(new HibernatePersistenceProvider());
        factory.setJpaProperties(jpaProperties());
        factory.afterPropertiesSet();


        return factory.getObject();
    }
    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        extraProperties.put("hibernate.dialect", HapiProperties.getHibernateDialect());
        extraProperties.put("hibernate.format_sql", "true");
        extraProperties.put("hibernate.show_sql", HapiProperties.getHibernateShowSql());
        extraProperties.put("hibernate.hbm2ddl.auto", "update");
        extraProperties.put("hibernate.jdbc.batch_size", "20");
        extraProperties.put("hibernate.jdbc.time_zone","UTC");
        extraProperties.put("hibernate.cache.use_query_cache", "false");
        extraProperties.put("hibernate.cache.use_second_level_cache", "false");
        extraProperties.put("hibernate.cache.use_structured_entries", "false");
        extraProperties.put("hibernate.cache.use_minimal_puts", "false");


        extraProperties.put("hibernate.c3p0.min_size","5");
        extraProperties.put("hibernate.c3p0.max_size","20");
        extraProperties.put("hibernate.c3p0.timeout","300");
        extraProperties.put("hibernate.c3p0.max_statements","50");
        extraProperties.put("hibernate.c3p0.idle_test_period","3000");

        extraProperties.put("current_session_context_class","thread");


        return extraProperties;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        PlatformTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);

        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
    }


}
