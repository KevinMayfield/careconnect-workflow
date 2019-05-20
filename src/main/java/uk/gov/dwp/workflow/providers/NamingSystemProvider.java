package uk.gov.dwp.workflow.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.NamingSystem;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.workflow.dao.NamingSystemRepository;
import uk.gov.dwp.workflow.support.OperationOutcomeException;
import uk.gov.dwp.workflow.support.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class NamingSystemProvider implements ICCResourceProvider {


	@Autowired
    FhirContext ctx;

    @Autowired
    private NamingSystemRepository namingSystemDao;
    

    
    @Override
    public Class<NamingSystem> getResourceType() {
        return NamingSystem.class;
    }

    @Override
    public Long count() {
        return namingSystemDao.count();
    }



    private static final Logger log = LoggerFactory.getLogger(NamingSystemProvider.class);


    @Update()
    public MethodOutcome update(HttpServletRequest theRequest, @ResourceParam NamingSystem namingSystem) throws OperationOutcomeException

    {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        NamingSystem newNamingSystem = namingSystemDao.create(ctx, namingSystem);
        method.setId(newNamingSystem.getIdElement());
        method.setResource(newNamingSystem);

        return method;
    }

    @Search
    public List<NamingSystem> search(HttpServletRequest theRequest,
                                     @OptionalParam(name = NamingSystem.SP_NAME) StringParam name,
                                     @OptionalParam(name = NamingSystem.SP_PUBLISHER) StringParam publisher,
                                     @OptionalParam(name = NamingSystem.SP_VALUE) TokenParam unique
    ) {
        return namingSystemDao.search(ctx, name, publisher, unique);
    }




    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam NamingSystem namingSystem) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        NamingSystem newNamingSystem = namingSystemDao.create(ctx, namingSystem);
        method.setId(newNamingSystem.getIdElement());
        method.setResource(newNamingSystem);

        return method;
    }

    @Read
    public NamingSystem get
            (@IdParam IdType internalId) {

        NamingSystem namingSystem = namingSystemDao.read(ctx, internalId);

        if ( namingSystem == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No NamingSystem/" + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return namingSystem;
    }
    

    
}
