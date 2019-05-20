package uk.gov.dwp.workflow.dao;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dwp.workflow.entity.BaseIdentifier;
import uk.gov.dwp.workflow.entity.SystemEntity;
import uk.gov.dwp.workflow.entity.daoutils;
import uk.gov.dwp.workflow.support.OperationOutcomeException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;


@Repository
@Transactional
public class LibDao {


    @PersistenceContext
    EntityManager em;


    private static final Logger log = LoggerFactory.getLogger(LibDao.class);

    public BaseIdentifier setIdentifier(Identifier identifier, BaseIdentifier entityIdentifier) throws OperationOutcomeException {


        if (identifier.hasValue()) {
            entityIdentifier.setValue(daoutils.removeSpace(identifier.getValue()));
        }


        if (identifier.hasSystem()) {
            entityIdentifier.setSystem(findSystem(identifier.getSystem()));
        } else {
            entityIdentifier.setSystem(null);
        }

        if (identifier.hasUse()) {
            entityIdentifier.setUse(identifier.getUse());
        }

        return entityIdentifier;
    }


    public SystemEntity findSystem(String system) throws OperationOutcomeException {

        if (system==null || system.isEmpty()) {
            throw new OperationOutcomeException("System is required","System is required", OperationOutcome.IssueType.INVALID);
        }
        CriteriaBuilder builder = em.getCriteriaBuilder();

        SystemEntity systemEntity = null;
        CriteriaQuery<SystemEntity> criteria = builder.createQuery(SystemEntity.class);

        Root<SystemEntity> root = criteria.from(SystemEntity.class);
        List<Predicate> predList = new LinkedList<Predicate>();
        log.debug("Looking for System = " + system);

        Predicate p = builder.equal(root.<String>get("codeSystemUri"),system);
        predList.add(p);
        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            log.debug("Found System "+system);
            criteria.select(root).where(predArray);

            List<SystemEntity> qryResults = em.createQuery(criteria).getResultList();

            for (SystemEntity cme : qryResults) {
                systemEntity = cme;
                break;
            }
        }
        if (systemEntity == null) {
            log.info("Not found. Adding SystemEntity = "+system);
            systemEntity = new SystemEntity();
            systemEntity.setUri(system);

            em.persist(systemEntity);
        }
        return systemEntity;
    }
}
