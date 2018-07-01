/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.bugtracker.tgt.archer.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.client.ClientConfig;

import com.fortify.util.rest.connection.AbstractRestConnection;
import com.fortify.util.rest.connection.AbstractRestConnectionConfig;

/**
 * This class provides a basic, non-authenticating REST and SOAP
 * connection for Archer. It's main characteristics compared to a 
 * standard {@link AbstractRestConnection} is that it will add Accept 
 * headers for application/json (REST API) and text/xml (SOAP API)
 * and a SoapProvider to handle SOAP requests and responses. 
 */
public class ArcherBasicRestConnection extends AbstractRestConnection {
	public ArcherBasicRestConnection(AbstractRestConnectionConfig<?> config) {
		super(config);
	}
	
	/**
	 * Update the {@link Builder} to add the Accept header.
	 */
	@Override
	public Builder updateBuilder(Builder builder) {
		return super.updateBuilder(builder)
				.accept("application/json", "text/xml");
	}
	
	
	@Override
	protected ClientConfig createClientConfig() {
		ClientConfig result = super.createClientConfig();
		result.register(SoapProvider.class);
		return result;
	}
	
	
	@Provider
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public static final class SoapProvider implements MessageBodyWriter<SOAPMessage>, MessageBodyReader<SOAPMessage> {
	    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
	        return SOAPMessage.class.isAssignableFrom(aClass);
	    }
	 
	    public SOAPMessage readFrom(Class<SOAPMessage> soapEnvelopeClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
	        try {
	            MessageFactory messageFactory = MessageFactory.newInstance();
	            StreamSource messageSource = new StreamSource(inputStream);
	            SOAPMessage message = messageFactory.createMessage();
	            SOAPPart soapPart = message.getSOAPPart();
	            soapPart.setContent(messageSource);
	            return message;
	        } catch (SOAPException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	 
	    public long getSize(SOAPMessage soapMessage, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
	        return -1;
	    }
	 
	    public void writeTo(SOAPMessage soapMessage, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
	        try {
	            soapMessage.writeTo(outputStream);
	        } catch (SOAPException e) {
	            e.printStackTrace();
	        }
	    }
	 
	    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
	        return aClass.isAssignableFrom(SOAPMessage.class);
	    }
	}
}
