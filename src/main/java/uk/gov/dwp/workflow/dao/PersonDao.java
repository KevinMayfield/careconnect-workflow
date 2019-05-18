package uk.gov.dwp.workflow.dao;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import org.hl7.fhir.dstu3.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.workflow.entity.*;
import uk.gov.dwp.workflow.support.OperationOutcomeException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

@Repository
@Transactional
public class PersonDao implements PersonRepository {


    @PersistenceContext
    EntityManager em;

    @Autowired
    private LibDao libDao;

    private static final Logger log = LoggerFactory.getLogger(PersonDao.class);

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(PersonEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }

    @Transactional
    @Override
    public void save(FhirContext ctx, PersonEntity
            person)
    {


        em.persist(person);
    }

    @Override
    public Person read(FhirContext ctx, IdType theId) {

        log.info("Looking for person = "+theId.getIdPart());
        if (daoutils.isNumeric(theId.getIdPart())) {
            PersonEntity personEntity = (PersonEntity) em.find(PersonEntity.class, Long.parseLong(theId.getIdPart()));

            Person person = null;
            /*
            if (personEntity != null) {
                person = personEntityToFHIRPersonTransformer.transform(personEntity);
                personEntity.setResource(ctx.newJsonParser().encodeResourceToString(person));
                em.persist(personEntity);
            }

             */
            return person;
        } else {
            return null;
        }
    }

    @Override
    public PersonEntity readEntity(FhirContext ctx, IdType theId) {

        return  (PersonEntity) em.find(PersonEntity.class,Long.parseLong(theId.getIdPart()));

    }

