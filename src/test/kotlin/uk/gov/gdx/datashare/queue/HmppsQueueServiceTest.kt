package uk.gov.gdx.datashare.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import com.amazonaws.services.sqs.model.PurgeQueueRequest as AwsPurgeQueueRequest

class HmppsQueueServiceTest {

  private val hmppsTopicFactory = mock<HmppsTopicFactory>()
  private val hmppsQueueFactory = mock<HmppsQueueFactory>()
  private val hmppsSqsProperties = mock<HmppsSqsProperties>()
  private lateinit var hmppsQueueService: HmppsQueueService

  @Nested
  inner class HmppsQueues {

    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()

    @BeforeEach
    fun `add test data`() {
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(hmppsQueueFactory.createHmppsQueues(any(), any()))
        .thenReturn(
          listOf(
            HmppsQueue("some queue id", sqsClient, "some queue name", sqsDlqClient, "some dlq name"),
            HmppsQueue("another queue id", mock(), "another queue name", mock(), "another dlq name"),
          )
        )

      hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
    }

    @Test
    fun `finds an hmpps queue by queue id`() {
      assertThat(hmppsQueueService.findByQueueId("some queue id")?.queueUrl).isEqualTo("some queue url")
    }

    @Test
    fun `finds an hmpps queue by queue name`() {
      assertThat(hmppsQueueService.findByQueueName("some queue name")?.queueUrl).isEqualTo("some queue url")
    }

    @Test
    fun `finds an hmpps queue by dlq name`() {
      assertThat(hmppsQueueService.findByDlqName("some dlq name")?.dlqUrl).isEqualTo("some dlq url")
    }

    @Test
    fun `returns null if queue id not found`() {
      assertThat(hmppsQueueService.findByQueueId("unknown")).isNull()
    }

    @Test
    fun `returns null if queue not found`() {
      assertThat(hmppsQueueService.findByQueueName("unknown")).isNull()
    }

    @Test
    fun `returns null if dlq not found`() {
      assertThat(hmppsQueueService.findByDlqName("unknown")).isNull()
    }
  }

  @Nested
  inner class RetryDlqMessages {

    private val dlqSqs = mock<AmazonSQS>()
    private val queueSqs = mock<AmazonSQS>()

    @BeforeEach
    fun `stub getting of queue url`() {
      whenever(queueSqs.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("queueUrl"))
      whenever(dlqSqs.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("dlqUrl"))

      hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
    }

    @Nested
    inner class NoMessages {
      @BeforeEach
      fun `finds zero messages on dlq`() {
        whenever(dlqSqs.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages")))).thenReturn(
          GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "0"))
        )
      }

