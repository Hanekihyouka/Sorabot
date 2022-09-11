package haneki.module;

import java.util.HashMap;


public class ModuleLoader {
    private HashMap<String, BasicModule> moduleList = new HashMap<>();

    public HashMap<String, BasicModule> getModuleList() {
        return moduleList;
    }

    public void setModuleList(HashMap<String, BasicModule> moduleList) {
        this.moduleList = moduleList;
    }
}
