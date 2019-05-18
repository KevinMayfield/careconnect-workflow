package uk.gov.dwp.workflow.rest;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ProcessStartController {
    private Logger logger = LoggerFactory.getLogger(ProcessStartController.class);

    @Autowired
    private ProcessRuntime processRuntime;

    @RequestMapping("bpmn/start-process")
    public ProcessInstance startProcess(
            @RequestParam(value="processDefinitionKey", defaultValue="SampleProcess") String processDefinitionKey) {
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processDefinitionKey)
                .withName("Sample Process: " + new Date())
                .withVariable("someProcessVar", "someProcVarValue")
                .build());
        logger.info(">>> Created Process Instance: " + processInstance);

        return processInstance;
    }
}

