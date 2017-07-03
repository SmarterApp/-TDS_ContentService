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

package tds.content.mappers;

import AIR.Common.Utilities.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.processing.ITSDocumentHelper;
import tds.itemrenderer.processing.ITSDocumentProcessingException;
import tds.itemrenderer.processing.RendererSpecService;

@Component
public class RendererSpecItemDocumentMapper implements ItemDocumentMapper {
    private final RendererSpecService rendererSpecService;

    @Autowired
    public RendererSpecItemDocumentMapper(final RendererSpecService rendererSpecService) {
        this.rendererSpecService = rendererSpecService;
    }

    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        ItemPassage item = itemXml.getItemPassage();

        if (item.getRendererSpec() == null) {
            return;
        }

        String fileName = itemXml.getItemPassage().getRendererSpec().getFilename();

        if (fileName == null) {
            document.setRendererSpec(itemXml.getItemPassage().getRendererSpec().getValue());
            return;
        }

        String rendererSpecPath = document.getBaseUri().replace(Path.getFileName(document.getBaseUri ()), "");
        rendererSpecPath += fileName.trim();

        try {
            document.setRendererSpec(rendererSpecService.findOne(rendererSpecPath));
            return;
        } catch (final IOException e) {
            throw new ITSDocumentProcessingException("Problem reading Renderer Spec", e);
        }
    }
}
