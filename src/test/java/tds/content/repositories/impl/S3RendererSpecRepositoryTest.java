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

package tds.content.repositories.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;

import tds.content.configuration.S3Properties;

import static com.google.common.base.Charsets.UTF_8;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3RendererSpecRepositoryTest {

    @Mock
    private AmazonS3 mockAmazonS3;

    private S3Properties scoringS3Properties;

    private S3RendererSpecRepository repository;

    @Before
    public void setup() {
        scoringS3Properties = random(S3Properties.class);
        repository = new S3RendererSpecRepository(mockAmazonS3, scoringS3Properties);
    }

    @Test
    public void itShouldRetrieveARendererSpecUsingAmazonS3() throws Exception {
        final String rendererSpecPath = "items/My-Item/My-Renderer-Spec.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = repository.findOne(rendererSpecPath);
        assertThat(value).isEqualTo("Response Data");

        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getBucketName()).isEqualTo(scoringS3Properties.getBucketName());
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + rendererSpecPath);
    }

    @Test
    public void itShouldTrimLongUris() throws Exception {
        final String longPath = "/usr/local/tomcat/resources/tds/bank/items/My-Item/My-Renderer-Spec.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = repository.findOne(longPath);
        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + "items/My-Item/My-Renderer-Spec.xml");
    }

    private S3ObjectInputStream response(final String body) throws Exception {
        final ByteArrayInputStream delegate = new ByteArrayInputStream(body.getBytes(UTF_8));
        return new S3ObjectInputStream(delegate, mock(HttpRequestBase.class));
    }
}