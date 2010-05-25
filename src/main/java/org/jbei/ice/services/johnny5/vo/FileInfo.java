/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jbei.ice.services.johnny5.vo;

/**
 * 
 * @author Douglas Densmore
 */
public class FileInfo {

    private String _name;
    private String _file;

    public FileInfo() {
        _name = null;
        _file = null;
    }

    public String getName() {
        return _name;
    }

    public void setName(String n) {
        _name = n;
    }

    public String getFile() {
        return _file;
    }

    public void setFile(String f) {
        _file = f;
    }

}
