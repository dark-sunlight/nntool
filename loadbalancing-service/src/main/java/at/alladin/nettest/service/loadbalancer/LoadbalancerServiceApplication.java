/*******************************************************************************
 * Copyright 2019 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package at.alladin.nettest.service.loadbalancer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import at.alladin.nettest.service.loadbalancer.config.LoadbalancerServiceProperties;
import at.alladin.nettest.shared.server.config.spring.DevelopmentWebCorsConfiguration;
import at.alladin.nettest.shared.server.config.spring.MessageSourceConfiguration;
import at.alladin.nettest.shared.server.helper.spring.SpringApplicationHelper;
import at.alladin.nettest.shared.server.storage.couchdb.config.EnableCouchDbStorage;

/**
 * The result-service main class which allows the collector to be started as stand-alone Java application or inside a servlet container.
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 * @author alladin-IT GmbH (lb@alladin.at)
 *
 */
@SpringBootApplication
@EnableConfigurationProperties({
	LoadbalancerServiceProperties.class
})
@ComponentScan({
	"at.alladin.nettest.service.loadbalancer", 
	"at.alladin.nettest.shared.server.web.api.v1",
})
@Import({
	DevelopmentWebCorsConfiguration.class,
	MessageSourceConfiguration.class
})
@EnableCouchDbStorage
@EnableScheduling
public class LoadbalancerServiceApplication extends SpringBootServletInitializer {

	/**
	 * 
	 */
	private static final String CONFIGURATION_DIRECTORY_NAME = "loadbalancer-service";
	
	/**
	 * 
	 */
	private static final Class<?> APPLICATION_CLASS = LoadbalancerServiceApplication.class;
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.boot.context.web.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
	 */
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return SpringApplicationHelper.configure(CONFIGURATION_DIRECTORY_NAME, application, APPLICATION_CLASS);
    }
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplicationHelper.runSpingApplication(CONFIGURATION_DIRECTORY_NAME, args, APPLICATION_CLASS);
	}
}
