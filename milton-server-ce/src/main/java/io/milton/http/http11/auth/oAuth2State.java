/*
 * Copyright 2020 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11.auth;

import java.util.Map;

/**
 *
 * @author dylan
 */
public class oAuth2State {

	private String providerId;
	private String returnUrl;
	private Map<String, String> extraParameters;

	public oAuth2State() {
	}

	public oAuth2State(String providerId, String returnUrl, Map<String, String> extraParameters) {
		this.providerId = providerId;
		this.returnUrl = returnUrl;
		this.extraParameters = extraParameters;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public Map<String, String> getExtraParameters() {
		return extraParameters;
	}

	public void setExtraParameters(Map<String, String> extraParameters) {
		this.extraParameters = extraParameters;
	}

}
