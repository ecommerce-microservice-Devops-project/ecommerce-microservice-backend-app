pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "sebas3004tian"
        IMAGE_TAG = "${env.BRANCH_NAME}-2"
        BRANCH_NAME = "${env.BRANCH_NAME}"
        REPO_URL = "https://github.com/ecommerce-microservice-Devops-project/ecommerce-microservice-backend-app.git"
        K8S_NAMESPACE = "ecommerce-${BRANCH_NAME}" 
    }


    stages {
        stage('Prepare Namespace') {
            steps {
                sh "kubectl get namespace ${K8S_NAMESPACE} || kubectl create namespace ${K8S_NAMESPACE}"
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${BRANCH_NAME}", url: "${REPO_URL}"
            }
        }

        stage('Change Image in Manifests') {
            steps {
                script {
                    def allServicesCore = ["cloud-config", "service-discovery"]
                    def allServices = [
                        "api-gateway", "favourite-service", "order-service",
                        "payment-service", "product-service", "shipping-service", "user-service", "proxy-client"
                    ]

                    for (serviceCore in allServicesCore) {
                        def manifestPath = "k8s/dev/core/${serviceCore}-deployment.yaml"
                        if (fileExists(manifestPath)) {
                            def newImage = "${DOCKERHUB_USER}/${serviceCore}-ecommerce-boot:${IMAGE_TAG}"
                            // Match imágenes como selimhorri/cloud-config-ecommerce-boot:0.1.0
                            sh "sed -i 's|image: .*/${serviceCore}-ecommerce-boot:.*|image: ${newImage}|' ${manifestPath}"
                            sh "grep 'image:' ${manifestPath}"
                        }
                    }

                    for (service in allServices) {
                        def manifestPath = "k8s/dev/${service}-deployment.yaml"
                        if (fileExists(manifestPath)) {
                            def newImage = "${DOCKERHUB_USER}/${service}:${IMAGE_TAG}"
                            // Match imágenes como selimhorri/api-gateway-ecommerce-boot:0.1.0
                            sh "sed -i 's|image: .*/${service}-ecommerce-boot:.*|image: ${newImage}|' ${manifestPath}"
                            sh "grep 'image:' ${manifestPath}"
                        }
                    }
                }
            }
        }


        stage('Deploy Core Services') {
            steps {
                echo "Desplegando servicios core..."
                sh "kubectl apply -f k8s/dev/core/ -n ${K8S_NAMESPACE}"
            }
        }

        stage('Wait 3.5 Minutes') {
            steps {
                echo "Esperando 3 minutos y medio para que los core services estén listos..."
                sleep time: 210, unit: 'SECONDS'
            }
        }

        stage('Deploy Other Services') {
            steps {
                script {
                    def otherServices = [
                        "api-gateway", "favourite-service", "order-service",
                        "payment-service", "product-service", "shipping-service", "user-service", "proxy-client"
                    ]
                    for (service in otherServices) {
                        def pathDeployment = "k8s/dev/${service}-deployment.yaml"
                        def pathService = "k8s/dev/${service}-service.yaml"
                        if (fileExists(pathDeployment) && fileExists(pathService)) {
                            echo "Desplegando ${service}..."
                            sh "kubectl apply -f ${pathDeployment} -n ${K8S_NAMESPACE}"
                            sh "kubectl apply -f ${pathService} -n ${K8S_NAMESPACE}"
                        } else {
                            echo "WARNING: No se encontró deployment o service para ${service}"
                        }
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Pipeline falló, revisa los logs.'
        }
        always {
            echo "Pipeline ejecutado en el namespace: ${K8S_NAMESPACE}"
        }
    }
}