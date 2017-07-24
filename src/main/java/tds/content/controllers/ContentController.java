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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import tds.content.services.ContentService;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;

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
                                                       @RequestBody final AccLookup accLookup) {
        ITSDocument itemDocument;

        try {
            itemDocument = contentService.loadItemDocument(new URI(itemPath), accLookup, contextPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided item path '%s' was malformed", itemPath));
        }

        return ResponseEntity.ok(itemDocument);
    }

    @GetMapping(value = "/resource")
    @ResponseBody
    public ResponseEntity<Resource> getResource(@RequestParam final String resourcePath) throws IOException {
        InputStream inputStream;

        try {
            inputStream = contentService.loadResource(new URI(resourcePath));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("The provided resource path '%s' was malformed", resourcePath));
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }
}
