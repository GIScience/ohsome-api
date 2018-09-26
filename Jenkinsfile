pipeline {

  /*tried slave dockers but only had disadvantages
  agent {
  docker{
  image 'maven:3-jdk-8'
  args '-v /root/.m2:/root/.m2'
  }
  }
   */

  agent any
  stages {
    stage ('Build and Test') {
      steps {
        script {
          author = sh(returnStdout: true, script: 'git show -s --pretty=%an')
          echo author
          reponame=sh(returnStdout: true, script: 'basename `git remote get-url origin` .git').trim()
          echo reponame
          echo env.BRANCH_NAME
          echo env.BUILD_NUMBER
        }
        script {
          server = Artifactory.server 'HeiGIT Repo'
          rtMaven = Artifactory.newMavenBuild()
          rtMaven.resolver server: server, releaseRepo: 'main', snapshotRepo: 'main'
          rtMaven.deployer server: server, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
          rtMaven.deployer.addProperty("deployer", "jenkinsOhsome")
          rtMaven.deployer.deployArtifacts = false
          env.MAVEN_HOME = '/usr/share/maven'
        }
        script {
          buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean compile javadoc:jar source:jar install -Dmaven.repo.local=.m2 -Dport1=8081 -Dport2=8082 -Dport3=8083 -DdbFilePathProperty="--database.db=/opt/data/heidelberg.oshdb"'
        } 
      }
      post{
        failure {
          rocketSend channel: 'jenkinsohsome', emoji: ':sob:' , message: "ohsome-api-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}. Review the code!" , rawMessage: true
        }
      }
    }
    
    stage ('publish Javadoc'){
      when {
        expression {
          return env.BRANCH_NAME ==~ /(^[0-9]+$)|(^(([0-9]+)(\.))+([0-9]+)?$)|(^master$)/
        }
      }
      steps {
        script{
          //load dependencies to artifactory
          rtMaven.run pom: 'pom.xml', goals: 'org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version -Dmaven.repo.local=.m2'
          projver=sh(returnStdout: true, script: 'mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev "(^\\[|Download\\w+:)"').trim()

          javadc_dir="/srv/javadoc/java/" + reponame + "/" + projver + "/"
          echo javadc_dir
        
          rtMaven.run pom: 'pom.xml', goals: 'clean javadoc:javadoc -Dadditionalparam=-Xdoclint:none -Dmaven.repo.local=.m2'
          sh "echo $javadc_dir"
          //make shure jenkins uses bash not dash!
          sh "mkdir -p $javadc_dir && rm -Rf $javadc_dir* && find . -path '*/target/site/apidocs' -exec cp -R --parents {} $javadc_dir \\; && find $javadc_dir -path '*/target/site/apidocs' | while read line; do echo \$line; neu=\${line/target\\/site\\/apidocs/} ;  mv \$line/* \$neu ; done && find $javadc_dir -type d -empty -delete"
        }
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', message: "Deployment of javadoc ohsome-api-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}." , rawMessage: true
        }
      }
    }
    
    stage ('reports and statistics'){
      steps {
        script{
          projver=sh(returnStdout: true, script: 'mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev "(^\\[|Download\\w+:)"').trim()
          report_dir="/srv/reports/" + reponame + "/" + projver + "/site/"
          
          rtMaven.run pom: 'pom.xml', goals: 'clean site -Dmaven.repo.local=.m2'
          sh "mkdir -p $report_dir && rm -Rf $report_dir* && find . -path '*/target/site' -exec cp -R --parents {} $report_dir \\; && find $report_dir -path '*/target/site' | while read line; do echo \$line; neu=\${line/target\\/site/} ;  mv \$line/* \$neu ; done && find $report_dir -type d -empty -delete"
        }
      }   
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', message: "Reporting of ohsome-api-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}." , rawMessage: true
        }
      }  
    }
    
    stage ('encourage') {
      when {         
        expression {
          if(currentBuild.number > 1){
            datepre=new Date(currentBuild.previousBuild.rawBuild.getStartTimeInMillis()).clearTime()
            echo datepre.format( 'yyyyMMdd' )
            datenow=new Date(currentBuild.rawBuild.getStartTimeInMillis()).clearTime()
            echo datenow.format( 'yyyyMMdd' )
            return datepre.numberAwareCompareTo(datenow)<0
          }
          return false
        }
      }
      steps {
        rocketSend channel: 'jenkinsohsome', message: "Hey, this is just your daily notice that Jenkins is still working for you on the ohsome-API! Happy and for free! Keep it up!" , rawMessage: true
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', emoji: ':wink:' , message: "Reporting of ohsome-api-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}." , rawMessage: true
        }
      }  
    }
    
    stage ('Report status change'){
      when {
        expression {
          return ((currentBuild.number > 1) && (currentBuild.getPreviousBuild().result == 'FAILURE'))
        }
      }
      steps {
        rocketSend channel: 'jenkinsohsome', message: "We had some problems, bu we are BACK TO NORMAL! Nice debugging: ohsome-api-build-nr. ${env.BUILD_NUMBER} *succeeded* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}." , rawMessage: true
      }
      post {
        failure {
          rocketSend channel: 'jenkinsohsome', message: "Reporting of ohsome-api-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Latest commit from  ${author}." , rawMessage: true
        }
      }
      
    }

  }
}
