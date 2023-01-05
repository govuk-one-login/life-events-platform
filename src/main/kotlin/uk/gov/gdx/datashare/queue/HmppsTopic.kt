package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sns.AmazonSNS

class HmppsTopic(
  val id: String,
  val arn: String,
  val snsClient: AmazonSNS,
)
