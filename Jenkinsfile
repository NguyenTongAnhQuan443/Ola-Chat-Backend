pipeline {
    agent any
    tools {
        jdk 'OpenJDK-17'
        maven 'Maven3'
    }
    environment {
        REGISTRY = "nguyentonganqhuan"
        IMAGE_NAME = "ola-chat-backend"
        TAG = "latest" // hoặc "${BUILD_NUMBER}"
    }
    stages {
        stage('Check JDK') {
            steps {
                sh 'java -version'
            }
        }
        stage('Check Maven') {
            steps {
                sh 'mvn -version'
            }
        }
        stage('Check Docker') {
            steps {
                sh 'docker --version'
            }
        }
        // Có thể thêm kiểm tra docker-compose nếu cần
        // stage('Check Docker Compose') {
        //     steps {
        //         sh 'docker-compose --version'
        //     }
        // }
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/NguyenTongAnhQuan443/Ola-Chat-Backend.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${REGISTRY}/${IMAGE_NAME}:${TAG}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                        docker.image("${REGISTRY}/${IMAGE_NAME}:${TAG}").push()
                    }
                }
            }
        }

    }
}
