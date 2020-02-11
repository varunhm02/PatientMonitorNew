/**
 * 
 */
package com.uottawa.cloud.patientMonitor.Listener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.uottawa.cloud.patientMonitor.config.AmazonClientConfig;
import com.uottawa.cloud.patientMonitor.config.PatientConfig;

/**
 */
public class PatientListener {
	private static final String TABLE = "test";
	private static final String SNS_TOPIC_ARN ="arn:aws:sns:us-east-1:073259727542:cloudAlert";
	private static final String custom_message = "\"My text published to SNS topic with email endpoint\"";
	private static final String cloud_channel = "cloud";

	public void listen() {

		//Get pubnub configurations
		PubNub pubnub = PatientConfig.getPubNubConifg();

		//Multple threads
		ExecutorService executorService = Executors.newFixedThreadPool(30);

		// creates Aysnc client 
		AmazonDynamoDBAsync ddbAsync = AmazonClientConfig.getDynamoDbAsyncClient();

		//creates sns client
		AmazonSNS snsClient = AmazonSNSClientBuilder.defaultClient();


		AmazonClientConfig.publishToSNS(SNS_TOPIC_ARN, custom_message);

		pubnub.addListener(new SubscribeCallback() {

			@Override
			public void status(PubNub pubnub, PNStatus status) {
				// TODO Auto-generated method stub

			}

			@Override
			public void presence(PubNub pubnub, PNPresenceEventResult presence) {
				// TODO Auto-generated method stub

			}

			@Override
			public void message(PubNub pubnub, PNMessageResult message) {

				try {
					executorService.execute(new Runnable() {
						public void run() {
							JsonElement message2 = message.getMessage();
							JsonObject asJsonObject = message2.getAsJsonObject();
							HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();
							String dateFormat = new SimpleDateFormat("dd:MM:yyyy").format(new Date());
							String deviceID = asJsonObject.get("deviceID").getAsString();
							String timestamp = asJsonObject.get("timestamp").getAsString();
							int hour = Integer.parseInt(timestamp.split(":")[0]);
							String partitionKey = deviceID + ":" + dateFormat + ":" + hour + ":00:00";
							item_values.put("deviceID", new AttributeValue().withS(partitionKey));
							item_values.put("timestamp",
									new AttributeValue().withS(asJsonObject.get("timestamp").getAsString()));
							int hmax = 150;
							int hmin = 40;
							String randomHeartBeatR = Integer.toString(new Random().nextInt((hmax - hmin) + 1) + hmin);
							int bmax = 6;
							int bmin = 1;
							String randomBR = Integer.toString(new Random().nextInt((bmax - bmin) + 1) + bmin);
							boolean fall;
							fall = false;

							item_values.put("HeartRate", new AttributeValue().withN(randomHeartBeatR));
							item_values.put("BreathRate", new AttributeValue().withN(randomBR));
							item_values.put("falldetected", new AttributeValue().withBOOL(fall));

							//Create your own table 
							
							Future<PutItemResult> putItemAsync = ddbAsync.putItemAsync(TABLE, item_values);
							try {
								PutItemResult putItemResult = putItemAsync.get();
								System.out.println(
										deviceID + "  " + timestamp + " " + putItemResult.getSdkResponseMetadata()
												+ "  " + putItemResult.getSdkHttpMetadata().getHttpStatusCode());
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		pubnub.subscribe().channels(Arrays.asList(cloud_channel)).execute();

	}
	public static void main(String[] args) {
		PatientListener patientListener = new PatientListener();
		patientListener.listen();
	}

}