      @Test
      fun `should not attempt any transfer`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs).getQueueAttributes("dlqUrl", listOf("ApproximateNumberOfMessages"))
        verify(dlqSqs, times(0)).receiveMessage(any<ReceiveMessageRequest>())
      }

      @Test
      fun `should return empty result`() {
        val result =
          hmppsQueueService.retryDlqMessages(
            RetryDlqRequest(
              HmppsQueue(
                "some queue id",
                queueSqs,
                "some queue name",
                dlqSqs,
                "some dlq name"
              )
            )
          )

        assertThat(result.messagesFoundCount).isEqualTo(0)
        assertThat(result.messages).isEmpty()
      }
    }

    @Nested
    inner class SingleMessage {
      @BeforeEach
      fun `finds a single message on the dlq`() {
        whenever(dlqSqs.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages")))).thenReturn(
          GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "1"))
        )
        whenever(dlqSqs.receiveMessage(any<ReceiveMessageRequest>()))
          .thenReturn(
            ReceiveMessageResult().withMessages(
              Message()
                .withBody("message-body")
                .withReceiptHandle("message-receipt-handle")
                .withMessageAttributes(mutableMapOf("some" to stringAttributeOf("attribute")))
            )
          )

        hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
      }

      @Test
      fun `should receive message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs).receiveMessage(
          check<ReceiveMessageRequest> {
            assertThat(it.queueUrl).isEqualTo("dlqUrl")
            assertThat(it.maxNumberOfMessages).isEqualTo(1)
          }
        )
      }

      @Test
      fun `should delete message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs).deleteMessage(
          check {
            assertThat(it.queueUrl).isEqualTo("dlqUrl")
            assertThat(it.receiptHandle).isEqualTo("message-receipt-handle")
          }
        )
      }

      @Test
      fun `should send message to the main queue`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )
        verify(queueSqs).sendMessage(
          SendMessageRequest().withQueueUrl("queueUrl").withMessageBody("message-body")
            .withMessageAttributes(mutableMapOf("some" to stringAttributeOf("attribute")))
        )
      }

      @Test
      fun `should return the message`() {
        val result = hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        assertThat(result.messagesFoundCount).isEqualTo(1)
        assertThat(result.messages)
          .extracting(Message::getBody, Message::getReceiptHandle)
          .containsExactly(tuple("message-body", "message-receipt-handle"))
      }
    }

    @Nested
    inner class MultipleMessages {
      @BeforeEach
      fun `finds two message on the dlq`() {
        whenever(dlqSqs.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages")))).thenReturn(
          GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "2"))
        )
        whenever(dlqSqs.receiveMessage(any<ReceiveMessageRequest>()))
          .thenReturn(
            ReceiveMessageResult().withMessages(
              Message()
                .withBody("message-1-body")
                .withReceiptHandle("message-1-receipt-handle")
                .withMessageAttributes((mutableMapOf("attribute-key-1" to stringAttributeOf("attribute-value-1"))))
            )
          )
          .thenReturn(
            ReceiveMessageResult().withMessages(
              Message()
                .withBody("message-2-body")
                .withReceiptHandle("message-2-receipt-handle")
                .withMessageAttributes((mutableMapOf("attribute-key-2" to stringAttributeOf("attribute-value-2"))))
            )
          )

        hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
      }

      @Test
      fun `should receive message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs, times(2)).receiveMessage(
          check<ReceiveMessageRequest> {
            assertThat(it.queueUrl).isEqualTo("dlqUrl")
            assertThat(it.maxNumberOfMessages).isEqualTo(1)
          }
        )
      }

      @Test
      fun `should delete message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        val captor = argumentCaptor<DeleteMessageRequest>()
        verify(dlqSqs, times(2)).deleteMessage(captor.capture())

        assertThat(captor.firstValue.receiptHandle).isEqualTo("message-1-receipt-handle")
        assertThat(captor.secondValue.receiptHandle).isEqualTo("message-2-receipt-handle")
      }

      @Test
      fun `should send message to the main queue`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(queueSqs).sendMessage(
          SendMessageRequest().withQueueUrl("queueUrl").withMessageBody("message-1-body")
            .withMessageAttributes((mutableMapOf("attribute-key-1" to stringAttributeOf("attribute-value-1"))))
        )
        verify(queueSqs).sendMessage(
          SendMessageRequest().withQueueUrl("queueUrl").withMessageBody("message-2-body")
            .withMessageAttributes((mutableMapOf("attribute-key-2" to stringAttributeOf("attribute-value-2"))))
        )
      }

      @Test
      fun `should return the message`() {
        val result = hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        assertThat(result.messagesFoundCount).isEqualTo(2)
        assertThat(result.messages)
          .extracting(Message::getBody, Message::getReceiptHandle)
          .containsExactly(
            tuple("message-1-body", "message-1-receipt-handle"),
            tuple("message-2-body", "message-2-receipt-handle")
          )
      }
    }

    @Nested
    inner class MultipleMessagesSomeNotFound {
      @BeforeEach
      fun `finds only one of two message on the dlq`() {
        whenever(dlqSqs.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages")))).thenReturn(
          GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "2"))
        )
        whenever(dlqSqs.receiveMessage(any<ReceiveMessageRequest>()))
          .thenReturn(
            ReceiveMessageResult().withMessages(
              Message().withBody("message-1-body").withReceiptHandle("message-1-receipt-handle")
                .withMessageAttributes(mutableMapOf("some" to stringAttributeOf("attribute")))
            )
          )
          .thenReturn(ReceiveMessageResult())

        hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
      }

      @Test
      fun `should receive message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs, times(2)).receiveMessage(
          check<ReceiveMessageRequest> {
            assertThat(it.queueUrl).isEqualTo("dlqUrl")
            assertThat(it.maxNumberOfMessages).isEqualTo(1)
          }
        )
      }

      @Test
      fun `should delete message from the dlq`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(dlqSqs).deleteMessage(
          check {
            assertThat(it.queueUrl).isEqualTo("dlqUrl")
            assertThat(it.receiptHandle).isEqualTo("message-1-receipt-handle")
          }
        )
      }

      @Test
      fun `should send message to the main queue`() {
        hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        verify(queueSqs).sendMessage(
          SendMessageRequest().withQueueUrl("queueUrl").withMessageBody("message-1-body")
            .withMessageAttributes(mutableMapOf("some" to stringAttributeOf("attribute")))
        )
      }

      @Test
      fun `should return the message`() {
        val result = hmppsQueueService.retryDlqMessages(
          RetryDlqRequest(
            HmppsQueue(
              "some queue id",
              queueSqs,
              "some queue name",
              dlqSqs,
              "some dlq name"
            )
          )
        )

        assertThat(result.messagesFoundCount).isEqualTo(2)
        assertThat(result.messages)
          .extracting(Message::getBody, Message::getReceiptHandle)
          .containsExactly(tuple("message-1-body", "message-1-receipt-handle"))
      }
    }
  }

  @Nested
  inner class GetDlqMessages {
    private val dlqSqs = mock<AmazonSQS>()
    private val queueSqs = mock<AmazonSQS>()

    @BeforeEach
    fun `stub getting of queue url`() {
      whenever(queueSqs.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("queueUrl"))
      whenever(dlqSqs.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("dlqUrl"))

      hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
    }

    @BeforeEach
    fun `gets a message on the dlq`() {
      whenever(dlqSqs.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages")))).thenReturn(
        GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "1"))
      )
      whenever(dlqSqs.receiveMessage(any<ReceiveMessageRequest>()))
        .thenReturn(
          ReceiveMessageResult().withMessages(
            Message().withBody(
              """{
                                            "Message":{
                                                "id":"event-id",
                                                "contents":"event-contents",
                                                "longProperty":12345678
                                            },
                                            "MessageId":"message-id-1"
                                          }"""
            )
              .withReceiptHandle("message-1-receipt-handle").withMessageId("external-message-id-1")
          )
        )

      hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
    }

    @Test
    fun `should get messages from the dlq`() {
      val dlqResult = hmppsQueueService.getDlqMessages(
        GetDlqRequest(
          HmppsQueue(
            "some queue id",
            queueSqs,
            "some queue name",
            dlqSqs,
            "some dlq name"
          ), 10
        )
      )
      assertThat(dlqResult.messagesFoundCount).isEqualTo(1)
      assertThat(dlqResult.messagesReturnedCount).isEqualTo(1)
      assertThat(dlqResult.messages).hasSize(1)
      assertThat(dlqResult.messages[0].messageId).isEqualTo("external-message-id-1")
      val messageMap = dlqResult.messages[0].body["Message"] as Map<*, *>
      assertThat(messageMap["longProperty"]).isEqualTo(12345678L)
      verify(dlqSqs).receiveMessage(
        check<ReceiveMessageRequest> {
          assertThat(it.queueUrl).isEqualTo("dlqUrl")
        }
      )
    }
  }

  @Nested
  inner class FindQueueToPurge {

    private val sqsClient = mock<AmazonSQS>()
    private val sqsDlqClient = mock<AmazonSQS>()

    @BeforeEach
    fun `add test data`() {
      whenever(sqsClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsDlqClient.getQueueUrl(anyString())).thenReturn(GetQueueUrlResult().withQueueUrl("some dlq url"))
      whenever(hmppsQueueFactory.createHmppsQueues(any(), any()))
        .thenReturn(
          listOf(
            HmppsQueue("some queue id", sqsClient, "some queue name", sqsDlqClient, "some dlq name"),
            HmppsQueue("another queue id", mock(), "another queue name", mock(), "another dlq name"),
          )
        )

      hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)
    }

    @Test
    fun `should find the main queue`() {
      val request = hmppsQueueService.findQueueToPurge("some queue name")

      assertThat(request?.queueName).isEqualTo("some queue name")
    }

    @Test
    fun `should find the dlq`() {
      val request = hmppsQueueService.findQueueToPurge("some dlq name")

      assertThat(request?.queueName).isEqualTo("some dlq name")
    }

    @Test
    fun `should return null if not queue or dlq`() {
      val request = hmppsQueueService.findQueueToPurge("unknown queue name")

      assertThat(request).isNull()
    }
  }

  @Nested
  inner class PurgeQueue {

    private val sqsClient = mock<AmazonSQS>()
    private val hmppsQueueService = HmppsQueueService(hmppsTopicFactory, hmppsQueueFactory, hmppsSqsProperties)

    @Test
    fun `no messages found, should not attempt to purge queue`() {
      stubMessagesOnQueue(0)

      hmppsQueueService.purgeQueue(PurgeQueueRequest("some queue", sqsClient, "some queue url"))

      verify(sqsClient, times(0)).purgeQueue(any())
    }

    @Test
    fun `messages found, should attempt to purge queue`() {
      stubMessagesOnQueue(1)

      hmppsQueueService.purgeQueue(PurgeQueueRequest("some queue", sqsClient, "some queue url"))

      verify(sqsClient).purgeQueue(AwsPurgeQueueRequest("some queue url"))
    }

    @Test
    fun `should return number of messages found to purge`() {
      stubMessagesOnQueue(5)

      val result = hmppsQueueService.purgeQueue(PurgeQueueRequest("some queue", sqsClient, "some queue url"))

      assertThat(result.messagesFoundCount).isEqualTo(5)
    }

    private fun stubMessagesOnQueue(messageCount: Int) {
      whenever(sqsClient.getQueueUrl(anyString()))
        .thenReturn(GetQueueUrlResult().withQueueUrl("some queue url"))
      whenever(sqsClient.getQueueAttributes(anyString(), eq(listOf("ApproximateNumberOfMessages"))))
        .thenReturn(GetQueueAttributesResult().withAttributes(mapOf("ApproximateNumberOfMessages" to "$messageCount")))
    }
  }
}

private fun stringAttributeOf(value: String?): MessageAttributeValue? {
  return MessageAttributeValue()
    .withDataType("String")
    .withStringValue(value)
}
