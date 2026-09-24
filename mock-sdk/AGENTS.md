# mock-sdk

```
lib JAR · IBioApiV2 · no HTTP / no Boot repackage
├─ SampleSDKV2 (prefer) · SampleSDK (IBioApi legacy)
├─ services: CheckQuality · Match · ExtractTemplate
│            ConvertFormat · Segment · SDKInfo
└─ consumed via biosdk_class in biosdk-services
```

```
flags
└─ mosip.mocksdk.extraction=true → random bio (perf only)
```

```
deps: kernel-core · kernel-biometrics-api · kernel-bio-converter · spring-boot-jackson2
parent: mosip-mock-services 1.4.1-SNAPSHOT · NO kernel-bom
build: mvn clean install "-Dgpg.skip=true"
test:  mvn test "-Dgpg.skip=true" [-Dtest=Class#method]
```
