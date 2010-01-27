package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.services.blazeds.VectorEditor.vo.User;

public class AuthorizationService {
    public User fetchUser(String authToken) {
        return new User("Zinovii", "Dmytriv", "123197266389123");
    }
}
