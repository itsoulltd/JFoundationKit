package com.infoworks.objects;

import com.infoworks.utils.MessageMapper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.util.*;

public class Message implements Externalizable {

    @Ignore
    private String payload;
    public String getPayload() {
        return payload;
    }
    public Message setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public final Field[] getDeclaredFields(boolean inherit) {
        List<Field> fields = new ArrayList();
        fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
        if (inherit){
            //Inherit properties from one immediate parent which is not Entity.class.
            Class mySuperClass = getClass().getSuperclass();
            while(!mySuperClass.getSimpleName().equalsIgnoreCase(Message.class.getSimpleName())){
                fields.addAll(Arrays.asList(mySuperClass.getDeclaredFields()));
                mySuperClass = mySuperClass.getSuperclass();
            }
        }
        return fields.toArray(new Field[0]);
    }

    public Map<String, Object> marshalling(boolean inherit) throws RuntimeException {
        Map<String, Object> result = new HashMap();
        for (Field field : getDeclaredFields(inherit)) {
            if (field.isAnnotationPresent(Ignore.class)) continue;
            try {
                field.setAccessible(true);
                //Notice:We are interested into reading just the filed name:value into a map.
                try {
                    Object fieldValue = field.get(this);
                    if (fieldValue != null && Message.class.isAssignableFrom(fieldValue.getClass())){
                        Message enIf = (Message) fieldValue;
                        result.put(field.getName(), enIf.marshalling(inherit));
                    }else {
                        result.put(field.getName(), fieldValue);
                    }
                } catch (IllegalAccessException | IllegalArgumentException e) {}
                field.setAccessible(false);
            } catch (SecurityException e) {}
        }
        return result;
    }

    public void unmarshalling(Map<String, Object> data, boolean inherit) throws RuntimeException {
        if (data == null) {
            return;
        }
        //Un-marshaling:
        Field[] fields = getDeclaredFields(inherit);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Ignore.class)) continue;
            try {
                field.setAccessible(true);
                Object entry = data.get(field.getName());
                if(entry != null) {
                    try {
                        if (Message.class.isAssignableFrom(field.getType())){
                            //Now we can say this might-be a marshaled object that confirm to Message,
                            Message enIf = (Message) field.getType().getDeclaredConstructor().newInstance();
                            if(entry instanceof Map)
                                enIf.unmarshalling((Map<String, Object>) entry, true);
                            field.set(this, enIf);
                        }else{
                            field.set(this, entry);
                        }
                    } catch (Exception e) {}
                }
                field.setAccessible(false);
            } catch (SecurityException e) {
                throw new RuntimeException(e.getMessage());
            }
        } //END-Of-Loop
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(marshalling(true));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Map<String, Object> data = (Map<String, Object>) in.readObject();
        unmarshalling(data, true);
    }

    @Override
    public String toString() {
        //Convert into Json:
        try {
            return MessageMapper.printJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Otherwise plain string for debug:
        return "Message{" + "payload='" + payload + '\'' + '}';
    }
}
