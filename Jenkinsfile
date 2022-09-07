@Library('k8s-cicd-pipelines') _

gradleDeliveryPipeline {
  buildCommand = './gradlew clean bootJar --no-daemon'
  testCommand = './gradlew check --no-daemon'
  accTestCommand = './gradlew runAcceptance --no-daemon'
  publishCommand = './gradlew assemble artifactoryPublish'
  channel = 'CJM6LA9AB'
  testArtifacts = ['timestamps-client/build/reports/','timestamps-common/build/reports/','timestamps-service/build/reports/']
}
