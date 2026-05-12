pipeline {
    agent any

    tools {
        nodejs 'NODE_HOME'   // Nombre del NodeJS configurado en Jenkins → Global Tool Configuration
    }

    environment {
        // ── Credenciales desde Jenkins Credentials Manager ──────────
        GITHUB_TOKEN = credentials('github-token')
        SONAR_TOKEN  = credentials('Sonarqube')
        TOMCAT_CREDS = credentials('tomcat-credentials')
        // TOMCAT_CREDS_USR y TOMCAT_CREDS_PSW se generan automáticamente

        // ── URLs de servicios en la red de Jenkins ───────────────────
        SONAR_HOST_URL = 'http://sonarqube:9000'
        TOMCAT_URL     = 'http://tomcat-psd:8080'

        // ── Proyecto Angular ─────────────────────────────────────────
        PROJECT_DIR  = 'prurba-ang19'           // carpeta raíz del repo
        DIST_DIR     = 'dist/sysalmacenf'       // outputPath producción en angular.json
        WAR_NAME     = 'sysalmacenf.war'        // nombre del artefacto final
        APP_PATH     = '/sysalmacenf'           // context-path en Tomcat (= baseHref)
    }

    stages {

        // ─────────────────────────────────────────────────────────────
        stage('Clone') {
        // ─────────────────────────────────────────────────────────────
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    git branch: 'main',
                        credentialsId: 'github-token',
                        url: 'https://github.com/dmamanipar/PSD-DMP.git'
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        stage('Install Dependencies') {
        // ─────────────────────────────────────────────────────────────
            steps {
                timeout(time: 8, unit: 'MINUTES') {
                    dir("${PROJECT_DIR}") {
                        // --legacy-peer-deps evita conflictos comunes en Angular 19+
                        sh 'npm ci --legacy-peer-deps'
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        //stage('Test') {
        // Karma con Chrome headless; genera reporte de cobertura en coverage/
        // ─────────────────────────────────────────────────────────────
        //    steps {
        //        timeout(time: 10, unit: 'MINUTES') {
        //            dir("${PROJECT_DIR}") {
        //                sh '''
        //                   npx ng test \
        //                        --no-watch \
        //                        --no-progress \
        //                        --browsers=ChromeHeadless \
        //                        --code-coverage
        //                '''
        //            }
        //        }
        //    }
        //    post {
        //        always {
        //            // Publicar resultados de tests en la UI de Jenkins
        //            junit allowEmptyResults: true,
        //                  testResults: "${PROJECT_DIR}/test-results/**/*.xml"
        //        }
        //    }
        //}

        // ─────────────────────────────────────────────────────────────
        stage('Sonar') {
        // ─────────────────────────────────────────────────────────────
		// se omitio esta linea:  -Dsonar.javascript.lcov.reportPaths=coverage/prurba-ang19/lcov.info 
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    withSonarQubeEnv('sonarqube') {
                        dir("${PROJECT_DIR}") {
                            sh """
                                npx sonar-scanner \
                                    -Dsonar.projectKey=prurba-ang19 \
                                    -Dsonar.projectName=prurba-ang19 \
                                    -Dsonar.sources=src \
                                    -Dsonar.exclusions=**/node_modules/**,**/*.spec.ts \
                                    -Dsonar.token=${SONAR_TOKEN} \
                                    -Dsonar.host.url=${SONAR_HOST_URL}
                            """
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        //stage('Quality Gate') {
        // ─────────────────────────────────────────────────────────────
        //    steps {
        //        sleep(10)
        //        timeout(time: 10, unit: 'MINUTES') {
        //            waitForQualityGate abortPipeline: true
        //        }
        //    }
        //}

        // ─────────────────────────────────────────────────────────────
        stage('Build') {
        // ng build --configuration production → dist/sysalmacenf/
        // ─────────────────────────────────────────────────────────────
            steps {
                timeout(time: 8, unit: 'MINUTES') {
                    dir("${PROJECT_DIR}") {
                        sh 'npx ng build --configuration production'
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        stage('Package WAR') {
        // Empaqueta dist/sysalmacenf/* → sysalmacenf.war con jar -cvf
        // ─────────────────────────────────────────────────────────────
            steps {
                timeout(time: 3, unit: 'MINUTES') {
                    dir("${PROJECT_DIR}/${DIST_DIR}") {
                        // jar -cvf crea el WAR dentro de la propia carpeta del compilado
                        sh "jar -cvf ${WORKSPACE}/${PROJECT_DIR}/${WAR_NAME} *"
                    }
                }
            }
            post {
                success {
                    // Guardar el WAR como artefacto descargable en Jenkins
                    archiveArtifacts artifacts: "${PROJECT_DIR}/${WAR_NAME}",
                                     fingerprint: true
                }
            }
        }

        // ─────────────────────────────────────────────────────────────
        stage('Deploy to Tomcat') {
        // Sube el WAR al Tomcat Manager vía curl (hot-deploy)
        // ─────────────────────────────────────────────────────────────
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh """
                        curl -u ${TOMCAT_CREDS_USR}:${TOMCAT_CREDS_PSW} \
                             -T "${PROJECT_DIR}/${WAR_NAME}" \
                             "${TOMCAT_URL}/manager/text/deploy?path=${APP_PATH}&update=true"
                    """
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    post {
    // ─────────────────────────────────────────────────────────────────
        success {
            echo "✅ Deploy exitoso → ${TOMCAT_URL}${APP_PATH}"

            withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
                sh """
                    curl -s \
                         -H "Authorization: token ${GH_TOKEN}" \
                         -H "Content-Type: application/json" \
                         -X POST \
                         -d '{"state":"success","description":"Pipeline OK","context":"jenkins/ci"}' \
                         https://api.github.com/repos/dmamanipar/PSD-DMP/statuses/${GIT_COMMIT}
                """
            }
        }

        failure {
            echo "❌ Pipeline falló — revisar logs en Jenkins"

            withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
                sh """
                    curl -s \
                         -H "Authorization: token ${GH_TOKEN}" \
                         -H "Content-Type: application/json" \
                         -X POST \
                         -d '{"state":"failure","description":"Pipeline falló","context":"jenkins/ci"}' \
                         https://api.github.com/repos/dmamanipar/PSD-DMP/statuses/${GIT_COMMIT}
                """
            }
        }

        always {
            cleanWs()
        }
    }
}
