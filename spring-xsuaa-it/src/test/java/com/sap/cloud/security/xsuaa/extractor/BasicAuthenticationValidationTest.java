/**
 * SPDX-FileCopyrightText: 2018-2023 SAP SE or an SAP affiliate company and Cloud Security Client Java contributors
 *<p>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.sap.cloud.security.xsuaa.extractor;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.sap.cloud.security.xsuaa.mock.JWTUtil;

import testservice.api.XsuaaITApplication;
import testservice.api.basic.SecurityConfiguration;
import testservice.api.basic.TestController;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"xsuaa.xsappname=java-hello-world",
		"xsuaa.clientid=sb-java-hello-world" }, classes = { XsuaaITApplication.class, SecurityConfiguration.class,
				TestController.class })
@AutoConfigureMockMvc
@ActiveProfiles("test.api.basic")
public class BasicAuthenticationValidationTest {

	@Autowired
	MockMvc mvc;

	@Test
	public void testToken_testdomain() throws Exception {
		SecurityConfiguration.tokenBrokerResolver
				.setAuthenticationConfig(new DefaultAuthenticationInformationExtractor(AuthenticationMethod.OAUTH2));

		this.mvc.perform(get("/user").with(bearerToken(JWTUtil.createJWT("/saml.txt", "testdomain"))))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user/useridp/Mustermann")));
		this.mvc.perform(get("/user").with(bearerToken(JWTUtil.createJWT("/saml.txt", "testdomain"))))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user/useridp/Mustermann")));
	}

	@Test
	public void testToken_testdomain_basic() throws Exception {
		SecurityConfiguration.tokenBrokerResolver
				.setAuthenticationConfig(new DefaultAuthenticationInformationExtractor(AuthenticationMethod.BASIC));
		this.mvc.perform(get("/user").with(new BasicTokenRequestPostProcessor("basic.user", "basic.password")))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user/useridp/Mustermann")));
		this.mvc.perform(get("/user").with(new BasicTokenRequestPostProcessor("basic.user", "basic.password")))
				.andExpect(status().isOk()).andExpect(content().string(containsString("user/useridp/Mustermann")));
		this.mvc.perform(get("/user").with(new BasicTokenRequestPostProcessor("basic.2user", "basic.password")))
				.andExpect(status().is4xxClientError());
		this.mvc.perform(get("/user").with(new BasicTokenRequestPostProcessor("basic.user", "basic.2password")))
				.andExpect(status().is4xxClientError());

	}

	@Test
	public void testToken_testdomain_client_credentials() throws Exception {
		SecurityConfiguration.tokenBrokerResolver.setAuthenticationConfig(
				new DefaultAuthenticationInformationExtractor(AuthenticationMethod.CLIENT_CREDENTIALS));
		this.mvc.perform(
				get("/user").with(new BasicTokenRequestPostProcessor("sb-java-hello-world", "basic.clientsecret")))
				.andExpect(status().isOk()).andExpect(content().string(containsString("client/sb-java-hello-world")));

	}

	@Test
	public void testToken_testdomain_oauth_and_client_credentials() throws Exception {
		SecurityConfiguration.tokenBrokerResolver.setAuthenticationConfig(
				new DefaultAuthenticationInformationExtractor(AuthenticationMethod.OAUTH2,
						AuthenticationMethod.CLIENT_CREDENTIALS));
		this.mvc.perform(
				get("/user").with(new BasicTokenRequestPostProcessor("sb-java-hello-world", "basic.clientsecret")))
				.andExpect(status().isOk()).andExpect(content().string(containsString("client/sb-java-hello-world")));

	}

	@Test
	public void testToken_testdomain_basic_fail() throws Exception {
		SecurityConfiguration.tokenBrokerResolver
				.setAuthenticationConfig(new DefaultAuthenticationInformationExtractor(AuthenticationMethod.OAUTH2));
		this.mvc.perform(get("/user").with(new BasicTokenRequestPostProcessor("basic.user", "basic.password")))
				.andExpect(status().is4xxClientError());
	}

	private static class BearerTokenRequestPostProcessor implements RequestPostProcessor {
		private String token;

		public BearerTokenRequestPostProcessor(String token) {
			this.token = token;
		}

		@Override
		public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
			request.addHeader("Authorization", "Bearer " + this.token);
			return request;
		}
	}

	private static class BasicTokenRequestPostProcessor implements RequestPostProcessor {
		private String token;

		public BasicTokenRequestPostProcessor(String username, String password) {
			this.token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
		}

		@Override
		public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
			request.addHeader("Authorization", "Basic " + this.token);
			return request;
		}
	}

	private static BearerTokenRequestPostProcessor bearerToken(String token) {
		return new BearerTokenRequestPostProcessor(token);
	}
}
