pipeline {
    agent { label 'worker' }
    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Setup') {
            environment {
                REPO_NAME = sh(returnStdout: true, script: 'basename `git remote get-url origin` .git').trim()
                VERSION = sh(returnStdout: true, script: 'uv version --short').trim()
                LATEST_AUTHOR = sh(returnStdout: true, script: 'git show -s --pretty=%an').trim()
                LATEST_COMMIT_ID = sh(returnStdout: true, script: 'git describe --tags --long  --always').trim()
            }
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
                    sh 'uv sync --locked --no-editable'
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
                    sh 'pytest --cov-report=xml --maxfail=1'
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
                        dockerImage = docker.build(DOCKER_REPOSITORY + ':' + 'main')
                        dockerImage.push()
                        dockerImage.push('main')
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
