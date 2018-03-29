pipeline {
    agent any
    tools {
        maven 'apache-maven-3.0.1' 
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean scmp:package'
                archiveArtifacts artifacts: '**/target/*.scmp', fingerprint: true
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }
}
