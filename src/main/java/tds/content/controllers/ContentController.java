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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

import tds.content.services.ItemDocumentService;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;

@RestController
public class ContentController {
    private final ItemDocumentService itemDocumentService;

    @Autowired
    public ContentController(final ItemDocumentService itemDocumentService) {
        this.itemDocumentService = itemDocumentService;
    }

    @PutMapping(value = "/item", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ITSDocument> getItemDocument(@RequestParam final String itemPath,
                                                       @RequestBody final AccLookup accLookup) {
        ITSDocument itemDocument;

        try {
            itemDocument = itemDocumentService.loadItemDocument(new URI(itemPath), accLookup);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The provided item path was malformed.");
        }

        return ResponseEntity.ok(itemDocument);
    }
}
