package haneki.module;


public abstract class BasicModule implements MessageModule{

    private String module_name;

    public BasicModule(String module_name) {
        setModule_name(module_name);
    }

    public abstract void moduleReact();

    public String getModule_name() {
        return module_name;
    }

    public void setModule_name(String module_name) {
        this.module_name = module_name;
    }
}
