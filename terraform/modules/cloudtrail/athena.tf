data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

module "query_results_bucket" {
  source = "../s3"

  account_id      = var.account_id
  region          = var.region
  prefix          = var.environment
  name            = "athena-cloudtrail-query-results"
  suffix          = "gdx-data-share-poc"
  expiration_days = 30

  object_writer_owner = true
  sns_arn             = var.sns_topic_arn
}

resource "aws_athena_workgroup" "athena" {
  name = "primary"

  configuration {
    bytes_scanned_cutoff_per_query     = 524288000 # 500 MiB
    publish_cloudwatch_metrics_enabled = false
    result_configuration {
      output_location = "s3://${module.query_results_bucket.id}"
      encryption_configuration {
        encryption_option = "SSE_KMS"
        kms_key_arn       = module.query_results_bucket.kms_arn
      }
    }
  }
}

resource "aws_glue_catalog_database" "cloudtrail" {
  name = "cloudtrail"
}

resource "aws_glue_catalog_table" "cloudtrail" {
  database_name = aws_glue_catalog_database.cloudtrail.name
  name          = "cloudtrail_logs_${module.bucket.id}"
  table_type    = "EXTERNAL_TABLE"
  parameters = {
    "classification"                     = "cloudtrail"
    "projection.enabled"                 = "true"
    "projection.timestamp.format"        = "yyyy/MM/dd"
    "projection.timestamp.interval"      = "1"
    "projection.timestamp.interval.unit" = "DAYS"
    "projection.timestamp.range"         = "2023/01/01,NOW"
    "projection.timestamp.type"          = "date"
    "storage.location.template"          = "s3://${module.bucket.id}/AWSLogs/${data.aws_caller_identity.current.account_id}/CloudTrail/${data.aws_region.current.name}/$${timestamp}"
  }
  partition_keys {
    name = "timestamp"
    type = "string"
  }

  storage_descriptor {
    input_format  = "com.amazon.emr.cloudtrail.CloudTrailInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat"
    location      = "s3://${module.bucket.id}/AWSLogs/${data.aws_caller_identity.current.account_id}/CloudTrail/${data.aws_region.current.name}"

    ser_de_info {
      name                  = "org.apache.hive.hcatalog.data.JsonSerDe"
      serialization_library = "org.apache.hive.hcatalog.data.JsonSerDe"
    }

    columns {
      name = "eventversion"
      type = "string"
    }
    columns {
      name = "useridentity"
      type = "struct<type:string,principalId:string,arn:string,accountId:string,invokedBy:string,accessKeyId:string,userName:string,sessionContext:struct<attributes:struct<mfaAuthenticated:string,creationDate:string>,sessionIssuer:struct<type:string,principalId:string,arn:string,accountId:string,userName:string>,ec2RoleDelivery:string,webIdFederationData:map<string,string>>>"
    }
    columns {
      name = "eventtime"
      type = "string"
    }
    columns {
      name = "eventsource"
      type = "string"
    }
    columns {
      name = "eventname"
      type = "string"
    }
    columns {
      name = "awsregion"
      type = "string"
    }
    columns {
      name = "sourceipaddress"
      type = "string"
    }
    columns {
      name = "useragent"
      type = "string"
    }
    columns {
      name = "errorcode"
      type = "string"
    }
    columns {
      name = "errormessage"
      type = "string"
    }
    columns {
      name = "requestparameters"
      type = "string"
    }
    columns {
      name = "responseelements"
      type = "string"
    }
    columns {
      name = "additionaleventdata"
      type = "string"
    }
    columns {
      name = "requestid"
      type = "string"
    }
    columns {
      name = "eventid"
      type = "string"
    }
    columns {
      name = "resources"
      type = "array<struct<arn:string,accountId:string,type:string>>"
    }
    columns {
      name = "eventtype"
      type = "string"
    }
    columns {
      name = "apiversion"
      type = "string"
    }
    columns {
      name = "readonly"
      type = "string"
    }
    columns {
      name = "recipientaccountid"
      type = "string"
    }
    columns {
      name = "serviceeventdetails"
      type = "string"
    }
    columns {
      name = "sharedeventid"
      type = "string"
    }
    columns {
      name = "vpcendpointid"
      type = "string"
    }
    columns {
      name = "tlsdetails"
      type = "struct<tlsversion:string,ciphersuite:string,clientprovidedhostheader:string>"
    }
  }
}
