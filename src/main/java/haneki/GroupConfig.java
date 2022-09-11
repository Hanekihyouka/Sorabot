package haneki;


import java.util.ArrayList;
import java.util.List;

public class GroupConfig {
    private Long group_id;
    private List<String> module_enabled = new ArrayList<>();
    private String greet = "欢迎新人！";
    private boolean isGreet = false;

    public Long getGroup_id() {
        return group_id;
    }

    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }

    public List<String> getModule_enabled() {
        return module_enabled;
    }

    public void setModule_enabled(List<String> module_enabled) {
        this.module_enabled = module_enabled;
    }

    public String getGreet() {
        return greet;
    }

    public void setGreet(String greet) {
        this.greet = greet;
    }

    public boolean isGreet() {
        return isGreet;
    }

    public void setGreet(boolean greet) {
        isGreet = greet;
    }
}
