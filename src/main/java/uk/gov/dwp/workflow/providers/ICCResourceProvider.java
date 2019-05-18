package uk.gov.dwp.workflow.providers;

import ca.uhn.fhir.rest.server.IResourceProvider;

public interface ICCResourceProvider extends IResourceProvider {

    Long count();
}
