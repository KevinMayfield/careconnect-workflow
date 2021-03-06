package uk.gov.workflow.dao;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.IdType;

import uk.gov.workflow.entity.IBaseResource;
import uk.gov.workflow.support.OperationOutcomeException;


public interface BaseRepository<R extends IBaseResource,F extends DomainResource> {

    Long count();

     R readEntity(FhirContext ctx, IdType theId);

    void save(FhirContext ctx, R resource) throws OperationOutcomeException;

     F read(FhirContext ctx, IdType theId);
}
