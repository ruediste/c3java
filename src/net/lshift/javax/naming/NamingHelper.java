package net.lshift.javax.naming;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;

public class NamingHelper
{
    public static Map<String,String> map(Reference ref)
    {
        Map<String,String> properties = new HashMap<String,String>();
        for(int i = 0; i != ref.size(); ++i)
            properties.put(ref.get(i).getType(), ref.get(i).getContent().toString());

        return properties;
    }

}
