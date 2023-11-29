import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


public class Test {
	
	public static void main(String[] args) throws Exception{
		
		FhirContext ctx = FhirContext.forR4();
		NpmPackageValidationSupport npmPackageSupport = new NpmPackageValidationSupport(ctx);
		npmPackageSupport.loadPackageFromClasspath("classpath:package/package.tgz");

		ValidationSupportChain validationSupportChain = new ValidationSupportChain(
				npmPackageSupport,
				new DefaultProfileValidationSupport(ctx),
				new CommonCodeSystemsTerminologyService(ctx),
				new InMemoryTerminologyServerValidationSupport(ctx),
				new SnapshotGeneratingValidationSupport(ctx));
		CachingValidationSupport validationSupport = new CachingValidationSupport(validationSupportChain);


		FhirValidator validator = ctx.newValidator();
		FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
		validator.registerValidatorModule(instanceValidator);

		// Create a simple patient data
		Patient patient = new Patient();
		patient.getMeta().addProfile("https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/Patient-twcore");
		patient.addIdentifier()
		.setType(new CodeableConcept().addCoding(new Coding().setCode("MR").setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")))
		.setSystem("https://www.tph.mohw.gov.tw/")
		.setValue("12345");
		
		//set date
		ZoneId defaultZoneId = ZoneId.systemDefault();
		LocalDate localDate = LocalDate.now();     
        Date date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());  
        
		patient.setBirthDate(date);
		// patient.setGender(AdministrativeGender.FEMALE);

		ValidationResult outcome = validator.validateWithResult(patient);
		
		if (outcome.isSuccessful()) {
            System.out.println("Successful");
        } else {
            System.out.println("Failed");
            System.out.println(outcome.getMessages());

        }
		
	}

}
