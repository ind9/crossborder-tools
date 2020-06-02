package org.avlara;

import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer
{
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String render(Object model) throws Exception {
        return mapper.writeValueAsString(model);
    }

    public static String toJson(Object model) throws Exception {
        return mapper.writeValueAsString(model);
    }

    public static String toCookie(Object model) throws Exception {
        return new String(Base64.getEncoder().encode(mapper.writeValueAsBytes(model)));        
    }

    public static <T extends Object>T fromCookie(String b64,Class<T> valueType ) throws Exception
    {
        byte[] _v = Base64.getDecoder().decode(b64.getBytes());
        return (T) mapper.readValue(_v, valueType);
    }

}
