# HapiFhir_Validation_Example
An example of validating using packages 

## Description

 - 主程式位於
   [Test.java](https://github.com/aikawa294224612/HapiFhir_Validation_Example/blob/main/src/main/java/Test.java)
- 示範HAPI FHIR document中 [14.2.5 Validating Using
   Packages](https://hapifhir.io/hapi-fhir/docs/validation/instance_validator.html#packages)
- 使用HAPI FHIR所提供之範例程式
   hapi-fhir/.../[ValidatorExamples.java](https://github.com/hapifhir/hapi-fhir/blob/master/hapi-fhir-docs/src/main/java/ca/uhn/hapi/fhir/docs/ValidatorExamples.java)

## Validating Using Packages

According to the instruction:

***void org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport.loadPackageFromClasspath(String theClasspath) throws IOException
Load an NPM package using a classpath specification, e.g. `/path/to/resource/my_package.tgz`. The classpath spec can optionally be prefixed with the string `classpath:`***


## Outcome
成功驗證:

    Failed
    [SingleValidationMessage[col=311,row=1,locationString=Patient,message=Patient.gender: minimum required = 1, but only found 0 (from https://twcore.mohw.gov.tw/ig/twcore/StructureDefinition/Patient-twcore|0.1.1),Validation_VAL_Profile_Minimum,severity=error]]


