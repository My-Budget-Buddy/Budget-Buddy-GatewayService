// TEMPLATE REPLACEMENTS
// <service-name> - the name of the service (i.e. user-service for kubernetes)
// <pascal-service-name> - the name of the service in PascalCase (i.e. UserService for the SonarQube project key)
// <namespace> - the namespace of the service
pipeline {
  agent {
    kubernetes {
      yaml '''
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: maven
                image: 924809052459.dkr.ecr.us-east-1.amazonaws.com/maven:latest
                command:
                - "sleep"
                args:
                - "9999999"
              - name: aws-kubectl
                image: 924809052459.dkr.ecr.us-east-1.amazonaws.com/aws-kubectl:latest
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

  options {
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
  }

    // Global Environment Variables
    environment {
        SERVICE_NAME = 'gateway-service'
        PASCAL_SERVICE_NAME = 'GatewayService'
        NAMESPACE = 'staging'
        EUREKA_URL = 'http://discovery-service.staging.svc.cluster.local:8761/eureka'
    }

    stages {
        // Pull all git dependencies
        stage('Pull Dependencies'){
            steps{
              sh 'git clone https://github.com/My-Budget-Buddy/Budget-Buddy-Kubernetes.git'
            }
        }

        // Build the project for the staging environment
        stage('Build for Staging') {
            when {
                branch 'testing-cohort'
            }

            steps {
                container('maven') {
                    sh 'mvn clean install -DskipTests=true -Dspring.profiles.active=build'
                }
            }
        }
    
        // Set up the database for the staging environment
        stage('Set Up Database for Staging') {
            when {
                branch 'testing-cohort'
            }

            steps {
                container('aws-kubectl') {
                    withCredentials([
                            string(credentialsId: 'STAGING_DATABASE_USER', variable: 'DATABASE_USERNAME'),
                            string(credentialsId: 'STAGING_DATABASE_PASSWORD', variable: 'DATABASE_PASSWORD')])
                    {
                    sh '''
                    aws eks --region us-east-1 update-kubeconfig --name project3-eks
                    
                    # deploy staging db

                    cd Budget-Buddy-Kubernetes/Databases
                    chmod +x ./deploy-database.sh
                    ./deploy-database.sh ${NAMESPACE} $DATABASE_USERNAME $DATABASE_PASSWORD
                    '''
                    }
                }
            }
        }
    
        // Run coverage and analysis for the staging environment
        stage('Test and Analyze for Staging') {
            when {
                branch 'testing-cohort'
            }

            steps {
                container('maven') {
                    withCredentials([
                    string(credentialsId: 'STAGING_DATABASE_USER', variable: 'DATABASE_USER'),
                    string(credentialsId: 'STAGING_DATABASE_PASSWORD', variable: 'DATABASE_PASS')])
                    {
                        sh '''
                            export DATABASE_URL=jdbc:postgresql://${SERVICE_NAME}-postgres.${NAMESPACE}.svc.cluster.local:5432/my_budget_buddy
                            mvn clean verify -Pcoverage -Dspring.profiles.active=test \
                                -Dspring.datasource.url=$DATABASE_URL \
                                -Dspring.datasource.username=$DATABASE_USER \
                                -Dspring.datasource.password=$DATABASE_PASS
                        '''
                        withSonarQubeEnv('SonarCloud') {
                            sh '''
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=My-Budget-Buddy_Budget-Buddy-${PASCAL_SERVICE_NAME} \
                                    -Dsonar.projectName=Budget-Buddy-${PASCAL_SERVICE_NAME} \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                    -Dsonar.branch.name=testing-cohort
                            '''
                        }
                    }
                }
            }
        }

        // Run coverage and analysis for the production environment
        stage('Test and Analyze for Production') {
            when {
                branch 'testing-main'
            }

            steps {
                container('maven') {
                    withCredentials([
                    string(credentialsId: 'STAGING_DATABASE_USER', variable: 'DATABASE_USER'),
                    string(credentialsId: 'STAGING_DATABASE_PASSWORD', variable: 'DATABASE_PASS')])
                    {
                        sh '''
                            export DATABASE_URL=jdbc:postgresql://${SERVICE_NAME}-postgres.${NAMESPACE}.svc.cluster.local:5432/my_budget_buddy
                            mvn clean verify -Pcoverage -Dspring.profiles.active=test \
                                -Dspring.datasource.url=$DATABASE_URL \
                                -Dspring.datasource.username=$DATABASE_USER \
                                -Dspring.datasource.password=$DATABASE_PASS
                        '''
                        withSonarQubeEnv('SonarCloud') {
                            sh '''
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=My-Budget-Buddy_Budget-Buddy-${PASCAL_SERVICE_NAME} \
                                    -Dsonar.projectName=Budget-Buddy-${PASCAL_SERVICE_NAME} \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                    -Dsonar.branch.name=testing-main
                            '''
                        }
                    }
                }
            }
        }
    
        // Build and push the docker image to ECR
        stage('Build and Push Docker Image') {
            steps {
                container('kaniko') {
                    script {
                        def imageTag = 'latest'
                        
                        // Determine the image tag based on the branch
                        // note that the var must nonetheless be exported in the operative shell command
                        if (BRANCH_NAME == 'testing-cohort') {
                        imageTag = 'test-latest'
                        } else if (env.BRANCH_NAME == 'main') {
                        imageTag = 'latest'
                        }

                        sh '''
                        export IMAGE_TAG=''' + imageTag + '''
                        rm -rf /var/lock
                        # Get the ECR login password
                        export ECR_LOGIN=$(aws ecr get-login-password --region $AWS_REGION)
                        if [ -z "$ECR_LOGIN" ]; then
                            echo "Failed to get ECR login password"
                            exit 1
                        fi
                        mkdir -p /kaniko/.docker
                        echo "{\"auths\":{\"924809052459.dkr.ecr.us-east-1.amazonaws.com\":{\"auth\":\"$(echo -n AWS:$ECR_LOGIN | base64)\"}}}" > /kaniko/.docker/config.json
                            echo ${imageTag}
                            
                        /kaniko/executor --dockerfile=Dockerfile.prod --context=dir://. --destination=924809052459.dkr.ecr.us-east-1.amazonaws.com/${SERVICE_NAME}:${IMAGE_TAG}
                        '''
                    }
                }
            }
        }

//ASDFASDFASDFAS
    // after this stage, the operative app is deployed to the staging EKS cluster
    // and should be ready for testing on the staging eks
    stage('Deploy to Staging EKS') {
    when {
        branch 'testing-cohort'
    }
    
    steps {
        container('aws-kubectl') {
            withCredentials([
                  string(credentialsId: 'STAGING_DATABASE_USER', variable: 'DATABASE_USERNAME'),
                  string(credentialsId: 'STAGING_DATABASE_PASSWORD', variable: 'DATABASE_PASSWORD')])
            {
            sh '''
            aws eks --region us-east-1 update-kubeconfig --name project3-eks

            # deploy service

            cd Budget-Buddy-Kubernetes/Deployments/Services
            # set test image
            sed -i "s/<image-version>/test-latest/" deployment-${SERVICE_NAME}.yaml
            # set test DB url
            # note use of | as delimiter because of forward slashes in the url
            sed -i 's|<db-url>|jdbc:****ql://${SERVICE_NAME}-postgres.${NAMESPACE}.svc.cluster.local:5432/my_budget_buddy/|' deployment-${SERVICE_NAME}.yaml

            # set eureka URL
            echo ${EUREKA_URL}
            export MY_EUREKA_URL=${EUREKA_URL}
            sed -i 's|<eureka-url>|$MY_EUREKA_URL|' deployment-${SERVICE_NAME}.yaml

            # reapply

            kubectl delete -f ./deployment-${SERVICE_NAME}.yaml --namespace=${NAMESPACE} || true 
            kubectl apply -f ./deployment-${SERVICE_NAME}.yaml --namespace=${NAMESPACE}
            '''
            }
        }
      }
    }
  }
  
  // add functional, performance tests

  post {
    always {
      cleanWs()
    }
  }
}

