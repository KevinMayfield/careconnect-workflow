package uk.gov.dwp.workflow;

import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.dbcp2.BasicDataSource;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uk.gov.dwp.workflow.support.CorsFilter;

import javax.sql.DataSource;
import java.sql.Driver;

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

}
