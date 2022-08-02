/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
 *
 */
package uk.ac.ebi.gdp.intervene.user.manager.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = -4850629090293414750L;
	private final HttpStatus httpStatus;

	public ServiceException(final HttpStatus httpStatus, final String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public static ServiceException badRequest(final String message) {
		return new ServiceException(HttpStatus.BAD_REQUEST, message);
	}

	public static ServiceException invalidToken(final String message) {
		return new ServiceException(HttpStatus.UNAUTHORIZED, message);
	}

	public static ServiceException resourceNotFound(final String message) {
		return new ServiceException(HttpStatus.NOT_FOUND, message);
	}

	public static ServiceException dataConflict(final String message) {
		return new ServiceException(HttpStatus.CONFLICT, message);
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
