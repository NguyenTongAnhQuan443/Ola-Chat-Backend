pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = credentials('dockerhub')
    DOCKER_REPO = 'your-dockerhub-username/ola-chat-backend'
    IMAGE_TAG = '1.0'
  }

  stages {
    stage('Build') {
      steps {
        bat """
          docker build -t %DOCKER_REPO%:%IMAGE_TAG% .
        """
      }
    }

    stage('Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          bat """
            echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
            docker push %DOCKER_REPO%:%IMAGE_TAG%
          """
        }
      }
    }

    stage('Deploy to Render') {
      steps {
        withCredentials([string(credentialsId: 'render-ola-chat', variable: 'DEPLOY_HOOK')]) {
          bat """
            curl -X POST "%DEPLOY_HOOK%"
          """
        }
      }
    }
  }

  post {
    always {
      bat "docker logout"
    }
  }
}