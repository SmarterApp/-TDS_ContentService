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

import org.springframework.stereotype.Component;

import java.util.List;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSResource;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.Resource;

@Component
public class ResourcesItemDocumentMapper implements ItemDocumentMapper {
    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        if (itemXml.getItemPassage().getResourceslist() == null) {
            return;
        }

        List<Resource> resources = itemXml.getItemPassage().getResourceslist().getResource();

        if (resources != null && resources.size() > 0) {
            for (Resource resource : resources) {
                ITSResource itsResource = new ITSResource();
                itsResource.setType(resource.getType());
                if (resource.getId() != null) {
                    itsResource.setId(Long.parseLong(resource.getId().trim()));
                }
                if (resource.getBankkey() != null) {
                    itsResource.setBankKey(Long.parseLong(resource.getBankkey().trim()));
                }
                document.getResources().add(itsResource);
            }
        }
    }
}
