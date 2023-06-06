package github.umer0586.sensorserver.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

object JsonUtil
{


    private val objectMapper = ObjectMapper()
    fun toJSON(`object`: Any?): String
    {
        var json = ""
        try
        {
            json = objectMapper.writer().writeValueAsString(`object`)
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        return json
    }

    // Maps string elements in JSON array to Java list e.g [a,b,c]
    fun readJSONArray(jsonArrayString: String?): List<String>?
    {
        try
        {
            return objectMapper.readValue<List<String>>(jsonArrayString, object :
                TypeReference<List<String>?>()
            {})
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        return null
    }
}