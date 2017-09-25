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

import tds.itemrenderer.data.ITSAttribute;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Attrib;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;

@Component
public class AttributesItemDocumentMapper implements ItemDocumentMapper {
    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        ItemPassage item = itemXml.getItemPassage();

        if (item.getAttriblist() != null) {
            for (Attrib a : item.getAttriblist().getAttrib()) {
                ITSAttribute attribute = new ITSAttribute();
                attribute.setId(a.getAttid());
                attribute.setName(a.getName().trim());
                attribute.setDescription(a.getDesc().trim());
                attribute.setValue(a.getVal().trim());
                document.addAttribute(attribute);
            }
        }
    }
}
