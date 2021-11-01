package github.umer0586.util;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {


    private static ObjectMapper objectMapper = new ObjectMapper();


    public static String toJSON(Object object)
    {
        String json = "";

        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }



}
