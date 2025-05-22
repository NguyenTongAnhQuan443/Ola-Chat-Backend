pipeline {
    agent any
    tools {
        jdk 'OpenJDK-17'
        maven 'Maven3'
    }
    environment {
        REGISTRY = "nguyentonganqhuan"
        IMAGE_NAME = "ola-chat-backend"
        TAG = "latest"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/NguyenTongAnhQuan443/Ola-Chat-Backend.git'
            }
        }
        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                bat "docker build -t %REGISTRY%/%IMAGE_NAME%:%TAG% ."
            }
        }
        stage('Push Docker Image') {
            steps {
                bat "docker push %REGISTRY%/%IMAGE_NAME%:%TAG%"
            }
        }
    }
}
