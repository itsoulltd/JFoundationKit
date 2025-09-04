package com.infoworks.utils.states.client.states;

import com.infoworks.utils.states.client.machine.iDocument;
import com.infoworks.utils.states.client.users.Role;
import com.infoworks.utils.states.client.users.User;
import com.infoworks.utils.states.context.State;
import com.infoworks.utils.states.context.StateContext;

public class Moderation implements iDocState{

    private iDocument document;
    private User user;

    @Override
    public void setContext(StateContext context) {
        this.document = (iDocument) context;
    }

    @Override
    public boolean isValidNextState(Class<? extends State> sType) {
        return Published.class.isAssignableFrom(sType);
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
        if (user.getRole() == Role.AUTHOR || user.getRole() == Role.MODERATOR){
            System.out.println("Moderation Successfully.");
        }else {
            System.out.println("Moderation Permission Denied.");
        }
    }

    @Override
    public void render() {
        if (user.getRole() == Role.AUTHOR || user.getRole() == Role.MODERATOR){
            System.out.println("Moderation Rendering Successfully.");
        }else {
            System.out.println("Moderation Rendering Permission Denied.");
        }
    }
}
