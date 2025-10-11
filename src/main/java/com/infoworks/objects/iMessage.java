package com.infoworks.objects;

import java.lang.reflect.Field;
import java.util.Map;

public interface iMessage {
    Field[] getDeclaredFields(boolean inherit);
	Map<String, Object> marshalling(boolean inherit);
    void unmarshalling(Map<String, Object> data, boolean inherit);
}
