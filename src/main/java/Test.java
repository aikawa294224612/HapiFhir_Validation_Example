import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.ConceptValidationOptions;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SchemaBaseValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.schematron.SchematronBaseValidator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;


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

//		FhirContext fhirContext = FhirContext.forR4();
//
//        String yourFHIRResourceJSON = "{\r\n"
//        		+ "  \"resourceType\" : \"Patient\",\r\n"
//        		+ "  \"id\" : \"pat-example\",\r\n"
//        		+ "  \"meta\" : {\r\n"
//        		+ "    \"profile\" : [\"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/Patient-twcore\"]\r\n"
//        		+ "  },\r\n"
//        		+ "  \"text\" : {\r\n"
//        		+ "    \"status\" : \"generated\",\r\n"
//        		+ "    \"div\" : \"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\"><h3><b>病人基本資料</b></h3><blockquote><p><b>識別碼型別</b>：National Person Identifier <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"http://terminology.hl7.org/CodeSystem/v2-0203\\\">Identifier Type Codes</a>#NNxxx <b>[extension: <a href=\\\"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/identifier-suffix\\\">Identifier Suffix</a>]：</b>TWN）</span><br/><b>身分證字號（official）</b>：A123456789 （http://www.moi.gov.tw/）</p></blockquote><blockquote><p><b>識別碼型別</b>：Medical record number <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"http://terminology.hl7.org/CodeSystem/v2-0203\\\">Identifier Type Codes</a>#MR）</span><br/><b>病歷號（official）</b>：8862168 （https://www.tph.mohw.gov.tw/）</p></blockquote><p><b>病人的紀錄（active）</b>：使用中</p><p><b>姓名（official）</b>：陳加玲 Chan, Chia Lin</p><p><b>性別</b>：女性</p><p><b>出生日期</b>：1990-01-01</p><p><b>年齡[extension: <a href=\\\"StructureDefinition-person-age.html\\\">person-age</a>]</b>：32</p><p><b>國籍[extension: <a href=\\\"http://hl7.org/fhir/StructureDefinition/patient-nationality\\\">patient-nationality</a>]</b>：<span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\"> （ <a href=\\\"https://terminology.hl7.org/CodeSystem-ISO3166Part1.html\\\">ISO3166Part1</a>#TW）</span></p><p><b>聯絡方式</b>：Phone <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"https://hl7.org/fhir/R4/valueset-contact-point-system.html\\\">ContactPointSystem</a>#phone）</span><br/><b>聯絡電話</b>：（Mobile）0911327999 <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"https://build.fhir.org/codesystem-contact-point-use.html\\\">ContactPointUse</a>#mobile）</span><br/><b>聯絡電話使用效期</b>：2022-07-31至2024-07-31</p><p><b>聯絡地址</b>：(103)臺北市大同區大有里19鄰承德路三段52巷6弄210號2樓B室<br/><b>郵遞區號（postalCode） [extension: <a href=\\\"StructureDefinition-tw-postal-code.html\\\">tw-postal-code</a>] </b>： <a href=\\\"CodeSystem-postal-code3-tw.html\\\">103</a><br/><b>縣/市（city）</b>：臺北市<br/><b>鄉/鎮/市/區（district）</b>：大同區<br/><b>村(里)（village） [extension: <a href=\\\"StructureDefinition-tw-village.html\\\">tw-village</a>] </b>：大有里<br/><b>鄰（neighborhood） [extension: <a href=\\\"StructureDefinition-tw-neighborhood.html\\\">tw-neighborhood</a>] </b>：19鄰<br/><b>路/街（line）</b>：承德路<br/><b>段(section) [extension: <a href=\\\"StructureDefinition-tw-section.html\\\">tw-section</a>] </b>：三段<br/><b>巷/衖（lane） [extension: <a href=\\\"StructureDefinition-tw-lane.html\\\">tw-lane</a>] </b>：52巷<br/><b>弄（alley） [extension: <a href=\\\"StructureDefinition-tw-alley.html\\\">tw-alley</a>] </b>：6弄<br/><b>號（number） [extension: <a href=\\\"StructureDefinition-tw-number.html\\\">tw-number</a>] </b>：210號<br/><b>樓（floor） [extension: <a href=\\\"StructureDefinition-tw-floor.html\\\">tw-floor</a>] </b>：2樓<br/><b>室（room） [extension: <a href=\\\"StructureDefinition-tw-room.html\\\">tw-room</a>] </b>：B室<br/><b>國家（country）</b>：臺灣<br/><br/></p><p><b>婚姻狀態</b>：unmarried <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\\\">臺灣婚姻狀態值集</a>#U）</span></p><p><b>聯絡人（official）</b>：李立偉 Li, Li Wei<br/><b>關係</b>：father <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"http://terminology.hl7.org/CodeSystem/v3-RoleCode\\\">PatientRelationshipType</a>#FTH）</span><br/><b>聯絡方式</b>：Phone <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"https://hl7.org/fhir/R4/valueset-contact-point-system.html\\\">ContactPointSystem</a>#phone）</span><br/><b>聯絡電話</b>：（Mobile）0917159753 <span style=\\\"background: LightGoldenRodYellow; margin: 4px; border: 1px solid khaki\\\">（ <a href=\\\"https://build.fhir.org/codesystem-contact-point-use.html\\\">ContactPointUse</a>#mobile）</span><br/><b>聯絡電話使用效期</b>：2022-07-31至2024-07-31</p><p><b>向病人說明健康狀態時所使用的語言</b>：中文</p><p><b>紀錄的保管機構</b>： <a href=\\\"Organization-org-hosp-example.html\\\">Organization/org-hosp-example</a> \\\"衛生福利部臺北醫院\\\"</p><p><b>病人照片</b>： <a href=\\\"https://2.bp.blogspot.com/-v3yEwItkXKQ/VaMN_1Nx6TI/AAAAAAAAvhM/zDXN_eZw_UE/s800/youngwoman_42.png\\\">https://2.bp.blogspot.com/-v3yEwItkXKQ/VaMN_1Nx6TI/AAAAAAAAvhM/zDXN_eZw_UE/s800/youngwoman_42.png</a><br/><img src=\\\"https://2.bp.blogspot.com/-v3yEwItkXKQ/VaMN_1Nx6TI/AAAAAAAAvhM/zDXN_eZw_UE/s800/youngwoman_42.png\\\" width=\\\"250px\\\"/></p></div>\"\r\n"
//        		+ "  },\r\n"
//        		+ "  \"extension\" : [{\r\n"
//        		+ "    \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/person-age\",\r\n"
//        		+ "    \"valueAge\" : {\r\n"
//        		+ "      \"value\" : 32,\r\n"
//        		+ "      \"system\" : \"http://unitsofmeasure.org\",\r\n"
//        		+ "      \"code\" : \"a\"\r\n"
//        		+ "    }\r\n"
//        		+ "  },\r\n"
//        		+ "  {\r\n"
//        		+ "    \"extension\" : [{\r\n"
//        		+ "      \"url\" : \"code\",\r\n"
//        		+ "      \"valueCodeableConcept\" : {\r\n"
//        		+ "        \"coding\" : [{\r\n"
//        		+ "          \"system\" : \"urn:iso:std:iso:3166\",\r\n"
//        		+ "          \"code\" : \"TW\"\r\n"
//        		+ "        }]\r\n"
//        		+ "      }\r\n"
//        		+ "    }],\r\n"
//        		+ "    \"url\" : \"http://hl7.org/fhir/StructureDefinition/patient-nationality\"\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"identifier\" : [{\r\n"
//        		+ "    \"use\" : \"official\",\r\n"
//        		+ "    \"type\" : {\r\n"
//        		+ "      \"coding\" : [{\r\n"
//        		+ "        \"system\" : \"http://terminology.hl7.org/CodeSystem/v2-0203\",\r\n"
//        		+ "        \"code\" : \"NNxxx\",\r\n"
//        		+ "        \"_code\" : {\r\n"
//        		+ "          \"extension\" : [{\r\n"
//        		+ "            \"extension\" : [{\r\n"
//        		+ "              \"url\" : \"suffix\",\r\n"
//        		+ "              \"valueString\" : \"TWN\"\r\n"
//        		+ "            },\r\n"
//        		+ "            {\r\n"
//        		+ "              \"url\" : \"valueSet\",\r\n"
//        		+ "              \"valueCanonical\" : \"http://hl7.org/fhir/ValueSet/iso3166-1-3\"\r\n"
//        		+ "            }],\r\n"
//        		+ "            \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/identifier-suffix\"\r\n"
//        		+ "          }]\r\n"
//        		+ "        }\r\n"
//        		+ "      }]\r\n"
//        		+ "    },\r\n"
//        		+ "    \"system\" : \"http://www.moi.gov.tw/\",\r\n"
//        		+ "    \"value\" : \"A123456789\"\r\n"
//        		+ "  },\r\n"
//        		+ "  {\r\n"
//        		+ "    \"use\" : \"official\",\r\n"
//        		+ "    \"type\" : {\r\n"
//        		+ "      \"coding\" : [{\r\n"
//        		+ "        \"system\" : \"http://terminology.hl7.org/CodeSystem/v2-0203\",\r\n"
//        		+ "        \"code\" : \"MR\"\r\n"
//        		+ "      }]\r\n"
//        		+ "    },\r\n"
//        		+ "    \"system\" : \"https://www.tph.mohw.gov.tw/\",\r\n"
//        		+ "    \"value\" : \"8862168\"\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"active\" : true,\r\n"
//        		+ "  \"name\" : [{\r\n"
//        		+ "    \"use\" : \"official\",\r\n"
//        		+ "    \"text\" : \"陳加玲\",\r\n"
//        		+ "    \"family\" : \"Chen\",\r\n"
//        		+ "    \"given\" : [\"Chia Lin\"]\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"telecom\" : [{\r\n"
//        		+ "    \"system\" : \"phone\",\r\n"
//        		+ "    \"value\" : \"0911327999\",\r\n"
//        		+ "    \"use\" : \"mobile\",\r\n"
//        		+ "    \"period\" : {\r\n"
//        		+ "      \"start\" : \"2022-07-31\",\r\n"
//        		+ "      \"end\" : \"2024-07-31\"\r\n"
//        		+ "    }\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"gender\" : \"female\",\r\n"
//        		+ "  \"birthDate\" : \"1990-01-01\",\r\n"
//        		+ "  \"address\" : [{\r\n"
//        		+ "    \"extension\" : [{\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-section\",\r\n"
//        		+ "      \"valueString\" : \"三段\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-number\",\r\n"
//        		+ "      \"valueString\" : \"210號\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-village\",\r\n"
//        		+ "      \"valueString\" : \"大有里\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-neighborhood\",\r\n"
//        		+ "      \"valueString\" : \"19鄰\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-lane\",\r\n"
//        		+ "      \"valueString\" : \"52巷\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-alley\",\r\n"
//        		+ "      \"valueString\" : \"6弄\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-floor\",\r\n"
//        		+ "      \"valueString\" : \"2樓\"\r\n"
//        		+ "    },\r\n"
//        		+ "    {\r\n"
//        		+ "      \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-room\",\r\n"
//        		+ "      \"valueString\" : \"B室\"\r\n"
//        		+ "    }],\r\n"
//        		+ "    \"text\" : \"臺北市大同區大有里19鄰承德路三段52巷6弄210號2樓B室\",\r\n"
//        		+ "    \"line\" : [\"承德路\"],\r\n"
//        		+ "    \"city\" : \"臺北市\",\r\n"
//        		+ "    \"district\" : \"大同區\",\r\n"
//        		+ "    \"_postalCode\" : {\r\n"
//        		+ "      \"extension\" : [{\r\n"
//        		+ "        \"url\" : \"https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/tw-postal-code\",\r\n"
//        		+ "        \"valueCodeableConcept\" : {\r\n"
//        		+ "          \"coding\" : [{\r\n"
//        		+ "            \"system\" : \"https://twcore.mohw.gov.tw/ig/twcore/CodeSystem/postal-code3-tw\",\r\n"
//        		+ "            \"code\" : \"103\"\r\n"
//        		+ "          }]\r\n"
//        		+ "        }\r\n"
//        		+ "      }]\r\n"
//        		+ "    },\r\n"
//        		+ "    \"country\" : \"TW\"\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"maritalStatus\" : {\r\n"
//        		+ "    \"coding\" : [{\r\n"
//        		+ "      \"system\" : \"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\",\r\n"
//        		+ "      \"code\" : \"U\"\r\n"
//        		+ "    }]\r\n"
//        		+ "  },\r\n"
//        		+ "  \"photo\" : [{\r\n"
//        		+ "    \"contentType\" : \"image/jpeg\",\r\n"
//        		+ "    \"data\" : \"R0lGODlhEwARAPcAAAAAAAAA/+9aAO+1AP/WAP/eAP/eCP/eEP/eGP/nAP/nCP/nEP/nIf/nKf/nUv/nWv/vAP/vCP/vEP/vGP/vIf/vKf/vMf/vOf/vWv/vY//va//vjP/3c//3lP/3nP//tf//vf///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////yH5BAEAAAEALAAAAAATABEAAAi+AAMIDDCgYMGBCBMSvMCQ4QCFCQcwDBGCA4cLDyEGECDxAoAQHjxwyKhQAMeGIUOSJJjRpIAGDS5wCDly4AALFlYOgHlBwwOSNydM0AmzwYGjBi8IHWoTgQYORg8QIGDAwAKhESI8HIDgwQaRDI1WXXAhK9MBBzZ8/XDxQoUFZC9IiCBh6wEHGz6IbNuwQoSpWxEgyLCXL8O/gAnylNlW6AUEBRIL7Og3KwQIiCXb9HsZQoIEUzUjNEiaNMKAAAA7\",\r\n"
//        		+ "    \"url\" : \"https://2.bp.blogspot.com/-v3yEwItkXKQ/VaMN_1Nx6TI/AAAAAAAAvhM/zDXN_eZw_UE/s800/youngwoman_42.png\"\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"contact\" : [{\r\n"
//        		+ "    \"relationship\" : [{\r\n"
//        		+ "      \"coding\" : [{\r\n"
//        		+ "        \"system\" : \"http://terminology.hl7.org/CodeSystem/v3-RoleCode\",\r\n"
//        		+ "        \"code\" : \"FTH\"\r\n"
//        		+ "      }]\r\n"
//        		+ "    }],\r\n"
//        		+ "    \"name\" : {\r\n"
//        		+ "      \"use\" : \"official\",\r\n"
//        		+ "      \"text\" : \"李立偉\",\r\n"
//        		+ "      \"family\" : \"Li\",\r\n"
//        		+ "      \"given\" : [\"Li Wei\"]\r\n"
//        		+ "    },\r\n"
//        		+ "    \"telecom\" : [{\r\n"
//        		+ "      \"system\" : \"phone\",\r\n"
//        		+ "      \"value\" : \"0917159753\",\r\n"
//        		+ "      \"use\" : \"mobile\",\r\n"
//        		+ "      \"period\" : {\r\n"
//        		+ "        \"start\" : \"2022-07-31\",\r\n"
//        		+ "        \"end\" : \"2024-07-31\"\r\n"
//        		+ "      }\r\n"
//        		+ "    }]\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"communication\" : [{\r\n"
//        		+ "    \"language\" : {\r\n"
//        		+ "      \"coding\" : [{\r\n"
//        		+ "        \"system\" : \"urn:ietf:bcp:47\",\r\n"
//        		+ "        \"code\" : \"zh-TW\"\r\n"
//        		+ "      }]\r\n"
//        		+ "    }\r\n"
//        		+ "  }],\r\n"
//        		+ "  \"managingOrganization\" : {\r\n"
//        		+ "    \"reference\" : \"Organization/org-hosp-example\"\r\n"
//        		+ "  }\r\n"
//        		+ "}";
//        IBaseResource resource = fhirContext.newJsonParser().parseResource(yourFHIRResourceJSON);
//
//        FhirValidator validator = fhirContext.newValidator();
//
//        ca.uhn.fhir.validation.ValidationResult result = validator.validateWithResult(resource);
//
//        if (result.isSuccessful()) {
//            System.out.println("Validation successful");
//        } else {
//            System.out.println("Validation failed");
//            System.out.println(result.getOperationOutcome());
//        }
		
	}

}
