pipeline {

    agent {
       node {
         label "${AGENT}"
       }
    }

    environment {
      agent_label = "$AGENT"
      branch = "$BRANCH_NAME"
    }

    stages {
        stage('check dependency') {
            steps {
              script {
                echo 'Check Dependency...'
                sh """
                command -v mvn
                command -v sshpass
                """
              }

              script {
                if ("$VERSION" != "") {
                  env.version = "$VERSION"
                } else {
                  env.version = getDate()
                }
              }
              echo "$version"
            }
        }
        stage('build') {
            steps {
              echo 'Building...'
              sh 'mvn package'
            }
        }
        stage('transmitting package') {
           steps {
               script {
                 def refId = getRefId()
                 def commitDate = getCommitDate()
                 sh """
                    echo "\"xcalscanjenkinstogerritplugin\":{
                    \"GitRefId\":\"${refId}\",
                    \"Branch\":\"${branch}\",
                    \"Date\":\"${commitDate}\"
                     }" >> "VER.txt"
                    """
                 sh """
                    mkdir -p xcalscanjenkinstogerritplugin/$branch/$version
                    mv target/*.hpi VER.txt xcalscanjenkinstogerritplugin/$branch/$version
                 """
               }

               script {
                 if ("$agent_label" == "4.154-JenSlave") {
                    sh """
                    cp -r xcalscanjenkinstogerritplugin /xcal-artifacts/inhouse
                    """
                 } else {
                    withCredentials([usernamePassword(credentialsId: 'xxx', passwordVariable: 'password', usernameVariable:'user')]) {
                      sh """
                      sshpass -p $password scp -r xcalscanjenkinstogerritplugin $user@127.0.0.1:/xcal-artifacts/inhouse
                      """
                 }
               }
              }
            }
        }
    }
}

def getRefId() {
    def branch = env.branch
    return sh(returnStdout: true, script: 'git show-ref | grep ${branch} | head -n 1 | awk \'{print $1}\'').trim()
}

def getCommitDate() {
   def ref = getRefId()
   return sh(returnStdout: true, script: 'git log --pretty=format:"%cd" ${ref} -1')
}

def getDate() {
    return sh(returnStdout: true, script: 'date +%Y-%m-%d').trim()
}
