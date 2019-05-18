package uk.gov.dwp.workflow.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import org.activiti.engine.*;
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

    @Override
    public Task
    read(FhirContext ctx, IdType internalId) {
        return null;
    }

    @Override
    public Task create(FhirContext ctx, Task task) {

        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        return task;
    }

    @Override
    public List<Task> search(FhirContext ctx, ReferenceParam patient) throws Exception {

        taskService.createTaskQuery().taskAssignee("NHS-9876543210").list();
        return null;
    }
}
