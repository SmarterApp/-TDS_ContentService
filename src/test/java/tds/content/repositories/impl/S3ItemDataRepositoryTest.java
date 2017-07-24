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
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import tds.common.web.exceptions.NotFoundException;
import tds.content.configuration.S3Properties;

import static com.google.common.base.Charsets.UTF_8;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3ItemDataRepositoryTest {

    @Mock
    private AmazonS3 mockAmazonS3;

    private S3Properties scoringS3Properties;

    private S3ItemDataRepository itemReader;

    @Before
    public void setup() {
        scoringS3Properties = random(S3Properties.class);
        itemReader = new S3ItemDataRepository(mockAmazonS3, scoringS3Properties);
    }

    @Test
    public void itShouldRetrieveAnItemUsingAmazonS3() throws Exception {
        final String itemDataPath = "items/My-Item/My-Item.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(itemDataPath);
        assertThat(value).isEqualTo("Response Data");

        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getBucketName()).isEqualTo(scoringS3Properties.getBucketName());
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + itemDataPath);
    }

    @Test
    public void itShouldTrimLongUris() throws Exception {
        final String longPath = "/usr/local/tomcat/resources/tds/bank/items/My-Item/My-Item.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(longPath);
        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + "items/My-Item/My-Item.xml");
    }

    @Test
    public void itShouldTrimLongUrisStimuli() throws Exception {
        final String longPath = "/usr/local/tomcat/resources/tds/bank/stimuli/My-Stim/My-Stim.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(longPath);
        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + "stimuli/My-Stim/My-Stim.xml");
    }

    private S3ObjectInputStream response(final String body) throws Exception {
        final ByteArrayInputStream delegate = new ByteArrayInputStream(body.getBytes(UTF_8));
        return new S3ObjectInputStream(delegate, mock(HttpRequestBase.class));
    }

    @Test
    public void itShouldPreserveCaseInFilePathName() throws Exception {
        final String itemDataPath = "items/my-Item/My-Item.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(itemDataPath);
        assertThat(value).isEqualTo("Response Data");

        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getBucketName()).isEqualTo(scoringS3Properties.getBucketName());
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + itemDataPath);
    }

    @Test
    public void shouldFindResource() throws Exception {
        final String resourcePath = "items/my-Item/My-resource.xml";
        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));
        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);
        InputStream retData = itemReader.findResource(resourcePath);
        assertThat(IOUtils.toString(retData)).isEqualTo("Response Data");
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldThrowAccessDenidFor403() throws Exception {
        final String resourcePath = "items/my-Item/My-resource.xml";
        AmazonS3Exception exception = new AmazonS3Exception("Exception");
        exception.setStatusCode(HttpStatus.SC_FORBIDDEN);
        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(exception);
        itemReader.findResource(resourcePath);
    }

    @Test(expected = IOException.class)
    public void shouldThrowIOException() throws Exception {
        final String resourcePath = "items/my-Item/My-resource.xml";
        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenThrow(AmazonS3Exception.class);
        itemReader.findResource(resourcePath);
    }
}