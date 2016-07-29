package io.github.cutedb.runner.ws;

import flexjson.JSONDeserializer;
import io.github.cutedb.runner.utils.DateTransformer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class BaseWsConsumer {

    public Response createAndFireGetRequest(Map<String, String> parameters, String url) {
        return createAndFireRequest(parameters, url, false, null);
    }

    public Response createAndFirePostRequest(Map<String, String> parameters, String url) {
        return createAndFireRequest(parameters, url, true, null);
    }

    public Response createAndFirePostRequest(Map<String, String> parameters, String url, String content) {
        return createAndFireRequest(parameters, url, true, content);
    }

    public Response createAndFireRequest(Map<String, String> parameters, String url, boolean isPost, String postContent) {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);

        Form form = null;
        if(parameters != null && !parameters.isEmpty()) {
            form =  new Form();
            for (String key : parameters.keySet()) {
                form.param(key, parameters.get(key));
            }
        }

        Response response = null;

            if (isPost)
                if (postContent == null)
                    response = target.request(MediaType.APPLICATION_JSON_VALUE).post(Entity.entity(form, javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE),Response.class);
                else
                    response = target.request(MediaType.APPLICATION_JSON_VALUE).post(Entity.json(postContent));
            else
                response = target.request(MediaType.APPLICATION_JSON_VALUE).get();

        return response;
    }



    public void readResponse(Response response, String url) {

        if (response.getStatus() == HttpStatus.OK.value())
            return;

        throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s, content is %s",
                url, response.getStatus(), response.readEntity(String.class)));
    }

    public String readResponseAsString(Response response, String url) {

        if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
            return null;
        }

        if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != HttpStatus.CONFLICT.value()) {
            throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s, content is %s",
                    url, response.getStatus(), response.readEntity(String.class)));
        }

        return response.readEntity(String.class);
    }

    public <T> T readResponse(Class<T> targetClass, Response response, String url) {

        T result = null;

        try {

            result = targetClass.newInstance();

        } catch (Exception ex) {
            throw new WSConsumerException("An error occured when instantiating return type when deserializing JSON from WS request.", ex);
        }

        if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
            return null;
        }

        if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != HttpStatus.CONFLICT.value()) {
            throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s, content is %s",
                    url, response.getStatus(), response.readEntity(String.class)));
        }

        String output = response.readEntity(String.class);

        result = new JSONDeserializer<T>().use(Date.class, new DateTransformer()).deserializeInto(output, result);

        return result;
    }

    public <T> List<T> readResponseAsList(Class<T> targetClass, Response response, String url) {

        List<T> result = null;

        result = new ArrayList<>();

        if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
            return result;
        }

        if (response.getStatus() != HttpStatus.OK.value()) {
            throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s", url, response.getStatus()));
        }

        String output = response.readEntity(String.class);

        result = new JSONDeserializer<List<T>>().use(null, ArrayList.class).use(Date.class, new DateTransformer()).use("values", targetClass).deserialize(output);

        return result;
    }

    public <T> T readResponseWithReturnMessageDto(Class<T> targetClass, Response response, String url) {

        T result = null;

        try {
            result = targetClass.newInstance();
        } catch (Exception ex) {
            throw new WSConsumerException("An error occured when instantiating return type when deserializing JSON from SIRH ABS WS request.", ex);
        }

        if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
            return null;
        }

        if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return null;
        }

        String output = response.readEntity(String.class);
        result = new JSONDeserializer<T>().use(Date.class, new DateTransformer()).deserializeInto(output, result);
        return result;
    }

    public byte[] readResponseWithFile(Response response, String url) {

        if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
            return null;
        }

        if (response.getStatus() != HttpStatus.OK.value()) {
            throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s", url, response.getStatus()));
        }

        return response.readEntity(byte[].class);
    }
}

