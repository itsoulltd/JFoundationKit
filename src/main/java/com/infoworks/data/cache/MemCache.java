package com.infoworks.data.cache;

import com.infoworks.data.base.iDataSource;
import com.infoworks.data.base.iMemorySource;
import com.infoworks.objects.Message;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemCache<Entity extends Message> implements iDataSource<String, Entity>, AutoCloseable {
    private static Logger LOG = Logger.getLogger(MemCache.class.getSimpleName());
    private iMemorySource client;
    private static final String CLASS_NAME_KEY = "classname";
    private String entityClassFullName;
    private long timeToLive = 0l;

    public MemCache(iMemorySource client) {
        this.client = client;
        timeToLive = client.getTimeToLive();
    }

    public MemCache(iMemorySource client, Class<? extends Message> aClass) {
        this(client);
        if(aClass != null)
            setEntityClassFullName(aClass.getName());
    }

    public MemCache(iMemorySource client, Class<? extends Message> aClass, Duration ttl) {
        this(client, aClass);
        this.timeToLive = ttl.toMillis();
        this.client.setTimeToLive(timeToLive);
    }

    public Entity read(String key) {
        Map<String, Object> rData = client.read(key);
        if (rData != null && rData.size() > 0) {
            try {
                //Retrieving: Type
                Entity instance = initFromClassname(rData.get(CLASS_NAME_KEY));
                instance.unmarshalling(rData, true);
                return instance;
            } catch (InstantiationException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } catch (InvocationTargetException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }

    private Entity initFromClassname(Object classname)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (classname == null) return null;
        String classFullName = classname.toString();
        if (classFullName.isEmpty()) return null;
        //
        Entity instance = (Entity) Class.forName(classFullName).getDeclaredConstructor().newInstance();
        return instance;
    }

    @Override
    public Entity remove(String key) {
        Entity value = read(key);
        //then clear the cache:
        client.remove(key);
        if (value != null){
            if (isEnabledItemCounter()) decrement();
        }
        return value;
    }

    @Override
    public void put(String key, Entity entity) {
        put(key, entity, timeToLive);
    }

    protected final void put(String key, Entity entity, long ttl) {
        Map<String, Object> data = entity.marshalling(true);
        if (!client.containsKey(key)){
            if (isEnabledItemCounter()) increment();
        }
        //Saving: Type
        String classFullName = entity.getClass().getName();
        data.put(CLASS_NAME_KEY, classFullName);
        client.put(key, data, ttl);
    }

    @Override
    public Entity replace(String s, Entity entity) {
        Entity old = read(s);
        if(old != null)
            put(s, entity);
        return old;
    }

    @Override
    public boolean containsKey(String key) {
        return client.containsKey(key);
    }

    @Override
    public int size() {
        if (isEnabledItemCounter()) return getCounter().getCount();
        return 0;
    }

    public String getEntityClassFullName() {
        return entityClassFullName;
    }

    protected void setEntityClassFullName(String entityClassFullName) {
        if (this.entityClassFullName == null || this.entityClassFullName.isEmpty())
            this.entityClassFullName = entityClassFullName;
    }

    protected boolean isEnabledItemCounter() {return timeToLive <= 0l;}

    public long getTimeToLive() {
        return timeToLive;
    }

    public iMemorySource getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        if (client != null){
            client.close();
            client = null;
        }
    }

    @Override
    public void clear() {
        //TODO:
    }

    @Override
    public String add(Entity e) {
        String key = String.valueOf(e.hashCode());
        put(key, e);
        return key;
    }

    public iDataSource<String, Entity> add(Entity...items){
        for (Entity dh: items) add(dh);
        return this;
    }

    @Override
    public void delete(Entity e) {
        remove(String.valueOf(e.hashCode()));
    }

    public iDataSource<String, Entity> delete(Entity...items){
        for (Entity dh: items) delete(dh);
        return this;
    }

    @Override
    public boolean contains(Entity e) {
        return containsKey(String.valueOf(e.hashCode()));
    }

    ///////////////////////////////////////////Private Inner Classes////////////////////////////////////////////////////

    private ItemCounter counter;

    private ItemCounter getCounter() {
        if (counter == null){
            synchronized (this){
                if (getEntityClassFullName() != null && !getEntityClassFullName().isEmpty()) {
                    Map<String, Object> countMap = client.read("item_count_map");
                    int initial = 0;
                    if (countMap != null && countMap.size() > 0){
                        try {
                            Object obj = countMap.get(getEntityClassFullName());
                            if(obj != null) initial = Integer.valueOf(obj.toString());
                        } catch (Exception e) {
                            LOG.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                    counter = new ItemCounter(getEntityClassFullName(), initial);
                }else {
                    counter = new ItemCounter(0);
                }
            }
        }
        return counter;
    }

    protected void increment(){
        int value = getCounter().increment();
        updateMemCounter(value);
    }

    protected void decrement(){
        int value = getCounter().decrement();
        updateMemCounter(value);
    }

    private void updateMemCounter(int value) {
        if (getEntityClassFullName() != null && !getEntityClassFullName().isEmpty()) {
            Map<String, Object> countMap = client.read("item_count_map");
            if(countMap != null)
                countMap.put(getEntityClassFullName(), value);
        }
    }

    private static class ItemCounter {

        private AtomicInteger itemCount;
        public int getCount(){
            return itemCount.get();
        }

        private String uuid;
        public String getUuid() {
            if (uuid == null || uuid.isEmpty()){
                synchronized (this){
                    uuid = UUID.randomUUID().toString();
                }
            }
            return uuid;
        }

        public ItemCounter(int initialValue) {
            this.itemCount = new AtomicInteger(initialValue);
        }

        public ItemCounter(String uuid, int initialValue) {
            this(initialValue);
            this.uuid = uuid;
        }

        public int increment(){
            return itemCount.incrementAndGet();
        }

        public int decrement(){
            if(itemCount.get() > 0)
                return itemCount.decrementAndGet();
            return itemCount.get();
        }
    }

}
