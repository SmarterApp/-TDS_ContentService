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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.content.services.ItemDocumentService;
import tds.itemrenderer.data.AccLookup;
import tds.itemrenderer.data.ITSDocument;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ContentController.class)
@Import({ExceptionAdvice.class, SecurityConfiguration.class})
public class ContentControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemDocumentService itemDocumentService;

    @Test
    public void shouldReturnITSDocument() throws Exception {
        ITSDocument document = random(ITSDocument.class);
        URI uri = new URI("/path/to/item.xml");
        AccLookup accLookup = random(AccLookup.class);

        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        when(itemDocumentService.loadItemDocument(eq(uri), isA(AccLookup.class))).thenReturn(document);
        URI restUri = UriComponentsBuilder.fromUriString("/item").build().toUri();

        MvcResult result = http.perform(post(restUri)
            .param("itemPath", uri.toString())
            .content(ow.writeValueAsString(accLookup))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        ITSDocument parsedDocument = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ITSDocument.class);
        assertThat(parsedDocument.getBaseUri()).isEqualTo(document.getBaseUri());
    }
}
