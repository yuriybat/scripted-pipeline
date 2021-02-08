properties([
    parameters([
        string(defaultValue: '', description: 'Enter IP Address', name: 'IP', trim: true)
    ])
])
node {
    stage("Pull repo"){
        git 'https://github.com/ikambarov/ansible-Flaskex.git'
    }
    stage('Install Prerequisites'){
        withEnv(['FLASKEX_REPO=https://github.com/ikambarov/Flaskex.git', 'FLASKEX_BRANCH=master']) {
            ansiblePlaybook become: true, colorized: true, credentialsId: 'jenkins-master', disableHostKeyChecking: true, inventory: "${ params.IP },", playbook: 'prerequisites.yml'
        } 
    }    
    stage('Pull FlaskEx'){
        withEnv(['FLASKEX_REPO=https://github.com/ikambarov/Flaskex.git', 'FLASKEX_BRANCH=master']) {
            ansiblePlaybook become: true, colorized: true, credentialsId: 'jenkins-master', disableHostKeyChecking: true, inventory: "${ params.IP },", playbook: 'pull_repo.yml'
        } 
    }
    stage('Install Python'){
        withEnv(['FLASKEX_REPO=https://github.com/ikambarov/Flaskex.git', 'FLASKEX_BRANCH=master']) {
            ansiblePlaybook become: true, colorized: true, credentialsId: 'jenkins-master', disableHostKeyChecking: true, inventory: "${ params.IP },", playbook: 'install_python.yml'
        } 
    }  
    stage('Start App'){
        withEnv(['FLASKEX_REPO=https://github.com/ikambarov/Flaskex.git', 'FLASKEX_BRANCH=master']) {
            ansiblePlaybook become: true, colorized: true, credentialsId: 'jenkins-master', disableHostKeyChecking: true, inventory: "${ params.IP },", playbook: 'start_app.yml'
        } 
    }
}