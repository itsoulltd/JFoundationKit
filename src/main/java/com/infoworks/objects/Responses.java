package com.infoworks.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Responses<C extends Response> extends Response {

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

    public final List<C> sort(SortOrder order, String sortBy) {
        synchronized (this){
            Response[] items = getCollections().toArray(new Response[0]);
            Arrays.sort(items, (o1, o2) ->
                    compareWithOrder((C) o1, (C) o2, sortBy, order)
            );
            return new ArrayList(Arrays.asList(items));
        }
    }
}
