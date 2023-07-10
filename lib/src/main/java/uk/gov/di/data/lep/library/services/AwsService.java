package uk.gov.di.data.lep.library.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import uk.gov.di.data.lep.library.config.Config;

public class AwsService<O> {
    public void putOnQueue(O output){
        var sqsClient = AmazonSQSClientBuilder.standard()
            .withRegion(Regions.EU_WEST_2)
            .build();

        sqsClient.sendMessage(new SendMessageRequest()
            .withQueueUrl(Config.targetQueue)
            .withMessageBody(new Gson().toJson(output)));
    }

    public void putOnTopic(O output){
        var snsClient = AmazonSNSClientBuilder.standard()
            .withRegion(Regions.EU_WEST_2)
            .build();

        snsClient.publish(new PublishRequest()
            .withTopicArn(Config.targetTopic)
            .withMessage(new Gson().toJson(output)));
    }
}
