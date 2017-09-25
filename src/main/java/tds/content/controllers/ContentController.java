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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

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
                                                       @RequestBody final AccLookup accLookup) {
        return ResponseEntity.ok(contentService.loadItemDocument(getEncodedUri(itemPath), accLookup, contextPath, oggAudioSupport));
    }

    @GetMapping(value = "/resource")
    @ResponseBody
    public ResponseEntity<?> getResource(@RequestParam final String resourcePath) throws IOException {
        final InputStream inputStream = contentService.loadResource(getEncodedUri(resourcePath));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
            .headers(headers)
            .body(new InputStreamResource(inputStream));
    }

    @GetMapping(value = "/wordlist")
    @ResponseBody
    public ResponseEntity<Itemrelease> getWordListItem(@RequestParam final String itemPath,
                                                       @RequestParam final String contextPath,
                                                       @RequestParam final boolean oggAudioSupport) throws IOException {
        return ResponseEntity.ok(contentService.loadWordListItem(getEncodedUri(itemPath), contextPath, oggAudioSupport));
    }

    @GetMapping(value = "/loadData")
    @ResponseBody
    public ResponseEntity<String> getItemData(@RequestParam final String itemPath) throws IOException {
        return ResponseEntity.ok(contentService.loadData(getEncodedUri(itemPath)));
    }

    private URI getEncodedUri(final String uriPath) {
        return UriComponentsBuilder.fromUriString(uriPath).build().toUri();
    }
}
