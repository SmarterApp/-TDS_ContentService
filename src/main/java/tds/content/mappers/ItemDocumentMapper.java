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

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;

/**
 * A mapper for mapping an {@link tds.itemrenderer.data.ITSDocument}
 */
public interface ItemDocumentMapper {
    /**
     * Maps the {@link tds.itemrenderer.data.ITSDocument} based on metadata in the {@link tds.itemrenderer.data.xml.itemrelease.Itemrelease}
     *
     * @param document
     * @param itemXml
     */
    void mapItemDocument(final ITSDocument document, final Itemrelease itemXml);
}
