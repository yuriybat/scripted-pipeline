node {
    withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master', keyFileVariable: 'SSH_KEY', passphraseVariable: '', usernameVariable: 'SSH_USERNAME')]) {
        stage('Install Preprequisites'){
            sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible -i \"68.183.21.58,\" all --private-key $SSH_KEY -u $SSH_USERNAME -b -m ping
            """
        }
    }
}