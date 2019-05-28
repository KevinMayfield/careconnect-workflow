package uk.gov.workflow.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskDao implements ITask {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskDao.class);



    @Override
    public Task
    read(FhirContext ctx, IdType internalId) {
        return null;
    }

    @Override
    public Task create(FhirContext ctx, Task task) {

        runtimeService.startProcessInstanceByKey("ESA1");
        return task;
    }

    @Override
    public List<Task> search(FhirContext ctx, ReferenceParam patient) throws Exception {
        Model model = repositoryService.getModel("123");

        List<Deployment> deployments = repositoryService.createDeploymentQuery().list();

        for (Deployment deployment : deployments) {
            List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deployment.getId());

            log.info(deploymentResources.toString());
        }

        taskService.createTaskQuery().taskAssignee("NHS-9876543210").list();
        return null;
    }
}
