package com.infoworks.objects;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message implements Externalizable, Comparable<Message> {

    @Ignore
    protected static Logger LOG = Logger.getLogger(Message.class.getSimpleName());
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
            return MessageParser.printJson(this);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage());
        }
        //Otherwise plain string for debug:
        return "Message{" + "payload='" + payload + '\'' + '}';
    }

    public static int compareWithOrder(Message o1, Message o2, String sortBy, SortOrder order) {
        if (order == SortOrder.ASC)
            return compare(o1, o2, sortBy);
        else
            return compare(o2, o1, sortBy);
    }

    public static int compare(Message o1, Message o2, String sortBy) {
        return o1.compareTo(o2, sortBy);
    }

    @Override
    public int compareTo(Message other) {
        return compareTo(other, "payload");
    }

    public int compareTo(Message other, String sortBy) {
        Object obj1 = this.getSortBy(sortBy);
        Object obj2 = other.getSortBy(sortBy);
        if (obj1 != null && obj2 != null) {
            if (obj1 instanceof Integer && obj2 instanceof Integer) {
                return Integer.compare((Integer) obj1, (Integer) obj2);
            } else if (obj1 instanceof Long && obj2 instanceof Long) {
                return Long.compare((Long) obj1, (Long) obj2);
            } else if (obj1 instanceof Float && obj2 instanceof Float) {
                return Float.compare((Float) obj1, (Float) obj2);
            } else if (obj1 instanceof Double && obj2 instanceof Double) {
                return Double.compare((Double) obj1, (Double) obj2);
            } else if (obj1 instanceof Boolean && obj2 instanceof Boolean) {
                return Boolean.compare((Boolean) obj1, (Boolean) obj2);
            } else if (obj1 instanceof BigDecimal && obj2 instanceof BigDecimal) {
                return ((BigDecimal) obj1).compareTo((BigDecimal) obj2);
            } else {
                return obj1.toString().compareTo(obj2.toString());
            }
        } else {
            return 0; //So that, list remain as is;
        }
    }

    protected final Object getSortBy(String sortBy) {
        if (sortByIsEmpty(sortBy)) return null;
        Field fl = null;
        try {
            fl = getClass().getDeclaredField(sortBy);
            fl.setAccessible(true);
            return fl.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            if (fl != null) fl.setAccessible(false);
        }
        return null;
    }

    protected final boolean sortByIsEmpty(String sortBy) {
        return sortBy == null || sortBy.isEmpty();
    }
}
