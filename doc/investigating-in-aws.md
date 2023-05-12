# Investigating in AWS
We have CloudTrail which records lots of the events that happen in AWS. These events are saved to an S3 bucket, which means
we can use Athena to search through them. The easiest way to do this is through the AWS console. Select the `cloudtrail`
database in the query editor, and search for things.

The data it partitioned on `timestamp` to make things faster and cheaper, so if possible always include timestamp in the
`WHERE` clause to take advantage of this.

Under the hood, this is using AWS Glue, which is configured in CloudTrail terraform module
