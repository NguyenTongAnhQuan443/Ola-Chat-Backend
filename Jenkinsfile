pipeline {
    agent any
    tools {
        jdk 'OpenJDK-17'
        maven 'Maven3'
    }
    environment {
        REGISTRY = "nguyentonganhquan"
        IMAGE_NAME = "ola-chat-backend"
        TAG = "latest"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/NguyenTongAnhQuan443/Ola-Chat-Backend.git'
            }
        }
        stage('Add Firebase Service Account') {
            steps {
                withCredentials([file(credentialsId: 'firebase-service-account', variable: 'FIREBASE_KEY_FILE')]) {
                    sh 'cp %FIREBASE_KEY_FILE% src\\main\\resources\\serviceAccountKey.json'
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Login to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PASS')]) {
                    sh 'docker login -u %DOCKER_HUB_USER% -p %DOCKER_HUB_PASS%'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                sh "docker build -t %REGISTRY%/%IMAGE_NAME%:%TAG% ."
            }
        }
        stage('Push Docker Image') {
            steps {
                sh "docker push %REGISTRY%/%IMAGE_NAME%:%TAG%"
            }
        }
    }
}
