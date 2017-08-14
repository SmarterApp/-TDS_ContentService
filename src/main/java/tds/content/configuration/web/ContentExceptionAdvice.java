/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.content.configuration.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileNotFoundException;

import tds.common.web.advice.ExceptionAdvice;
import tds.common.web.resources.ExceptionMessageResource;

/**
 * Adds ContentService specific handling by extending the Common exception handling
 */
@ControllerAdvice
public class ContentExceptionAdvice extends ExceptionAdvice {
    private final static Logger LOG = LoggerFactory.getLogger(ContentExceptionAdvice.class);

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    ResponseEntity<ExceptionMessageResource> handleValidationException(final FileNotFoundException ex) {
        LOG.warn("FileNotFoundException Exception", ex);
        return new ResponseEntity<>(
            new ExceptionMessageResource(HttpStatus.NOT_FOUND.toString(), ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
