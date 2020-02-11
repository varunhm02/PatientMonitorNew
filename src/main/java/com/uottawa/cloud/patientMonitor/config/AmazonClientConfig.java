/**
 * 
 */
package com.uottawa.cloud.patientMonitor.config;

import java.util.concurrent.Executors;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * @author Varun Hanabe Muralidhara UOttawa ID : 300055628
 */
public class AmazonClientConfig {

	//Dynamo DB async client
	public static AmazonDynamoDBAsync getDynamoDbAsyncClient() {
		ClientConfiguration config = new ClientConfiguration();
		config.setMaxConnections(400);
		return AmazonDynamoDBAsyncClientBuilder.standard().withClientConfiguration(config)
				.withExecutorFactory(() -> Executors.newFixedThreadPool(30)).build();

	}

	// Publish to SNS
	public  static void publishToSNS(String topic, String msg) {
		AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();
		PublishRequest publishRequest = new PublishRequest(topic, msg);
		PublishResult publishResult = snsClient.publish(publishRequest);
	}

}
