package org.jbei.ice.lib.vo;

import java.util.Date;

/**
 * Value Object for storing AssemblyProject information.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class AssemblyProject extends Project {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_NAME = "ASSEMBLY_PROJECT";

    private AssemblyTable assemblyTable;

    public AssemblyProject() {
        super();
    }

    public AssemblyProject(String name, String description, String uuid, String ownerEmail,
            String ownerName, Date creationTime, Date modificationTime, AssemblyTable assemblyTable) {
        super(name, description, uuid, ownerEmail, ownerName, creationTime, modificationTime);

        this.assemblyTable = assemblyTable;
    }

    public AssemblyTable getAssemblyTable() {
        return assemblyTable;
    }

    public void setAssemblyTable(AssemblyTable assemblyTable) {
        this.assemblyTable = assemblyTable;
    }

    @Override
    public String typeName() {
        return "assembly";
    }
}