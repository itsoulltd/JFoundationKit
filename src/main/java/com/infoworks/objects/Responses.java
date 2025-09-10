package com.infoworks.objects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Responses<C extends Response> extends Response{

    public enum SortOrder {
        ASC,
        DESC
    }

    private List<C> collections;

    public Responses() {/**/}

    public Responses(List<C> collections) {
        this.collections = collections;
    }

    public List<C> getCollections() {
        return collections;
    }

    public Responses<C> setCollections(List<C> collections) {
        this.collections = collections;
        return this;
    }

    public final List<C> sort(SortOrder order, String sortBy){
        synchronized (this){
            Response[] items = getCollections().toArray(new Response[0]);
            Arrays.sort(items, (o1, o2) ->
                    compareWithOrder(order, sortBy, (C) o1, (C) o2)
            );
            return new ArrayList(Arrays.asList(items));
        }
    }

    private int compareWithOrder(SortOrder order, String sortBy, C o1, C o2){
        if (order == SortOrder.ASC)
            return compare(sortBy, o1, o2);
        else
            return compare(sortBy, o2, o1);
    }

    protected int compare(String sortBy, C o1, C o2){
        Object obj1 = getSortBy(sortBy, o1);
        Object obj2 = getSortBy(sortBy, o2);
        if (obj1 != null && obj2 != null){
            return obj1.toString().compareToIgnoreCase(obj2.toString());
        }else{
            return 0; //So that, list remain as is;
        }
    }

    protected final Object getSortBy(String sortBy, C obj) {
        if (sortByIsEmpty(sortBy)) return null;
        Field fl = null;
        try {
            fl = obj.getClass().getDeclaredField(sortBy);
            fl.setAccessible(true);
            return fl.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } finally {
            if (fl != null) fl.setAccessible(false);
        }
        return null;
    }

    protected boolean sortByIsEmpty(String sortBy) {
        return sortBy == null || sortBy.isEmpty();
    }
}
