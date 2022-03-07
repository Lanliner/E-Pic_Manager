package application.model.dirTree;

import java.io.File;

public class DirItemData {
    private final File file;

    public File getFile() {
        return file;
    }

    public DirItemData(){
        file = null;
    }

    public DirItemData(File f){
        file = f;
    }

    @Override
    public String toString() {
        if(file == null || file.getParent() == null) {
            return file.toString();
        }else {
            return file.getName();
        }
    }
}
