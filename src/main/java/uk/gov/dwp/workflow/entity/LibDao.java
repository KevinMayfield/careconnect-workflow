package uk.gov.dwp.workflow.entity;

import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.dwp.workflow.support.OperationOutcomeException;


@Component
public class LibDao {



    private static final Logger log = LoggerFactory.getLogger(LibDao.class);

    public BaseIdentifier setIdentifier(Identifier identifier, BaseIdentifier entityIdentifier) throws OperationOutcomeException {


        if (identifier.hasValue()) {
            entityIdentifier.setValue(daoutils.removeSpace(identifier.getValue()));
        }

        /* TODO
        if (identifier.hasSystem()) {
            entityIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
        } else {
            entityIdentifier.setSystem(null);
        }
         */
        if (identifier.hasUse()) {
            entityIdentifier.setUse(identifier.getUse());
        }

        return entityIdentifier;
    }
}
