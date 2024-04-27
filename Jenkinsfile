pipeline {
    agent {
        docker {
            image 'alpinelinux/docker-cli'
        }
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
                    sh 'docker-compose up --build --abort-on-container-exit'
                    sh 'ls -al'
                    // Insert your build commands here, e.g., 'mvn clean install'
                }
            }
        }
        stage('Export result') {
            steps {
                echo 'exporting...'
                        sh 'docker cp /target:/docker/target'
                        sh 'ls -al /target'
                // Insert your test commands here, e.g., 'mvn test'
            }
        }
        stage ('Send reporting') {
            steps {
                echo 'Tear down...'
            }
        }
        // stage('Tear down') {
        //     steps {
        //         echo 'Tear down...'
        //         sh 'docker-compose down'
        //         // Insert your build commands here, e.g., 'mvn clean install'
        //     }
        // }
    }
    post {
        always {
            emailext mimeType: 'text/html',
            body: 'Hi',
            subject: "Selenium: Job '${env.JOB_NAME}' Status: currentBuild.resul",
            to: 'noikhongvoitrai@gmail.com'
        }
    }
}
