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
        RELEASE_REGEX = /^([0-9]+(\.[0-9]+)*)((rc|b|a)[0-9]+)?$/

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

                    echo env.BRANCH_NAME
                    echo env.BUILD_NUMBER
                    echo env.TAG_NAME
                }
                script {
                    sh 'uv sync --locked --all-groups'
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
                recordCoverage(tools: [[parser: 'COBERTURA', pattern: 'coverage.xml']])
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

        stage('Build and Deploy Image') {
            steps {
                script {
                    docker.withRegistry('https://repo.heigit.org', DOCKER_CREDENTIALS_ID) {
                        if (env.BRANCH_NAME ==~ MAIN_BRANCH_REGEX) {
                            sh 'uv version "$VERSION+$LATEST_COMMIT_ID"'
                            dockerImage = docker.build(DOCKER_REPOSITORY + ':' + env.BRANCH_NAME)
                            dockerImage.push()
                        }
                        if (VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX) {
                            dockerImage = docker.build(DOCKER_REPOSITORY + ':' + VERSION)
                            dockerImage.push()
                            dockerImage.push('latest')
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

        stage('Build API Docs') {
            when {
                anyOf {
                    expression {
                        return env.BRANCH_NAME ==~ MAIN_BRANCH_REGEX
                    }
                    expression {
                        return VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX
                    }
                }
            }
            steps {
                script {
                    DOC_RELEASE_REGEX = /^([0-9]+(\.[0-9]+)*)$/
                    // development
                    API_DOCS_PATH = 'staging'
                    if (VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX && VERSION ==~ RELEASE_REGEX) {
						// TODO: remove after v2 release
                        // release candidate
                        API_DOCS_PATH = 'v2-rc'
                    }
                    if (VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX && VERSION ==~ DOC_RELEASE_REGEX) {
                        // release
                        API_DOCS_PATH = sh(returnStdout: true, script: 'cd docs && uv version --short | awk -F \'.\' \'{ print "v" $1 }\'').trim()
                    }
                    env.DOCS_PUBLISH_DIR = "/var/lib/jenkins/apidocs/${REPO_NAME}/${API_DOCS_PATH}/"

                    sh """
                    cd docs/
                    # install dependencies
                    uv sync --group docs
                    # compile
                    uv run make html
                    """
                    stash includes: 'docs/build/html/**', name: 'build_docs'
                }
            }
            post {
                failure {
                    rocket_basicsend("Building of API Docs failed on ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Publish API Docs') {
            when {
                anyOf {
                    expression {
                        return env.BRANCH_NAME ==~ MAIN_BRANCH_REGEX
                    }
                    expression {
                        return VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX
                    }
                }
            }
            agent {
                label 'builtin'
            }
            steps {
                script {
                    unstash 'build_docs'
                    sh """
                    # publish
                    rm -rf ${env.DOCS_PUBLISH_DIR}
                    mkdir -p ${env.DOCS_PUBLISH_DIR}
                    cp -r docs/build/html/* ${env.DOCS_PUBLISH_DIR}
                    """
                }
            }
            post {
                failure {
                    rocket_basicsend("Publishing of API Docs failed on ${env.BRANCH_NAME}")
                }
            }
        }

        stage('Trigger End-to-End Tests') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ MAIN_BRANCH_REGEX
                }
            }
            steps {
                build job: 'ohsome-api-end-to-end/main', quietPeriod: 180, wait: false
            }
            post {
                failure {
                    rocket_basicsend("Triggering of End-to-End Tests for ${REPO_NAME}-build nr. ${env.BUILD_NUMBER} *failed* on Branch - ${env.BRANCH_NAME}  (<${env.BUILD_URL}|Open Build in Jenkins>). Does the end-to-end job still exist?")
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
