import time
import json
import boto3
import urllib


def lambda_handler(event, context):
    # Create a low-level service client for EC2
    client = boto3.client("ec2")
    # Create a low-level service client for SSM
    ssm = boto3.client("ssm")

    # Get S3 bucket name and the filename of the file that has been added into the S3 bucket
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'], encoding = 'utf-8')


    # getting instance information
    # describeInstance = client.describe_instances()

    # Get configuration of the EC2 instance where the shell script has to be triggered
    with open("application.json", "r") as f:
        data = json.load(f)
    instanceid = data.get('aws-instance-id') # Instance Id
    shellscript = data.get('shellscript') # Shell script
    # instanceid = "i-07927351fee3bfe4b"


    # send_command runs a command on one or more managed nodes e.g. EC2 instance
    response = ssm.send_command(
        InstanceIds=[instanceid],
        DocumentName="AWS-RunShellScript",
        Parameters={
            "commands": ["sh /home/ec2-user/"+ shellscript +" "+ key] # Command to be executed
        },  # replace command_to_be_executed with command
    )

    # Fetching command id for the output
    command_id = response["Command"]["CommandId"]

    # Sleep timer - Sometimes there is a delay in response and we do not want to run the output command
    time.sleep(3)

    # Fetching command output
    output = ssm.get_command_invocation(CommandId=command_id, InstanceId=instanceid)

    # Logging output for the CloudWatch Event logger
    print(output)

    # Logging output for the CloudWatch Event logger
    return {"statusCode": 200, "body": json.dumps(str(output))}