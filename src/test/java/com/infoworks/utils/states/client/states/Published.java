package com.infoworks.utils.states.client.states;

import com.infoworks.utils.states.client.machine.iDocument;
import com.infoworks.utils.states.client.users.Role;
import com.infoworks.utils.states.client.users.User;
import com.infoworks.utils.states.context.State;
import com.infoworks.utils.states.context.StateContext;

public class Published implements iDocState{

    private iDocument document;
    private User user;

    @Override
    public void setContext(StateContext context) {
        this.document = (iDocument) context;
    }

    @Override
    public boolean isValidNextState(Class<? extends State> sType) {
        return false;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public StateContext getContext() {
        return document;
    }

    @Override
    public void publish() {
        if (user.getRole() == Role.PUBLISHER){
            System.out.println("Published Successfully.");
        }else {
            System.out.println("Publishing Permission Denied.");
        }
    }

    @Override
    public void render() {
        if (user.getRole() == Role.PUBLISHER){
            System.out.println("Published Rendering Successfully.");
        }else {
            System.out.println("Published Rendering Permission Denied.");
        }
    }
}
