package uk.gov.workflow.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.workflow.dao.ITask;
import uk.gov.workflow.support.OperationOutcomeFactory;
import uk.gov.workflow.support.ProviderResponseLibrary;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class TaskResourceProvider implements IResourceProvider {


    @Autowired
    FhirContext ctx;


    @Autowired
    ITask resourceDao;



    private static final Logger log = LoggerFactory.getLogger(TaskResourceProvider.class);

    @Override
    public Class<Task> getResourceType() {
        return Task.class;
    }

    @Search
    public List<Task> search(HttpServletRequest httpRequest,
                             @OptionalParam(name = Task.SP_PATIENT) ReferenceParam patient
    ) throws Exception {

        return resourceDao.search(ctx,patient);

    }

    @Create
    public MethodOutcome create(HttpServletRequest theRequest, @ResourceParam Task task) {

        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

        try {
            Task newTask =  resourceDao.create(ctx,task);
            method.setId(newTask.getIdElement());
            method.setResource(newTask);
        } catch (Exception ex) {

            ProviderResponseLibrary.handleException(method,ex);
        }

        return method;
    }

    @Read
    public Task read(@IdParam IdType internalId) {


       Task task = resourceDao.read(ctx,internalId);
        if (task == null) {
            throw OperationOutcomeFactory
                    .buildOperationOutcomeException(
                    new ResourceNotFoundException("No task details found for task ID: " + internalId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return task;
    }





}
