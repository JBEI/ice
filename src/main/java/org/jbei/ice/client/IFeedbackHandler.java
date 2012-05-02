package org.jbei.ice.client;

import java.util.ArrayList;

public interface IFeedbackHandler {
    void setText(ArrayList<String> msg, FeedbackType type);
}