    @Override
    public Person update(FhirContext ctx, Person person, IdType theId, String theConditional) throws OperationOutcomeException {

        PersonEntity personEntity = null;
        log.info("Started person updated");
        if (theId != null) {
            log.trace("theId.getIdPart()="+theId.getIdPart());
            personEntity = (PersonEntity) em.find(PersonEntity.class, Long.parseLong(theId.getIdPart()));
        }

        if (theConditional != null) {
            try {
                log.trace("Conditional Url = "+theConditional);

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("PPMIdentifier")) {
                    URI uri = new URI(theConditional);

                    //String scheme = uri.getScheme();
                    //String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.trace(query);
                    String[] spiltStr = query.split("%7C");
                    log.trace(spiltStr[1]);

                    List<PersonEntity> results = searchEntity(ctx, null);
                    log.trace("Loop over results");
                    for (PersonEntity pat : results) {
                        personEntity = pat;
                        break;
                    }
                    // This copes with the new identifier being added.
                    if (personEntity == null && daoutils.isNumeric(spiltStr[1])) {
                        log.trace("Looking for person with id of "+spiltStr[1]);
                        personEntity = (PersonEntity) em.find(PersonEntity.class, Long.parseLong(spiltStr[1]));
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }


        if (personEntity == null) {
            log.trace("Adding new Person");
            personEntity = new PersonEntity();
        }
        personEntity.setResource(null);

        if (person.hasActive()) {
            personEntity.setActive(person.getActive());
        } else {
            personEntity.setActive(null);
        }

        if (person.hasGender()) {
            switch (person.getGender()) {
                case MALE:
                    personEntity.setGender("MALE");
                    break;
                case FEMALE:
                    personEntity.setGender("FEMALE");
                    break;
                case UNKNOWN:
                    personEntity.setGender("UNKNOWN");
                    break;
                case OTHER:
                    personEntity.setGender("OTHER");
                    break;
                case NULL:
                    personEntity.setGender(null);
                    break;
            }
        } else {
            personEntity.setGender(null);
        }

        if (person.hasBirthDate()) {
            personEntity.setDateOfBirth(person.getBirthDate());
        } else {
            personEntity.setDateOfBirth(null);
        }



        em.persist(personEntity);

        // Remove all identifiers with systems that match the updated Person, leave unmatched identifiers alone.
        for (PersonIdentifier orgSearch : personEntity.getIdentifiers()) {
            Boolean found = false;
            for (Identifier identifier : person.getIdentifier()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri())) {
                    found = true;
                }
            }
            if (found) {
                em.remove(orgSearch);
            }
        }

        for (Identifier identifier : person.getIdentifier()) {

            PersonIdentifier personIdentifier = null;
            /* No longer necessary
            for (PersonIdentifier orgSearch : personEntity.getIdentifiers()) {
                // overwrites existing
                if (identifier.getSystem().equals(orgSearch.getSystemUri())) {

                }
                    if ( identifier.getValue().equals(orgSearch.getValue())) {
                    personIdentifier = orgSearch;
                    break;
                }
            }
            */
            if (personIdentifier == null) {
                personIdentifier = new PersonIdentifier();
                personEntity.getIdentifiers().add(personIdentifier);
            }

            personIdentifier = (PersonIdentifier) libDao.setIdentifier(identifier,  personIdentifier);
            personIdentifier.setPerson(personEntity);

            em.persist(personIdentifier);
        }
        em.persist(personEntity);

        for (PersonName nameSearch : personEntity.getNames()) {
           em.remove(nameSearch);
        }
        personEntity.setNames(new ArrayList<>());

        for (HumanName name : person.getName()) {
            PersonName personName = null;
            /*
            for (PersonName nameSearch : personEntity.getNames()) {
                // look for matching surname and also if the have matching given name
                if (nameSearch.getFamilyName().equals(name.getFamily())) {
                    if (name.getGiven().size()> 0) {
                        if (nameSearch.getGivenName().equals(name.getGiven().get(0).getValue())) {
                            personName = nameSearch;
                            break;
                        }
                    }
                    else {
                        personName = nameSearch;
                        break;
                    }
                }
            }
            */
            if (personName == null)  {
                personName = new PersonName();
                personName.setPersonEntity(personEntity);
            }

            personName.setFamilyName(name.getFamily());
            if (name.getGiven().size()>0)
                personName.setGivenName(name.getGiven().get(0).getValue());
            if (name.getPrefix().size()>0)
                personName.setPrefix(name.getPrefix().get(0).getValue());
            if (name.getUse() != null) {
                personName.setNameUse(name.getUse());
            }
            em.persist(personName);
        }

        // Temp fix to remove old addresses 16/8/2018 KGM
        for (PersonAddress adrSearch : personEntity.getAddresses()) {
            em.remove(adrSearch);
        }
        for (Address address : person.getAddress()) {
            PersonAddress personAdr = null;
            /*
            for (PersonAddress adrSearch : personEntity.getAddresses()) {
                // look for matching postcode and first line of address
                if (adrSearch.getAddress().getPostcode().equals(address.getPostalCode())) {
                    if (address.hasLine() && address.getLine().get(0)!=null && adrSearch.getAddress().getAddress1().equals(address.getLine().get(0))) {
                        personAdr = adrSearch;
                        break;
                    }
                }
            }*/
            if (personAdr == null) {
                personAdr = new PersonAddress();
                personAdr.setPersonEntity(personEntity);
                personEntity.getAddresses().add(personAdr);
            }

            AddressEntity addr = personAdr.getAddress();
            if (addr == null) {
                addr = personAdr.setAddress(new AddressEntity());
            }

            if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue().trim());
            if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue().trim());
            if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue().trim());
            if (address.getCity() != null) addr.setCity(address.getCity());
            if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
            if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
            if (address.getCountry() != null) addr.setCountry(address.getCountry());

            if (address.getUse() != null) personAdr.setAddressUse(address.getUse());
            if (address.getType() != null) personAdr.setAddressType(address.getType());


            em.persist(addr);
            em.persist(personAdr);
        }
        for (PersonTelecom telSearch : personEntity.getTelecoms()) {
            em.remove(telSearch);
        }
        for (ContactPoint contact : person.getTelecom()) {
            PersonTelecom personTel  = new PersonTelecom();

            personTel.setPersonEntity(personEntity);
            personTel.setValue(contact.getValue());

            if (contact.hasSystem())
                personTel.setSystem(contact.getSystem());
            if (contact.hasUse())
                personTel.setTelecomUse(contact.getUse());
            em.persist(personTel);
            personEntity.getTelecoms().add(personTel);
        }


        Person newPerson = null;


       // TODO  newPerson = personEntityToFHIRPersonTransformer.transform(personEntity);

        //em.persist(personEntity);


        return newPerson;
    }

    @Override
    public List<Resource> search (FhirContext ctx,

          @OptionalParam(name= Person.SP_NAME) StringParam name

    ) {
        List<PersonEntity> qryResults = searchEntity(ctx, name);
        List<Resource> results = new ArrayList<>();

        for (PersonEntity personEntity : qryResults)
        {
            Person person;
            if (personEntity.getResource() != null) {
                person = (Person) ctx.newJsonParser().parseResource(personEntity.getResource());
            } else {
                person = null;
                /* todo
                    person = personEntityToFHIRPersonTransformer.transform(personEntity);
                    String resourceStr = ctx.newJsonParser().encodeResourceToString(person);
                    personEntity.setResource(resourceStr);
                    em.persist(personEntity);

                 */
            }
            results.add(person);

            // If reverse include selected

        }


        return results;
    }

    @Override
    public List<PersonEntity> searchEntity (FhirContext ctx,
                          @OptionalParam(name= Person.SP_NAME) StringParam name
    )
    {


        CriteriaBuilder builder = em.getCriteriaBuilder();

        // KGM 18/12/2017 Added distinct
        CriteriaQuery<PersonEntity> criteria = builder.createQuery(PersonEntity.class).distinct(true);
        Root<PersonEntity> root = criteria.from(PersonEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<PersonEntity> results = new ArrayList<PersonEntity>();


        if ((name != null)) {

            Join<PersonEntity,PersonName> namejoin = root.join("names",JoinType.LEFT);


            if (name != null) {
                Predicate pgiven = builder.like(
                        builder.upper(namejoin.get("givenName").as(String.class)),
                        builder.upper(builder.literal(name.getValue()+"%"))
                );
                Predicate pfamily = builder.like(
                        builder.upper(namejoin.get("familyName").as(String.class)),
                        builder.upper(builder.literal(name.getValue()+"%"))
                );
                Predicate p = builder.or(pfamily, pgiven);
                predList.add(p);
            }
        }





        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0) {
            criteria.select(root).where(predArray);
        }
        else {
            criteria.select(root);
        }
        List<PersonEntity> qryResults = null;
        TypedQuery<PersonEntity> typedQuery = em.createQuery(criteria).setMaxResults(daoutils.MAXROWS);


        qryResults = typedQuery.getResultList();

        log.debug("Found Persons = "+qryResults.size());

        return qryResults;
    }


}
