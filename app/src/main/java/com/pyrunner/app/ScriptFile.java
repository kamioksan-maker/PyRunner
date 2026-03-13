package com.pyrunner.app;

import java.io.File;

public class ScriptFile {
    public File file;
    public String name;
    public String lastModified;
    public String description;
    
    public ScriptFile() {
    }
    
    public ScriptFile(File file, String name, String lastModified) {
        this.file = file;
        this.name = name;
        this.lastModified = lastModified;
    }
}
