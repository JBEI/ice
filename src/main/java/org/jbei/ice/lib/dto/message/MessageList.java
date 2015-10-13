package org.jbei.ice.lib.dto.message;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Wrapper around a list of messages that also contains information about start and count
 * of messages returned for paging.
 *
 * @author Hector Plahar
 */
public class MessageList implements IDataTransferModel {

    private int count;      // requested count. this may differ from list.size();
    private int totalSize;  // total number of messages available
    private int start;
    private ArrayList<MessageInfo> list;  // actual messages

    public MessageList() {
        list = new ArrayList<MessageInfo>();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public ArrayList<MessageInfo> getList() {
        return list;
    }

    public void setList(ArrayList<MessageInfo> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
