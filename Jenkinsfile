pipeline {
    agent any
    tools {
        maven '3.9.6'
        jdk 'jdk11'
    }
    parameters {
        extendedChoice(
            name: 'BROWSER',
            type: 'PT_SINGLE_SELECT',
            value: 'chromeGCP,chrome,firefox',
            description: 'Please select the browser that you want to run',
            visibleItemCount: 3,
            multiSelectDelimiter: ',',
            quoteValue: false
        )
        extendedChoice(
            name: 'TAGGING',
            type: 'PT_CHECKBOX',
            value: 'Tiki,Shopee,Google',
            description: 'Please select the tagging that you want to run',
            visibleItemCount: 3,
            multiSelectDelimiter: ',',
            quoteValue: false
        )
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }
        stage('Checkout') {
            steps {
                echo 'Checkout...'
                checkout([$class: 'GitSCM', branches: [
                    [name: '*/master']
                ], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [
                    [credentialsId: 'jenkins-user-github', url: 'https://github.com/thitbachuy/sample-run-jenkins.git']
                ]])
                sh "ls -lart ./*"
            }
        }
        stage('Create containers and run test') {
            steps {
                script {
                    echo 'Creating containers...'
                    echo "BROWSER: ${params.BROWSER}"
                    echo "TAGGING: ${params.TAGGING}"
                    def ipAddress = "127.0.0.1"
                    echo "IP address of selenium: ${ipAddress}"
                    def selectedOptions = params.TAGGING.split(',')
                    selectedOptions = selectedOptions.collect { "@${it}" }
                    def tagging = selectedOptions.join(',')
                    echo "Selected options with '@': ${selectedOptions.join(',')}"
                    echo "tagging: ${tagging}"
                    sh "mvn test -Dcucumber.filter.tags=\"@Tiki or @Google\" -Dcucumber.filter -Dbrowser=${params.BROWSER} -Dhostname=${ipAddress} -DexecutingEnv=test -DtestedEnv=uat -Dplatform=desktop"
                    sh 'ls -al'
                    // Insert your build commands here, e.g., 'mvn clean install'
                }
            }
        }
        // stage('Export result') {
        //     steps {
        //         echo 'exporting...'
        //         // sh 'docker cp /target:/target'
        //         // sh 'ls -al /target'
        //         // Insert your test commands here, e.g., 'mvn test'
        //     }
        // }
        // 
        //   stage('Send Email') {
        //     steps {
        //         emailext subject: 'Build Notification',
        //                   body: 'Your build has completed.',
        //                   to: 'noikhongvoitrai@gmail.com',
        //                   replyTo: 'noikhongvoitrai1@gmail.com',
        //                   mimeType: 'text/html'
        //     }
        // }
    }
}
