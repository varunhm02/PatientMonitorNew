/**
 * 
 */
package com.uottawa.cloud.patientMonitor.config;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

/**
 * @author Varun Hanabe Muralidhara UOttawa ID : 300055628
 */
public class PatientConfig {

	//Pubnub configuration
	public static PubNub getPubNubConifg() {
		PNConfiguration pnConfiguration = new PNConfiguration();
		//Do not use this keys
		pnConfiguration.setSubscribeKey("sub-c-2c446f30-32cd-11e9-aca0-3eee1dbf820c");
		pnConfiguration.setPublishKey("pub-c-a49f577d-81c8-43c9-922e-3b13d0bbc91d");

		return new PubNub(pnConfiguration);

	}

}
