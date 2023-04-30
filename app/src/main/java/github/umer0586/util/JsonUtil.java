package github.umer0586.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JsonUtil {


    private static ObjectMapper objectMapper = new ObjectMapper();


    public static String toJSON(Object object)
    {
        String json = "";

        try {
            json = objectMapper.writer().writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    // Maps string elements in JSON array to Java list e.g [a,b,c]
    public static List<String> readJSONArray(String jsonArrayString)
    {
        try
        {
            return objectMapper.readValue(jsonArrayString,new TypeReference<List<String>>(){});

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }



}
