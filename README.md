# Sagemaker Multi-Model Server for Clojure

[Sagemaker](https://aws.amazon.com/sagemaker/) is AWS's machine learning product.
It is mostly targeted at Python users, but it's possible to deploy and train models using any language via docker containers.

This package is an implementation of the [API contract for handling multiple models](https://docs.aws.amazon.com/sagemaker/latest/dg/mms-container-apis.html) in the same Sagemaker Endpoint.
It uses pedestal and an XGBoost model trained in R, loaded using [clj-boost](https://gitlab.com/alanmarazzi/clj-boost).

Everything is a WORK IN PROGRESS and subject to change! THIS IS A PROOF-OF-CONCEPT.

## How to use it?
### Locally
To use the server locally build the docker container and launch it:

```shell
docker build -t sagemaker-multimodel-server . && \
docker run -p 8080:8080 sagemaker-multimodel-server serve
```

tell the container to load a model (modify the path first!):

```shell
curl --location --request POST 'http://localhost:8080/models' \
--header 'Content-Type: application/json' \
--data-raw '{
    "model_name": "no_cyl",
    "url": "<local path to the repo>/example_models/no_cyl/model"
}'
```

you should receive a `200`, which means the container is ready for scoring:

```shell
curl --location --request POST 'http://localhost:8080/models/no_cyl/invoke' \
--header 'Content-Type: application/json' \
--data-raw '{
    "input": {
      "mpg": 21,
      "disp": 160,
      "hp": 110,
      "drat": 3.90,
      "wt": 2.620,
      "qsec": 16.46,
      "vs": 0,
      "gear": 4,
      "carb": 4
   }
}'
```

### On AWS

I'll assume you have an AWS account and know how Sagemaker works in general.
If you don't, please take a look at their [docs](https://docs.aws.amazon.com/sagemaker/latest/dg/how-it-works-hosting.html).

You'll need an [Elastic Container Registry](https://aws.amazon.com/ecr/) (ECR) repository to keep the docker image used in Sagemaker.
After creating the repository build the image locally, then tag and push it to the ECR repository

```shell
> docker tag sagemaker-multimodel-server <aws_account_id>.dkr.ecr.<aws_region>.amazonaws.com/<your_repo>
> docker push <aws_account_id>.dkr.ecr.<aws_region>.amazonaws.com/<your_repo>
```

You'll also need an [S3](https://aws.amazon.com/s3/) bucket where you have to upload a `.tar.gz` archive containing the `model.xgb` file inside the `example_models` folder.

With these elements in place, you can now create a Sagemaker Model pointing to
the ECR image and the S3 bucket, a Sagemaker Endpoint Configuration and finally
a Sagemaker Endpoint.

To finally use the Endpoint you still need to create a [Lambda](https://aws.amazon.com/lambda/) to call the endpoint (no way to avoid Python here AFAIK):

```python
import os
import boto3
import json

runtime = boto3.client('runtime.sagemaker')

def lambda_handler(event, context):

    response = runtime.invoke_endpoint(EndpointName = '<your_endpoint_name>',
                                       ContentType = 'application/json',
                                       Body = json.dumps(event),
                                       TargetModel  = 'no_cyl.tar.gz')
                                       
    result = json.loads(response['Body'].read().decode())

    return result
```

to test the whole setup add a test to the lambda using the same payload also used for local testing.

## See also
* [jcpsantiago/sagemaker-multimodel-R](https://github.com/jcpsantiago/sagemaker-multimodel-R) -- Sagemaker multimodel server for R using Plumber
