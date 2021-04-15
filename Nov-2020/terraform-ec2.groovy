properties([
    parameters([
        booleanParam(defaultValue: false, description: 'Do you want to run Terraform apply?', name: 'terraform_apply'),
        booleanParam(defaultValue: false, description: 'Do you want to run Terraform destroy?', name: 'terraform_destroy'),
        choice(choices: ['dev', 'qa', 'prod'], description: 'Choose environment: ', name: 'environment'),
        string(defaultValue: '', description: 'Provide AMI NAME', name: 'ami_name', trim: true)
        ])
    ])
node{
    def aws_region_var = ''

    if(params.environment == 'dev'){
        println("Applying for dev")
        aws_region_var = 'us-east-1'
    }
    else if(params.environment == 'qa'){
        println("Applying for qa")
        aws_region_var = 'us-east-2'
    }
    else{
        println("Applying for prod")
        aws_region_var = 'us-west-2'
    }

    def tfvar = """
    s3_bucket = "jenkins-yuriy-bucket"
    s3_folder_project = "terraform_ec2"
    s3_folder_region = "us-east-1"
    s3_folder_type = "class"
    s3_tfstate_file = "infrastructure.tfstate"
    
    environment = "${params.environment}"
    region      = "${aws_region_var}"
    public_key  = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDOfpyGzS3kvg3szLc1+W3QkuDpJFb1K3E8xnKJcsN7rG7i1ZikgGba1LVE/1fyU+xZKm+mB/dfRjFFigaPE2Y8fUBGlIakw8pV8ANuzO3bnyk62zSYNCJM8aHDw+h3k0zrIH3aLpK5TJKz3vU2lzNn8WLPSVOmiKA0ESA7z25WhwGnCTW2SyKlIGs+U0OfSPjCWAR7So0V+HRUJMNH+dXGK6wFYetNkBUvz9yfMK+/5LPrhQI7jyhPGdOGKGcJgkHHwSzTgoW7qntEZ2i0tr0YeO7ZXoKIujPWOHWOaWNb2jCkwJeYfXMnWUrXW8THA7GKq+uhQ/D2h+DYyQlGdg1f centos@Jenkins"
    ami_name      = "${params.ami_name}"
    """

    stage("Pull Repo"){
        cleanWs()
        git url: 'https://github.com/ikambarov/terraform-ec2-by-ami-name.git'
        writeFile file: "${params.environment}.tfvars", text: "${tfvar}"
    }

    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terrraform Init"){
                sh """
                    bash setenv.sh ${params.environment}.tfvars
                    terraform init
                    terraform plan -var-file dev.tfvars
                """
            }        
            
            if(params.terraform_apply){
                stage("Terraform Apply"){
                    sh """
                        terraform apply -var-file ${params.environment}.tfvars -auto-approve
                    """
                }
            }
            else if(params.terraform_destroy){
                stage("Terraform Destroy"){
                    sh """
                        terraform destroy -var-file ${params.environment}.tfvars -auto-approve
                    """
                }
            }
            else {
                stage("Terraform Plan"){
                    sh """
                        terraform plan -var-file ${environment}.tfvars
                    """
                }
            }
        }        
    }    
}
