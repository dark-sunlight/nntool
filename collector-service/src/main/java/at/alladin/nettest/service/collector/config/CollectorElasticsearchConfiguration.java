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

package at.alladin.nettest.service.collector.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import at.alladin.nettest.shared.server.config.ElasticSearchProperties;
import at.alladin.nettest.shared.server.opendata.config.ElasticsearchConfiguration;

/**
 * 
 * @author alladin-IT GmbH (bp@alladin.at)
 *
 */
@Configuration
@ConditionalOnProperty(name = "collector.elasticsearch.host")
public class CollectorElasticsearchConfiguration extends ElasticsearchConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(CollectorElasticsearchConfiguration.class);
	
	@Autowired
	private CollectorServiceProperties collectorServiceProperties;
	
	@Bean
	public ElasticSearchProperties elasticSearchProperties() {
		return collectorServiceProperties.getElasticsearch();
	}
}
