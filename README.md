# quarked-ddns
Provides Dynamic DNS solution by integrating  [Google WiFi Router API](https://github.com/olssonm/google-wifi-api) 
(or public services if Google WiFi Router is not available) and [Amazon Route53](https://aws.amazon.com/route53/). 

If Google WiFi is disabled quarked-ddns uses public services from the list below to get your public IP. Following list 
of public services is pre-configured and used out of the box:
```
http://checkip.amazonaws.com
https://api.ipify.org
https://api.myip.com
https://icanhazip.com
http://checkip.dyndns.org
http://trackip.net
http://ip-api.com
```
To avoid overloading some specific server the quarked-ddns service will randomly pick and use one endpoint from the list.
If public service endpoint returns IP which is different from the current one - the result is validated by using another public endpoint.
Additional/different services can be configured, [read here](http://todo). It is also very cheap to deploy your own 
'what is my IP' service by using something like [Amazon Lambda](https://aws.amazon.com/lambda/) or any other 'Server-less' solution.
Consider doing this if you have the knowledge.

There is also hardcoded limitation for how frequently quarked-ddns can hit the public service from the list, currently it is 3 minutes.
So, if you need really fast sync between your dynamic IP and Route53 DNS record - consider investing in Google WiFi.
If you own or aware of any other mainstream router that provides the REST API or some reliable public IP service - let us know. 

## Prerequisites
1. Domain managed by the [Amazon Route53](https://aws.amazon.com/route53/)
2. AWS_ACCESS_KEY_ID and AWS_SECRET_KEY for IAM that can manage your domain (read below how to get it)

## Target Platforms
quarked-ddns service supports following deployments:
- Native linux binary
- Java process
- Docker container
- Helm Chart
- Rancher Application

## Configuration
1. Use [Amazon IAM](https://console.aws.amazon.com/iam) to add/update user for programmatic access with permissions to 
   list and manage your hosted zone. It can look like this:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "route53:GetHostedZone",
        "route53:GetHostedZoneCount",
        "route53:ListHostedZonesByName",
        "route53:ListResourceRecordSets",
        "route53:ChangeResourceRecordSets"
      ],
      "Resource": [
        "arn:aws:route53:::hostedzone/<HOSTEDZONEID>"
      ]
    }
  ]
}
```
2. Get your domain ZoneId from AWS Route53 service
3. If you use Google WiFi Router - verify you got the response by hitting http://192.168.86.1/api/v1/status

# How to run
## Linux Binary
Grab the quarked-ddns-linux binary from the recent release, and create .env file in the working directory with the following content:
```
route53.domain=<your-domain>
route53.region=<your-iam-user-region>
route53.hostedZoneId=<your-hosted-zone-id>
AWS_ACCESS_KEY_ID=<your-key-id>
AWS_SECRET_KEY=<your-secret-key>
```
You can also set your AWS key and secret as env variables for the process: 
```
$export AWS_ACCESS_KEY_ID=<your-key-id>
$export AWS_SECRET_KEY=<your-secret-key>
$./gwifi-ddns-linux
```
Or provide them as cmd line parameters:
```
$./gwifi-ddns-linux -Daws.accessKeyId=<your-key-id> -Daws.secretKey=your-secret-key
```
With no extra cmd line parameters the service will use the defaults specified below:
```
logLevel=INFO
qddns.ipCheckInterval=5m
qddns.routerIP=192.168.86.1
```
If you want for example to disable Google WiFi Router (if you don't have one) and want 10m check interval:
```
$./gwifi-ddns-linux -Dqddns.routerIP=none -Dqddns.ipCheckInterval=10m
```

## Java Process
Java 11+ is required, verify your environment (and update if needed):
```
$java -version
```
Running and configuring Java process is not different from running native Linux executable. Grab the quarkus-app.tar.gz
binary from the recent release, unpack it and create .env file in your working directory (see above):
```
$java -jar quarkus-app/quarkus-run.jar
```
To provide config properties the same env variables can be used or cmd line parameters:
```
$java -DlogLevel=DEBUG -jar quarkus-app/quarkus-run.jar
```

## Docker Container
```
$docker pull zeppelinux/quarked-ddns
```
Then you have to set up all the required env variables, [read here](https://docs.docker.com/engine/reference/commandline/run/) 
about how to do it.

## Helm Chart
Helm/Kubernetes deployments require quarked-ddns secret with AWS_ACCESS_KEY_ID and AWS_SECRET_KEY keys/values available 
in the same namespace where app is installed.
```
helm repo add zeppelinux https://charts.diligesoft.com
helm show values zeppelinux/quarked-ddns
```
Following values have to be customized, normally in your own values.yaml file:
```yaml
route53:
  hostedZoneId: <YOUR-HOSTED-ZONE-ID>
  domain: <your-domain>
  region: <IAM USER REGION>
```
```helm install -f values.yaml quarked-ddns zeppelinux/quarked-ddns``` - installs the service in the default namespace


## Rancher
Add [Helm Charts Catalogue](https://charts.diligesoft.com) to the Rancher catalogs and enjoy Rancher Apps UI to run it



# How To compile and run locally

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

