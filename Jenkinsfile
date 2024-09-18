pipeline {
  agent {
    kubernetes {
      yaml '''
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: maven
                image: maven:latest
                command:
                - "sleep"
                args:
                - "9999999"
              - name: kaniko
                image: 924809052459.dkr.ecr.us-east-1.amazonaws.com/kaniko:latest
                imagePullPolicy: Always
                volumeMounts:
                - name: kaniko-cache
                  mountPath: /kaniko/.cache
                env:
                - name: AWS_REGION
                  valueFrom:
                    secretKeyRef:
                      name: ecr-login
                      key: AWS_REGION
                - name: AWS_ACCESS_KEY_ID
                  valueFrom:
                    secretKeyRef:
                      name: ecr-login
                      key: AWS_ACCESS_KEY_ID
                - name: AWS_SECRET_ACCESS_KEY
                  valueFrom:
                    secretKeyRef:
                      name: ecr-login
                      key: AWS_SECRET_ACCESS_KEY
                command:
                - sleep
                args:
                - '9999999'
                tty: true
              volumes:
              - name: kaniko-cache
                emptyDir: {}
        '''
    }
  }

  stages {
    stage('Build and Push Docker Image') {
      steps {
        container('kaniko') {
              sh '''
                rm -rf /var/lock
                # Get the ECR login password
                export ECR_LOGIN=$(aws ecr get-login-password --region $AWS_REGION)
                if [ -z "$ECR_LOGIN" ]; then
                  echo "Failed to get ECR login password"
                  exit 1
                fi
                /kaniko/executor --dockerfile=Dockerfile --context=dir://. --destination=924809052459.dkr.ecr.us-east-1.amazonaws.com/gateway-service:latest
              '''
        }
      }
    }
  }
  
  post {
    always {
      cleanWs()
    }
  }
}