package uk.gov.workflow;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.FifoMemoryPagingProvider;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.util.VersionUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import uk.gov.dwp.workflow.providers.*;
import uk.gov.workflow.providers.NamingSystemProvider;
import uk.gov.workflow.providers.PersonProvider;
import uk.gov.workflow.providers.TaskResourceProvider;
import uk.gov.workflow.support.ServerInterceptor;
import uk.gov.workflow.providers.ConformanceProvider;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@WebServlet(urlPatterns = {"/*"}, displayName = "FHIR Server")
public class RestfulServer extends ca.uhn.fhir.rest.server.RestfulServer {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestfulServer.class);

    private ApplicationContext appCtx;

    RestfulServer(ApplicationContext context) {
        this.appCtx = context;
    }


    @Override
    public void addHeadersToResponse(HttpServletResponse theHttpResponse) {
        theHttpResponse.addHeader("X-Powered-By", "HAPI FHIR " + VersionUtil.getVersion() + " RESTful Server (INTEROPen Care Connect STU3)");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String ccri_role = this.appCtx.getEnvironment().getProperty("ccri.role");


        FhirVersionEnum fhirVersion = FhirVersionEnum.DSTU3;
        setFhirContext(new FhirContext(fhirVersion));

        setDefaultResponseEncoding(HapiProperties.getDefaultEncoding());

        String serverAddress = HapiProperties.getServerAddress();
        if (serverAddress != null && !serverAddress.isEmpty()) {
            setServerAddressStrategy(new HardcodedServerAddressStrategy(serverAddress));
        }
        List<IResourceProvider> resourceProviders = new ArrayList<>();

        resourceProviders.add(appCtx.getBean(TaskResourceProvider.class));
        resourceProviders.add(appCtx.getBean(PersonProvider.class));
        resourceProviders.add(appCtx.getBean(NamingSystemProvider.class));


        setFhirContext(appCtx.getBean(FhirContext.class));

        registerProviders(resourceProviders);


        /*
         * Enable ETag Support (this is already the default)
         */
        setETagSupport(HapiProperties.getEtagSupport());
        // Replace built in conformance provider (CapabilityStatement)
        setServerConformanceProvider(new ConformanceProvider());

        setServerName(HapiProperties.getServerName());
        setServerVersion(HapiProperties.getSoftwareVersion());
        setImplementationDescription(HapiProperties.getSoftwareImplementationDesc());

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("x-fhir-starter");
        config.addAllowedHeader("Origin");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Content-Type");

        config.addAllowedOrigin("*");

        config.addExposedHeader("Location");
        config.addExposedHeader("Content-Location");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Create the interceptor and register it
        CorsInterceptor interceptor = new CorsInterceptor(config);
        registerInterceptor(interceptor);

        ServerInterceptor loggingInterceptor = new ServerInterceptor(log);
        registerInterceptor(loggingInterceptor);

        //ServerInterceptor gatewayInterceptor = new ServerInterceptor(log);
        //registerInterceptor(gatewayInterceptor);

        FifoMemoryPagingProvider pp = new FifoMemoryPagingProvider(10);
        pp.setDefaultPageSize(10);
        pp.setMaximumPageSize(100);
        setPagingProvider(pp);

        setDefaultPrettyPrint(true);
        setDefaultResponseEncoding(EncodingEnum.JSON);

        FhirContext ctx = getFhirContext();
        // Remove as believe due to issues on docker ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
    }


}
