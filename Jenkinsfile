pipeline {
    agent { label 'worker' }
    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        REPO_NAME = sh(returnStdout: true, script: 'basename `git remote get-url origin` .git').trim()
        VERSION = sh(returnStdout: true, script: 'uv version --short').trim()
        LATEST_AUTHOR = sh(returnStdout: true, script: 'git show -s --pretty=%an').trim()
        LATEST_COMMIT_ID = sh(returnStdout: true, script: 'git describe --tags --long  --always').trim()
        MAIN_BRANCH_REGEX = /(^main$)/

        DOCKER_CREDENTIALS_ID = 'docker-heigit-ci-service'
        DOCKER_REPOSITORY = 'repo.heigit.org/heigit/ohsome-api'
    }

    stages {
        stage('Setup') {
            steps {
                script {
                    echo REPO_NAME
                    echo LATEST_AUTHOR
                    echo LATEST_COMMIT_ID
                    echo VERSION
                    
                    echo env.BUILD_NUMBER
                    echo env.TAG_NAME
                }
                script {
                    sh 'uv sync --locked'
                }
            }
            post {
                failure {
                  rocket_buildfail()
                }
            }
        }

        stage('Test') {
            environment {
                VIRTUAL_ENV="${WORKSPACE}/.venv"
                PATH="${VIRTUAL_ENV}/bin:${PATH}"
            }
            steps {
                script {
                    sh 'pytest --maxfail=1 --cov-report=xml --cov=ohsome_api --cov-fail-under=80'
                }
            }
            post {
                failure {
                  rocket_testfail()
                }
            }
        }

        stage('Lint') {
            environment {
                VIRTUAL_ENV="${WORKSPACE}/.venv"
                PATH="${VIRTUAL_ENV}/bin:${PATH}"
            }
            steps {
                script {
                    sh 'ruff format --check --diff'
                    sh 'ruff check'
                }
            }
            post {
                failure {
                  rocket_testfail()
                }
            }
        }

        stage('Type Analysis') {
            environment {
                VIRTUAL_ENV="${WORKSPACE}/.venv"
                PATH="${VIRTUAL_ENV}/bin:${PATH}"
            }
            steps {
                script {
                    sh 'ty check'
                }
            }
            post {
                failure {
                  rocket_testfail()
                }
            }
        }

        stage('Build and Deploy Release Image') {
            steps {
                script {
                    docker.withRegistry('https://repo.heigit.org', DOCKER_CREDENTIALS_ID) {
                        if (env.BRANCH_NAME ==~ MAIN_BRANCH_REGEX) {
                            dockerImage = docker.build(DOCKER_REPOSITORY + ':' + env.BRANCH_NAME)
                            dockerImage.push()
                        }
                    }
                }
            }
            post {
                failure {
                    rocket_releasedeployfail()
                }
            }
        }


        stage('Wrapping Up') {
            steps {
                encourage()
                status_change()
            }
        }
    }
}
