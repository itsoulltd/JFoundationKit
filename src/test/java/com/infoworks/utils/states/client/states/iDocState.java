package com.infoworks.utils.states.client.states;

import com.infoworks.utils.states.client.users.User;
import com.infoworks.utils.states.context.State;

public interface iDocState extends State {
    void setUser(User user);
    void publish();
    void render();
}
