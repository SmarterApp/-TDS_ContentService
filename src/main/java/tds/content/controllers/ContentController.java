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

package tds.content.controllers;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import tds.content.services.ContentService;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.wordlist.Itemrelease;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

@RestController
public class ContentController {
    private final ContentService contentService;

    @Autowired
    public ContentController(final ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping(value = "/item", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ITSDocument> getItemDocument(@RequestParam final String itemPath, @RequestParam(required = false) final String contextPath,
                                                       @RequestParam(required = false) final boolean oggAudioSupport,
                                                       @RequestBody final AccLookup accLookup) throws IOException {
        ITSDocument itemDocument;

        try {
            itemDocument = contentService.loadItemDocument(getEncodedUri(itemPath), accLookup, contextPath, oggAudioSupport);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided item path '%s' was malformed", itemPath));
        }

        return ResponseEntity.ok(itemDocument);
    }

    @GetMapping(value = "/resource")
    @ResponseBody
    public ResponseEntity<?> getResource(@RequestParam final String resourcePath) throws IOException {
        InputStream inputStream;

        try {
            inputStream = contentService.loadResource(getEncodedUri(resourcePath));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided resource path '%s' was malformed", resourcePath));
        } catch (final AmazonS3Exception ex) {
            if (ex.getStatusCode() == org.apache.http.HttpStatus.SC_NOT_FOUND) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
            } else if (ex.getStatusCode() == org.apache.http.HttpStatus.SC_FORBIDDEN) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
            }
            throw ex;
        } catch (final FileNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }

    @GetMapping(value = "/wordlist")
    @ResponseBody
    public ResponseEntity<Itemrelease> getWordListItem(@RequestParam final String itemPath,
                                                       @RequestParam final String contextPath,
                                                       @RequestParam final boolean oggAudioSupport) throws IOException {
        try {
            return ResponseEntity.ok(contentService.loadWordListItem(getEncodedUri(itemPath), contextPath, oggAudioSupport));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided item path '%s' was malformed", itemPath));
        }
    }

    @GetMapping(value = "/loadData")
    @ResponseBody
    public ResponseEntity<String> getItemData(@RequestParam final String itemPath) throws IOException {
        try {
            return ResponseEntity.ok(contentService.loadData(getEncodedUri(itemPath)));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided item path '%s' was malformed", itemPath));
        } catch (final FileNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private URI getEncodedUri(final String uriPath) throws URISyntaxException, UnsupportedEncodingException {
        return new URI(URLEncoder.encode(uriPath, UTF_8));
    }
}
