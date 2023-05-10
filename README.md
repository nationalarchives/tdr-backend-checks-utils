#  TDR Backend Checks Utils

The TDR backend checks are now running in a step function. 

There is a size limit of 256Kb for messages passed between steps and for large consignments, the message size is more than this.

To solve this, each step reads its input from S3 and once it has carried out its step, writes the new output to S3 again.

AWS recommends this approach for [passing large messages](https://docs.aws.amazon.com/step-functions/latest/dg/avoid-exec-failures.html).

The input JSON is the same for each lambda in the backend checks so this project contains all the shared case classes.

There are also two helper methods to read the input from S3 and write the output back to S3
