pipeline {
    agent {
        label 'worker'
    }
    options {
        timeout(time: 30, unit: 'MINUTES')
    }
    tools {
        maven 'Maven 3'
    }

    environment {
        // START CUSTOM ohsome API
        MAVEN_TEST_OPTIONS = '-Dport_get=8081 -Dport_post=8082 -Dport_data=8083 -DdbFilePathProperty=--database.db=/data/heidelberg-v1.0-beta.oshdb'
        // END CUSTOM ohsome API
        // this regex determines which branch is deployed as a snapshot
        SNAPSHOT_BRANCH_REGEX = /(^master$)/
        RELEASE_REGEX = /^([0-9]+(\.[0-9]+)*)(-(RC|beta-|alpha-)[0-9]+)?$/
        RELEASE_DEPLOY = false
        SNAPSHOT_DEPLOY = false
    }

    stages {
        stage('Build and Test') {
            steps {
                // setting up a few basic env variables like REPO_NAME and LATEST_AUTHOR
                setup_basic_env()

                mavenbuild('clean compile javadoc:jar source:jar verify -P jacoco,sign,git')
            }
            post {
                failure {
                    rocket_buildfail()
                    rocket_testfail()
                }
            }
        }

        stage('Reports and Statistics') {
            steps {
                reports_sonar_jacoco()
            }
        }

        stage('Deploy Snapshot') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ SNAPSHOT_BRANCH_REGEX && VERSION ==~ /.*-SNAPSHOT$/
                }
            }
            steps {
                deploy_snapshot('clean compile javadoc:jar source:jar deploy -P sign,git')
                // START CUSTOM ohsome API
                script {
                    SNAPSHOT_DEPLOY = true
                }
                // END CUSTOM ohsome API
            }
            post {
                failure {
                    rocket_snapshotdeployfail()
                }
            }
        }

        stage('Deploy Release') {
            when {
                expression {
                    return VERSION ==~ RELEASE_REGEX && env.TAG_NAME ==~ RELEASE_REGEX
                }
            }
            steps {
                deploy_release('clean compile javadoc:jar source:jar deploy -P sign,git')

                deploy_release_central('clean compile javadoc:jar source:jar deploy -P sign,git,deploy-central')
                // START CUSTOM ohsome API
                script {
                    RELEASE_DEPLOY = true
                }
                // END CUSTOM ohsome API
            }
            post {
                failure {
                    rocket_releasedeployfail()
                }
            }
        }

            // START CUSTOM ohsome API
            stage('Compile API Docs') {
                when {
                    anyOf {
                        equals expected: true, actual: RELEASE_DEPLOY
                        equals expected: true, actual: SNAPSHOT_DEPLOY
                    }
                }
                steps {
                    script {
                        DOC_RELEASE_REGEX = /^([0-9]+(\.[0-9]+)*)$/
                        DOCS_DEPLOYMENT = 'development'
                        API_DOCS_PATH = 'development'
                        if (VERSION ==~ DOC_RELEASE_REGEX) {
                        DOCS_DEPLOYMENT = 'release'
                        API_DOCS_PATH = sh(returnStdout: true, script: 'cd docs && python3 get_pom_metadata.py | awk \'/^Path:/{ print $2 }\'').trim()
                        }

                        if (!fileExists('venv')) {
                        sh 'python3 -m venv venv'
                        }

                        sh '''
                        . venv/bin/activate
                        venv/bin/pip install --upgrade pip
                        venv/bin/pip install -r docs/requirements.txt
                        cd docs
                        ../venv/bin/sphinx-build -b html . _build
                        '''
                        sh 'rm -rf venv'
                    }
                }
                post {
                    failure {
                        rocket_basicsend('Compile of API Docs failed on ${env.BRANCH_NAME}')
                    }
                }
            }

            stage('Publish API Docs') {
                when {
                    anyOf {
                        equals expected: true, actual: RELEASE_DEPLOY
                        equals expected: true, actual: SNAPSHOT_DEPLOY
                    }
                }
                agent {
                    label 'builtin'
                }
                steps {
                    script {
                        working_dir = sh(returnStdout: true, script: 'basename $(pwd)').trim()
                        publish_dir = "/var/lib/jenkins/apidocs/${REPO_NAME}/${API_DOCS_PATH}/"
                        sh """
                        rm -rf ${publish_dir}
                        mkdir -p ${publish_dir}
                        cp -r /var/lib/jenkins/workspace/${working_dir}/docs/_build/* ${publish_dir}
                        """
                    }
                }
                post {
                    failure {
                        rocket_basicsend('Publishing of API Docs failed on ${env.BRANCH_NAME}')
                    }
                }
            }
    // END CUSTOM ohsome API

        stage('Check Dependencies') {
            when {
                expression {
                    if (currentBuild.number > 1) {
                        return (((currentBuild.getStartTimeInMillis() - currentBuild.previousBuild.getStartTimeInMillis()) > 2592000000) && (env.BRANCH_NAME ==~ SNAPSHOT_BRANCH_REGEX)) //2592000000 30 days in milliseconds
                    }
                    return false
                }
            }
            steps {
                check_dependencies()
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
