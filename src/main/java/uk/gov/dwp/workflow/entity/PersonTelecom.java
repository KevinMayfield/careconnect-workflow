package uk.gov.dwp.workflow.entity;

import javax.persistence.*;


@Entity
@Table(name="fhir_PersonTelecom",
		uniqueConstraints= @UniqueConstraint(name="PK_PERSON_TELECOM", columnNames={"PERSON_TELECOM_ID"})
		,indexes =
		{
				@Index(name = "IDX_PERSON_TELECOM", columnList="CONTACT_VALUE,SYSTEM_ID")
		})
public class PersonTelecom extends BaseContactPoint {

	public PersonTelecom() {

	}

	public PersonTelecom(PersonEntity personEntity) {
		this.personEntity = personEntity;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "PERSON_TELECOM_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "PERSON_ID",foreignKey= @ForeignKey(name="FK_PERSON_PERSON_TELECOM"))
	private PersonEntity personEntity;


    public Long getTelecomId() { return identifierId; }
	public void setTelecomId(Long identifierId) { this.identifierId = identifierId; }

	public PersonEntity getPerson() {
	        return this.personEntity;
	}

	public void setPersonEntity(PersonEntity personEntity) {
	        this.personEntity = personEntity;
	}

}
